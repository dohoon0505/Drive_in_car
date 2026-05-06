import Foundation

struct SubmitTimeUseCase {
    let users: UserRepository
    let rankings: RankingRepository

    func callAsFunction(
        uid: String,
        courseId: String,
        timeMs: Int64,
        averageKmh: Double,
        flagged: Bool
    ) async throws -> String {
        guard let user = try await users.fetchUser(uid: uid) else {
            throw NSError(
                domain: "DriveInCar.SubmitTime",
                code: 404,
                userInfo: [NSLocalizedDescriptionKey: "user not found: \(uid)"]
            )
        }
        return try await rankings.submit(
            courseId: courseId,
            uid: uid,
            nickname: user.nickname,
            carDisplay: user.carDisplay,
            profileImageId: user.profileImageId,
            timeMs: timeMs,
            averageKmh: averageKmh,
            flagged: flagged
        )
    }
}
