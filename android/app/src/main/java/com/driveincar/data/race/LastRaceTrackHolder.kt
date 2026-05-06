package com.driveincar.data.race

import com.driveincar.domain.model.LatLng
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RaceScreen 에서 수집한 GPS 궤적을 ResultScreen 으로 전달하기 위한 메모리 보관소.
 *
 * Nav 인자로는 List<LatLng> 을 직렬화해서 보내기 어렵고, 화면 간 동일 ViewModel 도
 * 공유 안 되므로 Hilt @Singleton 으로 한 번 보관하고 Result 측에서 읽어간다.
 *
 * 프로세스 사망 시 데이터가 손실되는 건 의도된 트레이드오프(레이스가 결과 화면 보기 전에
 * 사망하는 케이스는 매우 드물고, 영속화하려면 파일/DataStore 가 필요해 비용이 큼).
 */
@Singleton
class LastRaceTrackHolder @Inject constructor() {

    /** 마지막 완주의 누적 위치 시퀀스. 새 레이스 시작 시 자동 초기화하지 않는다 — 다음 레이스의 finish 가 덮어씀. */
    var track: List<LatLng> = emptyList()
        private set

    /** 마지막 레이스의 코스 ID — Result 가 자기 코스를 식별할 때 보조 검증용. */
    var courseId: String? = null
        private set

    fun set(track: List<LatLng>, courseId: String) {
        this.track = track
        this.courseId = courseId
    }

    fun clear() {
        track = emptyList()
        courseId = null
    }
}
