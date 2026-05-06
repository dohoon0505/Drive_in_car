package com.driveincar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.driveincar.R

/**
 * Pretendard via Google Fonts downloadable. 디바이스에 설치 안 돼 있으면
 * Play Services 가 백그라운드에서 받아오고, 그 사이엔 시스템 sans-serif 로 폴백.
 */
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular,    FontWeight.Normal,    FontStyle.Normal),
    Font(R.font.pretendard_medium,     FontWeight.Medium,    FontStyle.Normal),
    Font(R.font.pretendard_semibold,   FontWeight.SemiBold,  FontStyle.Normal),
    Font(R.font.pretendard_bold,       FontWeight.Bold,      FontStyle.Normal),
    Font(R.font.pretendard_extrabold,  FontWeight.ExtraBold, FontStyle.Normal),
    Font(R.font.pretendard_black,      FontWeight.Black,     FontStyle.Normal),
)

/**
 * APEX Lines 디자인 핸드오프의 type scale 을 Material3 Typography 슬롯에 매핑.
 *
 * Display lg / md  → display{Large, Medium}
 * Heading lg/md/sm → headline{Large, Medium, Small}
 * Body lg/md/sm    → body{Large, Medium, Small}
 * Label            → label{Large, Medium}
 */
val ApexTypography = Typography(
    // Display lg — 56/1.05, weight 800, -0.035em
    displayLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 56.sp,
        lineHeight = 58.8.sp,
        letterSpacing = (-0.035).em,
    ),
    // Display md — 44/1.05, 800
    displayMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp,
        lineHeight = 46.2.sp,
        letterSpacing = (-0.035).em,
    ),
    // Display sm (lap time) — 84/1.0, 900
    displaySmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Black,
        fontSize = 84.sp,
        lineHeight = 84.sp,
        letterSpacing = (-0.04).em,
    ),
    // Heading lg — 32/1.15, 800
    headlineLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 36.8.sp,
        letterSpacing = (-0.030).em,
    ),
    // Heading md — 24/1.2, 700
    headlineMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.8.sp,
        letterSpacing = (-0.025).em,
    ),
    // Heading sm — 19/1.2, 700
    headlineSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 22.8.sp,
        letterSpacing = (-0.020).em,
    ),
    // Title roles 도 매거진 헤딩으로 통일
    titleLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 26.4.sp,
        letterSpacing = (-0.025).em,
    ),
    titleMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.6.sp,
    ),
    // Body lg — 17/1.6
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 27.2.sp,
    ),
    // Body md (default) — 15/1.6
    bodyMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp,
    ),
    // Body sm — 13/1.55
    bodySmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
    ),
    // Label — 13/1.4, 600
    labelLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.2.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    // Overline — 11/1.0, 500-600, 0.16-0.32em UPPERCASE (callsite 에서 letterSpacing 변경 가능)
    labelSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.20.em,
    ),
)
