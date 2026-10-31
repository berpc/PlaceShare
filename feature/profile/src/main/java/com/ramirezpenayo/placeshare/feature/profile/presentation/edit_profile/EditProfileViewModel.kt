package com.ramirezpenayo.placeshare.feature.profile.presentation.edit_profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramirezpenayo.placeshare.core.domain.model.DomainError
import com.ramirezpenayo.placeshare.core.domain.repository.AuthRepository
import com.ramirezpenayo.placeshare.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Idle)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var initialName by mutableStateOf("")
    private var initialUsername by mutableStateOf("")
    private var initialBio by mutableStateOf("")

    var name by mutableStateOf("")
        private set

    var username by mutableStateOf("")
        private set

    var bio by mutableStateOf("")
        private set

    var currentPhotoUrl by mutableStateOf<String?>(null)
        private set

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    val hasChanges: Boolean
        get() = selectedImageUri != null ||
                name != initialName ||
                username != initialUsername ||
                bio != initialBio

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserUid() ?: return@launch
            val result = userRepository.getUserProfile(uid)
            result.onSuccess { user ->
                initialName = user.name ?: ""
                initialUsername = user.username ?: ""
                initialBio = user.bio ?: ""
                name = initialName
                username = initialUsername
                bio = initialBio
                currentPhotoUrl = user.photoUrl
            }
        }
    }

    fun onNameChanged(newName: String) { name = newName }
    fun onUsernameChanged(newUsername: String) { username = newUsername }
    fun onBioChanged(newBio: String) { bio = newBio }

    fun onImageSelected(uri: Uri?) {
        selectedImageUri = uri
    }

    fun saveProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            val uid = authRepository.getCurrentUserUid()

            if (uid.isNullOrBlank()) {
                _uiState.value = EditProfileUiState.Error(DomainError.UnknownError("Sesión no encontrada"))
                return@launch
            }

            val imageBytes = selectedImageUri?.let { uri ->
                try {
                    uriToJpegBytes(context, uri)
                } catch (e: Exception) {
                    null
                }
            }

            val profileResult = userRepository.getUserProfile(uid)
            val currentUser = profileResult.getOrNull()

            if (currentUser == null) {
                _uiState.value = EditProfileUiState.Error(DomainError.UnknownError("No se pudo obtener el usuario"))
                return@launch
            }

            val updatedUser = currentUser.copy(
                name = name.trim(),
                username = username.trim(),
                bio = bio.trim()
            )

            val result = userRepository.updateUser(updatedUser, imageBytes)

            result.fold(
                onSuccess = {
                    _uiState.value = EditProfileUiState.Success
                    onSuccess()
                },
                onFailure = { throwable ->
                    _uiState.value = EditProfileUiState.Error(
                        DomainError.UnknownError(throwable.localizedMessage ?: "Error al actualizar perfil")
                    )
                }
            )
        }
    }

    private fun uriToJpegBytes(context: Context, uri: Uri): ByteArray {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir la imagen")
        val bitmap = input.use { BitmapFactory.decodeStream(it) }
            ?: throw IllegalStateException("Imagen no válida")
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        return out.toByteArray()
    }
}
