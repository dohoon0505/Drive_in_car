package com.driveincar.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driveincar.core.geo.Geo
import com.driveincar.data.auth.AuthRepository
import com.driveincar.data.course.CourseRepository
import com.driveincar.data.location.LocationProvider
import com.driveincar.data.user.UserRepository
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.LatLng
import com.driveincar.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val courseRepo: CourseRepository,
    private val userRepo: UserRepository,
    private val auth: AuthRepository,
    private val location: LocationProvider,
) : ViewModel() {

    val courses: StateFlow<List<Course>> = courseRepo.observeActiveCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _me = MutableStateFlow<User?>(null)
    val me: StateFlow<User?> = _me.asStateFlow()

    /**
     * 첫 GPS fix 1회만 받아 보관. 지속 추적은 Race 화면에서만 (배터리 절약).
     * null = 권한 미허용 또는 아직 fix 못 잡음 → MapScreen 은 한국 중심 fallback 카메라.
     */
    private val _myLocation = MutableStateFlow<LatLng?>(null)
    val myLocation: StateFlow<LatLng?> = _myLocation.asStateFlow()

    /**
     * 현재 위치 기준 거리순 가장 가까운 코스 3개. 위치가 없으면 시드 순서 앞 3개.
     */
    val nearestCourses: StateFlow<List<Course>> = combine(courses, myLocation) { all, here ->
        if (here == null) all.take(3)
        else all.sortedBy { Geo.distanceMeters(here, it.startCoord) }.take(3)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val uid = auth.currentUid ?: return@launch
            userRepo.observeUser(uid).collect { _me.value = it }
        }
        viewModelScope.launch {
            // 첫 정확도 통과 샘플 1개 받고 그만. LocationProvider 가 30m 필터링 + 슬라이딩 윈도우
            // 통과한 샘플만 흘려보내므로 첫 emit 이 곧 신뢰 가능한 fix.
            runCatching {
                location.samples().take(1).collect { sample ->
                    _myLocation.value = sample.coord
                }
            }.onFailure { Timber.w(it, "myLocation first-fix failed (permission denied?)") }
        }
    }

    fun signOut() = auth.signOut()
}
