package com.ramirezpenayo.placeshare.core.domain.usecase

import com.ramirezpenayo.placeshare.core.domain.model.Place
import com.ramirezpenayo.placeshare.core.domain.repository.PlaceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePlacesUseCase @Inject constructor(
    private val placeRepository: PlaceRepository
) {
    operator fun invoke(userId: String): Flow<List<Place>> {
        return placeRepository.getPlacesForUser(userId)
    }
}
