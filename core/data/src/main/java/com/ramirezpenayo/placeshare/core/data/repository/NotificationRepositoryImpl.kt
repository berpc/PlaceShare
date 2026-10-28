package com.ramirezpenayo.placeshare.core.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ramirezpenayo.placeshare.core.domain.model.NotificationItem
import com.ramirezpenayo.placeshare.core.domain.model.NotificationType
import com.ramirezpenayo.placeshare.core.domain.repository.NotificationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<List<NotificationItem>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("notifications")
            .whereEqualTo("recipientUserId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepo", "Error al obtener notificaciones: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    val typeStr = doc.getString("type") ?: "LIKE"
                    val type = try {
                        NotificationType.valueOf(typeStr)
                    } catch (e: Exception) {
                        NotificationType.LIKE
                    }

                    NotificationItem(
                        id = doc.id,
                        recipientUserId = doc.getString("recipientUserId") ?: "",
                        senderId = doc.getString("senderId") ?: "",
                        senderName = doc.getString("senderName") ?: "",
                        type = type,
                        postId = doc.getString("postId") ?: "",
                        postTitle = doc.getString("postTitle") ?: "",
                        postPhotoUrl = doc.getString("postPhotoUrl") ?: "",
                        message = doc.getString("message") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        isRead = doc.getBoolean("isRead") ?: false
                    )
                }?.sortedByDescending { it.createdAt } ?: emptyList()

                trySend(items)
            }

        awaitClose { listener.remove() }
    }

    override fun getUnreadCount(userId: String): Flow<Int> = callbackFlow {
        if (userId.isBlank()) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("notifications")
            .whereEqualTo("recipientUserId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepo", "Error al obtener conteo de no leídas: ${error.message}", error)
                    trySend(0)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("recipientUserId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications")
                .document(notificationId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllNotifications(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("recipientUserId", userId)
                .get()
                .await()

            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createNotification(notification: NotificationItem): Result<Unit> {
        return try {
            val data = mapOf(
                "recipientUserId" to notification.recipientUserId,
                "senderId" to notification.senderId,
                "senderName" to notification.senderName,
                "type" to notification.type.name,
                "postId" to notification.postId,
                "postTitle" to notification.postTitle,
                "postPhotoUrl" to notification.postPhotoUrl,
                "message" to notification.message,
                "createdAt" to if (notification.createdAt == 0L) System.currentTimeMillis() else notification.createdAt,
                "isRead" to false
            )
            firestore.collection("notifications").add(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
