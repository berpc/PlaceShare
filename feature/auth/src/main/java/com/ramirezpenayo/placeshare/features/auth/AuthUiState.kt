package com.ramirezpenayo.placeshare.features.auth

import com.google.firebase.auth.FirebaseUser
import com.ramirezpenayo.placeshare.core.domain.model.DomainError

sealed interface AuthUiState {
    data object Initial : AuthUiState
    data object Loading : AuthUiState
    data class Success(val user: FirebaseUser) : AuthUiState
    data class Error(val error: DomainError) : AuthUiState
}
