package com.ramirezpenayo.placeshare.feature.explore

data class ExplorePlace(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val note: String = "",
    val photoUrl: String = "",
    val createdAt: Long = 0L,
    val likedBy: List<String> = emptyList(),
    val commentsCount: Long = 0L
)