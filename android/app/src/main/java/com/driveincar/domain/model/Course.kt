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
