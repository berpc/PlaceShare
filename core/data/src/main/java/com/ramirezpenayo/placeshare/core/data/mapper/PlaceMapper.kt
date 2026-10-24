package com.ramirezpenayo.placeshare.core.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.ramirezpenayo.placeshare.core.domain.model.Place

fun DocumentSnapshot.toPlace(): Place {
    return Place(
        id = id,
        userId = getString("userId") ?: "",
        userName = getString("userName") ?: "",
        title = getString("title") ?: "",
        note = getString("note") ?: "",
        photoUrl = getString("photoUrl") ?: "",
        lat = getDouble("lat"),
        lng = getDouble("lng"),
        createdAt = getLong("createdAt") ?: 0L,
        visibility = getString("visibility") ?: "public",
        likedBy = (get("likedBy") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    )
}

fun Place.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "userId" to userId,
        "userName" to userName,
        "title" to title,
        "note" to note,
        "photoUrl" to photoUrl,
        "lat" to lat,
        "lng" to lng,
        "createdAt" to createdAt,
        "visibility" to visibility,
        "likedBy" to likedBy
    )
}