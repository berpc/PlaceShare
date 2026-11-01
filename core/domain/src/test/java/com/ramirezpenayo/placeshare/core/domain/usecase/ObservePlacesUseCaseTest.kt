package com.ramirezpenayo.placeshare.core.domain.usecase

import com.ramirezpenayo.placeshare.core.domain.model.Place
import com.ramirezpenayo.placeshare.core.domain.repository.PlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObservePlacesUseCaseTest {

    private val fakePlaceRepository = FakePlaceRepositoryForObserve()
    private val useCase = ObservePlacesUseCase(fakePlaceRepository)

    @Test
    fun `invoke returns flow of places for given user`() = runTest {
        val placeList = listOf(
            Place("p1", "u1", "User 1", "Plaza", "Nota", "", 0.0, 0.0, 100L, "public")
        )
        fakePlaceRepository.placesList = placeList

        val result = useCase("u1").first()

        assertEquals(1, result.size)
        assertEquals("Plaza", result[0].title)
    }
}

private class FakePlaceRepositoryForObserve : PlaceRepository {
    var placesList = listOf<Place>()

    override fun getPlacesForUser(userId: String): Flow<List<Place>> = flowOf(placesList)
    override fun getPlaceById(placeId: String): Flow<Place?> = flowOf(null)
    override suspend fun deletePlace(placeId: String): Result<Unit> = Result.success(Unit)
    override suspend fun updatePlace(
        placeId: String,
        title: String,
        note: String,
        isPublic: Boolean
    ): Result<Unit> = Result.success(Unit)

    override suspend fun savePlace(
        userId: String,
        userName: String,
        title: String,
        note: String,
        isPublic: Boolean,
        lat: Double?,
        lng: Double?,
        imageBytes: ByteArray?
    ): Result<Unit> = Result.success(Unit)
}
