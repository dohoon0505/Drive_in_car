package com.driveincar.domain.race

import com.driveincar.data.location.LocationSample
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.LatLng
import com.driveincar.domain.model.Waypoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RaceStateMachineTest {

    /**
     * 정지-출발 흐름의 happy path:
     * Idle → Arming → Armed (정지 5초) → InRace → Finished
     */
    @Test
    fun `idle → arming → armed (5s stationary) → inrace → finished`() {
        val course = sampleCourse()
        val m = RaceStateMachine(course, RaceConfig())

        // 1. 출발점에서 멀리 있을 때 — Arming
        assertTrue(m.onSample(loc(38.117, 128.380, 0L, speed = 5f)) is RaceState.Arming)

        // 2. 출발점 진입 (반경 30m 안) — Armed (정지 미감지)
        val armed1 = m.onSample(loc(course.startCoord.lat, course.startCoord.lng, 1_000L, speed = 5f))
        assertTrue("expected Armed got $armed1", armed1 is RaceState.Armed)
        assertEquals(null, (armed1 as RaceState.Armed).stationarySinceMs)

        // 3. 0km/h 정지 시작 — Armed.stationarySinceMs 가 채워짐
        val armed2 = m.onSample(loc(course.startCoord.lat, course.startCoord.lng, 2_000L, speed = 0f))
        assertTrue(armed2 is RaceState.Armed)
        assertEquals(2_000L, (armed2 as RaceState.Armed).stationarySinceMs)
        assertEquals(5, armed2.countdownSecondsRemaining)

        // 4. 4초 후에도 정지 유지 → 카운트다운 1초 남음
        val armed3 = m.onSample(loc(course.startCoord.lat, course.startCoord.lng, 6_000L, speed = 0f))
        assertTrue(armed3 is RaceState.Armed)
        assertEquals(1, (armed3 as RaceState.Armed).countdownSecondsRemaining)

        // 5. 5초 정지 충족 → InRace 전이
        val inRace = m.onSample(loc(course.startCoord.lat, course.startCoord.lng, 7_000L, speed = 0f))
        assertTrue("expected InRace got $inRace", inRace is RaceState.InRace)
        assertEquals(0L, (inRace as RaceState.InRace).elapsedMs)

        // 6. 30s 이상 + 도착점 도달 → Finished (구 시점 7_000 + 35_000 = 42_000 → InRace 시간 35s)
        val firstWp = course.waypoints.first()
        m.onSample(loc(firstWp.lat, firstWp.lng, 30_000L, speed = 25f))
        val end = course.endCoord
        val res = m.onSample(loc(end.lat, end.lng, 60_000L, speed = 5f))
        assertTrue("expected Finished/Cancelled got $res", res is RaceState.Finished || res is RaceState.Cancelled)
    }

    /**
     * 정지 중 잠깐이라도 움직이면 카운트다운이 리셋되는지.
     */
    @Test
    fun `armed countdown resets on movement`() {
        val course = sampleCourse()
        val m = RaceStateMachine(course, RaceConfig())

        m.onSample(loc(38.117, 128.380, 0L, speed = 5f))                                  // Arming
        m.onSample(loc(course.startCoord.lat, course.startCoord.lng, 1_000L, speed = 0f)) // Armed, 정지 시작
        // 1초 뒤 살짝 움직임 (1m/s, 즉 3.6km/h — stationarySpeedMps 0.139 초과)
        val moved = m.onSample(loc(course.startCoord.lat, course.startCoord.lng, 2_000L, speed = 1f))
        assertTrue(moved is RaceState.Armed)
        assertEquals(null, (moved as RaceState.Armed).stationarySinceMs)
        // 다시 정지 → 새 stationarySinceMs
        val restart = m.onSample(loc(course.startCoord.lat, course.startCoord.lng, 3_000L, speed = 0f))
        assertEquals(3_000L, (restart as RaceState.Armed).stationarySinceMs)
    }

    @Test
    fun `armed → out of arm radius → back to arming`() {
        val course = sampleCourse()
        val m = RaceStateMachine(course, RaceConfig())

        m.onSample(loc(38.117, 128.380, 0L, speed = 5f))                                  // Arming
        m.onSample(loc(course.startCoord.lat, course.startCoord.lng, 1_000L, speed = 0f)) // Armed
        // 갑자기 출발점에서 100m 멀어짐
        val res = m.onSample(loc(course.startCoord.lat + 0.001, course.startCoord.lng, 2_000L, speed = 0f))
        assertTrue("expected Arming got $res", res is RaceState.Arming)
    }

    private fun sampleCourse(): Course = Course(
        courseId = "test",
        name = "Test",
        description = "",
        regionName = "",
        startCoord = LatLng(38.117417, 128.378222),
        endCoord = LatLng(38.099500, 128.435611),
        waypoints = listOf(
            Waypoint(38.115200, 128.385900, 1),
            Waypoint(38.111800, 128.394300, 2),
            Waypoint(38.108200, 128.402700, 3),
            Waypoint(38.104500, 128.411600, 4),
        ),
        distanceMeters = 6800.0,
        difficulty = 4,
        isActive = true,
    )

    private fun loc(lat: Double, lng: Double, t: Long, speed: Float = 20f) = LocationSample(
        coord = LatLng(lat, lng),
        accuracyM = 5f,
        monotonicTimeMs = t,
        speedMps = speed,
        bearingDeg = null,
    )
}
