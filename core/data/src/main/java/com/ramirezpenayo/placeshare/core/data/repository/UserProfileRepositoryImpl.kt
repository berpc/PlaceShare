package com.ramirezpenayo.placeshare.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.ramirezpenayo.placeshare.core.data.mapper.toFirestoreMap
import com.ramirezpenayo.placeshare.core.data.mapper.toUser
import com.ramirezpenayo.placeshare.core.domain.model.User
import com.ramirezpenayo.placeshare.core.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    override suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            if (document.exists()) {
                val userData = document.data ?: emptyMap()
                Result.success(userData.toUser(uid))
            } else {
                Result.failure(Exception("El usuario no existe"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createOrUpdateUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.uid)
                .set(user.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrCreateUser(
        uid: String,
        name: String?,
        email: String?,
        photoUrl: String?
    ): Result<User> {
        return try {
            val document = firestore.collection("users").document(uid).get().await()

            if (document.exists()) {
                val userData = document.data ?: emptyMap()
                Result.success(userData.toUser(uid))
            } else {
                val newUser = User(
                    uid = uid,
                    name = name,
                    username = email?.substringBefore("@") ?: "user_${uid.take(5)}",
                    bio = "¡Hola! Estoy usando PlaceShare.",
                    email = email,
                    photoUrl = photoUrl,
                    themeColor = "SYSTEM",
                    totalPlacesCount = 0,
                    totalLikesReceived = 0,
                    totalFollowers = 0,
                    totalFollowing = 0
                )

                firestore.collection("users").document(uid).set(newUser.toFirestoreMap()).await()
                Result.success(newUser)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.uid)
                .set(user.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateThemeColor(themeColor: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No user"))
            firestore.collection("users").document(uid)
                .set(mapOf("themeColor" to themeColor), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        var userListener: ListenerRegistration? = null
        var placesListener: ListenerRegistration? = null

        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            userListener?.remove()
            placesListener?.remove()

            if (uid == null) {
                trySend(null)
            } else {
                var currentUser: User? = null
                var calculatedPlacesCount = 0
                var calculatedLikesCount = 0

                fun emitUser() {
                    currentUser?.let { user ->
                        trySend(
                            user.copy(
                                totalPlacesCount = calculatedPlacesCount,
                                totalLikesReceived = calculatedLikesCount
                            )
                        )
                    }
                }

                userListener = firestore.collection("users").document(uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            val userData = snapshot.data ?: emptyMap()
                            currentUser = userData.toUser(uid)
                            emitUser()
                        } else {
                            trySend(null)
                        }
                    }

                placesListener = firestore.collection("places")
                    .whereEqualTo("userId", uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error == null && snapshot != null) {
                            val docs = snapshot.documents
                            calculatedPlacesCount = docs.size
                            calculatedLikesCount = docs.sumOf { doc ->
                                val likedBy = doc.get("likedBy") as? List<*>
                                likedBy?.size ?: 0
                            }
                            emitUser()
                        }
                    }
            }
        }

        auth.addAuthStateListener(authStateListener)

        awaitClose {
            userListener?.remove()
            placesListener?.remove()
            auth.removeAuthStateListener(authStateListener)
        }
    }
}
