package com.ramirezpenayo.placeshare.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.height
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PlaceDetailScreen(
    placeId: String,
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val places by viewModel.places.collectAsState()
    val place = places.find { it.id == placeId }
    var confirmDelete by remember { mutableStateOf(false) }

    if (place == null) {
        // Puede pasar un instante mientras carga, o si el lugar fue borrado.
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Text("Lugar no encontrado", style = MaterialTheme.typography.bodyLarge)
            TextButton(onClick = onBack) { Text("Volver") }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)

    )

    {
        if (place.photoUrl.isNotBlank()) {
            AsyncImage(
                model = place.photoUrl,
                contentDescription = place.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
        }

        Text(place.title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = formatDate(place.createdAt),
            style = MaterialTheme.typography.labelMedium
        )
        if (place.note.isNotBlank()) {
            Text(place.note, style = MaterialTheme.typography.bodyLarge)
        }

        Button(
            onClick = { confirmDelete = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.borrar_lugar))
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.volver))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.borrar_este_lugar)) },
            text = { Text(stringResource(R.string.esta_acci_n_no_se_puede_deshacer)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.deletePlace(placeId)
                    onBack()
                }) { Text(stringResource(R.string.borrar)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancelar)) }
            }
        )
    }
}