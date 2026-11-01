package com.ramirezpenayo.placeshare.feature.places

import android.content.Context
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class AddPlaceViewModelTest {

    private val fakeRepository = FakePlaceRepository()
    private val mockAuth: FirebaseAuth = mock(FirebaseAuth::class.java)
    private val mockUser: FirebaseUser = mock(FirebaseUser::class.java)
    private val mockContext: Context = mock(Context::class.java)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("user_test")
        `when`(mockUser.displayName).thenReturn("Usuario Pruebas")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AddPlaceViewModel(fakeRepository, mockAuth)

    @Test
    fun `guardar lugar con titulo vacio establece error y no invoca repositorio`() = runTest {
        val viewModel = createViewModel()

        var onSavedCalled = false
        viewModel.savePlace(mockContext, "", "Nota de prueba", true) {
            onSavedCalled = true
        }

        assertNotNull(viewModel.error.value)
        assertEquals("El título no puede estar vacío.", viewModel.error.value)
        assertFalse(onSavedCalled)
        assertEquals(0, fakeRepository.savedPlaces.size)
    }

    @Test
    fun `guardar lugar valido invoca repositorio y callback`(): Unit = runTest {
        val viewModel = createViewModel()

        var onSavedCalled = false
        viewModel.savePlace(mockContext, "Plaza Mayor", "Hermoso lugar", true) {
            onSavedCalled = true
        }

        assertTrue(onSavedCalled)
        assertEquals(1, fakeRepository.savedPlaces.size)
        assertEquals("Plaza Mayor", fakeRepository.savedPlaces[0].title)
    }

    @Test
    fun `setLocationDenied actualiza el texto de ubicacion`() = runTest {
        val viewModel = createViewModel()

        viewModel.setLocationDenied()

        assertEquals("Permiso de ubicación denegado", viewModel.locationText.value)
    }
}

private class FakePlaceRepository : PlaceRepository {
    val savedPlaces = mutableListOf<Place>()

    override fun getPlacesForUser(userId: String): Flow<List<Place>> = flowOf(emptyList())

    override fun getPlaceById(placeId: String): Flow<Place?> = flowOf(null)

    override suspend fun updatePlace(
        placeId: String,
        title: String,
        note: String,
        isPublic: Boolean
    ): Result<Unit> = Result.success(Unit)

    override suspend fun deletePlace(placeId: String): Result<Unit> = Result.success(Unit)

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
            Place(
                id = "generated_${savedPlaces.size}",
                userId = userId,
                userName = userName,
                title = title,
                note = note,
                photoUrl = "",
                lat = lat,
                lng = lng,
                createdAt = System.currentTimeMillis(),
                visibility = if (isPublic) "public" else "private"
            )
        )
        return Result.success(Unit)
    }
}
