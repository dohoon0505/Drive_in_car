package com.driveincar.ui.race

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driveincar.data.auth.AuthRepository
import com.driveincar.data.course.CourseRepository
import com.driveincar.data.location.LocationProvider
import com.driveincar.data.race.LastRaceTrackHolder
import com.driveincar.data.ranking.RankingRepository
import com.driveincar.data.user.UserRepository
import com.driveincar.domain.model.Course
import com.driveincar.domain.model.LatLng
import com.driveincar.domain.race.CancelReason
import com.driveincar.domain.race.RaceConfig
import com.driveincar.domain.race.RaceState
import com.driveincar.domain.race.RaceStateMachine
import com.driveincar.domain.race.SubmitTimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RaceEvent {
    data class Finished(
        val timeMs: Long,
        val averageKmh: Double,
        val flagged: Boolean,
        val personalBest: Boolean,
    ) : RaceEvent
    data class CancelledEvent(val reason: CancelReason) : RaceEvent
}

@HiltViewModel
class RaceViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val courseRepo: CourseRepository,
    private val location: LocationProvider,
    private val auth: AuthRepository,
    private val users: UserRepository,
    private val rankings: RankingRepository,
    private val submit: SubmitTimeUseCase,
    private val trackHolder: LastRaceTrackHolder,
) : ViewModel() {

    private val courseId: String = savedState["courseId"] ?: ""

    private val _state = MutableStateFlow<RaceState>(RaceState.Idle)
    val state: StateFlow<RaceState> = _state.asStateFlow()

    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    /**
     * InRace 진입 이후 누적된 GPS 좌표. 미니맵의 라이브 폴리라인 + Result 의
     * 완주 궤적 표시에 쓰인다. Arming/Armed 단계의 샘플은 노이즈로 간주해 제외.
     */
    private val _track = MutableStateFlow<List<LatLng>>(emptyList())
    val track: StateFlow<List<LatLng>> = _track.asStateFlow()

    private val _events = MutableSharedFlow<RaceEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<RaceEvent> = _events.asSharedFlow()

    private var machine: RaceStateMachine? = null
    private var trackingJob: Job? = null

    init {
        viewModelScope.launch {
            val c = courseRepo.fetchCourse(courseId) ?: return@launch
            _course.value = c
            machine = RaceStateMachine(c, RaceConfig())
            startTracking()
        }
    }

    private fun startTracking() {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            location.samples().collect { sample ->
                val m = machine ?: return@collect
                val newState = m.onSample(sample)
                _state.value = newState

                // InRace 동안 또는 InRace → Finished 직전 샘플까지 누적.
                // (Finished 샘플은 도착선 통과 직후라 trace 마지막 점으로 의미 있음.)
                if (newState is RaceState.InRace || newState is RaceState.Finished) {
                    _track.update { it + sample.coord }
                }

                when (newState) {
                    is RaceState.Finished -> handleFinish(newState)
                    is RaceState.Cancelled -> handleCancel(newState.reason)
                    else -> Unit
                }
            }
        }
    }

    fun userCancel() {
        machine?.cancel(CancelReason.USER_CANCELLED)
        viewModelScope.launch {
            _events.emit(RaceEvent.CancelledEvent(CancelReason.USER_CANCELLED))
        }
    }

    private fun handleFinish(s: RaceState.Finished) {
        trackingJob?.cancel()
        viewModelScope.launch {
            val uid = auth.currentUid ?: return@launch
            val pb = isPersonalBest(uid, s.timeMs)
            // Result 화면이 읽어갈 수 있도록 트랙 보관 — submit 보다 먼저 (네트워크 실패해도 화면엔 표시되게)
            trackHolder.set(track = _track.value, courseId = courseId)
            submit.invoke(
                uid = uid,
                courseId = courseId,
                timeMs = s.timeMs,
                averageKmh = s.averageKmh,
                flagged = s.flagged,
            )
            _events.emit(
                RaceEvent.Finished(
                    timeMs = s.timeMs,
                    averageKmh = s.averageKmh,
                    flagged = s.flagged,
                    personalBest = pb,
                )
            )
        }
    }

    private fun handleCancel(reason: CancelReason) {
        trackingJob?.cancel()
        viewModelScope.launch {
            _events.emit(RaceEvent.CancelledEvent(reason))
        }
    }

    private suspend fun isPersonalBest(uid: String, timeMs: Long): Boolean {
        // MVP: 기존 베스트와 비교는 추후. 일단 false로 두고 결과 화면에서 단순 표시.
        return false
    }
}
