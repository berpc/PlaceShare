package com.ramirezpenayo.placeshare.feature.profile.presentation.profile

import com.ramirezpenayo.placeshare.core.domain.model.User
import com.ramirezpenayo.placeshare.core.domain.repository.AuthRepository
import com.ramirezpenayo.placeshare.core.domain.repository.UserRepository
import com.ramirezpenayo.placeshare.feature.profile.ProfileViewModel
import com.ramirezpenayo.placeshare.feature.profile.presentation.components.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val fakeUserRepository = FakeUserRepository()
    private val fakeAuthRepository = FakeAuthRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ProfileViewModel(fakeUserRepository, fakeAuthRepository)

    @Test
    fun `loadUserProfile emits Success when active user exists`() = runTest {
        val testUser = User(
            uid = "user_123",
            name = "Carlos Ramirez",
            username = "carlosr",
            bio = "Desarrollador",
            email = "carlos@example.com",
            photoUrl = "",
            themeColor = "DARK",
            totalPlacesCount = 3,
            totalLikesReceived = 5,
            totalFollowers = 10,
            totalFollowing = 4
        )
        fakeAuthRepository.currentUid = "user_123"
        fakeAuthRepository.currentName = "Carlos Ramirez"
        fakeAuthRepository.currentEmail = "carlos@example.com"
        fakeUserRepository.userFlow.value = testUser

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Success)
        assertEquals(testUser, (state as ProfileUiState.Success).user)
    }

    @Test
    fun `loadUserProfile emits Error when no active session`() = runTest {
        fakeAuthRepository.currentUid = null

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Error)
    }

    @Test
    fun `updateTheme updates user repository theme color`() = runTest {
        fakeAuthRepository.currentUid = "user_123"
        val viewModel = createViewModel()

        viewModel.updateTheme(AppThemeMode.DARK)

        assertEquals("DARK", fakeUserRepository.updatedTheme)
    }

    @Test
    fun `signOut calls authRepository signOut and triggers callback`() = runTest {
        val viewModel = createViewModel()
        var callbackCalled = false

        viewModel.signOut {
            callbackCalled = true
        }

        assertTrue(fakeAuthRepository.signOutCalled)
        assertTrue(callbackCalled)
    }
}

private class FakeUserRepository : UserRepository {
    val userFlow = MutableStateFlow<User?>(null)
    var updatedTheme: String? = null

    override suspend fun getUserProfile(userId: String): Result<User> {
        return userFlow.value?.let { Result.success(it) }
            ?: Result.failure(Exception("Not found"))
    }

    override suspend fun createOrUpdateUser(user: User): Result<Unit> {
        userFlow.value = user
        return Result.success(Unit)
    }

    override suspend fun getOrCreateUser(
        uid: String,
        name: String?,
        email: String?,
        photoUrl: String?
    ): Result<User> {
        val user = userFlow.value ?: User(
            uid = uid,
            name = name ?: "",
            username = "",
            bio = "",
            email = email ?: "",
            photoUrl = photoUrl ?: "",
            themeColor = "SYSTEM",
            totalPlacesCount = 0,
            totalLikesReceived = 0,
            totalFollowers = 0,
            totalFollowing = 0
        )
        userFlow.value = user
        return Result.success(user)
    }

    override suspend fun updateUser(user: User, imageBytes: ByteArray?): Result<Unit> {
        userFlow.value = user
        return Result.success(Unit)
    }

    override suspend fun updateThemeColor(themeColor: String): Result<Unit> {
        updatedTheme = themeColor
        return Result.success(Unit)
    }

    override fun getCurrentUserFlow(): Flow<User?> = userFlow
}

private class FakeAuthRepository : AuthRepository {
    var currentUid: String? = "user_123"
    var currentName: String? = "Test User"
    var currentEmail: String? = "test@example.com"
    var currentPhotoUrl: String? = null
    var signOutCalled = false

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = Result.success(Unit)
    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = Result.success(Unit)
    override fun getCurrentUserUid(): String? = currentUid
    override fun getCurrentUserEmail(): String? = currentEmail
    override fun getCurrentUserName(): String? = currentName
    override fun getCurrentUserPhotoUrl(): String? = currentPhotoUrl
    override fun signOut() {
        signOutCalled = true
    }
}
