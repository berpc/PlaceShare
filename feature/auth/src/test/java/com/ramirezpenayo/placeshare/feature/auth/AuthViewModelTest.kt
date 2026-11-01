package com.ramirezpenayo.placeshare.feature.auth

import android.content.Context
import com.ramirezpenayo.placeshare.core.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val fakeAuthRepository = FakeAuthRepository()
    private val mockContext: Context = mock(Context::class.java)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AuthViewModel(fakeAuthRepository, mockContext)

    @Test
    fun `estado inicial del viewModel es AuthUiState Initial`() = runTest {
        val viewModel = createViewModel()

        assertEquals(AuthUiState.Initial, viewModel.uiState.value)
    }
}

private class FakeAuthRepository : AuthRepository {
    var signInResult: Result<Unit> = Result.success(Unit)

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = signInResult
    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> = signInResult
    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = signInResult
    override fun getCurrentUserUid(): String? = "user_auth"
    override fun getCurrentUserEmail(): String? = "auth@test.com"
    override fun getCurrentUserName(): String? = "Auth User"
    override fun getCurrentUserPhotoUrl(): String? = null
    override fun signOut() {}
}
