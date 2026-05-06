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
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject

data class ProfileSetupUiState(
    val step: Int = 0,                  // 0=닉네임, 1=메이커, 2=모델
    val nickname: String = "",
    val carBrand: String = "",          // 예: "BMW"
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

    fun onNickname(s: String) = _uiState.update { it.copy(nickname = s, error = null) }
    fun onBrand(name: String) = _uiState.update {
        it.copy(carBrand = name, carModel = "", error = null)   // 브랜드 변경 시 모델 초기화
    }
    fun onModel(s: String) = _uiState.update { it.copy(carModel = s, error = null) }
    fun onAvatar(id: String) = _uiState.update { it.copy(avatarId = id) }

    fun next() {
        val s = _uiState.value
        when (s.step) {
            0 -> {
                if (s.nickname.length !in 2..12) {
                    _uiState.update { it.copy(error = "닉네임은 2~12자예요.") }
                    return
                }
            }
            1 -> {
                if (s.carBrand.isBlank()) {
                    _uiState.update { it.copy(error = "메이커를 골라주세요.") }
                    return
                }
            }
            2 -> {
                if (s.carModel.isBlank()) {
                    _uiState.update { it.copy(error = "모델을 골라주세요.") }
                    return
                }
                save()
                return
            }
        }
        _uiState.update { it.copy(step = it.step + 1, error = null) }
    }

    fun back() {
        val s = _uiState.value
        if (s.step > 0) _uiState.update { it.copy(step = it.step - 1, error = null) }
    }

    private fun save() {
        val s = _uiState.value
        val uid = auth.currentUid ?: run {
            _uiState.update { it.copy(error = "로그인 세션이 만료됐어요. 다시 로그인해주세요.") }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            // Firestore set() 가 네트워크/규칙 이슈로 매달리는 경우 UI 가 영원히 submitting 으로
            // 보이는 freeze 를 막기 위해 15초 안전망. 정상 흐름은 1초 이내에 완료된다.
            val result = withTimeoutOrNull(15_000L) {
                users.createUser(
                    User(
                        uid = uid,
                        nickname = s.nickname.trim(),
                        carBrand = s.carBrand.trim(),
                        carModel = s.carModel.trim(),
                        profileImageId = s.avatarId,
                    )
                )
            }
            when {
                result == null -> {
                    Timber.w("createUser timed out after 15s for uid=$uid")
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = "저장이 오래 걸려요. 인터넷과 Firestore 규칙 배포 상태를 확인해주세요.",
                        )
                    }
                }
                result.isFailure -> {
                    val e = result.exceptionOrNull()
                    Timber.e(e, "createUser failed for uid=$uid")
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = e?.localizedMessage ?: "저장에 실패했어요. 다시 시도해주세요.",
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.emit(ProfileSetupEvent.Saved)
                }
            }
        }
    }
}
