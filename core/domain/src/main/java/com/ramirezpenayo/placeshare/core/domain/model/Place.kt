package com.ramirezpenayo.placeshare.core.domain.model

import java.util.Date

data class Place(
    val id: String,
    val userId: String,
    val imageUrl: String,
    val category: String,
    val note: String?,

    val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val date: Date,

    val viewsCount: Int = 0,
    val likesCount: Int = 0,
    val likedUserIds: List<String> = emptyList()
)