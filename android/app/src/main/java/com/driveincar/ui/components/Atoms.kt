package com.driveincar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.driveincar.ui.theme.ApexColors

/**
 * APEX Lines 의 시그니처 "overline" — 11px, 0.16-0.32em tracking, UPPERCASE.
 * 매거진 톤을 잡아주는 가장 중요한 타이포 디테일.
 */
@Composable
fun Overline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = ApexColors.TextTer,
    tracking: Float = 0.20f,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = tracking.em,
    )
}

/** 상단/카드용 borderStrong 1px outline 컨테이너. */
@Composable
fun ApexCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    bg: Color = ApexColors.BgRaised,
    borderColor: Color = ApexColors.Border,
    cornerDp: Int = 18,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(cornerDp.dp))
            .border(1.dp, borderColor, RoundedCornerShape(cornerDp.dp))
            .padding(padding),
    ) { content() }
}

/** 풀-너비 primary CTA 버튼 (브랜드 색). */
@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ApexColors.Brand,
            contentColor = ApexColors.Text,
            disabledContainerColor = ApexColors.BgElevated,
            disabledContentColor = ApexColors.TextTer,
        ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (leadingIcon != null) leadingIcon()
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

/** Secondary CTA — bgElevated + 1px borderStrong. */
@Composable
fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ApexColors.BgElevated,
            contentColor = ApexColors.Text,
            disabledContainerColor = ApexColors.BgRaised,
            disabledContentColor = ApexColors.TextTer,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, ApexColors.BorderStrong),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (leadingIcon != null) leadingIcon()
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}
