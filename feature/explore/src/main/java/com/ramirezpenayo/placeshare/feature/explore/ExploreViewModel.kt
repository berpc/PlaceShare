package com.ramirezpenayo.placeshare.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ramirezpenayo.placeshare.core.domain.model.Comment
import com.ramirezpenayo.placeshare.core.domain.model.ExplorePlace
import com.ramirezpenayo.placeshare.core.domain.repository.ExploreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val currentUid: String? get() = auth.currentUser?.uid

    private val _places = MutableStateFlow<List<ExplorePlace>>(emptyList())
    val places: StateFlow<List<ExplorePlace>> = _places

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _selectedPlaceForComments = MutableStateFlow<ExplorePlace?>(null)
    val selectedPlaceForComments: StateFlow<ExplorePlace?> = _selectedPlaceForComments

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _isSendingComment = MutableStateFlow(false)
    val isSendingComment: StateFlow<Boolean> = _isSendingComment

    private var commentsJob: Job? = null

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

    fun openComments(place: ExplorePlace) {
        _selectedPlaceForComments.value = place
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            exploreRepository.getComments(place.id).collect { fetchedComments ->
                _comments.value = fetchedComments
            }
        }
    }

    fun closeComments() {
        commentsJob?.cancel()
        _selectedPlaceForComments.value = null
        _comments.value = emptyList()
    }

    fun sendComment(text: String) {
        val place = _selectedPlaceForComments.value ?: return
        val uid = currentUid
        if (uid == null) {
            _error.value = "No hay sesión iniciada."
            return
        }

        if (text.isBlank()) return

        val userName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: auth.currentUser?.email?.substringBefore("@")
            ?: "Usuario"

        _isSendingComment.value = true
        viewModelScope.launch {
            exploreRepository.addComment(place.id, uid, userName, text.trim())
                .onSuccess {
                    _isSendingComment.value = false
                }
                .onFailure { e ->
                    _isSendingComment.value = false
                    _error.value = "Error al enviar comentario: ${e.message}"
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
