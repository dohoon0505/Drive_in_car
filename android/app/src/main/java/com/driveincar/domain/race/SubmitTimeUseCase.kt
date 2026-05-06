package com.driveincar.domain.race

import com.driveincar.data.ranking.RankingRepository
import com.driveincar.data.user.UserRepository
import javax.inject.Inject

class SubmitTimeUseCase @Inject constructor(
    private val rankingRepo: RankingRepository,
    private val userRepo: UserRepository,
) {
    suspend operator fun invoke(
        uid: String,
        courseId: String,
        timeMs: Long,
        averageKmh: Double,
        flagged: Boolean,
    ): Result<String> {
        val user = userRepo.fetchUser(uid)
            ?: return Result.failure(IllegalStateException("user not found: $uid"))

        return rankingRepo.submitRanking(
            courseId = courseId,
            uid = uid,
            nickname = user.nickname,
            carDisplay = user.carDisplay,
            profileImageId = user.profileImageId,
            timeMs = timeMs,
            averageKmh = averageKmh,
            flagged = flagged,
        )
    }
}
