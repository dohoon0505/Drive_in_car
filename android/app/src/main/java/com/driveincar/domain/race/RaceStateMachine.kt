package com.driveincar.domain.race

import com.driveincar.core.geo.Geo
import com.driveincar.data.location.LocationSample
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.LatLng

/**
 * 순수 도메인 함수: 위치 샘플 시퀀스를 받아 상태 전이를 계산한다.
 *
 * Firebase / Android SDK / 코루틴에 의존하지 않으므로 단위 테스트가 쉽다.
 * 동일 픽스처(`docs/race-fixtures/*.json`)로 iOS 측 구현과 비교 검증한다.
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

                if (dEnd <= config.endTriggerRadiusM) {
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
     * 출발선 통과 판정: 단순 반경 진입은 정차 GPS 지터로 오트리거.
     *  (1) 직전 샘플은 START_TRIGGER_RADIUS 밖
     *  (2) 현재 샘플은 안
     *  (3) 진행 베어링이 출발→첫 웨이포인트 베어링과 ≤90°
     */
    private fun shouldStart(prev: LocationSample, cur: LocationSample): Boolean {
        val prevDist = Geo.distanceMeters(prev.coord, course.startCoord)
        val curDist = Geo.distanceMeters(cur.coord, course.startCoord)
        if (prevDist <= config.startTriggerRadiusM) return false
        if (curDist > config.startTriggerRadiusM) return false

        val bearing = Geo.bearingDegrees(prev.coord, cur.coord)
        val delta = Geo.bearingDelta(bearing, firstWaypointBearing)
        return delta <= config.startBearingToleranceDeg
    }

    val totalSampleCount: Int get() = sampleCount
}
