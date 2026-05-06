package com.driveincar.domain.race

/** 레이스 상태머신 튜닝 상수. 추후 meta/config에서 동적 로드 가능. */
data class RaceConfig(
    val armRadiusM: Double = 30.0,
    val startTriggerRadiusM: Double = 15.0,
    val endTriggerRadiusM: Double = 15.0,
    val sampleIntervalMs: Long = 1_000L,
    val minRaceTimeMs: Long = 30_000L,
    val maxAverageKmh: Double = 200.0,
    val maxOutOfCorridorM: Double = 200.0,
    val maxOutOfCorridorMs: Long = 15_000L,
    val startBearingToleranceDeg: Double = 90.0,
)
