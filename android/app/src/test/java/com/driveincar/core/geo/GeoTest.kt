package com.driveincar.core.geo

import com.driveincar.domain.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoTest {

    @Test
    fun `distance Seoul-Busan ~ 325km`() {
        val seoul = LatLng(37.5665, 126.9780)
        val busan = LatLng(35.1796, 129.0756)
        val d = Geo.distanceMeters(seoul, busan)
        // 실제 약 325km
        assertTrue("d=$d", d in 320_000.0..335_000.0)
    }

    @Test
    fun `distance same point is 0`() {
        val a = LatLng(37.5, 127.0)
        assertEquals(0.0, Geo.distanceMeters(a, a), 0.1)
    }

    @Test
    fun `bearing east is 90`() {
        val origin = LatLng(0.0, 0.0)
        val east = LatLng(0.0, 1.0)
        assertEquals(90.0, Geo.bearingDegrees(origin, east), 0.5)
    }

    @Test
    fun `bearingDelta wraps correctly`() {
        assertEquals(10.0, Geo.bearingDelta(355.0, 5.0), 0.01)
        assertEquals(180.0, Geo.bearingDelta(0.0, 180.0), 0.01)
    }

    @Test
    fun `point on polyline has near-zero distance`() {
        val poly = listOf(
            LatLng(37.0, 127.0),
            LatLng(37.0, 128.0),
            LatLng(37.0, 129.0),
        )
        val d = Geo.distanceToPolylineMeters(LatLng(37.0, 128.5), poly)
        assertTrue("d=$d", d < 5.0)
    }

    // ===== CCW segment intersection =====

    @Test
    fun `crossing segments intersect`() {
        // 선분 1: 위→아래 가로질러
        val a = LatLng(37.001, 127.000)
        val b = LatLng(36.999, 127.000)
        // 선분 2: 좌→우 가로질러
        val c = LatLng(37.000, 126.999)
        val d = LatLng(37.000, 127.001)
        assertTrue(Geo.segmentsIntersect(a, b, c, d))
    }

    @Test
    fun `non-crossing parallel segments do not intersect`() {
        val a = LatLng(37.001, 127.000)
        val b = LatLng(37.001, 127.001)
        val c = LatLng(36.999, 127.000)
        val d = LatLng(36.999, 127.001)
        assertTrue(!Geo.segmentsIntersect(a, b, c, d))
    }

    @Test
    fun `T-shape touch (endpoint on line) is treated as non-intersect`() {
        // a→b 의 끝점 b 가 c→d 의 한가운데에 닿음 — degenerate, false 로 처리.
        val a = LatLng(37.001, 127.000)
        val b = LatLng(37.000, 127.000)
        val c = LatLng(37.000, 126.999)
        val d = LatLng(37.000, 127.001)
        assertTrue(!Geo.segmentsIntersect(a, b, c, d))
    }

    @Test
    fun `perpendicularLine builds line of expected width`() {
        val p = LatLng(37.0, 127.0)
        val (left, right) = Geo.perpendicularLine(p, bearingDeg = 90.0, halfWidthM = 20.0)
        // bearing=90(동) 의 직각은 0(북) — 결과 선분은 남북 방향이라 경도는 동일해야.
        assertEquals(p.lng, (left.lng + right.lng) / 2, 0.0001)
        // 선분 길이는 약 40m (양쪽 20m)
        val length = Geo.distanceMeters(left, right)
        assertEquals(40.0, length, 1.0)
    }
}
