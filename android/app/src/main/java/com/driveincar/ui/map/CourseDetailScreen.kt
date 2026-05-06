package com.driveincar.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driveincar.core.time.TimeFormat
import com.driveincar.domain.model.Ranking
import com.driveincar.ui.profile.AvatarBadge

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course?.name ?: "코스") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        if (course == null) {
            Column(modifier = Modifier.padding(padding).padding(24.dp)) {
                Text("코스를 불러오는 중…")
            }
            return@Scaffold
        }
        val c = course!!
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Text(c.regionName, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("거리 ${"%.1f".format(c.distanceMeters / 1000.0)} km")
                Text("난이도 ${"★".repeat(c.difficulty)}")
            }
            Spacer(Modifier.height(16.dp))
            Text(c.description, style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(24.dp))
            Text("Top 3", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            if (top3.isEmpty()) {
                Text("아직 기록이 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                top3.forEachIndexed { idx, r -> RankingRow(idx + 1, r) }
            }

            Spacer(Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onJoinRace,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("랭킹전 참여") }
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("참여 안내") }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onViewRanking,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
            ) { Text("전체 랭킹") }
        }
    }
}

@Composable
private fun RankingRow(rank: Int, r: Ranking) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "#$rank",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(end = 4.dp),
        )
        AvatarBadge(avatarId = r.profileImageId, sizeDp = 32)
        Column(modifier = Modifier.weight(1f)) {
            Text(r.nickname, style = MaterialTheme.typography.bodyLarge)
            Text(r.carDisplay, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(TimeFormat.raceTime(r.timeMs), style = MaterialTheme.typography.titleLarge)
    }
}
