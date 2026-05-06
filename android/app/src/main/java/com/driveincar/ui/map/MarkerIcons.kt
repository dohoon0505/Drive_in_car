package com.driveincar.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

/**
 * res/drawable 의 vector drawable 을 GoogleMap 마커용 BitmapDescriptor 로 변환.
 *
 * GoogleMap 의 Marker 는 BitmapDescriptor 만 받기 때문에 vector → bitmap 변환이 필요하다.
 * Composition-scoped remember 로 싱글톤처럼 동작.
 */
@Composable
fun rememberMarkerIcon(@DrawableRes id: Int, sizeDp: Int = 36): BitmapDescriptor? {
    val context = LocalContext.current
    val density = LocalContext.current.resources.displayMetrics.density
    return remember(id, sizeDp, density) {
        runCatching { vectorToBitmapDescriptor(context, id, sizeDp, density) }.getOrNull()
    }
}

private fun vectorToBitmapDescriptor(
    context: Context,
    @DrawableRes id: Int,
    sizeDp: Int,
    density: Float,
): BitmapDescriptor {
    val sizePx = (sizeDp * density).toInt().coerceAtLeast(1)
    val drawable = ContextCompat.getDrawable(context, id)
        ?: throw IllegalStateException("drawable not found: $id")
    drawable.setBounds(0, 0, sizePx, sizePx)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
