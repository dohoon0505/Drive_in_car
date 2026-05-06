package com.driveincar.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driveincar.ui.components.Overline
import com.driveincar.ui.components.PrimaryButton
import com.driveincar.ui.components.SecondaryButton
import com.driveincar.ui.theme.ApexColors
import com.driveincar.ui.theme.Pretendard

@Composable
fun LoginScreen(
    onSignedIn: (needsProfile: Boolean) -> Unit,
    vm: LoginViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.events.collect { ev ->
            when (ev) {
                is LoginEvent.SignedIn -> onSignedIn(ev.needsProfile)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexColors.Bg)
            .drawBehind {
                // 상단 라디얼 인디고 글로우 (디자인의 ellipse gradient)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x2E4F46E5),  // 18% alpha
                            Color(0x00000000),
                        ),
                        center = androidx.compose.ui.geometry.Offset(size.width / 2, 0f),
                        radius = size.width * 0.9f,
                    )
                )
            }
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(88.dp))

        BrandMark()

        Spacer(Modifier.height(28.dp))
        Text(
            text = "전국의 가장 아름다운 와인딩 코스에서, 당신의 라인을 새겨요.",
            color = ApexColors.TextSec,
            fontSize = 15.sp,
            lineHeight = 24.sp,
            fontFamily = Pretendard,
        )

        Spacer(Modifier.height(40.dp))

        ApexField(
            label = "이메일",
            value = state.email,
            onValueChange = vm::onEmailChange,
            placeholder = "you@apex.kr",
            keyboardType = KeyboardType.Email,
        )
        Spacer(Modifier.height(12.dp))
        ApexField(
            label = "비밀번호",
            value = state.password,
            onValueChange = vm::onPasswordChange,
            placeholder = "6자 이상",
            keyboardType = KeyboardType.Password,
            isPassword = true,
        )

        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.error!!, color = ApexColors.Red, fontSize = 13.sp, fontFamily = Pretendard)
        }

        Spacer(Modifier.weight(1f))

        if (state.isSubmitting) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ApexColors.Brand)
            }
            Spacer(Modifier.height(8.dp))
        }

        PrimaryButton(label = "로그인하기", onClick = vm::signIn, enabled = !state.isSubmitting)
        Spacer(Modifier.height(10.dp))
        SecondaryButton(label = "새로 시작하기", onClick = vm::signUp, enabled = !state.isSubmitting)

        Spacer(Modifier.height(16.dp))
        Text(
            text = "계속하면 약관 및 개인정보 처리방침에 동의해요.",
            modifier = Modifier.fillMaxWidth(),
            color = ApexColors.TextTer,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontFamily = Pretendard,
        )
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun BrandMark() {
    Column {
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

@Composable
private fun ApexField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
) {
    Column {
        Overline(text = label, color = ApexColors.TextTer, tracking = 0.16f)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = ApexColors.TextTer) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ApexColors.Brand,
                unfocusedBorderColor = ApexColors.BorderStrong,
                focusedContainerColor = ApexColors.BgRaised,
                unfocusedContainerColor = ApexColors.BgRaised,
                focusedTextColor = ApexColors.Text,
                unfocusedTextColor = ApexColors.Text,
                cursorColor = ApexColors.Brand,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
