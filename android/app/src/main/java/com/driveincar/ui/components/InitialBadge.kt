package com.driveincar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveincar.ui.profile.CarBrands
import com.driveincar.ui.theme.ApexColors
import com.driveincar.ui.theme.Pretendard

/**
 * 닉네임 첫 글자 + 차 브랜드 색의 원형 배지.
 * Apex Lines 디자인의 시그니처 프로필 마크.
 *
 * carDisplay 가 "BMW M4 CSL" 처럼 들어오면 첫 단어로 브랜드를 찾는다.
 * 일치하는 브랜드가 없으면 인디고 기본색.
 */
@Composable
fun InitialBadge(
    nickname: String,
    carDisplay: String? = null,
    sizeDp: Int = 40,
    modifier: Modifier = Modifier,
) {
    val color = remember(carDisplay) { resolveColor(carDisplay) }
    val initial = nickname.firstOrNull { !it.isWhitespace() }?.toString()?.uppercase() ?: "?"
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = ApexColors.Text,
            fontWeight = FontWeight.Bold,
            fontFamily = Pretendard,
            fontSize = (sizeDp * 0.42).sp,
        )
    }
}

private fun resolveColor(carDisplay: String?): Color {
    if (carDisplay.isNullOrBlank()) return ApexColors.Brand
    val firstWord = carDisplay.trim().substringBefore(' ')
    return CarBrands.byName(firstWord)?.color ?: ApexColors.Brand
}

@Composable
private fun <T> remember(key: T?, calc: () -> Color): Color =
    androidx.compose.runtime.remember(key) { calc() }
