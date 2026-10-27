package com.ramirezpenayo.placeshare.core.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ramirezpenayo.placeshare.core.data.mapper.toFirestoreMap
import com.ramirezpenayo.placeshare.core.data.mapper.toPlace
import com.ramirezpenayo.placeshare.core.domain.model.Place
import com.ramirezpenayo.placeshare.core.domain.repository.PlaceRepository
import com.ramirezpenayo.placeshare.core.network.CloudinaryUploader
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PlaceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val uploader: CloudinaryUploader
) : PlaceRepository {

    override fun getPlacesForUser(userId: String): Flow<List<Place>> = callbackFlow {
        Log.d("PlaceRepo", "Iniciando escucha para userId: $userId")

        val listener = firestore.collection("places")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PlaceRepo", "Error en snapshot listener: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val docs = snapshot?.documents ?: emptyList()
                Log.d("PlaceRepo", "Documentos recibidos de Firestore: ${docs.size}")

                val places = docs.map { it.toPlace() }.sortedByDescending { it.createdAt }
                trySend(places)
            }

        awaitClose {
            Log.d("PlaceRepo", "Cerrando listener de places")
            listener.remove()
        }
    }

    override suspend fun deletePlace(placeId: String): Result<Unit> {
        return try {
            val placeDoc = firestore.collection("places").document(placeId).get().await()
            val userId = placeDoc.getString("userId")

            firestore.collection("places").document(placeId).delete().await()

            if (!userId.isNullOrBlank()) {
                firestore.collection("users").document(userId)
                    .update("totalPlacesCount", FieldValue.increment(-1))
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePlace(
        userId: String,
        userName: String,
        title: String,
        note: String,
        isPublic: Boolean,
        lat: Double?,
        lng: Double?,
        imageBytes: ByteArray?
    ): Result<Unit> {
        return try {
            val photoUrl = imageBytes?.let { uploader.subirImagen(it) } ?: ""

            val place = Place(
                userId = userId,
                userName = userName,
                title = title.trim(),
                note = note.trim(),
                photoUrl = photoUrl,
                lat = lat,
                lng = lng,
                createdAt = System.currentTimeMillis(),
                visibility = if (isPublic) "public" else "private"
            )

            val placeRef = firestore.collection("places").document()
            val userRef = firestore.collection("users").document(userId)

            firestore.runTransaction { transaction ->
                transaction.set(placeRef, place.toFirestoreMap())
                transaction.update(userRef, "totalPlacesCount", FieldValue.increment(1))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
