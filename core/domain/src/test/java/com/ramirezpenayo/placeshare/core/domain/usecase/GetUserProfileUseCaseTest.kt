package com.ramirezpenayo.placeshare.core.domain.usecase

import com.ramirezpenayo.placeshare.core.domain.model.User
import com.ramirezpenayo.placeshare.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetUserProfileUseCaseTest {

    private val fakeUserRepository = FakeUserRepositoryForUseCase()
    private val useCase = GetUserProfileUseCase(fakeUserRepository)

    @Test
    fun `invoke returns success when user is found`() = runTest {
        val user = User(
            uid = "u1",
            name = "Juan",
            username = "juanp",
            bio = "Bio",
            email = "juan@test.com",
            photoUrl = "",
            themeColor = "LIGHT",
            totalPlacesCount = 1,
            totalLikesReceived = 2,
            totalFollowers = 3,
            totalFollowing = 4
        )
        fakeUserRepository.userToReturn = user

        val result = useCase("u1")

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when user is not found`() = runTest {
        fakeUserRepository.userToReturn = null

        val result = useCase("u99")

        assertTrue(result.isFailure)
    }
}

private class FakeUserRepositoryForUseCase : UserRepository {
    var userToReturn: User? = null

    override suspend fun getUserProfile(userId: String): Result<User> {
        return userToReturn?.let { Result.success(it) }
            ?: Result.failure(Exception("Not found"))
    }

    override suspend fun createOrUpdateUser(user: User): Result<Unit> = Result.success(Unit)
    override suspend fun getOrCreateUser(
        uid: String,
        name: String?,
        email: String?,
        photoUrl: String?
    ): Result<User> = getUserProfile(uid)
    override suspend fun updateUser(user: User, imageBytes: ByteArray?): Result<Unit> = Result.success(Unit)
    override suspend fun updateThemeColor(themeColor: String): Result<Unit> = Result.success(Unit)
    override fun getCurrentUserFlow(): Flow<User?> = flowOf(userToReturn)
}
