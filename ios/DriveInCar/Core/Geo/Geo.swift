import Foundation

/// 안드로이드 측 com.driveincar.core.geo.Geo 와 동일 사양.
/// 동일 픽스처로 단위 테스트하면 양 플랫폼 동작이 같아야 한다.
enum Geo {
    static let earthRadiusM: Double = 6_371_000

    static func distanceMeters(_ a: LatLng, _ b: LatLng) -> Double {
        let phi1 = a.lat * .pi / 180
        let phi2 = b.lat * .pi / 180
        let dphi = (b.lat - a.lat) * .pi / 180
        let dlam = (b.lng - a.lng) * .pi / 180

        let h = sin(dphi / 2) * sin(dphi / 2)
            + cos(phi1) * cos(phi2) * sin(dlam / 2) * sin(dlam / 2)
        let c = 2 * atan2(sqrt(h), sqrt(1 - h))
        return earthRadiusM * c
    }

    static func bearingDegrees(from a: LatLng, to b: LatLng) -> Double {
        let phi1 = a.lat * .pi / 180
        let phi2 = b.lat * .pi / 180
        let dlam = (b.lng - a.lng) * .pi / 180
        let y = sin(dlam) * cos(phi2)
        let x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dlam)
        let deg = atan2(y, x) * 180 / .pi
        return (deg + 360).truncatingRemainder(dividingBy: 360)
    }

    static func bearingDelta(_ a: Double, _ b: Double) -> Double {
        var d = (a - b).truncatingRemainder(dividingBy: 360) + 540
        d = d.truncatingRemainder(dividingBy: 360) - 180
        return min(abs(d), 360 - abs(d))
    }

    static func distanceToPolylineMeters(point: LatLng, polyline: [LatLng]) -> Double {
        guard polyline.count >= 2 else { return .infinity }
        var best = Double.infinity
        for i in 0..<(polyline.count - 1) {
            let d = distanceToSegmentMeters(p: point, a: polyline[i], b: polyline[i + 1])
            if d < best { best = d }
        }
        return best
    }

    private static func distanceToSegmentMeters(p: LatLng, a: LatLng, b: LatLng) -> Double {
        let mPerDegLat = 111_320.0
        let mPerDegLng = 111_320.0 * cos(a.lat * .pi / 180)

        let px = p.lng * mPerDegLng;  let py = p.lat * mPerDegLat
        let ax = a.lng * mPerDegLng;  let ay = a.lat * mPerDegLat
        let bx = b.lng * mPerDegLng;  let by = b.lat * mPerDegLat

        let dx = bx - ax;  let dy = by - ay
        let len2 = dx * dx + dy * dy
        guard len2 > 0 else { return distanceMeters(p, a) }

        var t = ((px - ax) * dx + (py - ay) * dy) / len2
        t = max(0, min(1, t))
        let cx = ax + t * dx;  let cy = ay + t * dy
        let ddx = px - cx;     let ddy = py - cy
        return sqrt(ddx * ddx + ddy * ddy)
    }
}
