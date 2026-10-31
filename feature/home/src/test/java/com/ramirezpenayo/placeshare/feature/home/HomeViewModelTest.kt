package com.ramirezpenayo.placeshare.feature.home

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ramirezpenayo.placeshare.core.domain.model.Place
import com.ramirezpenayo.placeshare.core.domain.repository.PlaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val fakeRepository = FakePlaceRepository()
    private val mockAuth: FirebaseAuth = mock(FirebaseAuth::class.java)
    private val mockUser: FirebaseUser = mock(FirebaseUser::class.java)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("user_123")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = HomeViewModel(fakeRepository, mockAuth)

    @Test
    fun `al iniciar el viewModel se cargan los lugares del usuario`() = runTest {
        fakeRepository.placesList = listOf(
            fakePlace("1", "Plaza Central"),
            fakePlace("2", "Parque MNS")
        )

        val viewModel = createViewModel()

        assertEquals(2, viewModel.places.value.size)
        assertEquals("Plaza Central", viewModel.places.value[0].title)
        assertEquals("Parque MNS", viewModel.places.value[1].title)
        assertFalse(viewModel.loading.value)
    }

    @Test
    fun `eliminar lugar invoca el repositorio y elimina el lugar`() = runTest {
        fakeRepository.placesList = listOf(
            fakePlace("place_10", "Lugar a borrar")
        )

        val viewModel = createViewModel()

        viewModel.deletePlace("place_10")

        assertTrue(fakeRepository.deletedPlaceIds.contains("place_10"))
    }
}

private class FakePlaceRepository : PlaceRepository {
    var placesList = listOf<Place>()
    val deletedPlaceIds = mutableListOf<String>()

    override fun getPlacesForUser(userId: String): Flow<List<Place>> {
        return flowOf(placesList)
    }

    override fun getPlaceById(placeId: String): Flow<Place?> {
        return flowOf(placesList.find { it.id == placeId })
    }

    override suspend fun updatePlace(
        placeId: String,
        title: String,
        note: String,
        isPublic: Boolean
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun deletePlace(placeId: String): Result<Unit> {
        deletedPlaceIds.add(placeId)
        placesList = placesList.filter { it.id != placeId }
        return Result.success(Unit)
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
        return Result.success(Unit)
    }
}

private fun fakePlace(id: String, title: String) = Place(
    id = id,
    userId = "user_123",
    userName = "Usuario Test",
    title = title,
    note = "Nota de prueba",
    photoUrl = "",
    lat = 0.0,
    lng = 0.0,
    createdAt = System.currentTimeMillis(),
    visibility = "public"
)
