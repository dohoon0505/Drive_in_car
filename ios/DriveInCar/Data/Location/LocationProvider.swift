import Foundation
import CoreLocation

struct LocationSample: Sendable {
    let coord: LatLng
    let accuracyM: Double
    /// 모노토닉 시각 (ms) — 레이스 타이머와 동일 시간축.
    let monotonicTimeMs: Int64
    let speedMps: Double?
    let bearingDeg: Double?
}

protocol LocationProvider: AnyObject, Sendable {
    func samples(intervalMs: Int) -> AsyncStream<LocationSample>
    func requestPermission()
}

final class CoreLocationProvider: NSObject, LocationProvider, CLLocationManagerDelegate {

    private let manager = CLLocationManager()
    private var continuations: [UUID: AsyncStream<LocationSample>.Continuation] = [:]
    private let lock = NSLock()

    override init() {
        super.init()
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.delegate = self
        manager.allowsBackgroundLocationUpdates = true
        manager.pausesLocationUpdatesAutomatically = false
    }

    func requestPermission() {
        manager.requestWhenInUseAuthorization()
    }

    func samples(intervalMs: Int) -> AsyncStream<LocationSample> {
        AsyncStream { cont in
            let id = UUID()
            lock.withLock { continuations[id] = cont }
            manager.startUpdatingLocation()

            cont.onTermination = { [weak self] _ in
                guard let self else { return }
                self.lock.withLock {
                    self.continuations.removeValue(forKey: id)
                    if self.continuations.isEmpty {
                        self.manager.stopUpdatingLocation()
                    }
                }
            }
        }
    }

    // MARK: - CLLocationManagerDelegate

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        for loc in locations where loc.horizontalAccuracy <= 30 {
            let sample = LocationSample(
                coord: LatLng(lat: loc.coordinate.latitude, lng: loc.coordinate.longitude),
                accuracyM: loc.horizontalAccuracy,
                monotonicTimeMs: MonotonicClock.nowMs(),
                speedMps: loc.speed >= 0 ? loc.speed : nil,
                bearingDeg: loc.course >= 0 ? loc.course : nil
            )
            lock.withLock {
                for cont in continuations.values { cont.yield(sample) }
            }
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        // MVP: 무시. 권한 없음/일시적 오류는 자동 재시도 가능.
    }
}

// NSLock convenience
private extension NSLock {
    func withLock<T>(_ body: () throws -> T) rethrows -> T {
        lock(); defer { unlock() }
        return try body()
    }
}
