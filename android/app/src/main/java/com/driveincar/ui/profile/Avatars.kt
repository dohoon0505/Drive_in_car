package com.driveincar.ui.profile

import androidx.compose.ui.graphics.Color

/** 8종 아바타 (`avatar_01`..`avatar_08`)의 표시용 메타. */
data class AvatarMeta(
    val id: String,
    val initial: String,
    val color: Color,
)

object Avatars {
    val all: List<AvatarMeta> = listOf(
        AvatarMeta("avatar_01", "🚗", Color(0xFFEF4444)),
        AvatarMeta("avatar_02", "🏎️", Color(0xFFF59E0B)),
        AvatarMeta("avatar_03", "🚙", Color(0xFF10B981)),
        AvatarMeta("avatar_04", "🚘", Color(0xFF3B82F6)),
        AvatarMeta("avatar_05", "🏁", Color(0xFF8B5CF6)),
        AvatarMeta("avatar_06", "🛣️", Color(0xFFEC4899)),
        AvatarMeta("avatar_07", "🌄", Color(0xFF14B8A6)),
        AvatarMeta("avatar_08", "🔧", Color(0xFF6B7280)),
    )

    fun byId(id: String): AvatarMeta = all.firstOrNull { it.id == id } ?: all.first()
}
