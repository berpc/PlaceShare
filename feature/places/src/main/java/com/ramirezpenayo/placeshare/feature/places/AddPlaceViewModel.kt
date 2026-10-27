package com.ramirezpenayo.placeshare.feature.places

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import com.ramirezpenayo.placeshare.core.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class AddPlaceViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _locationText = MutableStateFlow("Ubicación pendiente")
    val locationText: StateFlow<String> = _locationText

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri

    private var lat: Double? = null
    private var lng: Double? = null

    @SuppressLint("MissingPermission")
    fun fetchLocation(context: Context) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    lat = location.latitude
                    lng = location.longitude
                    _locationText.value = "%.5f, %.5f".format(location.latitude, location.longitude)
                } else {
                    _locationText.value = "No se pudo obtener la ubicación"
                }
            }.addOnFailureListener {
                _locationText.value = "No se pudo obtener la ubicación"
            }
    }

    fun setLocationDenied() {
        _locationText.value = "Permiso de ubicación denegado"
    }

    fun setPhoto(uri: Uri?) {
        _photoUri.value = uri
    }

    fun savePlace(context: Context, title: String, note: String, isPublic: Boolean, onSaved: () -> Unit) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        val userName = currentUser?.displayName ?: "Usuario"

        if (uid.isNullOrBlank()) {
            _error.value = "Tenés que iniciar sesión para guardar."
            return
        }
        if (title.isBlank()) {
            _error.value = "El título no puede estar vacío."
            return
        }

        _saving.value = true
        _error.value = null

        viewModelScope.launch {
            val imageBytes = _photoUri.value?.let { uri ->
                try {
                    uriToJpegBytes(context, uri)
                } catch (e: Exception) {
                    null
                }
            }

            placeRepository.savePlace(
                userId = uid,
                userName = userName,
                title = title.trim(),
                note = note.trim(),
                isPublic = isPublic,
                lat = lat,
                lng = lng,
                imageBytes = imageBytes
            ).fold(
                onSuccess = {
                    _saving.value = false
                    _photoUri.value = null
                    onSaved()
                },
                onFailure = { e ->
                    _saving.value = false
                    _error.value = "No se pudo guardar: ${e.message}"
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