package com.driveincar.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CourseTest {

    @Test
    fun `toLatLngList returns start, ordered waypoints, end`() {
        val course = Course(
            courseId = "test",
            name = "Test",
            description = "",
            regionName = "",
            startCoord = LatLng(38.0, 128.0),
            endCoord = LatLng(38.1, 128.5),
            // 일부러 순서 섞어서 넣음 — sortedBy(order) 가 동작하는지 확인
            waypoints = listOf(
                Waypoint(38.05, 128.2, order = 2),
                Waypoint(38.02, 128.1, order = 1),
                Waypoint(38.08, 128.4, order = 3),
            ),
            distanceMeters = 1000.0,
            difficulty = 3,
            isActive = true,
        )

        val pts = course.toLatLngList()

        assertEquals(5, pts.size)
        assertEquals(LatLng(38.0, 128.0), pts[0])      // start
        assertEquals(LatLng(38.02, 128.1), pts[1])     // wp order=1
        assertEquals(LatLng(38.05, 128.2), pts[2])     // wp order=2
        assertEquals(LatLng(38.08, 128.4), pts[3])     // wp order=3
        assertEquals(LatLng(38.1, 128.5), pts[4])      // end
    }

    @Test
    fun `toLatLngList with no waypoints returns just start and end`() {
        val course = Course(
            courseId = "minimal",
            name = "Minimal",
            description = "",
            regionName = "",
            startCoord = LatLng(0.0, 0.0),
            endCoord = LatLng(1.0, 1.0),
            waypoints = emptyList(),
            distanceMeters = 100.0,
            difficulty = 1,
            isActive = true,
        )
        val pts = course.toLatLngList()
        assertEquals(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0)), pts)
    }
}
