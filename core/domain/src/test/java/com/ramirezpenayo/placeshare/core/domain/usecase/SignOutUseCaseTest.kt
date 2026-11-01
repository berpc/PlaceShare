package com.ramirezpenayo.placeshare.core.domain.usecase

import com.ramirezpenayo.placeshare.core.domain.repository.AuthRepository
import org.junit.Assert.assertTrue
import org.junit.Test

class SignOutUseCaseTest {

    private val fakeAuthRepository = FakeAuthRepositoryForSignOut()
    private val useCase = SignOutUseCase(fakeAuthRepository)

    @Test
    fun `invoke calls authRepository signOut`() {
        useCase()

        assertTrue(fakeAuthRepository.signOutCalled)
    }
}

private class FakeAuthRepositoryForSignOut : AuthRepository {
    var signOutCalled = false

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = Result.success(Unit)
    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = Result.success(Unit)
    override fun getCurrentUserUid(): String? = "u1"
    override fun getCurrentUserEmail(): String? = "u1@test.com"
    override fun getCurrentUserName(): String? = "User"
    override fun getCurrentUserPhotoUrl(): String? = null
    override fun signOut() {
        signOutCalled = true
    }
}
