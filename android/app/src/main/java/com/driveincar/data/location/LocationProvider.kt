package com.driveincar.data.location

import com.driveincar.domain.model.LatLng
import kotlinx.coroutines.flow.Flow

data class LocationSample(
    val coord: LatLng,
    val accuracyM: Float,
    /** 모노토닉 시각 (ms) — 레이스 타이머와 동일 시간축. */
    val monotonicTimeMs: Long,
    /** GPS speed (m/s)가 있으면 사용, 없으면 null. */
    val speedMps: Float?,
    /** GPS bearing (도, 0~360)이 있으면 사용. */
    val bearingDeg: Float?,
)

interface LocationProvider {
    /** 1Hz 고정밀 GPS 샘플 스트림. 구독 시 시작, 중단 시 중단. */
    fun samples(intervalMs: Long = 1_000L): Flow<LocationSample>
}
