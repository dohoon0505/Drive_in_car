package com.driveincar.ui.ranking

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driveincar.core.time.TimeFormat
import com.driveincar.domain.model.Ranking
import com.driveincar.ui.components.InitialBadge
import com.driveincar.ui.components.Overline
import com.driveincar.ui.theme.ApexColors
import com.driveincar.ui.theme.Pretendard

@Composable
fun RankingScreen(
    courseId: String,
    onBack: () -> Unit,
    vm: RankingViewModel = hiltViewModel(),
) {
    val course by vm.course.collectAsStateWithLifecycle()
    val rankings by vm.rankings.collectAsStateWithLifecycle()
    val accent = ApexColors.accentFor(courseId)

    Column(modifier = Modifier
        .fillMaxSize()
        .background(ApexColors.Bg)
    ) {
        // Top app bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(ApexColors.BgRaised, CircleShape)
                    .border(1.dp, ApexColors.Border, CircleShape),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = ApexColors.Text)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Overline(text = "LEADERBOARD", color = accent, tracking = 0.32f)
                Text(
                    text = course?.name ?: "랭킹",
                    color = ApexColors.Text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Pretendard,
                )
            }
        }

        if (rankings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Overline(text = "EMPTY GRID", color = ApexColors.TextTer, tracking = 0.32f)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "첫 번째 라이더가 되어보세요",
                        color = ApexColors.TextSec,
                        fontSize = 15.sp,
                        fontFamily = Pretendard,
                    )
                }
            }
        } else {
            val bestMs = rankings.first().timeMs
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(rankings) { r ->
                    val rank = rankings.indexOf(r) + 1
                    val deltaMs = if (rank == 1) null else r.timeMs - bestMs
                    RankingRow(rank = rank, ranking = r, deltaMs = deltaMs)
                }
            }
        }
    }
}

@Composable
private fun RankingRow(rank: Int, ranking: Ranking, deltaMs: Long?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ApexColors.BgRaised, RoundedCornerShape(14.dp))
            .border(1.dp, ApexColors.Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "#$rank",
            color = if (rank <= 3) ApexColors.Amber else ApexColors.TextTer,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Pretendard,
            modifier = Modifier.width(32.dp),
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
