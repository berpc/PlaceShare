package com.ramirezpenayo.placeshare.core.domain.repository

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    fun getCurrentUserUid(): String?
    fun getCurrentUserEmail(): String?
    fun getCurrentUserName(): String?
    fun getCurrentUserPhotoUrl(): String?
    fun signOut()
}
