package com.driveincar.core.di

import com.driveincar.core.time.MonotonicClock
import com.driveincar.core.time.SystemMonotonicClock
import com.driveincar.data.auth.AuthRepository
import com.driveincar.data.auth.FirebaseAuthRepository
import com.driveincar.data.course.CourseRepository
import com.driveincar.data.course.FirestoreCourseRepository
import com.driveincar.data.location.FusedLocationProvider
import com.driveincar.data.location.LocationProvider
import com.driveincar.data.ranking.FirestoreRankingRepository
import com.driveincar.data.ranking.RankingRepository
import com.driveincar.data.user.FirestoreUserRepository
import com.driveincar.data.user.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: FirestoreUserRepository): UserRepository

    @Binds @Singleton
    abstract fun bindCourseRepository(impl: FirestoreCourseRepository): CourseRepository

    @Binds @Singleton
    abstract fun bindRankingRepository(impl: FirestoreRankingRepository): RankingRepository

    @Binds @Singleton
    abstract fun bindLocationProvider(impl: FusedLocationProvider): LocationProvider

    @Binds @Singleton
    abstract fun bindMonotonicClock(impl: SystemMonotonicClock): MonotonicClock
}
