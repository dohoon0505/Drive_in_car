package com.driveincar.core.nav

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.driveincar.domain.model.LatLng
import timber.log.Timber

/**
 * 외부 내비게이션 앱(Naver Map) 으로 길찾기를 시작.
 * 출발지는 디바이스 현재 위치를 Naver Map 이 자동으로 잡고, 도착지는 우리 좌표.
 *
 * 미설치 기기에서는 Play Store 로 폴백.
 */
object ExternalNavigation {

    private const val NAVER_MAP_PKG = "com.nhn.android.nmap"

    /**
     * 자동차 길찾기 호출. 성공 시 true, 실패(이미 Play Store 로 보냈거나 둘 다 안 됨) 시 false.
     */
    fun openNaverMapDriving(
        context: Context,
        dest: LatLng,
        destName: String,
    ): Boolean {
        val uri = Uri.parse(
            "nmap://route/car?dlat=${dest.lat}" +
                "&dlng=${dest.lng}" +
                "&dname=${Uri.encode(destName)}" +
                "&appname=com.driveincar"
        )
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(NAVER_MAP_PKG)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrElse { e ->
            Timber.i(e, "Naver Map 미설치 → Play Store 로 이동")
            val store = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$NAVER_MAP_PKG")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            runCatching { context.startActivity(store) }.isSuccess && false
        }
    }
}
