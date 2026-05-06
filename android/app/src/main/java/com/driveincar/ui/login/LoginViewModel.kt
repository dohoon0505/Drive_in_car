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
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val sheetVisible: Boolean = false,
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

    fun openSheet() = _uiState.update { it.copy(sheetVisible = true, error = null) }
    fun closeSheet() = _uiState.update { it.copy(sheetVisible = false) }
    fun onEmailChange(s: String) = _uiState.update { it.copy(email = s) }
    fun onPasswordChange(s: String) = _uiState.update { it.copy(password = s) }

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
            block(s).fold(
                onSuccess = { uid ->
                    val needsProfile = users.fetchUser(uid) == null
                    _uiState.update { it.copy(isSubmitting = false, sheetVisible = false) }
                    _events.emit(LoginEvent.SignedIn(needsProfile))
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.localizedMessage) }
                }
            )
        }
    }
}
