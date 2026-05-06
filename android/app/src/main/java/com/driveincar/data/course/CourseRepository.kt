package com.driveincar.data.course

import com.driveincar.domain.model.Course
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun observeActiveCourses(): Flow<List<Course>>
    suspend fun fetchCourse(courseId: String): Course?
}
