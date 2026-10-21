package com.ramirezpenayo.placeshare.feature.profile.presentation.profile

import com.ramirezpenayo.placeshare.core.domain.model.DomainError
import com.ramirezpenayo.placeshare.core.domain.model.User

sealed interface ProfileUiState {
    data object Initial : ProfileUiState
    data object Loading : ProfileUiState
    data class Success(val user: User) : ProfileUiState
    data class Error(val error: DomainError) : ProfileUiState
}
