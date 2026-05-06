package com.driveincar.ui.map

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.Ranking
import com.driveincar.ui.components.InitialBadge
import com.driveincar.ui.components.Overline
import com.driveincar.ui.components.PrimaryButton
import com.driveincar.ui.components.SecondaryButton
import com.driveincar.ui.theme.ApexColors
import com.driveincar.ui.theme.Pretendard

@Composable
fun CourseDetailScreen(
    courseId: String,
    onJoinRace: () -> Unit,
    onViewRanking: () -> Unit,
    onBack: () -> Unit,
    vm: CourseDetailViewModel = hiltViewModel(),
) {
    val course by vm.course.collectAsStateWithLifecycle()
    val top3 by vm.top3.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexColors.Bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 110.dp),  // sticky footer 만큼 여백
        ) {
            val c = course
            if (c != null) {
                Hero(course = c, onBack = onBack)
                StatsGrid(course = c)
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
            onJoinInfo = onViewRanking,
            onJoinRace = onJoinRace,
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
            .height(320.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(accent.copy(alpha = 0.15f), ApexColors.Bg),
                )
            )
    ) {
        // 추가 비스듬한 글로우
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

        // Top: 뒤로가기
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

        // Bottom-left: region overline + 이름 + blurb
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

@Composable
private fun StatsGrid(course: Course) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .background(ApexColors.Border, RoundedCornerShape(14.dp))
            .padding(1.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        StatCell(label = "거리", value = "${"%.1f".format(course.distanceMeters / 1000.0)} km", modifier = Modifier.weight(1f))
        StatCell(label = "난이도", value = "★".repeat(course.difficulty), modifier = Modifier.weight(1f))
        StatCell(label = "포인트", value = "${course.waypoints.size + 2}", modifier = Modifier.weight(1f))
        StatCell(label = "지역", value = course.regionName.take(2), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(ApexColors.BgRaised)
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            color = ApexColors.Text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Pretendard,
        )
        Spacer(Modifier.height(4.dp))
        Overline(text = label, color = ApexColors.TextTer, tracking = 0.04f)
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
            // top3 도 리스트로 한번 더 노출
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
    // 디자인의 podium: 1등=가운데(amber), 2등=좌(silver), 3등=우(bronze)
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
            modifier = Modifier.width(28.dp),
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
    onJoinInfo: () -> Unit,
    onJoinRace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SecondaryButton(
            label = "참여 안내",
            onClick = onJoinInfo,
            modifier = Modifier.weight(1f),
        )
        PrimaryButton(
            label = "랭킹전 참여",
            onClick = onJoinRace,
            modifier = Modifier.weight(1.4f),
        )
    }
}
