package com.driveincar.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driveincar.data.auth.AuthRepository
import com.driveincar.data.course.CourseRepository
import com.driveincar.data.user.UserRepository
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val courses: List<Course> = emptyList(),
    val me: User? = null,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val courseRepo: CourseRepository,
    private val userRepo: UserRepository,
    private val auth: AuthRepository,
) : ViewModel() {

    val courses: StateFlow<List<Course>> = courseRepo.observeActiveCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _me = MutableStateFlow<User?>(null)
    val me: StateFlow<User?> = _me.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = auth.currentUid ?: return@launch
            userRepo.observeUser(uid).collect { _me.value = it }
        }
    }

    fun signOut() = auth.signOut()
}
