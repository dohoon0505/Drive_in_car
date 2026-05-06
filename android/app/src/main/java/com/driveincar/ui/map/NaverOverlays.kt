package com.driveincar.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.driveincar.domain.model.LatLng
import com.naver.maps.geometry.LatLng as NaverLatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.overlay.OverlayImage

/** 도메인 LatLng → Naver LatLng 변환. */
fun LatLng.toNaver(): NaverLatLng = NaverLatLng(lat, lng)

fun List<LatLng>.toNaver(): List<NaverLatLng> = map { it.toNaver() }

/** Compose Color → Naver overlay 가 받는 ARGB int. */
fun Color.toNaverArgb(): Int = this.toArgb()

/**
 * res/drawable 의 vector 를 Naver Maps overlay 용 OverlayImage 로 변환.
 * GoogleMap 시절 MarkerIcons.kt 의 BitmapDescriptor 등가물.
 */
@Composable
fun rememberOverlayImage(@DrawableRes id: Int, sizeDp: Int = 36): OverlayImage {
    val context = LocalContext.current
    val density = LocalContext.current.resources.displayMetrics.density
    return remember(id, sizeDp, density) {
        vectorToOverlayImage(context, id, sizeDp, density)
    }
}

private fun vectorToOverlayImage(
    context: Context,
    @DrawableRes id: Int,
    sizeDp: Int,
    density: Float,
): OverlayImage {
    val sizePx = (sizeDp * density).toInt().coerceAtLeast(1)
    val drawable = ContextCompat.getDrawable(context, id)
        ?: throw IllegalStateException("drawable not found: $id")
    drawable.setBounds(0, 0, sizePx, sizePx)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.draw(canvas)
    return OverlayImage.fromBitmap(bitmap)
}

/** 좌표 시퀀스를 모두 포함하는 LatLngBounds — Result 미니맵 카메라 fit 용. */
fun boundsOf(vararg seqs: List<LatLng>): LatLngBounds? {
    val all = seqs.flatMap { it }
    if (all.isEmpty()) return null
    val builder = LatLngBounds.Builder()
    all.forEach { builder.include(it.toNaver()) }
    return builder.build()
}
