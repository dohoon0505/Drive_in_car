package com.driveincar.domain.race

/** 레이스 진행 상태. 자세한 의미는 docs/race-state-machine.md 참고. */
sealed interface RaceState {
    data object Idle : RaceState

    data class Arming(val distanceToStartM: Double) : RaceState

    /**
     * 출발 영역 진입 완료. 이제 5초간 정지(0km/h) 가 유지되면 레이스 시작.
     *
     * @param stationarySinceMs 정지 시작 모노토닉 시각. null = 아직 움직이는 중.
     * @param countdownSecondsRemaining stationarySinceMs 가 있을 때 5,4,3,2,1 카운트다운.
     */
    data class Armed(
        val stationarySinceMs: Long? = null,
        val countdownSecondsRemaining: Int? = null,
    ) : RaceState

    data class InRace(val elapsedMs: Long, val distanceToEndM: Double, val currentKmh: Double) : RaceState
    data class Finished(val timeMs: Long, val averageKmh: Double, val flagged: Boolean) : RaceState
    data class Cancelled(val reason: CancelReason) : RaceState
}

enum class CancelReason {
    USER_CANCELLED,
    PERMISSION_LOST,
    OUT_OF_CORRIDOR,
    BELOW_MIN_TIME,
}
