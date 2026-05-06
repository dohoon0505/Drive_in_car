package com.driveincar.ui.race

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driveincar.R
import com.driveincar.core.time.TimeFormat
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.toLatLngList
import com.driveincar.domain.race.RaceState
import com.driveincar.ui.components.Overline
import com.driveincar.ui.map.NaverMapBox
import com.driveincar.ui.map.rememberOverlayImage
import com.driveincar.ui.map.toNaver
import com.driveincar.ui.map.toNaverArgb
import com.driveincar.ui.theme.ApexColors
import com.driveincar.ui.theme.Pretendard
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.naver.maps.geometry.LatLng as NaverLatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.PathOverlay
import com.driveincar.domain.model.LatLng as DomainLatLng

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RaceScreen(
    courseId: String,
    onFinished: (timeMs: Long, averageKmh: Double, flagged: Boolean, personalBest: Boolean) -> Unit,
    onCancel: () -> Unit,
    vm: RaceViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val course by vm.course.collectAsStateWithLifecycle()
    val track by vm.track.collectAsStateWithLifecycle()

    val finePermission = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    LaunchedEffect(Unit) {
        if (!finePermission.status.isGranted) finePermission.launchPermissionRequest()
    }

    LaunchedEffect(vm) {
        vm.events.collect { ev ->
            when (ev) {
                is RaceEvent.Finished -> onFinished(ev.timeMs, ev.averageKmh, ev.flagged, ev.personalBest)
                is RaceEvent.CancelledEvent -> onCancel()
            }
        }
    }

    val accent = course?.let { ApexColors.accentFor(it.courseId) } ?: ApexColors.BrandLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.15f), Color.Black),
                    radius = 1200f,
                )
            )
    ) {
        // Top HUD: NOW DRIVING + course
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HudCard(modifier = Modifier.fillMaxWidth()) {
                Overline(text = "NOW DRIVING", color = accent, tracking = 0.20f)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = course?.name ?: "—",
                    color = ApexColors.Text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Pretendard,
                )
            }
        }

        // 미니맵: 코스 라인(반투명) + 라이브 궤적(전경) + 출발/도착/현재 마커
        course?.let { c ->
            RaceMiniMap(
                course = c,
                track = track,
                accent = accent,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 130.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, ApexColors.Border, RoundedCornerShape(18.dp))
            )
        }

        // Lap clock + metrics
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 130.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (val s = state) {
                is RaceState.Idle, is RaceState.Arming -> {
                    val dist = (s as? RaceState.Arming)?.distanceToStartM ?: 0.0
                    Overline(text = "MOVE TO START", color = accent, tracking = 0.32f)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${"%.0f".format(dist)} m",
                        color = ApexColors.Text,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = Pretendard,
                        letterSpacing = (-0.04).em,
                    )
                }
                is RaceState.Armed -> {
                    Overline(text = "READY", color = accent, tracking = 0.32f)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "출발선을 통과하세요",
                        color = ApexColors.Text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Pretendard,
                    )
                }
                is RaceState.InRace -> {
                    Overline(text = "ELAPSED", color = accent, tracking = 0.32f)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = TimeFormat.raceTime(s.elapsedMs),
                        color = ApexColors.Text,
                        fontSize = 84.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = Pretendard,
                        letterSpacing = (-0.04).em,
                        lineHeight = 84.sp,
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                        Metric(label = "SPEED", value = "${"%.0f".format(s.currentKmh)}", unit = "km/h")
                        Box(
                            modifier = Modifier
                                .size(width = 1.dp, height = 40.dp)
                                .background(ApexColors.Border)
                        )
                        Metric(label = "REMAINING", value = "${"%.0f".format(s.distanceToEndM)}", unit = "m")
                    }
                }
                is RaceState.Finished, is RaceState.Cancelled -> {
                    Text("처리 중…", color = ApexColors.TextSec, fontFamily = Pretendard)
                }
            }
        }

        // Abort pill
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .background(ApexColors.BgRaised.copy(alpha = 0.85f), CircleShape)
                .border(1.dp, ApexColors.Border, CircleShape)
                .clickable { vm.userCancel() }
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = null,
                tint = ApexColors.Text,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "주행 중단",
                color = ApexColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Pretendard,
            )
        }
    }
}

@Composable
private fun RaceMiniMap(
    course: Course,
    track: List<DomainLatLng>,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val startIcon = rememberOverlayImage(R.drawable.ic_marker_start, sizeDp = 30)
    val finishIcon = rememberOverlayImage(R.drawable.ic_marker_finish, sizeDp = 26)

    var naverMap by remember { mutableStateOf<NaverMap?>(null) }

    NaverMapBox(
        modifier = modifier,
        onMapReady = { map ->
            naverMap = map
            map.cameraPosition = CameraPosition(
                NaverLatLng(course.startCoord.lat, course.startCoord.lng),
                13.0,
            )
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

    // 코스 폴리라인 (정적), 라이브 트랙(동적) 을 분리해서 갱신.
    val staticOverlays = remember { mutableListOf<Overlay>() }
    DisposableEffect(naverMap, course) {
        val map = naverMap
        staticOverlays.forEach { it.map = null }
        staticOverlays.clear()

        if (map != null) {
            // 코스 폴리라인 (반투명 배경)
            val coursePts = course.toLatLngList().toNaver()
            val coursePath = PathOverlay().apply {
                coords = coursePts
                color = accent.copy(alpha = 0.4f).toNaverArgb()
                width = 16  // px
                outlineWidth = 0
            }
            coursePath.map = map
            staticOverlays.add(coursePath)

            // 출발/도착 마커
            val startMarker = Marker().apply {
                position = NaverLatLng(course.startCoord.lat, course.startCoord.lng)
                icon = startIcon
            }
            startMarker.map = map
            staticOverlays.add(startMarker)

            val endMarker = Marker().apply {
                position = NaverLatLng(course.endCoord.lat, course.endCoord.lng)
                icon = finishIcon
            }
            endMarker.map = map
            staticOverlays.add(endMarker)
        }

        onDispose {
            staticOverlays.forEach { it.map = null }
            staticOverlays.clear()
        }
    }

    // 라이브 트랙 + 현재 위치 — 매 샘플마다 갱신
    val livePath = remember { PathOverlay() }
    val liveMarker = remember { Marker() }
    DisposableEffect(naverMap) {
        onDispose {
            livePath.map = null
            liveMarker.map = null
        }
    }
    LaunchedEffect(naverMap, track, accent) {
        val map = naverMap ?: return@LaunchedEffect
        if (track.size >= 2) {
            livePath.coords = track.toNaver()
            livePath.color = accent.toNaverArgb()
            livePath.width = 22
            livePath.outlineWidth = 0
            if (livePath.map == null) livePath.map = map
        }
        track.lastOrNull()?.let { last ->
            liveMarker.position = last.toNaver()
            liveMarker.captionText = "지금"
            liveMarker.captionColor = ApexColors.Text.toNaverArgb()
            if (liveMarker.map == null) liveMarker.map = map
        }
    }
}

@Composable
private fun HudCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .background(ApexColors.BgRaised.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
            .border(1.dp, ApexColors.Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        content = content,
    )
}

@Composable
private fun Metric(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Overline(text = label, color = ApexColors.TextTer, tracking = 0.20f)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = value,
                color = ApexColors.Text,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard,
            )
            Text(
                text = unit,
                color = ApexColors.TextSec,
                fontSize = 12.sp,
                fontFamily = Pretendard,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
    }
}
