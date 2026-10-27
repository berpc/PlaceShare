package com.ramirezpenayo.placeshare.core.domain.usecase

import com.ramirezpenayo.placeshare.core.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() {
        authRepository.signOut()
    }
}