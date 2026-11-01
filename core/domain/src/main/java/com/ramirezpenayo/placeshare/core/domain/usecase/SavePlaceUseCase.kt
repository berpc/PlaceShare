package com.ramirezpenayo.placeshare.core.domain.usecase

import com.ramirezpenayo.placeshare.core.domain.repository.PlaceRepository
import javax.inject.Inject

class SavePlaceUseCase @Inject constructor(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(
        userId: String,
        userName: String,
        title: String,
        note: String,
        isPublic: Boolean,
        lat: Double? = null,
        lng: Double? = null,
        imageBytes: ByteArray? = null
    ): Result<Unit> {
        return placeRepository.savePlace(
            userId = userId,
            userName = userName,
            title = title,
            note = note,
            isPublic = isPublic,
            lat = lat,
            lng = lng,
            imageBytes = imageBytes
        )
    }
}
