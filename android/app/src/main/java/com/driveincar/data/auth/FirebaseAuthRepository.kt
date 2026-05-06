package com.driveincar.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
) : AuthRepository {

    override fun observeAuthState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            trySend(fa.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override val currentUid: String?
        get() = auth.currentUser?.uid

    override suspend fun signInWithEmail(email: String, password: String): Result<String> =
        runCatching {
            val r = auth.signInWithEmailAndPassword(email, password).await()
            requireNotNull(r.user).uid
        }

    override suspend fun signUpWithEmail(email: String, password: String): Result<String> =
        runCatching {
            val r = auth.createUserWithEmailAndPassword(email, password).await()
            requireNotNull(r.user).uid
        }

    override fun signOut() {
        auth.signOut()
    }
}
