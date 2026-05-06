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
    /**
     * 가상 결승선의 한쪽 길이 (m). 선분 양쪽으로 이만큼 뻗어 도로폭 + 여유 마진을 커버한다.
     */
    val finishLineHalfWidthM: Double = 20.0,

    /**
     * 정지 판정 임계 — 표시상 0km/h 인 임계 (3.6 * 0.139 ≈ 0.5km/h, 반올림 시 0).
     * 사용자 요구사항 "정확히 0km/h 만 인정" 에 대응.
     */
    val stationarySpeedMps: Double = 0.139,

    /**
     * 정지 상태가 이만큼 유지되면 레이스 시작.
     */
    val stationaryDurationMs: Long = 5_000L,
)
