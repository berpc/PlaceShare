package com.ramirezpenayo.placeshare.feature.auth

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.ramirezpenayo.placeshare.core.domain.model.DomainError
import com.ramirezpenayo.placeshare.core.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun getGoogleSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        client.signOut()
        return client.signInIntent
    }

    fun onGoogleSignInResult(result: Intent) {
        android.util.Log.d("AuthViewModel", "onGoogleSignInResult called")
        _uiState.value = AuthUiState.Loading
        val task = GoogleSignIn.getSignedInAccountFromIntent(result)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            android.util.Log.d("AuthViewModel", "Google account retrieved: ${account?.email}, idToken present: ${!idToken.isNullOrBlank()}")
            if (!idToken.isNullOrBlank()) {
                handleFirebaseAuth(idToken)
            } else {
                showAuthError("No se pudo obtener el token de Google (idToken nulo).")
            }
        } catch (e: ApiException) {
            android.util.Log.e("AuthViewModel", "ApiException code: ${e.statusCode}, message: ${e.message}", e)
            val msg = if (e.statusCode == 10) {
                "Error 10 (DEVELOPER_ERROR): La huella SHA-1 de la app no coincide con Firebase Console."
            } else {
                "Error de Google Sign-In (${e.statusCode}): ${e.localizedMessage}"
            }
            showAuthError(msg)
        } catch (e: Exception) {
            android.util.Log.e("AuthViewModel", "Exception in onGoogleSignInResult: ${e.message}", e)
            showAuthError(e.localizedMessage ?: "Error al iniciar sesión con Google")
        }
    }

    private fun handleFirebaseAuth(idToken: String) {
        android.util.Log.d("AuthViewModel", "handleFirebaseAuth called with idToken")
        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken)

            result.fold(
                onSuccess = {
                    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

                    if (firebaseUser != null) {
                        android.util.Log.d("AuthViewModel", "Firebase signIn successful, user: ${firebaseUser.uid}")
                        _uiState.value = AuthUiState.Success(firebaseUser)
                    } else {
                        showAuthError("No se pudo obtener el usuario de Firebase")
                    }
                },
                onFailure = { exception ->
                    android.util.Log.e("AuthViewModel", "Firebase signIn failed: ${exception.message}", exception)
                    showAuthError(exception.localizedMessage)
                }
            )
        }
    }

    fun checkAuth() {
        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            _uiState.value = AuthUiState.Success(firebaseUser)
        }
    }

    private fun showAuthError(message: String?) {
        _uiState.value = AuthUiState.Error(DomainError.UnknownError(message ?: "Error de autenticación"))
    }
}
