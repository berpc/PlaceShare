package com.ramirezpenayo.placeshare.feature.profile.presentation.edit_profile

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ramirezpenayo.placeshare.core.ui.components.LoadingContent
import com.ramirezpenayo.placeshare.core.ui.components.PlaceShareTopBar
import com.ramirezpenayo.placeshare.core.ui.components.asMessage

private const val MAX_NAME_LENGTH = 50
private const val MAX_USERNAME_LENGTH = 30
private const val MAX_BIO_LENGTH = 160

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileRoute(
    onBackClick: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDiscardDialog by remember { mutableStateOf(false) }

    fun handleBackClick() {
        if (viewModel.hasChanges) {
            showDiscardDialog = true
        } else {
            onBackClick()
        }
    }

    BackHandler(enabled = viewModel.hasChanges) {
        showDiscardDialog = true
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.onImageSelected(uri)
    }

    if (uiState is EditProfileUiState.Error) {
        val message = (uiState as EditProfileUiState.Error).error.asMessage()
        LaunchedEffect(uiState) {
            snackbarHostState.showSnackbar(message)
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("¿Descartar cambios?") },
            text = { Text("Tienes cambios sin guardar. ¿Deseas salir y descartar las modificaciones?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBackClick()
                    }
                ) {
                    Text("Descartar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            PlaceShareTopBar(
                title = "Editar perfil",
                onBackClick = ::handleBackClick,
                actions = {
                    TextButton(
                        onClick = { viewModel.saveProfile(onBackClick) },
                        enabled = viewModel.hasChanges && uiState !is EditProfileUiState.Loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Aceptar cambios",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Aceptar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState is EditProfileUiState.Loading) {
                LoadingContent(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable {
                                imageLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                    ) {
                        AsyncImage(
                            model = viewModel.selectedImageUri ?: viewModel.currentPhotoUrl ?: "https://via.placeholder.com/150",
                            contentDescription = "Cambiar foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Icono cámara",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = viewModel.name,
                        onValueChange = { if (it.length <= MAX_NAME_LENGTH) viewModel.onNameChanged(it) },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = {
                            Text(
                                text = "${viewModel.name.length}/$MAX_NAME_LENGTH",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.username,
                        onValueChange = { if (it.length <= MAX_USERNAME_LENGTH) viewModel.onUsernameChanged(it) },
                        label = { Text("Nombre de usuario") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = {
                            Text(
                                text = "${viewModel.username.length}/$MAX_USERNAME_LENGTH",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.bio,
                        onValueChange = { if (it.length <= MAX_BIO_LENGTH) viewModel.onBioChanged(it) },
                        label = { Text("Biografía") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        minLines = 3,
                        maxLines = 5,
                        supportingText = {
                            Text(
                                text = "${viewModel.bio.length}/$MAX_BIO_LENGTH",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                    )
                }
            }
        }
    }
}
