package com.ramirezpenayo.placeshare.feature.explore

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExploreViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    val currentUid: String? = FirebaseAuth.getInstance().currentUser?.uid

    private val _places = MutableStateFlow<List<ExplorePlace>>(emptyList())
    val places: StateFlow<List<ExplorePlace>> = _places

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private var registration: ListenerRegistration? = null

    init {
        registration = db.collection("places")
            .whereEqualTo("visibility", "public")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                _loading.value = false
                if (error != null) {
                    Log.e("ExploreViewModel", "Error al leer lugares públicos", error)
                    return@addSnapshotListener
                }
                _places.value = snapshot?.documents
                    ?.mapNotNull { doc -> doc.toObject(ExplorePlace::class.java)?.copy(id = doc.id) }
                    ?.filter { it.userId != currentUid }   // tus lugares van en Inicio
                    ?: emptyList()
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
        val change = if (isLiked(place)) FieldValue.arrayRemove(uid) else FieldValue.arrayUnion(uid)
        db.collection("places").document(place.id).update("likedBy", change)
            .addOnSuccessListener { _error.value = null }
            .addOnFailureListener { e ->
                _error.value = "No se pudo dar me gusta: ${e.message}"
            }
    }

    override fun onCleared() {
        registration?.remove()
    }
}