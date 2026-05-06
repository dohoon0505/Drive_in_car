import Foundation

enum CancelReason: String, Sendable {
    case userCancelled
    case permissionLost
    case outOfCorridor
    case belowMinTime
}

enum RaceState: Equatable, Sendable {
    case idle
    case arming(distanceToStartM: Double)
    case armed
    case inRace(elapsedMs: Int64, distanceToEndM: Double, currentKmh: Double)
    case finished(timeMs: Int64, averageKmh: Double, flagged: Bool)
    case cancelled(reason: CancelReason)
}

struct RaceConfig: Sendable {
    var armRadiusM: Double = 30
    var startTriggerRadiusM: Double = 15
    var endTriggerRadiusM: Double = 15
    var sampleIntervalMs: Int = 1_000
    var minRaceTimeMs: Int64 = 30_000
    var maxAverageKmh: Double = 200
    var maxOutOfCorridorM: Double = 200
    var maxOutOfCorridorMs: Int64 = 15_000
    var startBearingToleranceDeg: Double = 90
}
