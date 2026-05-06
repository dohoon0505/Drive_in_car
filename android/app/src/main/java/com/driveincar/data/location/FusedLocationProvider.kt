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

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (loc in result.locations) {
                    if (loc.accuracy > 30f) continue
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
}
