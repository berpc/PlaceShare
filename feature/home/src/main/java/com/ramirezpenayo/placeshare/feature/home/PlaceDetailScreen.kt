package com.ramirezpenayo.placeshare.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Lugar no encontrado", style = MaterialTheme.typography.bodyLarge)
            TextButton(onClick = onBack) { Text("Volver") }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Imagen ampliada
        if (place.photoUrl.isNotBlank()) {
            AsyncImage(
                model = place.photoUrl,
                contentDescription = place.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
        }

        // 2. Título
        Text(
            text = place.title,
            style = MaterialTheme.typography.headlineMedium
        )

        // 3. Fecha
        Text(
            text = formatDate(place.createdAt),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 4. Nota / Descripción
        if (place.note.isNotBlank()) {
            Text(
                text = place.note,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // 5. Ubicación en el mapa
        val lat = place.lat
        val lng = place.lng
        if (lat != null && lng != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ubicación en mapa",
                style = MaterialTheme.typography.titleMedium
            )

            androidx.compose.ui.viewinterop.AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(MaterialTheme.shapes.medium),
                factory = { ctx ->
                    org.osmdroid.views.MapView(ctx).apply {
                        setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        val point = org.osmdroid.util.GeoPoint(lat, lng)
                        controller.setZoom(15.0)
                        controller.setCenter(point)

                        val marker = org.osmdroid.views.overlay.Marker(this).apply {
                            position = point
                            title = place.title
                            setAnchor(
                                org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                                org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
                            )
                        }
                        overlays.add(marker)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 6. Botones de acción
        Button(
            onClick = { confirmDelete = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.borrar_lugar))
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
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
