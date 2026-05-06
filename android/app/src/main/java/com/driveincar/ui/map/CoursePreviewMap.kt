package com.driveincar.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.driveincar.R
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.toLatLngList
import com.driveincar.ui.theme.ApexColors
import com.naver.maps.geometry.LatLng as NaverLatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.PathOverlay

/**
 * 코스 상세 hero 아래 220dp 미리보기 지도.
 *
 * 코스 폴리라인 + 출발/도착 마커만 표시. 카메라는 폴리라인 bounds 에 맞춰 fit.
 * 모든 제스처 비활성화 — 사용자가 만지면 흔들리지 않게.
 */
@Composable
fun CoursePreviewMap(
    course: Course,
    modifier: Modifier = Modifier,
) {
    val accent = ApexColors.accentFor(course.courseId)
    val startIcon = rememberOverlayImage(R.drawable.ic_marker_start, sizeDp = 30)
    val finishIcon = rememberOverlayImage(R.drawable.ic_marker_finish, sizeDp = 26)

    var naverMap by remember { mutableStateOf<NaverMap?>(null) }

    NaverMapBox(
        modifier = modifier,
        onMapReady = { map ->
            naverMap = map
            map.uiSettings.apply {
                isCompassEnabled = false
                isScaleBarEnabled = false
                isZoomControlEnabled = false
                isLocationButtonEnabled = false
                isLogoClickEnabled = false
                isScrollGesturesEnabled = false
                isZoomGesturesEnabled = false
                isTiltGesturesEnabled = false
                isRotateGesturesEnabled = false
            }
        },
    )

    val overlays = remember { mutableListOf<Overlay>() }
    DisposableEffect(naverMap, course) {
        val map = naverMap
        overlays.forEach { it.map = null }
        overlays.clear()

        if (map != null) {
            val coursePts = course.toLatLngList()
            val coursePtsNaver = coursePts.toNaver()

            val path = PathOverlay().apply {
                coords = coursePtsNaver
                color = accent.toNaverArgb()
                width = 16
                outlineWidth = 0
            }
            path.map = map
            overlays.add(path)

            val startMarker = Marker().apply {
                position = NaverLatLng(course.startCoord.lat, course.startCoord.lng)
                icon = startIcon
            }
            startMarker.map = map
            overlays.add(startMarker)

            val endMarker = Marker().apply {
                position = NaverLatLng(course.endCoord.lat, course.endCoord.lng)
                icon = finishIcon
            }
            endMarker.map = map
            overlays.add(endMarker)

            // 코스 전체가 들어오게 카메라 fit
            val bounds = boundsOf(coursePts)
            if (bounds != null) {
                runCatching {
                    map.moveCamera(
                        CameraUpdate.fitBounds(bounds, /* paddingPx = */ 80)
                            .animate(CameraAnimation.None)
                    )
                }
            }
        }

        onDispose {
            overlays.forEach { it.map = null }
            overlays.clear()
        }
    }
}
