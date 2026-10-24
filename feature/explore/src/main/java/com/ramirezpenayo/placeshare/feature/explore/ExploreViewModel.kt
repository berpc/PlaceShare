package com.ramirezpenayo.placeshare.feature.explore

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ramirezpenayo.placeshare.core.domain.model.ExplorePlace
import com.ramirezpenayo.placeshare.core.domain.repository.ExploreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val auth: FirebaseAuth // O un AuthRepository si ya lo tienes modularizado
) : ViewModel() {

    val currentUid: String? get() = auth.currentUser?.uid

    private val _places = MutableStateFlow<List<ExplorePlace>>(emptyList())
    val places: StateFlow<List<ExplorePlace>> = _places

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init {
        observePublicPlaces()
    }

    private fun observePublicPlaces() {
        viewModelScope.launch {
            exploreRepository.getPublicPlaces(currentUid).collect { fetchedPlaces ->
                _loading.value = false
                _places.value = fetchedPlaces
            }
        }
    }

    fun isLiked(place: ExplorePlace): Boolean =
        currentUid?.let { place.likedBy.contains(it) } ?: false

    fun toggleLike(place: ExplorePlace) {
        val uid = currentUid
        if (uid == null) {
            _error.value = "No hay sesión iniciada. Cerrá sesión y volvé a entrar."
            return
        }

        val liking = !isLiked(place)

        // Optimistic update local
        _places.value = _places.value.map { p ->
            if (p.id == place.id) {
                p.copy(likedBy = if (liking) p.likedBy + uid else p.likedBy - uid)
            } else p
        }

        viewModelScope.launch {
            exploreRepository.toggleLike(place.id, uid, liking)
                .onSuccess {
                    _error.value = null
                }
                .onFailure { e ->
                    // Rollback si falla Firestore
                    _places.value = _places.value.map { p ->
                        if (p.id == place.id) place else p
                    }
                    _error.value = "No se pudo dar me gusta: ${e.message}"
                }
        }
    }
}