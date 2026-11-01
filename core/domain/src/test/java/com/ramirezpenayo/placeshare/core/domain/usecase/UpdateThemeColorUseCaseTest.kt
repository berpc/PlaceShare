package com.ramirezpenayo.placeshare.core.domain.usecase

import com.ramirezpenayo.placeshare.core.domain.model.User
import com.ramirezpenayo.placeshare.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateThemeColorUseCaseTest {

    private val fakeUserRepository = FakeUserRepositoryForTheme()
    private val useCase = UpdateThemeColorUseCase(fakeUserRepository)

    @Test
    fun `invoke updates theme color in repository`() = runTest {
        val result = useCase("DARK")

        assertTrue(result.isSuccess)
        assertEquals("DARK", fakeUserRepository.updatedTheme)
    }
}

private class FakeUserRepositoryForTheme : UserRepository {
    var updatedTheme: String? = null

    override suspend fun getUserProfile(userId: String): Result<User> = Result.failure(Exception())
    override suspend fun createOrUpdateUser(user: User): Result<Unit> = Result.success(Unit)
    override suspend fun getOrCreateUser(
        uid: String,
        name: String?,
        email: String?,
        photoUrl: String?
    ): Result<User> = Result.failure(Exception())
    override suspend fun updateUser(user: User, imageBytes: ByteArray?): Result<Unit> = Result.success(Unit)
    override suspend fun updateThemeColor(themeColor: String): Result<Unit> {
        updatedTheme = themeColor
        return Result.success(Unit)
    }
    override fun getCurrentUserFlow(): Flow<User?> = flowOf(null)
}
