package com.ramirezpenayo.placeshare.core.domain.usecase

import com.ramirezpenayo.placeshare.core.domain.repository.UserRepository
import javax.inject.Inject

class UpdateThemeColorUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(themeColor: String): Result<Unit> {
        return userRepository.updateThemeColor(themeColor)
    }
}
