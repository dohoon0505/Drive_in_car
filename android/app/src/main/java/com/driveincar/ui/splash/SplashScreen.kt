package com.driveincar.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.driveincar.ui.components.Overline
import com.driveincar.ui.theme.ApexColors
import com.driveincar.ui.theme.Pretendard

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexColors.Bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Overline(
                text = "THE DRIVER'S ATLAS",
                color = ApexColors.BrandLight,
                tracking = 0.32f,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(
                        color = ApexColors.Text,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 56.sp,
                        letterSpacing = (-0.04).em,
                    )) {
                        append("APEX")
                    }
                },
                fontFamily = Pretendard,
                lineHeight = 56.sp,
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(
                        color = ApexColors.BrandLight,
                        fontWeight = FontWeight.Light,
                        fontSize = 56.sp,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = (-0.04).em,
                    )) {
                        append("Lines.")
                    }
                },
                fontFamily = Pretendard,
                lineHeight = 56.sp,
            )
        }
    }
}
