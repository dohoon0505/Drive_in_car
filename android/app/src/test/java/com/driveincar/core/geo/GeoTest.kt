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
}
