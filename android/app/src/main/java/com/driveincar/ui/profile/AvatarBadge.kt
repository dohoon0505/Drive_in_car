package com.driveincar.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AvatarBadge(
    avatarId: String,
    sizeDp: Int = 56,
    fontSize: TextUnit = 24.sp,
    modifier: Modifier = Modifier,
) {
    val meta = Avatars.byId(avatarId)
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .background(meta.color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(meta.initial, fontSize = fontSize)
    }
}
