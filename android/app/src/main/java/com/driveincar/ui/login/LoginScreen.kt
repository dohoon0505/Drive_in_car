package com.driveincar.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
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
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Drive in Car",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "와인딩의 모든 순간을 기록하세요",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(48.dp))

        SocialLoginButton(label = "Google로 시작", onClick = vm::openSheet, bg = Color(0xFFFFFFFF), fg = Color(0xFF1F2937), tempBadge = true)
        Spacer(Modifier.height(12.dp))
        SocialLoginButton(label = "Naver로 시작", onClick = vm::openSheet, bg = Color(0xFF03C75A), fg = Color.White, tempBadge = true)
        Spacer(Modifier.height(12.dp))
        SocialLoginButton(label = "Kakao로 시작", onClick = vm::openSheet, bg = Color(0xFFFEE500), fg = Color(0xFF111111), tempBadge = true)

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = vm::openSheet, modifier = Modifier.fillMaxWidth()) {
            Text("이메일로 로그인 / 회원가입")
        }
    }

    if (state.sheetVisible) {
        val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = vm::closeSheet,
            sheetState = sheet,
        ) {
            EmailPasswordSheet(state = state, vm = vm)
        }
    }
}

@Composable
private fun SocialLoginButton(
    label: String,
    onClick: () -> Unit,
    bg: Color,
    fg: Color,
    tempBadge: Boolean,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.SemiBold)
            if (tempBadge) {
                Surface(
                    color = Color(0x33000000),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        "임시",
                        color = fg,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmailPasswordSheet(state: LoginUiState, vm: LoginViewModel) {
    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Text("이메일로 시작", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            label = { Text("이메일") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            label = { Text("비밀번호 (6자 이상)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = vm::signIn,
                enabled = !state.isSubmitting,
                modifier = Modifier.weight(1f).height(48.dp),
            ) {
                if (state.isSubmitting) CircularProgressIndicator(modifier = Modifier.height(20.dp))
                else Text("로그인")
            }
            Button(
                onClick = vm::signUp,
                enabled = !state.isSubmitting,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) { Text("회원가입") }
        }
        Spacer(Modifier.height(8.dp))
    }
}
