package com.driveincar.ui.map

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driveincar.ui.profile.AvatarBadge
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
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
        position = CameraPosition.fromLatLngZoom(LatLng(37.5, 127.5), 6.5f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
        ) {
            for (c in courses) {
                Marker(
                    state = MarkerState(LatLng(c.startCoord.lat, c.startCoord.lng)),
                    title = c.name,
                    snippet = c.regionName,
                    onClick = {
                        onCourseSelected(c.courseId)
                        true
                    }
                )
            }
        }

        ProfileChip(
            avatarId = me?.profileImageId ?: "avatar_01",
            nickname = me?.nickname.orEmpty(),
            onLogout = {
                vm.signOut()
                onSignedOut()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ProfileChip(
    avatarId: String,
    nickname: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = { menuOpen = true },
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarBadge(avatarId = avatarId, sizeDp = 40)
            if (nickname.isNotBlank()) {
                Text(
                    nickname,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
        ) {
            DropdownMenuItem(
                text = { Text("로그아웃") },
                leadingIcon = { Icon(Icons.Filled.Logout, null) },
                onClick = {
                    menuOpen = false
                    onLogout()
                }
            )
        }
    }
}
