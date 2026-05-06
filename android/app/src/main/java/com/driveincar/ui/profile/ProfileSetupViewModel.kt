package com.driveincar.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driveincar.data.auth.AuthRepository
import com.driveincar.data.user.UserRepository
import com.driveincar.domain.model.User
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

data class ProfileSetupUiState(
    val nickname: String = "",
    val carBrand: String = "",
    val carModel: String = "",
    val avatarId: String = "avatar_01",
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

sealed interface ProfileSetupEvent {
    data object Saved : ProfileSetupEvent
}

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val users: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileSetupEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ProfileSetupEvent> = _events.asSharedFlow()

    fun onNickname(s: String) = _uiState.update { it.copy(nickname = s) }
    fun onBrand(s: String) = _uiState.update { it.copy(carBrand = s) }
    fun onModel(s: String) = _uiState.update { it.copy(carModel = s) }
    fun onAvatar(id: String) = _uiState.update { it.copy(avatarId = id) }

    fun save() {
        val s = _uiState.value
        val uid = auth.currentUid ?: run {
            _uiState.update { it.copy(error = "로그인 세션이 만료되었습니다.") }
            return
        }
        if (s.nickname.length !in 2..16) {
            _uiState.update { it.copy(error = "닉네임은 2~16자입니다.") }; return
        }
        if (s.carBrand.isBlank() || s.carModel.isBlank()) {
            _uiState.update { it.copy(error = "차량 브랜드와 모델을 입력해주세요.") }; return
        }
        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            users.createUser(
                User(
                    uid = uid,
                    nickname = s.nickname.trim(),
                    carBrand = s.carBrand.trim(),
                    carModel = s.carModel.trim(),
                    profileImageId = s.avatarId,
                )
            ).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.emit(ProfileSetupEvent.Saved)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.localizedMessage) }
                }
            )
        }
    }
}
