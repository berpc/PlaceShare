package com.ramirezpenayo.placeshare.feature.auth

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.ramirezpenayo.placeshare.core.domain.model.DomainError
import com.ramirezpenayo.placeshare.features.auth.AuthRepository
import com.ramirezpenayo.placeshare.features.auth.AuthUiState

import com.ramirezpenayo.placeshare.features.auth.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    //
    val googleSignInIntent: Intent by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso).signInIntent
    }

    fun onGoogleSignInResult(result: Intent) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result)
        try {
            val account = task.getResult(ApiException::class.java) ?: return
            handleFirebaseAuth(account.idToken!!)
        } catch (e: ApiException) {
            showAuthError(e.localizedMessage)
        }
    }

    private fun handleFirebaseAuth(idToken: String) {
        _uiState.value = AuthUiState.Loading

        authRepository.signInWithGoogle(idToken)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user

                    if (user != null) {
                        _uiState.value = AuthUiState.Success(user)
                    } else {
                        showAuthError(task.exception?.localizedMessage)
                    }
                } else {
                    showAuthError(task.exception?.localizedMessage)
                }
            }
    }

    fun checkAuth() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            _uiState.value = AuthUiState.Success(user)
        }
    }

    private fun showAuthError(message: String?) {
        _uiState.value = AuthUiState.Error(DomainError.UnknownError(message ?: "Error de autenticación"))
    }
}