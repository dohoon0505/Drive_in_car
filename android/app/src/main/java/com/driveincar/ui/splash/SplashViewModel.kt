package com.driveincar.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driveincar.data.auth.AuthRepository
import com.driveincar.data.user.UserRepository
import com.driveincar.ui.nav.RootDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val users: UserRepository,
) : ViewModel() {

    private val _rootDestination = MutableStateFlow(RootDestination.Splash)
    val rootDestination: StateFlow<RootDestination> = _rootDestination.asStateFlow()

    init {
        decide()
    }

    private fun decide() {
        viewModelScope.launch {
            val uid = auth.currentUid
            _rootDestination.value = when {
                uid == null -> RootDestination.Login
                users.fetchUser(uid) == null -> RootDestination.ProfileSetup
                else -> RootDestination.Map
            }
        }
    }
}
