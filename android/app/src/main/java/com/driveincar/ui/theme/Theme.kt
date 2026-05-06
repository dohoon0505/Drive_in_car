package com.driveincar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ApexDarkColors = darkColorScheme(
    primary = ApexColors.Brand,
    onPrimary = ApexColors.Text,
    primaryContainer = ApexColors.BrandDeep,
    onPrimaryContainer = ApexColors.Text,
    secondary = ApexColors.BrandLight,
    onSecondary = ApexColors.Text,
    background = ApexColors.Bg,
    onBackground = ApexColors.Text,
    surface = ApexColors.BgRaised,
    onSurface = ApexColors.Text,
    surfaceVariant = ApexColors.BgElevated,
    onSurfaceVariant = ApexColors.TextSec,
    surfaceContainerHighest = ApexColors.BgElevated,
    outline = ApexColors.Border,
    outlineVariant = ApexColors.BorderStrong,
    error = ApexColors.Red,
    onError = ApexColors.Text,
    errorContainer = ApexColors.Red,
    onErrorContainer = ApexColors.Text,
)

@Composable
fun DriveInCarTheme(content: @Composable () -> Unit) {
    // APEX Lines 는 다크 모드 전용 — system Light 모드여도 강제 다크.
    MaterialTheme(
        colorScheme = ApexDarkColors,
        typography = ApexTypography,
        content = content,
    )
}
