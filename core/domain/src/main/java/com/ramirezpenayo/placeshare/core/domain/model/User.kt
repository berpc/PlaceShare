package com.ramirezpenayo.placeshare.core.domain.model

data class User(
    val id: String,
    val name: String?,
    val bio: String?,
    val email: String?,
    val imageUrl: String?,

    val totalPlacesCount: Int = 0,
    val totalLikesReceived: Int = 0
)