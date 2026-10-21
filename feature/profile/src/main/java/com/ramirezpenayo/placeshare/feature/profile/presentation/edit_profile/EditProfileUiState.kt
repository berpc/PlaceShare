package com.ramirezpenayo.placeshare.feature.profile.presentation.edit_profile

import com.ramirezpenayo.placeshare.core.domain.model.DomainError

sealed interface EditProfileUiState {
    data object Idle : EditProfileUiState
    data object Loading : EditProfileUiState
    data object Success : EditProfileUiState
    data class Error(val error: DomainError) : EditProfileUiState
}