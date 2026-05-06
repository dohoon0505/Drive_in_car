package com.driveincar.ui.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driveincar.core.geo.Geo
import com.driveincar.data.course.CourseRepository
import com.driveincar.data.location.LocationProvider
import com.driveincar.data.ranking.RankingRepository
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.LatLng
import com.driveincar.domain.model.Ranking
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
class CourseDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val courseRepo: CourseRepository,
    rankingRepo: RankingRepository,
    private val location: LocationProvider,
) : ViewModel() {

    private val courseId: String = savedState["courseId"] ?: ""

    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    val top3: StateFlow<List<Ranking>> = rankingRepo.observeCourseTop3(courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 1회성 GPS fix. null = 권한 미허용 또는 아직 못 잡음 → 5km 게이트는 보수적으로 닫힘. */
    private val _myLocation = MutableStateFlow<LatLng?>(null)

    /**
     * 코스 출발점 - 내 위치 거리(미터). null = 둘 중 하나가 아직 없음 → "거리 측정 중".
     */
    val distanceFromStartM: StateFlow<Double?> = combine(_course, _myLocation) { c, here ->
        if (c == null || here == null) null
        else Geo.distanceMeters(here, c.startCoord)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            _course.value = courseRepo.fetchCourse(courseId)
        }
        viewModelScope.launch {
            runCatching {
                location.samples().take(1).collect { sample ->
                    _myLocation.value = sample.coord
                }
            }.onFailure { Timber.w(it, "myLocation first-fix failed in CourseDetail") }
        }
    }
}
