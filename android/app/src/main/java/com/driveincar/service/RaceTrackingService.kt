package com.driveincar.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.driveincar.DriveInCarApp
import com.driveincar.MainActivity
import com.driveincar.R

/**
 * 포어그라운드 위치 서비스. 레이스 진행 중 앱이 백그라운드로 가도 GPS 추적이
 * 끊기지 않도록 시스템에 알린다.
 *
 * MVP 범위에서는 알림만 띄우고, 실제 GPS 콜백은 FusedLocationProvider
 * (ViewModel 측에서 collect 중)가 담당한다. 향후 강제 종료 후 재개를
 * 지원하려면 이 서비스에서 직접 LocationCallback을 구독하고 캐시 파일에
 * 스냅샷을 쓰는 형태로 확장한다 — docs/race-state-machine.md 참고.
 */
class RaceTrackingService : Service() {

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ContextCompat.startForegroundService(this, Intent(this, RaceTrackingService::class.java))
        startForeground(DriveInCarApp.RACE_NOTIFICATION_ID, buildNotification())
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun buildNotification(): Notification {
        val tapIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, DriveInCarApp.RACE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.race_in_progress))
            .setContentText(getString(R.string.app_name))
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }

    companion object {
        fun start(context: android.content.Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, RaceTrackingService::class.java)
            )
        }

        fun stop(context: android.content.Context) {
            context.stopService(Intent(context, RaceTrackingService::class.java))
        }
    }
}
