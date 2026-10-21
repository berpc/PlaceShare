package com.ramirezpenayo.placeshare.feature.profile.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ramirezpenayo.placeshare.core.domain.model.User
import com.ramirezpenayo.placeshare.core.ui.components.LoadingContent
import com.ramirezpenayo.placeshare.core.ui.components.PlaceShareTopBar
import com.ramirezpenayo.placeshare.core.ui.components.asMessage
import com.ramirezpenayo.placeshare.feature.profile.ProfileViewModel
import com.ramirezpenayo.placeshare.feature.profile.presentation.components.AppThemeMode
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

@Composable
fun ProfileContent(
    user: User,
    onEditClick: () -> Unit,
    onThemeClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        AsyncImage(
            model = user.photoUrl,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = user.name ?: "Sin nombre",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "@${user.username ?: "usuario"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        if (!user.bio.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user.bio!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStat(label = "Lugares", count = user.totalPlacesCount)
            ProfileStat(label = "Me gusta", count = user.totalLikesReceived)
            ProfileStat(label = "Seguidores", count = user.totalFollowers)
            ProfileStat(label = "Siguiendo", count = user.totalFollowing)
        }

        Spacer(modifier = Modifier.height(28.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))

        ProfileOptionRow(
            icon = Icons.Default.Edit,
            title = "Editar perfil",
            onClick = onEditClick
        )
        ProfileOptionRow(
            icon = Icons.Default.Palette,
            title = "Cambiar color del tema",
            onClick = onThemeClick
        )
        ProfileOptionRow(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            title = "Cerrar sesión",
            tint = MaterialTheme.colorScheme.error,
            onClick = onSignOutClick
        )
    }
}

@Composable
fun ProfileStat(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileOptionRow(
    icon: ImageVector,
    title: String,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = tint,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
