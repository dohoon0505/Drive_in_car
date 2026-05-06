package com.driveincar.ui.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driveincar.data.course.CourseRepository
import com.driveincar.data.race.LastRaceTrackHolder
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val courseRepo: CourseRepository,
    trackHolder: LastRaceTrackHolder,
) : ViewModel() {

    private val courseId: String = savedState["courseId"] ?: ""

    /** RaceViewModel 이 finish 시점에 저장한 마지막 트랙. 코스 ID 가 일치할 때만 표시. */
    val track: List<LatLng> =
        if (trackHolder.courseId == courseId) trackHolder.track else emptyList()

    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    init {
        viewModelScope.launch {
            _course.value = courseRepo.fetchCourse(courseId)
        }
    }
}
