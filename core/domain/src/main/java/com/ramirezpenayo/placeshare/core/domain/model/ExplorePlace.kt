package com.ramirezpenayo.placeshare.core.domain.model

data class ExplorePlace(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val note: String = "",
    val photoUrl: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val createdAt: Long = 0L,
    val likedBy: List<String> = emptyList(),
    val commentsCount: Long = 0L
)
