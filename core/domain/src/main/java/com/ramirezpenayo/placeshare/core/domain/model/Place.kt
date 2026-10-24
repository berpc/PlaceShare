package com.ramirezpenayo.placeshare.core.domain.model

data class Place(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val note: String = "",
    val photoUrl: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val createdAt: Long = 0L,
    val visibility: String = "public",
    val likedBy: List<String> = emptyList()
)