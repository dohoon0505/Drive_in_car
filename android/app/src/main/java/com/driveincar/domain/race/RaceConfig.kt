package com.driveincar.domain.race

/** 레이스 상태머신 튜닝 상수. 추후 meta/config에서 동적 로드 가능. */
data class RaceConfig(
    val armRadiusM: Double = 30.0,
    val startTriggerRadiusM: Double = 15.0,        // CCW 도입 후엔 fallback 만 사용
    val endTriggerRadiusM: Double = 15.0,
    val sampleIntervalMs: Long = 1_000L,
    val minRaceTimeMs: Long = 30_000L,
    val maxAverageKmh: Double = 200.0,
    val maxOutOfCorridorM: Double = 200.0,
    val maxOutOfCorridorMs: Long = 15_000L,
    val startBearingToleranceDeg: Double = 90.0,
    /**
     * 가상 결승선의 한쪽 길이 (m). 선분 양쪽으로 이만큼 뻗어 도로폭 + 여유 마진을 커버한다.
     * 20m → 40m 폭. 너무 짧으면 GPS 오차로 통과 누락, 너무 길면 다른 평행 도로에서 오트리거.
     */
    val finishLineHalfWidthM: Double = 20.0,
)
