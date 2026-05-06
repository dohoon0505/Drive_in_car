package com.driveincar.domain.model

data class LatLng(val lat: Double, val lng: Double)

data class Waypoint(val lat: Double, val lng: Double, val order: Int)

data class Course(
    val courseId: String,
    val name: String,
    val description: String,
    val regionName: String,
    val startCoord: LatLng,
    val endCoord: LatLng,
    val waypoints: List<Waypoint>,
    val distanceMeters: Double,
    val difficulty: Int,
    val isActive: Boolean,
)

/**
 * 코스의 좌표 시퀀스: 시작점 → order 정렬된 웨이포인트들 → 도착점.
 * UI 가 RaceStateMachine 내부 상태에 의존하지 않고 코스 라인을 그릴 때 사용한다.
 */
fun Course.toLatLngList(): List<LatLng> =
    listOf(startCoord) +
        waypoints.sortedBy { it.order }.map { LatLng(it.lat, it.lng) } +
        endCoord
