package com.driveincar.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driveincar.R
import com.driveincar.core.geo.Geo
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.LatLng
import com.driveincar.domain.model.toLatLngList
import com.driveincar.ui.components.InitialBadge
import com.driveincar.ui.components.Overline
import com.driveincar.ui.theme.ApexColors
import com.driveincar.ui.theme.Pretendard
import com.naver.maps.geometry.LatLng as NaverLatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
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
    val nearestCourses by vm.nearestCourses.collectAsStateWithLifecycle()
    val me by vm.me.collectAsStateWithLifecycle()
    val myLocation by vm.myLocation.collectAsStateWithLifecycle()

    var mapLoaded by remember { mutableStateOf(false) }
    var showLoadHint by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(8_000)
        if (!mapLoaded) showLoadHint = true
    }

    val startIcon = rememberOverlayImage(R.drawable.ic_marker_start, sizeDp = 36)
    val finishIcon = rememberOverlayImage(R.drawable.ic_marker_finish, sizeDp = 32)

    var naverMap by remember { mutableStateOf<NaverMap?>(null) }

    // 첫 GPS fix 가 떨어지면 카메라를 그 위치로 줌인.
    LaunchedEffect(naverMap, myLocation) {
        val map = naverMap ?: return@LaunchedEffect
        val here = myLocation ?: return@LaunchedEffect
        map.moveCamera(
            CameraUpdate.scrollAndZoomTo(
                NaverLatLng(here.lat, here.lng),
                13.0,
            ).animate(CameraAnimation.Easing)
        )
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(ApexColors.Bg)
    ) {
        NaverMapBox(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                naverMap = map
                // 위치 미허용 케이스 fallback: 한반도 중앙
                map.cameraPosition = CameraPosition(NaverLatLng(36.5, 127.8), 6.5)
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

        CourseOverlays(
            naverMap = naverMap,
            courses = courses,
            startIcon = startIcon,
            finishIcon = finishIcon,
            onMarkerClick = { onCourseSelected(it) },
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

        // 가까운 코스 3개 스택 (가장 가까운 순)
        if (nearestCourses.isNotEmpty()) {
            NearbyCourseStack(
                courses = nearestCourses,
                myLocation = myLocation,
                onCourseTap = { onCourseSelected(it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun NearbyCourseStack(
    courses: List<Course>,
    myLocation: LatLng?,
    onCourseTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Overline(
            text = "가까운 코스",
            color = ApexColors.TextSec,
            tracking = 0.20f,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
        )
        for (c in courses) {
            NearbyCourseCard(
                course = c,
                myLocation = myLocation,
                onTap = { onCourseTap(c.courseId) },
            )
        }
    }
}

@Composable
private fun NearbyCourseCard(
    course: Course,
    myLocation: LatLng?,
    onTap: () -> Unit,
) {
    val accent = ApexColors.accentFor(course.courseId)
    val distanceKm: Double? = remember(course, myLocation) {
        myLocation?.let { Geo.distanceMeters(it, course.startCoord) / 1000.0 }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ApexColors.BgRaised, RoundedCornerShape(16.dp))
            .border(1.dp, ApexColors.Border, RoundedCornerShape(16.dp))
            .clickable { onTap() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 액센트 색 dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(accent, CircleShape),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = course.name,
                color = ApexColors.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Pretendard,
            )
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (distanceKm != null) {
                    Text(
                        text = "${"%.1f".format(distanceKm)} km 거리",
                        color = ApexColors.TextSec,
                        fontSize = 12.sp,
                        fontFamily = Pretendard,
                    )
                }
                Text(
                    text = "★".repeat(course.difficulty),
                    color = ApexColors.Amber,
                    fontSize = 12.sp,
                    fontFamily = Pretendard,
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = ApexColors.TextTer,
            modifier = Modifier.size(18.dp),
        )
    }
}

/**
 * courses 리스트 / naverMap 변경 시 폴리라인 + 마커를 모두 재구성.
 * 각 overlay 의 .map 을 null 로 두어 정리, 새로 set.
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
        overlays.forEach { it.map = null }
        overlays.clear()

        if (map != null) {
            for (c in courses) {
                val accent = ApexColors.accentFor(c.courseId)
                val pts = c.toLatLngList().toNaver()

                val path = PathOverlay().apply {
                    coords = pts
                    color = accent.copy(alpha = 0.85f).toNaverArgb()
                    width = 18
                    outlineWidth = 0
                }
                path.map = map
                overlays.add(path)

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
