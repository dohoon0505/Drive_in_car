package com.driveincar.ui.map

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driveincar.core.nav.ExternalNavigation
import com.driveincar.core.time.TimeFormat
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.Ranking
import com.driveincar.ui.components.InitialBadge
import com.driveincar.ui.components.Overline
import com.driveincar.ui.components.PrimaryButton
import com.driveincar.ui.components.SecondaryButton
import com.driveincar.ui.theme.ApexColors
import com.driveincar.ui.theme.Pretendard

private const val MAX_RACE_DISTANCE_M = 5_000.0

@Composable
fun CourseDetailScreen(
    courseId: String,
    onJoinRace: () -> Unit,
    onViewRanking: () -> Unit,
    onBack: () -> Unit,
    vm: CourseDetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val course by vm.course.collectAsStateWithLifecycle()
    val top3 by vm.top3.collectAsStateWithLifecycle()
    val distanceFromMe by vm.distanceFromStartM.collectAsStateWithLifecycle()

    val withinRange = distanceFromMe != null && (distanceFromMe ?: Double.MAX_VALUE) <= MAX_RACE_DISTANCE_M

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexColors.Bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 110.dp),
        ) {
            val c = course
            if (c != null) {
                Hero(course = c, onBack = onBack)
                Spacer(Modifier.height(16.dp))
                CoursePreviewMap(
                    course = c,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, ApexColors.Border, RoundedCornerShape(18.dp))
                )
                Spacer(Modifier.height(20.dp))
                StatsRow(course = c)
                Spacer(Modifier.height(24.dp))
                BlurbBlock(blurb = c.description)
                Spacer(Modifier.height(24.dp))
                RankingSection(course = c, top3 = top3, onViewAll = onViewRanking)
                Spacer(Modifier.height(40.dp))
            } else {
                Spacer(Modifier.height(120.dp))
                Text(
                    "코스를 불러오는 중…",
                    color = ApexColors.TextSec,
                    fontFamily = Pretendard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                )
            }
        }

        // sticky footer CTA
        StickyFooter(
            distanceFromMeM = distanceFromMe,
            withinRange = withinRange,
            onJoinInfo = {
                val c = course ?: return@StickyFooter
                ExternalNavigation.openNaverMapDriving(
                    context = context,
                    dest = c.startCoord,
                    destName = c.name,
                )
            },
            onJoinRace = {
                if (withinRange) {
                    onJoinRace()
                } else {
                    Toast.makeText(
                        context,
                        "출발 지점 5km 이내에서만 시작할 수 있어요",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, ApexColors.Bg, ApexColors.Bg),
                    )
                )
                .padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun Hero(course: Course, onBack: () -> Unit) {
    val accent = ApexColors.accentFor(course.courseId)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(accent.copy(alpha = 0.15f), ApexColors.Bg),
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.20f),
                            Color.Transparent,
                            accent.copy(alpha = 0.10f),
                        )
                    )
                )
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(44.dp)
                .background(ApexColors.BgRaised.copy(alpha = 0.7f), CircleShape)
                .border(1.dp, ApexColors.Border, CircleShape),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = ApexColors.Text)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
        ) {
            Overline(text = course.regionName, color = accent, tracking = 0.16f)
            Spacer(Modifier.height(8.dp))
            Text(
                text = course.name,
                color = ApexColors.Text,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = Pretendard,
                letterSpacing = (-0.030).em,
                lineHeight = 40.sp,
            )
        }
    }
}

/**
 * 4-셀 통계 행. 모든 셀 동일 BgRaised + 1dp Border + 76dp 고정 height + 균일 패딩.
 * 4번째 셀은 "지역" 대신 "포인트 수" — 다른 셀(숫자) 와 시각 무게 통일.
 */
@Composable
private fun StatsRow(course: Course) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCell(
            label = "거리",
            value = "%.1f".format(course.distanceMeters / 1000.0),
            unit = "km",
            modifier = Modifier.weight(1f),
        )
        StatCell(
            label = "난이도",
            value = "★".repeat(course.difficulty),
            unit = null,
            valueColor = ApexColors.Amber,
            modifier = Modifier.weight(1f),
        )
        StatCell(
            label = "포인트",
            value = "${course.waypoints.size + 2}",
            unit = "개",
            modifier = Modifier.weight(1f),
        )
        StatCell(
            label = "참여",
            value = "${course.distanceMeters.toInt() / 100}",
            unit = "회",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
    valueColor: Color = ApexColors.Text,
) {
    Column(
        modifier = modifier
            .height(76.dp)
            .background(ApexColors.BgRaised, RoundedCornerShape(14.dp))
            .border(1.dp, ApexColors.Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Overline(text = label, color = ApexColors.TextTer, tracking = 0.16f)
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                color = valueColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard,
            )
            if (unit != null) {
                Text(
                    text = unit,
                    color = ApexColors.TextSec,
                    fontSize = 11.sp,
                    fontFamily = Pretendard,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun BlurbBlock(blurb: String) {
    Text(
        text = blurb,
        color = ApexColors.TextSec,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        fontFamily = Pretendard,
        modifier = Modifier.padding(horizontal = 24.dp),
    )
}

@Composable
private fun RankingSection(course: Course, top3: List<Ranking>, onViewAll: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "이 코스의 랭킹",
                color = ApexColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "참여 ${top3.size}명+",
                color = ApexColors.TextTer,
                fontSize = 12.sp,
                fontFamily = Pretendard,
            )
        }
        Spacer(Modifier.height(14.dp))

        if (top3.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ApexColors.BgRaised, RoundedCornerShape(18.dp))
                    .border(1.dp, ApexColors.Border, RoundedCornerShape(18.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Overline(text = "EMPTY GRID", color = ApexColors.TextTer, tracking = 0.32f)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "첫 번째 라이더가 되어보세요",
                        color = ApexColors.TextSec,
                        fontSize = 14.sp,
                        fontFamily = Pretendard,
                    )
                }
            }
        } else {
            Podium(top3 = top3)
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ApexColors.BgRaised, RoundedCornerShape(18.dp))
                    .border(1.dp, ApexColors.Border, RoundedCornerShape(18.dp))
                    .padding(vertical = 8.dp),
            ) {
                top3.forEachIndexed { idx, r ->
                    val delta = if (idx == 0) null else (r.timeMs - top3[0].timeMs)
                    RankingRow(rank = idx + 1, ranking = r, deltaMs = delta)
                }
            }
            Spacer(Modifier.height(12.dp))
            SecondaryButton(label = "전체 랭킹 보기", onClick = onViewAll)
        }
    }
}

@Composable
private fun Podium(top3: List<Ranking>) {
    val first = top3.getOrNull(0)
    val second = top3.getOrNull(1)
    val third = top3.getOrNull(2)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ApexColors.BgRaised, RoundedCornerShape(18.dp))
            .border(1.dp, ApexColors.Border, RoundedCornerShape(18.dp))
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PodiumColumn(rank = 2, ranking = second, color = Color(0xFFB1B6C4), barHeight = 64, modifier = Modifier.weight(1f))
        PodiumColumn(rank = 1, ranking = first, color = ApexColors.Amber, barHeight = 92, modifier = Modifier.weight(1f))
        PodiumColumn(rank = 3, ranking = third, color = Color(0xFFB68B00), barHeight = 48, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PodiumColumn(
    rank: Int,
    ranking: Ranking?,
    color: Color,
    barHeight: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (ranking != null) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = ApexColors.Bg,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = ranking.nickname,
                color = ApexColors.Text,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Pretendard,
            )
            Text(
                text = TimeFormat.raceTime(ranking.timeMs),
                color = ApexColors.TextSec,
                fontSize = 11.sp,
                fontFamily = Pretendard,
            )
            Spacer(Modifier.height(8.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight.dp)
                .background(
                    Brush.verticalGradient(listOf(color, color.copy(alpha = 0.55f))),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$rank",
                color = ApexColors.Bg,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = Pretendard,
            )
        }
    }
}

@Composable
private fun RankingRow(rank: Int, ranking: Ranking, deltaMs: Long?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "#$rank",
            color = ApexColors.TextTer,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Pretendard,
            modifier = Modifier.size(28.dp, 16.dp),
        )
        InitialBadge(nickname = ranking.nickname, carDisplay = ranking.carDisplay, sizeDp = 36)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ranking.nickname,
                color = ApexColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Pretendard,
            )
            Text(
                text = ranking.carDisplay,
                color = ApexColors.TextTer,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Pretendard,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = TimeFormat.raceTime(ranking.timeMs),
                color = ApexColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard,
            )
            if (deltaMs != null) {
                Text(
                    text = "+%.3f".format(deltaMs / 1000.0),
                    color = ApexColors.Red,
                    fontSize = 11.sp,
                    fontFamily = Pretendard,
                )
            }
        }
    }
}

@Composable
private fun StickyFooter(
    distanceFromMeM: Double?,
    withinRange: Boolean,
    onJoinInfo: () -> Unit,
    onJoinRace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (distanceFromMeM != null && !withinRange) {
            Text(
                text = "출발 지점에서 ${"%.1f".format(distanceFromMeM / 1000.0)} km 떨어져 있어요. 5km 이내로 이동하면 시작할 수 있어요.",
                color = ApexColors.Red,
                fontSize = 12.sp,
                fontFamily = Pretendard,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(
                label = "참여 안내",
                onClick = onJoinInfo,
                modifier = Modifier.weight(1f),
            )
            PrimaryButton(
                label = "랭킹전 참여",
                onClick = onJoinRace,
                enabled = withinRange,
                modifier = Modifier.weight(1.4f),
            )
        }
    }
}
