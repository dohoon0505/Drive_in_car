import XCTest
@testable import DriveInCar

final class GeoTests: XCTestCase {

    func testDistanceSeoulToBusan() {
        let seoul = LatLng(lat: 37.5665, lng: 126.9780)
        let busan = LatLng(lat: 35.1796, lng: 129.0756)
        let d = Geo.distanceMeters(seoul, busan)
        XCTAssertGreaterThan(d, 320_000)
        XCTAssertLessThan(d, 335_000)
    }

    func testSamePointZeroDistance() {
        let a = LatLng(lat: 37.5, lng: 127.0)
        XCTAssertEqual(Geo.distanceMeters(a, a), 0, accuracy: 0.1)
    }

    func testBearingEastIs90() {
        let origin = LatLng(lat: 0, lng: 0)
        let east = LatLng(lat: 0, lng: 1)
        XCTAssertEqual(Geo.bearingDegrees(from: origin, to: east), 90, accuracy: 0.5)
    }

    func testBearingDeltaWraps() {
        XCTAssertEqual(Geo.bearingDelta(355, 5), 10, accuracy: 0.01)
        XCTAssertEqual(Geo.bearingDelta(0, 180), 180, accuracy: 0.01)
    }

    func testPointOnPolylineNearZero() {
        let poly = [
            LatLng(lat: 37, lng: 127),
            LatLng(lat: 37, lng: 128),
            LatLng(lat: 37, lng: 129),
        ]
        let d = Geo.distanceToPolylineMeters(point: LatLng(lat: 37, lng: 128.5), polyline: poly)
        XCTAssertLessThan(d, 5)
    }
}
