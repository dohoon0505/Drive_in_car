package com.driveincar.data.ranking

import com.driveincar.domain.model.Ranking
import kotlinx.coroutines.flow.Flow

interface RankingRepository {
    fun observeCourseLeaderboard(courseId: String, limit: Int = 50): Flow<List<Ranking>>
    fun observeCourseTop3(courseId: String): Flow<List<Ranking>>

    suspend fun submitRanking(
        courseId: String,
        uid: String,
        nickname: String,
        carDisplay: String,
        profileImageId: String,
        timeMs: Long,
        averageKmh: Double,
        flagged: Boolean,
    ): Result<String>
}
