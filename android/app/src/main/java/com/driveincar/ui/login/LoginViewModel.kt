package com.driveincar.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driveincar.data.auth.AuthRepository
import com.driveincar.data.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

sealed interface LoginEvent {
    data class SignedIn(val needsProfile: Boolean) : LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val users: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun onEmailChange(s: String) = _uiState.update { it.copy(email = s, error = null) }
    fun onPasswordChange(s: String) = _uiState.update { it.copy(password = s, error = null) }

    fun signIn() = submit { auth.signInWithEmail(it.email.trim(), it.password) }
    fun signUp() = submit { auth.signUpWithEmail(it.email.trim(), it.password) }

    private fun submit(block: suspend (LoginUiState) -> Result<String>) {
        val s = _uiState.value
        if (s.isSubmitting) return
        if (s.email.isBlank() || s.password.length < 6) {
            _uiState.update { it.copy(error = "이메일과 6자 이상 비밀번호를 입력해주세요.") }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            // Auth 호출도 네트워크가 안 되면 매달릴 수 있어 안전망 timeout.
            val result = withTimeoutOrNull(15_000L) { block(s) }
            when {
                result == null -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = "응답이 늦어요. 인터넷 연결을 확인해주세요.",
                    )
                }
                result.isSuccess -> {
                    val uid = result.getOrThrow()
                    val needsProfile = users.fetchUser(uid) == null
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.emit(LoginEvent.SignedIn(needsProfile))
                }
                else -> {
                    val e = result.exceptionOrNull()
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = e?.localizedMessage ?: "로그인에 실패했어요.",
                        )
                    }
                }
            }
        }
    }
}
