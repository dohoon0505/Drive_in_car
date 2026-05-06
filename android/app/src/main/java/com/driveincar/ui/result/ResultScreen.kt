package com.driveincar.ui.result

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.driveincar.core.time.TimeFormat
import com.driveincar.ui.components.Overline
import com.driveincar.ui.components.PrimaryButton
import com.driveincar.ui.components.SecondaryButton
import com.driveincar.ui.theme.ApexColors
import com.driveincar.ui.theme.Pretendard

@Composable
fun ResultScreen(
    courseId: String,
    timeMs: Long,
    averageKmh: Double,
    flagged: Boolean,
    personalBest: Boolean,
    onViewRanking: () -> Unit,
    onRetry: () -> Unit,
    onBackToMap: () -> Unit,
) {
    val accent = ApexColors.accentFor(courseId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexColors.Bg)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.20f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.25f),
                        radius = size.width * 0.9f,
                    )
                )
            }
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(80.dp))

        // 메달 hero
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.4f))),
                    CircleShape,
                )
                .border(6.dp, accent.copy(alpha = 0.20f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = ApexColors.Bg,
                modifier = Modifier.size(48.dp),
            )
        }

        Spacer(Modifier.height(24.dp))

        if (personalBest) {
            Overline(text = "NEW PERSONAL BEST", color = accent, tracking = 0.32f)
            Spacer(Modifier.height(6.dp))
        }
        Text(
            text = "완주했어요",
            color = ApexColors.Text,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = Pretendard,
            letterSpacing = (-0.030).em,
        )
        if (flagged) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(ApexColors.Red.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(1.dp, ApexColors.Red.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "비정상 평균 속도로 리더보드에서 제외됐어요",
                    color = ApexColors.Red,
                    fontSize = 12.sp,
                    fontFamily = Pretendard,
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // 시간 카드
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ApexColors.BgRaised, RoundedCornerShape(20.dp))
                .border(1.dp, ApexColors.Border, RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Overline(text = "FINAL LAP", color = ApexColors.TextTer, tracking = 0.32f)
            Spacer(Modifier.height(8.dp))
            Text(
                text = TimeFormat.raceTime(timeMs),
                color = ApexColors.Text,
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                fontFamily = Pretendard,
                letterSpacing = (-0.04).em,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ApexColors.Border)
                    .height(1.dp),
            ) {}
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ResultStat(
                    label = "AVG SPEED",
                    value = "${"%.0f".format(averageKmh)}",
                    unit = "km/h",
                    modifier = Modifier.weight(1f),
                )
                Box(modifier = Modifier.width(1.dp).height(48.dp).background(ApexColors.Border))
                ResultStat(
                    label = "STATUS",
                    value = if (flagged) "FLAG" else "OK",
                    unit = "",
                    color = if (flagged) ApexColors.Red else ApexColors.Green,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(label = "다시 도전", onClick = onRetry, modifier = Modifier.weight(1f))
            PrimaryButton(label = "랭킹 보기", onClick = onViewRanking, modifier = Modifier.weight(1.4f))
        }
        Spacer(Modifier.height(8.dp))
        SecondaryButton(label = "지도로", onClick = onBackToMap)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ResultStat(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    color: Color = ApexColors.Text,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Overline(text = label, color = ApexColors.TextTer, tracking = 0.20f)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = value,
                color = color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard,
            )
            if (unit.isNotEmpty()) {
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
