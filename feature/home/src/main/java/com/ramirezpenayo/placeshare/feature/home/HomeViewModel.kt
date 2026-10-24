package com.ramirezpenayo.placeshare.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ramirezpenayo.placeshare.core.domain.model.Place
import com.ramirezpenayo.placeshare.core.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val uid = auth.currentUser?.uid

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init {
        if (uid.isNullOrBlank()) {
            _loading.value = false
        } else {
            observeUserPlaces(uid)
        }
    }

    private fun observeUserPlaces(userId: String) {
        viewModelScope.launch {
            placeRepository.getPlacesForUser(userId).collect { fetchedPlaces ->
                _loading.value = false
                _places.value = fetchedPlaces
            }
        }
    }

    fun deletePlace(placeId: String) {
        viewModelScope.launch {
            placeRepository.deletePlace(placeId)
                .onFailure { e ->
                    Log.e("HomeViewModel", "Error al borrar el lugar", e)
                }
        }
    }
}