package com.driveincar.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driveincar.R
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.toLatLngList
import com.driveincar.ui.components.InitialBadge
import com.driveincar.ui.components.Overline
import com.driveincar.ui.components.PrimaryButton
import com.driveincar.ui.theme.ApexColors
import com.driveincar.ui.theme.Pretendard
import com.naver.maps.geometry.LatLng as NaverLatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.PathOverlay
import kotlinx.coroutines.delay

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MapScreen(
    onCourseSelected: (String) -> Unit,
    onSignedOut: () -> Unit,
    vm: MapViewModel = hiltViewModel(),
) {
    val courses by vm.courses.collectAsStateWithLifecycle()
    val me by vm.me.collectAsStateWithLifecycle()

    var focusedCourseId by remember { mutableStateOf<String?>(null) }
    val focusedCourse: Course? = remember(focusedCourseId, courses) {
        focusedCourseId?.let { id -> courses.firstOrNull { it.courseId == id } }
            ?: courses.firstOrNull()
    }

    // 8초 안에 onMapLoaded 안 떨어지면 Cloud / NCP Console 안내 배너.
    var mapLoaded by remember { mutableStateOf(false) }
    var showLoadHint by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(8_000)
        if (!mapLoaded) showLoadHint = true
    }

    val startIcon = rememberOverlayImage(R.drawable.ic_marker_start, sizeDp = 36)
    val finishIcon = rememberOverlayImage(R.drawable.ic_marker_finish, sizeDp = 32)

    var naverMap by remember { mutableStateOf<NaverMap?>(null) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(ApexColors.Bg)
    ) {
        NaverMapBox(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                naverMap = map
                map.cameraPosition = CameraPosition(NaverLatLng(36.5, 127.8), 6.5)
                // 어떤 UI 컨트롤도 안 보이게 (글래스 오버레이가 따로 있음)
                map.uiSettings.apply {
                    isCompassEnabled = false
                    isScaleBarEnabled = false
                    isZoomControlEnabled = false
                    isLocationButtonEnabled = false
                    isLogoClickEnabled = false
                }
            },
            onMapLoaded = { mapLoaded = true },
        )

        // 코스 마커 + 폴리라인 — courses / naverMap 변화에 따라 갱신
        CourseOverlays(
            naverMap = naverMap,
            courses = courses,
            startIcon = startIcon,
            finishIcon = finishIcon,
            onMarkerClick = { focusedCourseId = it },
        )

        // 상단 검색바 + 프로필 칩
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 56.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchBar(modifier = Modifier.weight(1f))
            ProfileChip(
                nickname = me?.nickname.orEmpty(),
                carDisplay = me?.carDisplay.orEmpty(),
                onLogout = {
                    vm.signOut()
                    onSignedOut()
                },
            )
        }

        if (courses.isEmpty()) {
            EmptyHint(modifier = Modifier.align(Alignment.Center))
        }

        if (showLoadHint && !mapLoaded) {
            MapLoadFailureBanner(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
            )
        }

        focusedCourse?.let { c ->
            CoursePeekCard(
                course = c,
                onTap = { onCourseSelected(c.courseId) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 24.dp),
            )
        }
    }
}

/**
 * courses 리스트 / naverMap 변경 시 폴리라인 + 마커를 모두 재구성.
 * 각 overlay 의 .map 을 null 로 두어 정리, 새로 set.
 * remember 로 overlay 들을 보관해서 다음 갱신 때 정리할 수 있게 함.
 */
@Composable
private fun CourseOverlays(
    naverMap: NaverMap?,
    courses: List<Course>,
    startIcon: com.naver.maps.map.overlay.OverlayImage,
    finishIcon: com.naver.maps.map.overlay.OverlayImage,
    onMarkerClick: (String) -> Unit,
) {
    val overlays = remember { mutableListOf<Overlay>() }

    DisposableEffect(naverMap, courses) {
        val map = naverMap
        // 이전 overlay 정리
        overlays.forEach { it.map = null }
        overlays.clear()

        if (map != null) {
            for (c in courses) {
                val accent = ApexColors.accentFor(c.courseId)
                val pts = c.toLatLngList().toNaver()

                // 코스 폴리라인
                val path = PathOverlay().apply {
                    coords = pts
                    color = accent.copy(alpha = 0.85f).toNaverArgb()
                    width = 18  // px
                    outlineWidth = 0
                }
                path.map = map
                overlays.add(path)

                // 출발 마커 (▶) — OverlayImage 의 natural size (Marker.SIZE_AUTO) 사용
                val startMarker = Marker().apply {
                    position = NaverLatLng(c.startCoord.lat, c.startCoord.lng)
                    icon = startIcon
                    captionText = c.name
                    captionColor = ApexColors.Text.toNaverArgb()
                    captionHaloColor = ApexColors.Bg.copy(alpha = 0.7f).toNaverArgb()
                    onClickListener = Overlay.OnClickListener {
                        onMarkerClick(c.courseId)
                        true
                    }
                }
                startMarker.map = map
                overlays.add(startMarker)

                // 도착 마커 (체커)
                val endMarker = Marker().apply {
                    position = NaverLatLng(c.endCoord.lat, c.endCoord.lng)
                    icon = finishIcon
                    alpha = 0.85f
                }
                endMarker.map = map
                overlays.add(endMarker)
            }
        }

        onDispose {
            overlays.forEach { it.map = null }
            overlays.clear()
        }
    }
}

@Composable
private fun SearchBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(48.dp)
            .background(ApexColors.BgRaised.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
            .border(1.dp, ApexColors.Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = ApexColors.TextTer, modifier = Modifier.size(18.dp))
        Text(
            text = "코스, 지역, 라이더 검색",
            color = ApexColors.TextTer,
            fontSize = 14.sp,
            fontFamily = Pretendard,
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ProfileChip(
    nickname: String,
    carDisplay: String,
    onLogout: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(48.dp)
            .background(ApexColors.BgRaised.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
            .border(1.dp, ApexColors.Border, RoundedCornerShape(14.dp))
            .combinedClickable(onClick = {}, onLongClick = { menuOpen = true }),
        contentAlignment = Alignment.Center,
    ) {
        InitialBadge(nickname = nickname, carDisplay = carDisplay, sizeDp = 30)
    }
    DropdownMenu(
        expanded = menuOpen,
        onDismissRequest = { menuOpen = false },
        modifier = Modifier.background(ApexColors.BgElevated),
    ) {
        DropdownMenuItem(
            text = { Text("로그아웃", color = ApexColors.Text, fontFamily = Pretendard) },
            leadingIcon = { Icon(Icons.Filled.Logout, null, tint = ApexColors.TextSec) },
            onClick = {
                menuOpen = false
                onLogout()
            }
        )
    }
}

@Composable
private fun MapLoadFailureBanner(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(ApexColors.BgRaised, MaterialTheme.shapes.large)
            .border(1.dp, ApexColors.Red.copy(alpha = 0.5f), MaterialTheme.shapes.large)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Overline(text = "MAP TILES NOT LOADING", color = ApexColors.Red, tracking = 0.32f)
        Spacer(Modifier.height(8.dp))
        Text(
            "지도 타일이 안 받아져요",
            color = ApexColors.Text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Pretendard,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Naver Cloud Platform Console 에서:\n" +
                "1. Maps Mobile Dynamic Map 활성화\n" +
                "2. Application 의 Android 패키지명에 com.driveincar 등록\n" +
                "3. Client ID 가 local.properties 의 NAVER_MAP_CLIENT_ID 와 일치하는지 확인",
            color = ApexColors.TextSec,
            fontSize = 13.sp,
            fontFamily = Pretendard,
        )
    }
}

@Composable
private fun EmptyHint(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(ApexColors.BgRaised.copy(alpha = 0.92f), RoundedCornerShape(14.dp))
            .border(1.dp, ApexColors.Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Overline(text = "ATLAS LOADING", color = ApexColors.BrandLight, tracking = 0.32f)
        Spacer(Modifier.height(8.dp))
        Text(
            "코스를 불러오고 있어요",
            color = ApexColors.Text,
            fontSize = 15.sp,
            fontFamily = Pretendard,
        )
        Text(
            "한 번도 보이지 않으면 시드 스크립트를 돌려주세요",
            color = ApexColors.TextSec,
            fontSize = 12.sp,
            fontFamily = Pretendard,
        )
    }
}

@Composable
private fun CoursePeekCard(
    course: Course,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = ApexColors.accentFor(course.courseId)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ApexColors.BgRaised, RoundedCornerShape(22.dp))
            .border(1.dp, ApexColors.Border, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.4f))),
                        RoundedCornerShape(14.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("⛰", fontSize = 24.sp, color = ApexColors.Bg)
            }
            Column(modifier = Modifier.weight(1f)) {
                Overline(text = course.regionName, color = ApexColors.TextTer, tracking = 0.16f)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = course.name,
                    color = ApexColors.Text,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Pretendard,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${"%.1f".format(course.distanceMeters / 1000.0)} km",
                        color = ApexColors.TextSec,
                        fontSize = 12.sp,
                        fontFamily = Pretendard,
                    )
                    Text(
                        text = "★".repeat(course.difficulty),
                        color = ApexColors.Amber,
                        fontSize = 12.sp,
                        fontFamily = Pretendard,
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        PrimaryButton(
            label = "코스 자세히 보기",
            onClick = onTap,
            leadingIcon = {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = ApexColors.Text,
                    modifier = Modifier.size(18.dp),
                )
            },
        )
    }
}
