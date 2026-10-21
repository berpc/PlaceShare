package com.ramirezpenayo.placeshare.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramirezpenayo.placeshare.core.domain.model.DomainError
import com.ramirezpenayo.placeshare.core.domain.model.repository.AuthRepository
import com.ramirezpenayo.placeshare.core.domain.model.repository.UserRepository
import com.ramirezpenayo.placeshare.feature.profile.presentation.components.AppThemeMode
import com.ramirezpenayo.placeshare.feature.profile.presentation.profile.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Initial)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            val currentUserId = authRepository.getCurrentUserUid()
            if (currentUserId.isNullOrBlank()) {
                _uiState.value = ProfileUiState.Error(
                    DomainError.UnknownError("No hay una sesión activa")
                )
                return@launch
            }

            val name = authRepository.getCurrentUserName()
            val email = authRepository.getCurrentUserEmail()
            val photoUrl = authRepository.getCurrentUserPhotoUrl()

            userRepository.getOrCreateUser(
                uid = currentUserId,
                name = name,
                email = email,
                photoUrl = photoUrl
            )

            userRepository.getCurrentUserFlow().collect { user ->
                if (user != null) {
                    _uiState.value = ProfileUiState.Success(user)
                } else {
                    _uiState.value = ProfileUiState.Error(
                        DomainError.UnknownError("No se pudo obtener el perfil de usuario")
                    )
                }
            }
        }
    }

    fun updateTheme(themeMode: AppThemeMode) {
        viewModelScope.launch {
            val themeString = themeMode.name.uppercase()
            userRepository.updateThemeColor(themeString)
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        authRepository.signOut()
        onSignedOut()
    }
}
