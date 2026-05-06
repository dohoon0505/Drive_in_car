package com.driveincar.ui.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driveincar.data.course.CourseRepository
import com.driveincar.data.ranking.RankingRepository
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.Ranking
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val courseRepo: CourseRepository,
    rankingRepo: RankingRepository,
) : ViewModel() {

    private val courseId: String = savedState["courseId"] ?: ""

    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    val top3: StateFlow<List<Ranking>> = rankingRepo.observeCourseTop3(courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            _course.value = courseRepo.fetchCourse(courseId)
        }
    }
}
