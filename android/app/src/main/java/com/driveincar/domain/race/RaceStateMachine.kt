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
                if (dStart <= config.armRadiusM) RaceState.Armed()
                else RaceState.Arming(dStart)
            }
            is RaceState.Armed -> {
                // 출발 영역 이탈 → 다시 Arming (정지 카운트다운 리셋)
                val dStart = Geo.distanceMeters(s.coord, course.startCoord)
                if (dStart > config.armRadiusM) {
                    return RaceState.Arming(dStart)
                }

                // 정지-출발 측정: 0km/h (표시상) 가 5초 유지되면 레이스 시작.
                val speedMps = (s.speedMps ?: 0f).toDouble()
                val isStationary = speedMps < config.stationarySpeedMps
                val now = s.monotonicTimeMs
                val nextSinceMs: Long? = when {
                    !isStationary -> null                              // 움직임 → 카운트다운 리셋
                    cur.stationarySinceMs == null -> now               // 막 정지 시작
                    else -> cur.stationarySinceMs                      // 정지 유지
                }
                if (nextSinceMs != null && (now - nextSinceMs) >= config.stationaryDurationMs) {
                    // 5초 정지 충족 → 레이스 시작 (정지 상태에서 가속 시작 — drag race 식)
                    raceStartMs = now
                    traveledMeters = 0.0
                    outOfCorridorMs = 0L
                    val dEnd = Geo.distanceMeters(s.coord, course.endCoord)
                    return RaceState.InRace(elapsedMs = 0L, distanceToEndM = dEnd, currentKmh = 0.0)
                }
                val remaining = if (nextSinceMs == null) null else
                    ((config.stationaryDurationMs - (now - nextSinceMs)) / 1000)
                        .toInt()
                        .coerceAtLeast(0)
                return RaceState.Armed(
                    stationarySinceMs = nextSinceMs,
                    countdownSecondsRemaining = remaining,
                )
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

    val totalSampleCount: Int get() = sampleCount
}
