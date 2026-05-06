package com.driveincar

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.content.getSystemService
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class DriveInCarApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        createRaceNotificationChannel()
    }

    private fun createRaceNotificationChannel() {
        val nm = getSystemService<NotificationManager>() ?: return
        val channel = NotificationChannel(
            RACE_NOTIFICATION_CHANNEL_ID,
            "Race tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "레이스 진행 중 위치 추적 알림"
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)
    }

    companion object {
        const val RACE_NOTIFICATION_CHANNEL_ID = "race_tracking"
        const val RACE_NOTIFICATION_ID = 1001
    }
}
