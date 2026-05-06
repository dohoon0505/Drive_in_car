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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.toLatLngList
import com.driveincar.ui.components.InitialBadge
import com.driveincar.ui.components.Overline
import com.driveincar.ui.components.PrimaryButton
import com.driveincar.ui.theme.ApexColors
import com.driveincar.ui.theme.Pretendard
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MapScreen(
    onCourseSelected: (String) -> Unit,
    onSignedOut: () -> Unit,
    vm: MapViewModel = hiltViewModel(),
) {
    val courses by vm.courses.collectAsStateWithLifecycle()
    val me by vm.me.collectAsStateWithLifecycle()

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(36.5, 127.8), 6.5f)
    }
    var focusedCourseId by remember { mutableStateOf<String?>(null) }
    val focusedCourse: Course? = remember(focusedCourseId, courses) {
        focusedCourseId?.let { id -> courses.firstOrNull { it.courseId == id } }
            ?: courses.firstOrNull()
    }

    val mapStyle = remember { MapStyleOptions(MAP_STYLE_DARK_JSON) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(ApexColors.Bg)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            properties = MapProperties(mapStyleOptions = mapStyle),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = false,
            ),
        ) {
            for (c in courses) {
                val accent = ApexColors.accentFor(c.courseId)
                val pts = c.toLatLngList().map { LatLng(it.lat, it.lng) }
                // 코스 폴리라인 — 액센트 색으로 한반도 위에 빛난다
                Polyline(
                    points = pts,
                    color = accent.copy(alpha = 0.85f),
                    width = 6f,
                    jointType = JointType.ROUND,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    zIndex = 1f,
                )
                // 출발점 — 탭 가능, 코스 카드 트리거
                Marker(
                    state = MarkerState(LatLng(c.startCoord.lat, c.startCoord.lng)),
                    title = c.name,
                    snippet = c.regionName,
                    onClick = {
                        focusedCourseId = c.courseId
                        true
                    }
                )
                // 도착점 — 라인의 양 끝을 명확히 하기 위한 보조 마커
                Marker(
                    state = MarkerState(LatLng(c.endCoord.lat, c.endCoord.lng)),
                    title = "${c.name} (도착)",
                    alpha = 0.7f,
                )
            }
        }

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

        // 빈 코스일 때 안내
        if (courses.isEmpty()) {
            EmptyHint(modifier = Modifier.align(Alignment.Center))
        }

        // 하단 코스 peek 카드
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
                // 산 모양 단순화 (Lucide-style mountain 아이콘 자리에 글리프)
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
