package com.ramirezpenayo.placeshare.core.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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
            val change = if (isLiked) FieldValue.arrayUnion(userId) else FieldValue.arrayRemove(userId)
            firestore.collection("places").document(placeId)
                .update("likedBy", change)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
