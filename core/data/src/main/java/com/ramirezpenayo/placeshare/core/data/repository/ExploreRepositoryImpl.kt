package com.ramirezpenayo.placeshare.core.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ramirezpenayo.placeshare.core.domain.model.Comment
import com.ramirezpenayo.placeshare.core.domain.model.ExplorePlace
import com.ramirezpenayo.placeshare.core.domain.repository.ExploreRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ExploreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ExploreRepository {

    override fun getPublicPlaces(currentUid: String?): Flow<List<ExplorePlace>> = callbackFlow {
        val listener = firestore.collection("places")
            .whereEqualTo("visibility", "public")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ExploreRepo", "Error al obtener lugares públicos: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val places = snapshot?.documents
                    ?.mapNotNull { doc -> doc.toObject(ExplorePlace::class.java)?.copy(id = doc.id) }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()

                trySend(places)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun toggleLike(placeId: String, userId: String, isLiked: Boolean): Result<Unit> {
        return try {
            val placeRef = firestore.collection("places").document(placeId)
            val placeDoc = placeRef.get().await()
            val ownerId = placeDoc.getString("userId")
            val postTitle = placeDoc.getString("title") ?: "Publicación"
            val postPhotoUrl = placeDoc.getString("photoUrl") ?: ""

            val change = if (isLiked) FieldValue.arrayUnion(userId) else FieldValue.arrayRemove(userId)
            val likeIncrement = if (isLiked) 1L else -1L

            firestore.runTransaction { transaction ->
                transaction.update(placeRef, "likedBy", change)
                if (!ownerId.isNullOrBlank()) {
                    val ownerRef = firestore.collection("users").document(ownerId)
                    transaction.update(ownerRef, "totalLikesReceived", FieldValue.increment(likeIncrement))
                }
            }.await()

            if (isLiked && !ownerId.isNullOrBlank() && ownerId != userId) {
                val senderName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
                    ?: auth.currentUser?.email?.substringBefore("@")
                    ?: "Un usuario"

                val notifData = mapOf(
                    "recipientUserId" to ownerId,
                    "senderId" to userId,
                    "senderName" to senderName,
                    "type" to "LIKE",
                    "postId" to placeId,
                    "postTitle" to postTitle,
                    "postPhotoUrl" to postPhotoUrl,
                    "message" to "Le gustó tu publicación",
                    "createdAt" to System.currentTimeMillis(),
                    "isRead" to false
                )
                firestore.collection("notifications").add(notifData)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getComments(placeId: String): Flow<List<Comment>> = callbackFlow {
        val listener = firestore.collection("places")
            .document(placeId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ExploreRepo", "Error al obtener comentarios: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents
                    ?.mapNotNull { doc -> doc.toObject(Comment::class.java)?.copy(id = doc.id) }
                    ?: emptyList()

                trySend(comments)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun addComment(
        placeId: String,
        userId: String,
        userName: String,
        text: String
    ): Result<Unit> {
        return try {
            val placeRef = firestore.collection("places").document(placeId)
            val placeDoc = placeRef.get().await()
            val ownerId = placeDoc.getString("userId")
            val postTitle = placeDoc.getString("title") ?: "Publicación"
            val postPhotoUrl = placeDoc.getString("photoUrl") ?: ""

            val commentRef = placeRef.collection("comments").document()

            val commentData = mapOf(
                "userId" to userId,
                "userName" to userName,
                "text" to text,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.runTransaction { transaction ->
                transaction.set(commentRef, commentData)
                transaction.update(placeRef, "commentsCount", FieldValue.increment(1))
            }.await()

            if (!ownerId.isNullOrBlank() && ownerId != userId) {
                val notifData = mapOf(
                    "recipientUserId" to ownerId,
                    "senderId" to userId,
                    "senderName" to userName,
                    "type" to "COMMENT",
                    "postId" to placeId,
                    "postTitle" to postTitle,
                    "postPhotoUrl" to postPhotoUrl,
                    "message" to text,
                    "createdAt" to System.currentTimeMillis(),
                    "isRead" to false
                )
                firestore.collection("notifications").add(notifData)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
