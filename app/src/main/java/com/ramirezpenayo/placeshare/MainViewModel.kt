package com.ramirezpenayo.placeshare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramirezpenayo.placeshare.core.domain.model.User
import com.ramirezpenayo.placeshare.core.domain.model.repository.AuthRepository
import com.ramirezpenayo.placeshare.core.domain.model.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            // Escucha los cambios del usuario en tiempo real desde Firestore
            // para que ni bien guardes el tema en el perfil, la MainActivity lo reciba al instante.
            userRepository.getCurrentUserFlow().collect { user ->
                _uiState.value = MainUiState(user = user)
            }
        }
    }
}

data class MainUiState(
    val user: User? = null
)