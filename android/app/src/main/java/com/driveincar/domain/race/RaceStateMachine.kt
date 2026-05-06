package com.driveincar.domain.race

import com.driveincar.core.geo.Geo
import com.driveincar.data.location.LocationSample
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.LatLng

/**
 * 순수 도메인 함수: 위치 샘플 시퀀스를 받아 상태 전이를 계산한다.
 *
 * Firebase / Android SDK / 코루틴에 의존하지 않으므로 단위 테스트가 쉽다.
 * 동일 픽스처(docs/race-fixtures 의 JSON 파일들)로 iOS 측 구현과 비교 검증한다.
 */
class RaceStateMachine(
    private val course: Course,
    private val config: RaceConfig = RaceConfig(),
) {
    private var state: RaceState = RaceState.Idle
    private var lastSample: LocationSample? = null
    private var raceStartMs: Long = 0L
    private var traveledMeters: Double = 0.0
    private var sampleCount: Int = 0
    private var outOfCorridorMs: Long = 0L

    private val polyline: List<LatLng> by lazy {
        if (course.waypoints.isEmpty()) listOf(course.startCoord, course.endCoord)
        else listOf(course.startCoord) +
            course.waypoints.sortedBy { it.order }.map { LatLng(it.lat, it.lng) } +
            course.endCoord
    }

    private val firstWaypointBearing: Double by lazy {
        val target = course.waypoints.firstOrNull()
            ?.let { LatLng(it.lat, it.lng) }
            ?: course.endCoord
        Geo.bearingDegrees(course.startCoord, target)
    }

    /**
     * 출발 직전 마지막 웨이포인트(있으면) 또는 시작점 → 도착점 베어링.
     * 도착선의 직각 방향을 계산할 때 사용 — 도착선은 진행 방향에 직각인 가상 선분.
     */
    private val finishApproachBearing: Double by lazy {
        val from = course.waypoints.lastOrNull()
            ?.let { LatLng(it.lat, it.lng) }
            ?: course.startCoord
        Geo.bearingDegrees(from, course.endCoord)
    }

    /** 출발선 (가상) — 코스 진행 방향에 직각인 선분. */
    private val startLine: Pair<LatLng, LatLng> by lazy {
        Geo.perpendicularLine(
            p = course.startCoord,
            bearingDeg = firstWaypointBearing,
            halfWidthM = config.finishLineHalfWidthM,
        )
    }

    /** 도착선 (가상) — 진행 방향에 직각인 선분. */
    private val finishLine: Pair<LatLng, LatLng> by lazy {
        Geo.perpendicularLine(
            p = course.endCoord,
            bearingDeg = finishApproachBearing,
            halfWidthM = config.finishLineHalfWidthM,
        )
    }

    fun current(): RaceState = state

    /** 새 샘플을 입력해 상태를 갱신하고, 새 상태를 반환한다. */
    fun onSample(s: LocationSample): RaceState {
        sampleCount++
        val prev = lastSample
        val newState = transition(prev, s)
        if (newState !is RaceState.Cancelled) {
            // travelled distance accumulation only while InRace
            if (state is RaceState.InRace || newState is RaceState.InRace) {
                if (prev != null) {
                    traveledMeters += Geo.distanceMeters(prev.coord, s.coord)
                }
            }
        }
        lastSample = s
        state = newState
        return newState
    }

    fun cancel(reason: CancelReason): RaceState {
        state = RaceState.Cancelled(reason)
        return state
    }

    private fun transition(prev: LocationSample?, s: LocationSample): RaceState {
        return when (val cur = state) {
            is RaceState.Idle, is RaceState.Arming -> {
                val dStart = Geo.distanceMeters(s.coord, course.startCoord)
                if (dStart <= config.armRadiusM) RaceState.Armed
                else RaceState.Arming(dStart)
            }
            is RaceState.Armed -> {
                if (prev != null && shouldStart(prev, s)) {
                    raceStartMs = s.monotonicTimeMs
                    traveledMeters = 0.0
                    val dEnd = Geo.distanceMeters(s.coord, course.endCoord)
                    val kmh = (s.speedMps ?: 0f).toDouble() * 3.6
                    RaceState.InRace(elapsedMs = 0L, distanceToEndM = dEnd, currentKmh = kmh)
                } else cur
            }
            is RaceState.InRace -> {
                // 코리도 체크
                val corridorDist = Geo.distanceToPolylineMeters(s.coord, polyline)
                if (corridorDist > config.maxOutOfCorridorM && prev != null) {
                    outOfCorridorMs += (s.monotonicTimeMs - prev.monotonicTimeMs)
                    if (outOfCorridorMs >= config.maxOutOfCorridorMs) {
                        return RaceState.Cancelled(CancelReason.OUT_OF_CORRIDOR)
                    }
                }

                val dEnd = Geo.distanceMeters(s.coord, course.endCoord)
                val elapsed = s.monotonicTimeMs - raceStartMs
                val kmh = (s.speedMps ?: 0f).toDouble() * 3.6

                // 도착선 통과 판정: prev → s 가 finishLine 과 교차하면 즉시 종료.
                // 단순 반경 진입은 1Hz 샘플에서 고속 주행 시 결승선을 "터널링" 할 수 있어 누락.
                // CCW 라인 교차는 두 샘플 사이를 직선 보간했다고 가정하고 통과를 확실히 잡는다.
                // 반경 fallback 도 함께 검사 — 정차/저속 진입처럼 prev 가 이미 안에 있는 케이스 커버.
                val crossedFinish = prev != null &&
                    Geo.segmentsIntersect(prev.coord, s.coord, finishLine.first, finishLine.second)
                val withinEndRadius = dEnd <= config.endTriggerRadiusM

                if (crossedFinish || withinEndRadius) {
                    if (elapsed < config.minRaceTimeMs) {
                        RaceState.Cancelled(CancelReason.BELOW_MIN_TIME)
                    } else {
                        val avgKmh = if (elapsed > 0) {
                            (course.distanceMeters / 1000.0) / (elapsed / 3_600_000.0)
                        } else 0.0
                        val flagged = avgKmh > config.maxAverageKmh
                        RaceState.Finished(timeMs = elapsed, averageKmh = avgKmh, flagged = flagged)
                    }
                } else {
                    RaceState.InRace(
                        elapsedMs = elapsed,
                        distanceToEndM = dEnd,
                        currentKmh = kmh
                    )
                }
            }
            is RaceState.Finished, is RaceState.Cancelled -> cur
        }
    }

    /**
     * 출발선 통과 판정.
     *
     * 1차 — CCW 선분-선분 교차: prev → cur 의 이동 선분이 가상 출발선(startLine) 과 교차하면
     *   고속 통과/터널링도 확실히 잡힘. 추가로 진행 방향이 코스 진행 방향과 ≤90° 이내일 때만
     *   인정해 역주행/정차 지터로 오트리거되는 것을 막는다.
     * 2차 — 반경 fallback: prev 가 이미 출발점 가까이 있던 채로 cur 가 안으로 들어왔다면
     *   라인 교차가 안 잡힐 수 있어 (선이 짧거나 prev 가 선 안쪽에서 출발) 반경 + 베어링도 같이.
     */
    private fun shouldStart(prev: LocationSample, cur: LocationSample): Boolean {
        val bearing = Geo.bearingDegrees(prev.coord, cur.coord)
        val bearingDelta = Geo.bearingDelta(bearing, firstWaypointBearing)
        val movingForward = bearingDelta <= config.startBearingToleranceDeg
        if (!movingForward) return false

        // 1차: CCW 라인 교차 (가장 정확)
        if (Geo.segmentsIntersect(prev.coord, cur.coord, startLine.first, startLine.second)) {
            return true
        }
        // 2차: 반경 진입 fallback (정차/저속 진입 케이스)
        val prevDist = Geo.distanceMeters(prev.coord, course.startCoord)
        val curDist = Geo.distanceMeters(cur.coord, course.startCoord)
        return prevDist > config.startTriggerRadiusM && curDist <= config.startTriggerRadiusM
    }

    val totalSampleCount: Int get() = sampleCount
}
