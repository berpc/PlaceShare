package com.ramirezpenayo.placeshare.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val places by viewModel.places.collectAsState()
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                // Posición inicial: primer lugar del usuario o coordenadas por defecto (Asunción)
                val firstValid = places.firstOrNull { it.lat != null && it.lng != null }
                val startPoint = if (firstValid != null) {
                    GeoPoint(firstValid.lat!!, firstValid.lng!!)
                } else {
                    GeoPoint(-25.2637, -57.5759)
                }
                controller.setZoom(12.0)
                controller.setCenter(startPoint)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            // Dibuja un pin por cada lugar que tenga coordenadas
            places.filter { it.lat != null && it.lng != null }.forEach { place ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(place.lat!!, place.lng!!)
                    title = place.title
                    snippet = place.note.ifBlank { "Sin notas" }
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )
}