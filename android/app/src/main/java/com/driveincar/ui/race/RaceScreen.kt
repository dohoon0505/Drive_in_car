package com.driveincar.ui.race

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driveincar.core.time.TimeFormat
import com.driveincar.domain.race.RaceState
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

    val finePermission = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        when (val s = state) {
            is RaceState.Idle, is RaceState.Arming -> ArmingView(state = s, onCancel = vm::userCancel)
            is RaceState.Armed -> ArmedView(onCancel = vm::userCancel)
            is RaceState.InRace -> InRaceView(state = s, onCancel = vm::userCancel)
            is RaceState.Finished, is RaceState.Cancelled -> {
                Text("처리 중…", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun ArmingView(state: RaceState, onCancel: () -> Unit) {
    val dist = (state as? RaceState.Arming)?.distanceToStartM ?: 0.0
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("출발 지점으로", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "${"%.0f".format(dist)} m",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(48.dp))
        CancelButton(onCancel)
    }
}

@Composable
private fun ArmedView(onCancel: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("출발 준비 완료", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("출발선을 통과하면 자동으로 시작됩니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(48.dp))
        CancelButton(onCancel)
    }
}

@Composable
private fun InRaceView(state: RaceState.InRace, onCancel: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = TimeFormat.raceTime(state.elapsedMs),
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Stat("남은 거리", "${"%.0f".format(state.distanceToEndM)} m")
            Stat("현재 속도", "${"%.0f".format(state.currentKmh)} km/h")
        }
        Spacer(Modifier.height(48.dp))
        CancelButton(onCancel)
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun CancelButton(onCancel: () -> Unit) {
    Button(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    ) { Text("중단") }
}
