package com.driveincar.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.driveincar.core.time.MonotonicClock
import com.driveincar.domain.model.LatLng
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FusedLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: MonotonicClock,
) : LocationProvider {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override fun samples(intervalMs: Long): Flow<LocationSample> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs)
            .setWaitForAccurateLocation(true)
            .build()

        // 노이즈 필터:
        //  - 1단계 하드 컷: 정확도(오차 반경) > 30m 인 샘플은 즉시 폐기.
        //  - 2단계 윈도우 트림: 최근 4개 accept 된 샘플의 정확도 중 최악보다도 더 나쁜 샘플은 폐기.
        //    터널 출구나 빌딩 옆 멀티패스로 한두 번 정확도가 폭증하는 케이스를 흡수.
        //    실시간성 유지를 위해 지연은 0 (5번째 샘플도 즉시 emit 또는 즉시 폐기).
        val recentAccuracies = ArrayDeque<Float>(WINDOW_SIZE - 1)

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (loc in result.locations) {
                    if (loc.accuracy > MAX_ACCURACY_M) continue

                    // 윈도우가 가득 찼고, 새 샘플이 윈도우 내 최악보다 나쁘면 — 폐기.
                    if (recentAccuracies.size >= WINDOW_SIZE - 1) {
                        val worstInWindow = recentAccuracies.max()
                        if (loc.accuracy > worstInWindow) continue
                        recentAccuracies.removeFirst()
                    }
                    recentAccuracies.addLast(loc.accuracy)

                    trySend(
                        LocationSample(
                            coord = LatLng(loc.latitude, loc.longitude),
                            accuracyM = loc.accuracy,
                            monotonicTimeMs = clock.nowMs(),
                            speedMps = if (loc.hasSpeed()) loc.speed else null,
                            bearingDeg = if (loc.hasBearing()) loc.bearing else null,
                        )
                    )
                }
            }
        }

        client.requestLocationUpdates(request, callback, context.mainLooper)
        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }

    private companion object {
        const val MAX_ACCURACY_M = 30f
        const val WINDOW_SIZE = 5
    }
}
