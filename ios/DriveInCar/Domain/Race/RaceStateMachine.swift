import Foundation

/// Android 측 RaceStateMachine과 동일 사양. 동일 픽스처(JSON)로 상호 검증한다.
final class RaceStateMachine {

    private let course: Course
    private let config: RaceConfig

    private(set) var state: RaceState = .idle
    private var lastSample: LocationSample?
    private var raceStartMs: Int64 = 0
    private var traveledMeters: Double = 0
    private var sampleCount: Int = 0
    private var outOfCorridorMs: Int64 = 0

    private let polyline: [LatLng]
    private let firstWaypointBearing: Double

    init(course: Course, config: RaceConfig = RaceConfig()) {
        self.course = course
        self.config = config

        if course.waypoints.isEmpty {
            polyline = [course.startCoord, course.endCoord]
        } else {
            let mid = course.waypoints.sorted { $0.order < $1.order }
                .map { LatLng(lat: $0.lat, lng: $0.lng) }
            polyline = [course.startCoord] + mid + [course.endCoord]
        }
        let firstTarget: LatLng = {
            if let first = course.waypoints.sorted(by: { $0.order < $1.order }).first {
                return LatLng(lat: first.lat, lng: first.lng)
            }
            return course.endCoord
        }()
        firstWaypointBearing = Geo.bearingDegrees(from: course.startCoord, to: firstTarget)
    }

    @discardableResult
    func onSample(_ s: LocationSample) -> RaceState {
        sampleCount += 1
        let prev = lastSample
        let newState = transition(prev: prev, current: s)

        // Travelled distance accumulation while InRace
        if case .inRace = state {} else if case .inRace = newState {
            if let prev { traveledMeters += Geo.distanceMeters(prev.coord, s.coord) }
        } else if case .inRace = state {
            if let prev { traveledMeters += Geo.distanceMeters(prev.coord, s.coord) }
        }

        lastSample = s
        state = newState
        return newState
    }

    func cancel(reason: CancelReason) -> RaceState {
        state = .cancelled(reason: reason)
        return state
    }

    private func transition(prev: LocationSample?, current s: LocationSample) -> RaceState {
        switch state {
        case .idle, .arming:
            let dStart = Geo.distanceMeters(s.coord, course.startCoord)
            return dStart <= config.armRadiusM ? .armed : .arming(distanceToStartM: dStart)

        case .armed:
            if let prev, shouldStart(prev: prev, current: s) {
                raceStartMs = s.monotonicTimeMs
                traveledMeters = 0
                let dEnd = Geo.distanceMeters(s.coord, course.endCoord)
                let kmh = (s.speedMps ?? 0) * 3.6
                return .inRace(elapsedMs: 0, distanceToEndM: dEnd, currentKmh: kmh)
            }
            return state

        case .inRace:
            // 코리도 체크
            let corridorDist = Geo.distanceToPolylineMeters(point: s.coord, polyline: polyline)
            if corridorDist > config.maxOutOfCorridorM, let prev {
                outOfCorridorMs += s.monotonicTimeMs - prev.monotonicTimeMs
                if outOfCorridorMs >= config.maxOutOfCorridorMs {
                    return .cancelled(reason: .outOfCorridor)
                }
            }

            let dEnd = Geo.distanceMeters(s.coord, course.endCoord)
            let elapsed = s.monotonicTimeMs - raceStartMs
            let kmh = (s.speedMps ?? 0) * 3.6

            if dEnd <= config.endTriggerRadiusM {
                if elapsed < config.minRaceTimeMs {
                    return .cancelled(reason: .belowMinTime)
                }
                let avgKmh = elapsed > 0
                    ? (course.distanceMeters / 1000.0) / (Double(elapsed) / 3_600_000.0)
                    : 0
                let flagged = avgKmh > config.maxAverageKmh
                return .finished(timeMs: elapsed, averageKmh: avgKmh, flagged: flagged)
            }
            return .inRace(elapsedMs: elapsed, distanceToEndM: dEnd, currentKmh: kmh)

        case .finished, .cancelled:
            return state
        }
    }

    private func shouldStart(prev: LocationSample, current cur: LocationSample) -> Bool {
        let prevDist = Geo.distanceMeters(prev.coord, course.startCoord)
        let curDist = Geo.distanceMeters(cur.coord, course.startCoord)
        if prevDist <= config.startTriggerRadiusM { return false }
        if curDist > config.startTriggerRadiusM { return false }
        let bearing = Geo.bearingDegrees(from: prev.coord, to: cur.coord)
        return Geo.bearingDelta(bearing, firstWaypointBearing) <= config.startBearingToleranceDeg
    }

    var totalSampleCount: Int { sampleCount }
}
