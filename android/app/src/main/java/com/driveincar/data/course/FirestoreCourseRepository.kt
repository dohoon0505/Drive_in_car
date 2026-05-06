package com.driveincar.data.course

import com.driveincar.domain.model.Course
import com.driveincar.domain.model.LatLng
import com.driveincar.domain.model.Waypoint
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCourseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : CourseRepository {

    private fun coursesCol() = firestore.collection("courses")

    override fun observeActiveCourses(): Flow<List<Course>> = callbackFlow {
        val reg = coursesCol()
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toCourse() }.orEmpty()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun fetchCourse(courseId: String): Course? = runCatching {
        coursesCol().document(courseId).get().await().toCourse()
    }.getOrNull()

    private fun DocumentSnapshot?.toCourse(): Course? {
        val s = this ?: return null
        if (!s.exists()) return null
        val start = s.getGeoPoint("startCoord") ?: return null
        val end = s.getGeoPoint("endCoord") ?: return null
        @Suppress("UNCHECKED_CAST")
        val waypointsRaw = s.get("waypoints") as? List<Map<String, Any>> ?: emptyList()
        val waypoints = waypointsRaw.mapNotNull { m ->
            val lat = (m["lat"] as? Number)?.toDouble() ?: return@mapNotNull null
            val lng = (m["lng"] as? Number)?.toDouble() ?: return@mapNotNull null
            val order = (m["order"] as? Number)?.toInt() ?: 0
            Waypoint(lat, lng, order)
        }.sortedBy { it.order }

        return Course(
            courseId = s.id,
            name = s.getString("name") ?: return null,
            description = s.getString("description") ?: "",
            regionName = s.getString("regionName") ?: "",
            startCoord = start.toLatLng(),
            endCoord = end.toLatLng(),
            waypoints = waypoints,
            distanceMeters = s.getDouble("distanceMeters") ?: 0.0,
            difficulty = s.getLong("difficulty")?.toInt() ?: 0,
            isActive = s.getBoolean("isActive") ?: false,
        )
    }

    private fun GeoPoint.toLatLng() = LatLng(latitude, longitude)
}
