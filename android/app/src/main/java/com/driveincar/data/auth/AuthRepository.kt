package com.driveincar.data.auth

import kotlinx.coroutines.flow.Flow

/** 단순 Auth 추상화. MVP는 Firebase Email/Password만. */
interface AuthRepository {
    /** 현재 로그인 상태를 Flow로 노출. null이면 비로그인. */
    fun observeAuthState(): Flow<String?>

    val currentUid: String?

    suspend fun signInWithEmail(email: String, password: String): Result<String>
    suspend fun signUpWithEmail(email: String, password: String): Result<String>
    fun signOut()
}
