package com.ramirezpenayo.placeshare.core.domain.usecase

import com.ramirezpenayo.placeshare.core.domain.model.Place
import com.ramirezpenayo.placeshare.core.domain.repository.PlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SavePlaceUseCaseTest {

    private val fakePlaceRepository = FakePlaceRepositoryForSave()
    private val useCase = SavePlaceUseCase(fakePlaceRepository)

    @Test
    fun `invoke saves place to repository`() = runTest {
        val result = useCase(
            userId = "u1",
            userName = "Carlos",
            title = "Parque",
            note = "Una nota",
            isPublic = true
        )

        assertTrue(result.isSuccess)
        assertEquals(1, fakePlaceRepository.savedPlaces.size)
        assertEquals("Parque", fakePlaceRepository.savedPlaces[0].title)
    }
}

private class FakePlaceRepositoryForSave : PlaceRepository {
    val savedPlaces = mutableListOf<Place>()

    override fun getPlacesForUser(userId: String): Flow<List<Place>> = flowOf(emptyList())
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
    ): Result<Unit> {
        savedPlaces.add(
            Place("p1", userId, userName, title, note, "", lat, lng, System.currentTimeMillis(), if (isPublic) "public" else "private")
        )
        return Result.success(Unit)
    }
}
