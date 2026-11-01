package com.ramirezpenayo.placeshare.feature.profile.presentation.edit_profile

import android.content.Context
import com.ramirezpenayo.placeshare.core.domain.model.User
import com.ramirezpenayo.placeshare.core.domain.repository.AuthRepository
import com.ramirezpenayo.placeshare.core.domain.repository.UserRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    private val fakeUserRepository = FakeUserRepository()
    private val fakeAuthRepository = FakeAuthRepository()
    private val mockContext: Context = mock(Context::class.java)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeUserRepository.currentUser = User(
            uid = "user_1",
            name = "Maria Perez",
            username = "mariap",
            bio = "Viajera apasionada",
            email = "maria@example.com",
            photoUrl = "",
            themeColor = "SYSTEM",
            totalPlacesCount = 5,
            totalLikesReceived = 10,
            totalFollowers = 2,
            totalFollowing = 3
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = EditProfileViewModel(fakeUserRepository, fakeAuthRepository, mockContext)

    @Test
    fun `inicialmente carga los datos del perfil y no hay cambios`() = runTest {
        val viewModel = createViewModel()

        assertEquals("Maria Perez", viewModel.name)
        assertEquals("mariap", viewModel.username)
        assertEquals("Viajera apasionada", viewModel.bio)
        assertFalse(viewModel.hasChanges)
    }

    @Test
    fun `al modificar el nombre la propiedad hasChanges pasa a true`() = runTest {
        val viewModel = createViewModel()

        viewModel.onNameChanged("Maria Gomez")

        assertTrue(viewModel.hasChanges)
        assertEquals("Maria Gomez", viewModel.name)
    }

    @Test
    fun `guardar perfil invoca el repositorio`() = runTest {
        val viewModel = createViewModel()
        viewModel.onBioChanged("Nueva biografía")

        var successCalled = false
        viewModel.saveProfile { successCalled = true }

        assertTrue(successCalled)
        assertEquals("Nueva biografía", fakeUserRepository.currentUser?.bio)
    }
}

private class FakeUserRepository : UserRepository {
    var currentUser: User? = null

    override suspend fun getUserProfile(userId: String): Result<User> {
        return currentUser?.let { Result.success(it) }
            ?: Result.failure(Exception("User not found"))
    }

    override suspend fun createOrUpdateUser(user: User): Result<Unit> {
        currentUser = user
        return Result.success(Unit)
    }

    override suspend fun getOrCreateUser(
        uid: String,
        name: String?,
        email: String?,
        photoUrl: String?
    ): Result<User> {
        return currentUser?.let { Result.success(it) }
            ?: Result.failure(Exception("Not found"))
    }

    override suspend fun updateUser(user: User, imageBytes: ByteArray?): Result<Unit> {
        currentUser = user
        return Result.success(Unit)
    }

    override suspend fun updateThemeColor(themeColor: String): Result<Unit> {
        return Result.success(Unit)
    }

    override fun getCurrentUserFlow(): Flow<User?> {
        return flowOf(currentUser)
    }
}

private class FakeAuthRepository : AuthRepository {
    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = Result.success(Unit)
    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = Result.success(Unit)
    override fun getCurrentUserUid(): String? = "user_1"
    override fun getCurrentUserEmail(): String? = "maria@example.com"
    override fun getCurrentUserName(): String? = "Maria Perez"
    override fun getCurrentUserPhotoUrl(): String? = null
    override fun signOut() {}
}
