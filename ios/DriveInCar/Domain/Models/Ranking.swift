import Foundation

struct Ranking: Identifiable, Equatable, Sendable {
    let rankingId: String
    let courseId: String
    let uid: String
    let nickname: String
    let carDisplay: String
    let profileImageId: String
    let timeMs: Int64
    let averageKmh: Double
    let finishedAt: Date
    let flagged: Bool

    var id: String { rankingId }
}
