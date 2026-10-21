package com.ramirezpenayo.placeshare.feature.home

data class Place(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val note: String = "",
    val photoUrl: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val createdAt: Long = 0L
)