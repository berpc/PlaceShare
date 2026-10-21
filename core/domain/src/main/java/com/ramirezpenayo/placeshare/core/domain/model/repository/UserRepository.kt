package com.ramirezpenayo.placeshare.core.domain.model.repository

import com.ramirezpenayo.placeshare.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserProfile(userId: String): Result<User>
    suspend fun createOrUpdateUser(user: User): Result<Unit>
    suspend fun getOrCreateUser(uid: String, name: String?, email: String?, photoUrl: String?): Result<User>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun updateThemeColor(themeColor: String): Result<Unit>
    fun getCurrentUserFlow(): Flow<User?>
}
