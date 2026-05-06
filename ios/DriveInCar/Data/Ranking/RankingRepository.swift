import Foundation
import FirebaseFirestore

protocol RankingRepository: AnyObject, Sendable {
    func observeLeaderboard(courseId: String, limit: Int) -> AsyncStream<[Ranking]>
    func observeTop3(courseId: String) -> AsyncStream<[Ranking]>
    func submit(
        courseId: String,
        uid: String,
        nickname: String,
        carDisplay: String,
        profileImageId: String,
        timeMs: Int64,
        averageKmh: Double,
        flagged: Bool
    ) async throws -> String
}

final class FirestoreRankingRepository: RankingRepository {

    private let db = Firestore.firestore()
    private func rankingsCol() -> CollectionReference { db.collection("rankings") }

    func observeLeaderboard(courseId: String, limit: Int) -> AsyncStream<[Ranking]> {
        AsyncStream { cont in
            let reg = rankingsCol()
                .whereField("courseId", isEqualTo: courseId)
                .whereField("flagged", isEqualTo: false)
                .order(by: "timeMs")
                .limit(to: limit)
                .addSnapshotListener { snap, _ in
                    let list = snap?.documents.compactMap { $0.toRanking() } ?? []
                    cont.yield(list)
                }
            cont.onTermination = { _ in reg.remove() }
        }
    }

    func observeTop3(courseId: String) -> AsyncStream<[Ranking]> {
        observeLeaderboard(courseId: courseId, limit: 3)
    }

    func submit(
        courseId: String,
        uid: String,
        nickname: String,
        carDisplay: String,
        profileImageId: String,
        timeMs: Int64,
        averageKmh: Double,
        flagged: Bool
    ) async throws -> String {
        let ref = rankingsCol().document()
        try await ref.setData([
            "courseId": courseId,
            "uid": uid,
            "nickname": nickname,
            "carDisplay": carDisplay,
            "profileImageId": profileImageId,
            "timeMs": timeMs,
            "averageKmh": averageKmh,
            "flagged": flagged,
            "finishedAt": FieldValue.serverTimestamp(),
            "clientSampleCount": 0,
        ])
        return ref.documentID
    }
}

private extension DocumentSnapshot {
    func toRanking() -> Ranking? {
        guard exists, let d = data() else { return nil }
        guard
            let courseId = d["courseId"] as? String,
            let uid = d["uid"] as? String,
            let nickname = d["nickname"] as? String,
            let timeMs = (d["timeMs"] as? NSNumber)?.int64Value
        else { return nil }
        return Ranking(
            rankingId: documentID,
            courseId: courseId,
            uid: uid,
            nickname: nickname,
            carDisplay: d["carDisplay"] as? String ?? "",
            profileImageId: d["profileImageId"] as? String ?? "avatar_01",
            timeMs: timeMs,
            averageKmh: (d["averageKmh"] as? NSNumber)?.doubleValue ?? 0,
            finishedAt: (d["finishedAt"] as? Timestamp)?.dateValue() ?? Date.distantPast,
            flagged: d["flagged"] as? Bool ?? false
        )
    }
}
