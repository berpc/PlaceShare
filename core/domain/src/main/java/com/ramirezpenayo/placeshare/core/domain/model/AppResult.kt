package com.ramirezpenayo.placeshare.core.domain.model

sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Error(val exception: Exception) : AppResult<Nothing>
    object Loading : AppResult<Nothing>
}

sealed interface DomainError {
    data object NetworkError : DomainError
    data object NotFound : DomainError
    data object EmptyLocation : DomainError
    data class UnknownError(val message: String) : DomainError
}