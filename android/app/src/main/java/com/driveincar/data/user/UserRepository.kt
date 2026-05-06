package com.driveincar.data.user

import com.driveincar.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUser(uid: String): Flow<User?>
    suspend fun fetchUser(uid: String): User?
    suspend fun createUser(user: User): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>
}
