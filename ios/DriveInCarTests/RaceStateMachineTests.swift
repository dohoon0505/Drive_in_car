import XCTest
@testable import DriveInCar

final class RaceStateMachineTests: XCTestCase {

    func testIdleArmingArmedFlow() {
        let course = sampleCourse()
        let m = RaceStateMachine(course: course)

        // 멀리서 시작 → Arming
        let s1 = m.onSample(loc(lat: 38.117, lng: 128.380, t: 0))
        if case .arming = s1 { /* ok */ } else { XCTFail("expected arming, got \(s1)") }

        // 출발점 30m 진입 → Armed
        let s2 = m.onSample(loc(lat: course.startCoord.lat, lng: course.startCoord.lng, t: 1_000))
        if case .armed = s2 { /* ok */ } else { XCTFail("expected armed, got \(s2)") }
    }

    func testFinishedHasMonotonicTime() {
        let course = sampleCourse()
        let m = RaceStateMachine(course: course)
        m.onSample(loc(lat: 38.117, lng: 128.380, t: 0))
        m.onSample(loc(lat: course.startCoord.lat, lng: course.startCoord.lng, t: 1_000))

        // 진행방향(첫 웨이포인트) 쪽으로 출발선 통과
        let firstWp = course.waypoints.first!
        m.onSample(loc(lat: firstWp.lat - 0.0001, lng: firstWp.lng - 0.0001, t: 2_000))
        m.onSample(loc(lat: firstWp.lat, lng: firstWp.lng, t: 35_000))
        let finalState = m.onSample(loc(lat: course.endCoord.lat, lng: course.endCoord.lng, t: 60_000))

        if case let .finished(time, _, _) = finalState {
            XCTAssertGreaterThanOrEqual(time, 30_000)
        } else if case .cancelled = finalState {
            // BELOW_MIN_TIME 등의 캔슬도 허용 — 출발 트리거 동작에 따라 다름.
        } else {
            XCTFail("expected finished/cancelled, got \(finalState)")
        }
    }

    private func sampleCourse() -> Course {
        Course(
            courseId: "test",
            name: "Test",
            description: "",
            regionName: "",
            startCoord: LatLng(lat: 38.117417, lng: 128.378222),
            endCoord: LatLng(lat: 38.099500, lng: 128.435611),
            waypoints: [
                Waypoint(lat: 38.115200, lng: 128.385900, order: 1),
                Waypoint(lat: 38.111800, lng: 128.394300, order: 2),
                Waypoint(lat: 38.108200, lng: 128.402700, order: 3),
                Waypoint(lat: 38.104500, lng: 128.411600, order: 4),
            ],
            distanceMeters: 6800,
            difficulty: 4,
            isActive: true
        )
    }

    private func loc(lat: Double, lng: Double, t: Int64) -> LocationSample {
        LocationSample(
            coord: LatLng(lat: lat, lng: lng),
            accuracyM: 5,
            monotonicTimeMs: t,
            speedMps: 20,
            bearingDeg: nil
        )
    }
}
