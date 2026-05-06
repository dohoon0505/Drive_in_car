package com.driveincar.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * APEX Lines 디자인 시스템 색상 토큰. 다크 모드가 기본이며 라이트 모드는 미사용.
 * docs/design-ref 의 colors_and_type.css 와 1:1 매핑.
 */
object ApexColors {
    // Background (대부분 검정에 가까움)
    val Bg            = Color(0xFF0B0D12)
    val BgRaised      = Color(0xFF14171F)
    val BgElevated    = Color(0xFF1B1F29)

    // Border
    val Border        = Color(0xFF2A2D36)
    val BorderStrong  = Color(0xFF3C404B)

    // Text
    val Text          = Color(0xFFFFFFFF)
    val TextSec       = Color(0xFFB1B6C4)
    val TextTer       = Color(0xFF818797)

    // Brand (인디고)
    val Brand         = Color(0xFF4F46E5)
    val BrandLight    = Color(0xFF7968EE)
    val BrandDeep     = Color(0xFF3D34C2)

    // Accents
    val Amber         = Color(0xFFFFD60A)
    val AmberDeep     = Color(0xFFB68B00)
    val Green         = Color(0xFF22C55E)
    val Red           = Color(0xFFF87171)

    // Course accent palette (코스마다 시그니처 액센트색을 부여)
    val CourseAccents = listOf(Amber, BrandLight, Green, Red)

    /** 코스 ID 의 결정적 해시 → CourseAccents 인덱스. */
    fun accentFor(courseId: String): Color =
        CourseAccents[(courseId.hashCode().let { if (it < 0) -it else it }) % CourseAccents.size]
}
