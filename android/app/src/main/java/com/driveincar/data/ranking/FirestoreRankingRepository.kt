package com.driveincar.data.ranking

import com.driveincar.domain.model.Ranking
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRankingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : RankingRepository {

    private fun rankingsCol() = firestore.collection("rankings")

    override fun observeCourseLeaderboard(courseId: String, limit: Int): Flow<List<Ranking>> =
        callbackFlow {
            val reg = rankingsCol()
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("flagged", false)
                .orderBy("timeMs", Query.Direction.ASCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snap?.documents.orEmpty().mapNotNull { it.toRanking() })
                }
            awaitClose { reg.remove() }
        }

    override fun observeCourseTop3(courseId: String): Flow<List<Ranking>> =
        observeCourseLeaderboard(courseId, limit = 3)

    override suspend fun submitRanking(
        courseId: String,
        uid: String,
        nickname: String,
        carDisplay: String,
        profileImageId: String,
        timeMs: Long,
        averageKmh: Double,
        flagged: Boolean,
    ): Result<String> = runCatching {
        val docRef = rankingsCol().document()
        docRef.set(
            mapOf(
                "courseId" to courseId,
                "uid" to uid,
                "nickname" to nickname,
                "carDisplay" to carDisplay,
                "profileImageId" to profileImageId,
                "timeMs" to timeMs,
                "averageKmh" to averageKmh,
                "flagged" to flagged,
                "finishedAt" to FieldValue.serverTimestamp(),
                "clientSampleCount" to 0,
            )
        ).await()
        docRef.id
    }

    private fun DocumentSnapshot?.toRanking(): Ranking? {
        val s = this ?: return null
        if (!s.exists()) return null
        return Ranking(
            rankingId = s.id,
            courseId = s.getString("courseId") ?: return null,
            uid = s.getString("uid") ?: return null,
            nickname = s.getString("nickname") ?: return null,
            carDisplay = s.getString("carDisplay") ?: "",
            profileImageId = s.getString("profileImageId") ?: "avatar_01",
            timeMs = s.getLong("timeMs") ?: return null,
            averageKmh = s.getDouble("averageKmh") ?: 0.0,
            finishedAtEpochMs = s.getTimestamp("finishedAt")?.toDate()?.time ?: 0L,
            flagged = s.getBoolean("flagged") ?: false,
        )
    }
}
