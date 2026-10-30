package com.ramirezpenayo.placeshare.feature.profile.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramirezpenayo.placeshare.core.ui.components.LoadingContent
import com.ramirezpenayo.placeshare.core.ui.components.PlaceShareTopBar
import com.ramirezpenayo.placeshare.core.ui.components.asMessage
import com.ramirezpenayo.placeshare.feature.profile.ProfileViewModel
import com.ramirezpenayo.placeshare.feature.profile.presentation.components.AppThemeMode
import com.ramirezpenayo.placeshare.feature.profile.presentation.components.ProfileContent
import com.ramirezpenayo.placeshare.feature.profile.presentation.components.ThemeSelectionDialog

@Composable
fun ProfileRoute(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onSignOutSuccess: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileScreen(
        uiState = uiState,
        onEditClick = onNavigateToEditProfile,
        onThemeSelect = { selectedMode ->
            viewModel.updateTheme(selectedMode)
        },
        onSignOutClick = {
            viewModel.signOut(onSignOutSuccess)
        }
    )
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onEditClick: () -> Unit,
    onThemeSelect: (AppThemeMode) -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showThemeDialog by remember { mutableStateOf(false) }

    if (uiState is ProfileUiState.Error) {
        val errorMessage = uiState.error.asMessage()
        LaunchedEffect(uiState) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            PlaceShareTopBar(title = "Perfil")
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when (uiState) {
                is ProfileUiState.Loading, ProfileUiState.Initial -> {
                    LoadingContent()
                }
                is ProfileUiState.Success -> {
                    val user = uiState.user

                    val currentThemeMode = when (user.themeColor.uppercase()) {
                        "LIGHT" -> AppThemeMode.LIGHT
                        "DARK" -> AppThemeMode.DARK
                        else -> AppThemeMode.SYSTEM
                    }

                    ProfileContent(
                        user = user,
                        onEditClick = onEditClick,
                        onThemeClick = { showThemeDialog = true },
                        onSignOutClick = onSignOutClick
                    )

                    if (showThemeDialog) {
                        ThemeSelectionDialog(
                            currentTheme = currentThemeMode,
                            onThemeSelected = { selectedMode: AppThemeMode ->
                                showThemeDialog = false
                                onThemeSelect(selectedMode)
                            },
                            onDismiss = {
                                showThemeDialog = false
                            }
                        )
                    }
                }
                is ProfileUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error al cargar el perfil.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
