package com.driveincar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

/**
 * 시스템 sans-serif (안드로이드 기본 한글 폰트, 보통 Noto Sans CJK KR) 를 사용한다.
 *
 * 이전엔 Pretendard 를 Google Fonts downloadable 로 받으려 했으나 cert XML 의 base64
 * 파싱 이슈로 첫 텍스트 렌더 시 IllegalStateException 으로 크래시. 시스템 폰트는
 * 추가 다운로드 없이 즉시 사용 가능하고 한글 디자인 어휘에도 충분히 부합한다.
 *
 * 추후 Pretendard 가 꼭 필요하면 res/font/ 에 TTF 를 번들하거나, Compose 1.6+ 의
 * GoogleFont API + 정상 cert resource 로 전환.
 */
private val Sans = FontFamily.SansSerif
val Pretendard: FontFamily = Sans  // 다른 모듈이 import 하고 있어 alias 유지.

/**
 * APEX Lines 디자인 핸드오프의 type scale 을 Material3 Typography 슬롯에 매핑.
 */
val ApexTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 56.sp,
        lineHeight = 58.8.sp,
        letterSpacing = (-0.035).em,
    ),
    displayMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp,
        lineHeight = 46.2.sp,
        letterSpacing = (-0.035).em,
    ),
    displaySmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Black,
        fontSize = 84.sp,
        lineHeight = 84.sp,
        letterSpacing = (-0.04).em,
    ),
    headlineLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 36.8.sp,
        letterSpacing = (-0.030).em,
    ),
    headlineMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.8.sp,
        letterSpacing = (-0.025).em,
    ),
    headlineSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 22.8.sp,
        letterSpacing = (-0.020).em,
    ),
    titleLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 26.4.sp,
        letterSpacing = (-0.025).em,
    ),
    titleMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.6.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 27.2.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.2.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.20.em,
    ),
)
