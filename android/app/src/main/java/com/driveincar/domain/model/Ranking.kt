package com.driveincar.domain.model

data class Ranking(
    val rankingId: String,
    val courseId: String,
    val uid: String,
    val nickname: String,
    val carDisplay: String,
    val profileImageId: String,
    val timeMs: Long,
    val averageKmh: Double,
    val finishedAtEpochMs: Long,
    val flagged: Boolean,
)
