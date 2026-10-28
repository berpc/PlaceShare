package com.ramirezpenayo.placeshare.core.domain.repository

import com.ramirezpenayo.placeshare.core.domain.model.Place
import kotlinx.coroutines.flow.Flow

interface PlaceRepository {
    fun getPlacesForUser(userId: String): Flow<List<Place>>
    fun getPlaceById(placeId: String): Flow<Place?>
    suspend fun deletePlace(placeId: String): Result<Unit>
    suspend fun updatePlace(
        placeId: String,
        title: String,
        note: String,
        isPublic: Boolean
    ): Result<Unit>
    suspend fun savePlace(
        userId: String,
        userName: String,
        title: String,
        note: String,
        isPublic: Boolean,
        lat: Double?,
        lng: Double?,
        imageBytes: ByteArray?
    ): Result<Unit>
}
