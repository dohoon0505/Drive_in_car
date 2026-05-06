package com.driveincar.ui.ranking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driveincar.core.time.TimeFormat
import com.driveincar.domain.model.Ranking
import com.driveincar.ui.profile.AvatarBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    courseId: String,
    onBack: () -> Unit,
    vm: RankingViewModel = hiltViewModel(),
) {
    val course by vm.course.collectAsStateWithLifecycle()
    val rankings by vm.rankings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course?.name ?: "랭킹") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        if (rankings.isEmpty()) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "아직 기록이 없습니다",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(rankings) { r ->
                    val rank = rankings.indexOf(r) + 1
                    RankingRow(rank, r)
                }
            }
        }
    }
}

@Composable
private fun RankingRow(rank: Int, r: Ranking) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "#$rank",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(end = 4.dp),
        )
        AvatarBadge(avatarId = r.profileImageId, sizeDp = 36)
        Column(modifier = Modifier.weight(1f)) {
            Text(r.nickname, style = MaterialTheme.typography.bodyLarge)
            Text(
                r.carDisplay,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(TimeFormat.raceTime(r.timeMs), style = MaterialTheme.typography.titleLarge)
    }
}
