package com.driveincar.data.user

import com.driveincar.domain.model.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserRepository {

    private fun usersCol() = firestore.collection("users")

    override fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val reg = usersCol().document(uid).addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(null)
                return@addSnapshotListener
            }
            trySend(snap?.toUser())
        }
        awaitClose { reg.remove() }
    }

    override suspend fun fetchUser(uid: String): User? = runCatching {
        usersCol().document(uid).get().await().toUser()
    }.getOrNull()

    override suspend fun createUser(user: User): Result<Unit> = runCatching {
        usersCol().document(user.uid).set(
            mapOf(
                "nickname" to user.nickname,
                "carBrand" to user.carBrand,
                "carModel" to user.carModel,
                "profileImageId" to user.profileImageId,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    override suspend fun updateUser(user: User): Result<Unit> = runCatching {
        usersCol().document(user.uid).update(
            mapOf(
                "nickname" to user.nickname,
                "carBrand" to user.carBrand,
                "carModel" to user.carModel,
                "profileImageId" to user.profileImageId,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    private fun DocumentSnapshot?.toUser(): User? {
        val s = this ?: return null
        if (!s.exists()) return null
        return User(
            uid = s.id,
            nickname = s.getString("nickname") ?: return null,
            carBrand = s.getString("carBrand") ?: return null,
            carModel = s.getString("carModel") ?: return null,
            profileImageId = s.getString("profileImageId") ?: return null,
        )
    }
}
