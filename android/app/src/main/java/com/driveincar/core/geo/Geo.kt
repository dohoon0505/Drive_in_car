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

    /**
     * 선분 (a → b) 와 선분 (c → d) 가 교차하는지 CCW (Counter-Clockwise) 판정.
     *
     * 고속 주행에서 단순 반경 진입 판정은 측정 주기(1Hz) 사이에 차가 결승선을 "터널링"
     * 해버려서 통과를 놓치는 경우가 있다. 결승선을 도로를 가로지르는 가상의 선분으로 정의하고
     * 사용자의 직전→현재 이동 선분이 그 선과 교차하는지 검사하면 통과 누락이 사라진다.
     *
     * 짧은 거리(수십 m) 안에서는 평면 근사로 충분 — 위도/경도를 그대로 2D 좌표로 다룬다.
     * 4점이 동일 직선 상(degenerate)이면 false 를 반환해 이중 트리거를 막는다.
     */
    fun segmentsIntersect(a: LatLng, b: LatLng, c: LatLng, d: LatLng): Boolean {
        val d1 = ccw(c, d, a)
        val d2 = ccw(c, d, b)
        val d3 = ccw(a, b, c)
        val d4 = ccw(a, b, d)
        // 일반 케이스: 두 선분이 서로의 양 끝점을 다른 쪽에 두면 교차.
        return ((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))
        // 동일 직선/끝점-위 케이스는 의도적으로 false. 1Hz 샘플링 환경에서
        // 정확히 선 위에 떨어지는 확률은 무시할 만하고, true 로 처리하면 정차 중
        // GPS 지터로 오트리거가 생긴다.
    }

    /**
     * 점 r 이 선분 (p → q) 의 어느 쪽에 있는지: > 0 좌측(반시계), < 0 우측, = 0 직선 위.
     */
    private fun ccw(p: LatLng, q: LatLng, r: LatLng): Double {
        return (q.lng - p.lng) * (r.lat - p.lat) - (q.lat - p.lat) * (r.lng - p.lng)
    }

    /**
     * 점 p 를 통과하면서 베어링 `bearing` 방향에 직각인 가상 결승선의 두 끝점을 만든다.
     * 길이는 `halfWidthM * 2` 미터 (도로 폭 + 여유).
     *
     * 짧은 거리 안에서는 평면 근사: 1도 위도 ≈ 111,320m, 1도 경도 ≈ 111,320*cos(lat) m.
     */
    fun perpendicularLine(p: LatLng, bearingDeg: Double, halfWidthM: Double): Pair<LatLng, LatLng> {
        val perpBearing = (bearingDeg + 90.0) % 360.0
        val θ = Math.toRadians(perpBearing)
        val mPerDegLat = 111_320.0
        val mPerDegLng = 111_320.0 * cos(Math.toRadians(p.lat))
        val dx = sin(θ) * halfWidthM   // east 방향 미터
        val dy = cos(θ) * halfWidthM   // north 방향 미터
        val dLat = dy / mPerDegLat
        val dLng = dx / mPerDegLng
        val left = LatLng(p.lat - dLat, p.lng - dLng)
        val right = LatLng(p.lat + dLat, p.lng + dLng)
        return left to right
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
