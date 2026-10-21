package com.ramirezpenayo.placeshare.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private var registration: ListenerRegistration? = null

    init {
        if (uid == null) {
            _loading.value = false
        } else {
            registration = db.collection("places")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    _loading.value = false
                    if (error != null) {
                        Log.e("HomeViewModel", "Error al leer places", error)
                        return@addSnapshotListener
                    }
                    Log.d("HomeViewModel", "Documentos recibidos: ${snapshot?.size()}")
                    _places.value = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Place::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                }
        }
    }

    fun deletePlace(placeId: String) {
        db.collection("places").document(placeId).delete()
    }

    override fun onCleared() {
        registration?.remove()
    }
}