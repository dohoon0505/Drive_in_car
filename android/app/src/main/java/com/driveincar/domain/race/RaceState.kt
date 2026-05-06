package com.driveincar.domain.race

/** 레이스 진행 상태. 자세한 의미는 docs/race-state-machine.md 참고. */
sealed interface RaceState {
    data object Idle : RaceState
    data class Arming(val distanceToStartM: Double) : RaceState
    data object Armed : RaceState
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
