package com.ramirezpenayo.placeshare.core.domain.model

data class User(
    val uid: String = "",
    val name: String? = null,
    val bio: String? = null,
    val username: String = "",
    val email: String? = null,
    val photoUrl: String? = null,
    val themeColor: String = "SYSTEM",
    val totalPlacesCount: Int = 0,
    val totalLikesReceived: Int = 0,
    val totalFollowers: Int = 0,
    val totalFollowing: Int = 0
) {
    constructor() : this(
        uid = "",
        name = null,
        bio = null,
        username = "",
        email = null,
        photoUrl = null,
        themeColor = "SYSTEM",
        totalPlacesCount = 0,
        totalLikesReceived = 0,
        totalFollowers = 0,
        totalFollowing = 0
    )
}