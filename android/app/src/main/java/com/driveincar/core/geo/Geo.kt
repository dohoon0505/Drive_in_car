package com.driveincar.core.geo

import com.driveincar.domain.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object Geo {
    private const val EARTH_RADIUS_M = 6_371_000.0

    /** Haversine 거리 (미터). */
    fun distanceMeters(a: LatLng, b: LatLng): Double {
        val φ1 = Math.toRadians(a.lat)
        val φ2 = Math.toRadians(b.lat)
        val dφ = Math.toRadians(b.lat - a.lat)
        val dλ = Math.toRadians(b.lng - a.lng)

        val h = sin(dφ / 2).let { it * it } +
            cos(φ1) * cos(φ2) * sin(dλ / 2).let { it * it }
        val c = 2 * atan2(sqrt(h), sqrt(1 - h))
        return EARTH_RADIUS_M * c
    }

    /** A→B 진행 방향 베어링 (도, 0~360). */
    fun bearingDegrees(a: LatLng, b: LatLng): Double {
        val φ1 = Math.toRadians(a.lat)
        val φ2 = Math.toRadians(b.lat)
        val dλ = Math.toRadians(b.lng - a.lng)
        val y = sin(dλ) * cos(φ2)
        val x = cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(dλ)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    /** 두 베어링 차이 (도, 0~180). */
    fun bearingDelta(a: Double, b: Double): Double {
        val d = ((a - b) % 360.0 + 540.0) % 360.0 - 180.0
        return min(d, 360.0 - d).let { if (it < 0) -it else it }
    }

    /** point에서 폴리라인 구간들 중 가장 가까운 거리 (미터). */
    fun distanceToPolylineMeters(point: LatLng, polyline: List<LatLng>): Double {
        if (polyline.size < 2) return Double.POSITIVE_INFINITY
        var best = Double.POSITIVE_INFINITY
        for (i in 0 until polyline.size - 1) {
            val d = distanceToSegmentMeters(point, polyline[i], polyline[i + 1])
            if (d < best) best = d
        }
        return best
    }

    private fun distanceToSegmentMeters(p: LatLng, a: LatLng, b: LatLng): Double {
        // 짧은 구간에서는 평면 근사로 충분.
        val mPerDegLat = 111_320.0
        val mPerDegLng = 111_320.0 * cos(Math.toRadians(a.lat))

        val px = p.lng * mPerDegLng;  val py = p.lat * mPerDegLat
        val ax = a.lng * mPerDegLng;  val ay = a.lat * mPerDegLat
        val bx = b.lng * mPerDegLng;  val by = b.lat * mPerDegLat

        val dx = bx - ax; val dy = by - ay
        val len2 = dx * dx + dy * dy
        if (len2 == 0.0) return distanceMeters(p, a)
        val t = (((px - ax) * dx + (py - ay) * dy) / len2).coerceIn(0.0, 1.0)
        val cx = ax + t * dx; val cy = ay + t * dy
        val ddx = px - cx; val ddy = py - cy
        return sqrt(ddx * ddx + ddy * ddy)
    }
}
