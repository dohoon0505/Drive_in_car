package com.driveincar.ui.race

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driveincar.core.time.TimeFormat
import com.driveincar.domain.race.RaceState
import com.driveincar.ui.components.Overline
import com.driveincar.ui.theme.ApexColors
import com.driveincar.ui.theme.Pretendard
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

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
    val isArmed = state is RaceState.Armed

    // Armed 상태에서는 화면 전체에 초록 분위기. 그 외는 코스 액센트 분위기.
    val baseColor = if (isArmed) ApexColors.Green else accent
    val bgBrush = Brush.radialGradient(
        colors = listOf(baseColor.copy(alpha = if (isArmed) 0.25f else 0.15f), Color.Black),
        radius = 1400f,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        // Top HUD: NOW DRIVING + course name (항상 노출)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 60.dp),
        ) {
            HudCard {
                Overline(
                    text = if (isArmed) "READY ZONE" else "NOW DRIVING",
                    color = if (isArmed) ApexColors.Green else accent,
                    tracking = 0.20f,
                )
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

        // 중앙 — 상태별 분기
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = state,
                transitionSpec = { fadeIn(animationSpec = androidx.compose.animation.core.tween(250)) togetherWith fadeOut() },
                label = "race-state",
            ) { current ->
                when (current) {
                    is RaceState.Idle -> Spacer(Modifier.height(1.dp))
                    is RaceState.Arming -> ArmingContent(distanceM = current.distanceToStartM, accent = accent)
                    is RaceState.Armed -> ArmedContent(state = current)
                    is RaceState.InRace -> InRaceContent(state = current)
                    is RaceState.Finished, is RaceState.Cancelled -> Text(
                        "처리 중…",
                        color = ApexColors.TextSec,
                        fontFamily = Pretendard,
                    )
                }
            }
        }

        // 하단 — InRace 일 때 3-셀 HUD, 그 외엔 안내 텍스트 비움
        if (state is RaceState.InRace) {
            val inRace = state as RaceState.InRace
            BottomHud(
                speedKmh = inRace.currentKmh,
                remainingM = inRace.distanceToEndM,
                elapsedMs = inRace.elapsedMs,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
            )
        }

        // 주행 중단 pill
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
private fun ArmingContent(distanceM: Double, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Overline(text = "MOVE TO START", color = accent, tracking = 0.32f)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${"%.0f".format(distanceM)} m",
            color = ApexColors.Text,
            fontSize = 64.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = Pretendard,
            letterSpacing = (-0.04).em,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "출발 지점으로 이동해주세요",
            color = ApexColors.TextSec,
            fontSize = 14.sp,
            fontFamily = Pretendard,
        )
    }
}

@Composable
private fun ArmedContent(state: RaceState.Armed) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Overline(text = "READY", color = ApexColors.Green, tracking = 0.32f)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "출발 가능한 코스",
            color = ApexColors.Text,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = Pretendard,
            letterSpacing = (-0.030).em,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "속도가 0km/h 로 5초 지속되었을 때 출발합니다",
            color = ApexColors.TextSec,
            fontSize = 14.sp,
            fontFamily = Pretendard,
        )
        Spacer(Modifier.height(28.dp))
        when {
            state.stationarySinceMs == null -> {
                // 아직 움직이는 중
                Text(
                    text = "정지 대기",
                    color = ApexColors.Text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Pretendard,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "차를 완전히 멈춰주세요",
                    color = ApexColors.TextTer,
                    fontSize = 13.sp,
                    fontFamily = Pretendard,
                )
            }
            else -> {
                // 카운트다운 중
                val n = state.countdownSecondsRemaining ?: 5
                Text(
                    text = "$n",
                    color = ApexColors.Green,
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = Pretendard,
                    letterSpacing = (-0.04).em,
                    lineHeight = 120.sp,
                )
            }
        }
    }
}

@Composable
private fun InRaceContent(state: RaceState.InRace) {
    Text(
        text = TimeFormat.raceTime(state.elapsedMs),
        color = ApexColors.Text,
        fontSize = 84.sp,
        fontWeight = FontWeight.Black,
        fontFamily = Pretendard,
        letterSpacing = (-0.04).em,
        lineHeight = 84.sp,
    )
}

@Composable
private fun BottomHud(
    speedKmh: Double,
    remainingM: Double,
    elapsedMs: Long,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(ApexColors.BgRaised.copy(alpha = 0.85f), RoundedCornerShape(18.dp))
            .border(1.dp, ApexColors.Border, RoundedCornerShape(18.dp))
            .padding(vertical = 16.dp),
    ) {
        HudMetric(
            label = "현재 속도",
            value = "${"%.0f".format(speedKmh)}",
            unit = "km/h",
            modifier = Modifier.weight(1f),
        )
        Box(modifier = Modifier
            .size(width = 1.dp, height = 40.dp)
            .background(ApexColors.Border)
            .align(Alignment.CenterVertically)
        )
        HudMetric(
            label = "남은 거리",
            value = "${"%.0f".format(remainingM)}",
            unit = "m",
            modifier = Modifier.weight(1f),
        )
        Box(modifier = Modifier
            .size(width = 1.dp, height = 40.dp)
            .background(ApexColors.Border)
            .align(Alignment.CenterVertically)
        )
        HudMetric(
            label = "타이머",
            value = TimeFormat.raceTime(elapsedMs),
            unit = null,
            modifier = Modifier.weight(1.2f),
        )
    }
}

@Composable
private fun HudMetric(
    label: String,
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Overline(text = label, color = ApexColors.TextTer, tracking = 0.16f)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = value,
                color = ApexColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard,
            )
            if (unit != null) {
                Text(
                    text = unit,
                    color = ApexColors.TextSec,
                    fontSize = 11.sp,
                    fontFamily = Pretendard,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun HudCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ApexColors.BgRaised.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
            .border(1.dp, ApexColors.Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        content = content,
    )
}
