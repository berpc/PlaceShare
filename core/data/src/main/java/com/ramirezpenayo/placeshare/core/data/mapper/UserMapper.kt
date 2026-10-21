package com.ramirezpenayo.placeshare.core.data.mapper

import com.ramirezpenayo.placeshare.core.domain.model.User

fun User.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "uid" to uid,
        "name" to name,
        "username" to username,
        "bio" to bio,
        "email" to email,
        "photoUrl" to photoUrl,
        "themeColor" to themeColor,
        "totalPlacesCount" to totalPlacesCount,
        "totalLikesReceived" to totalLikesReceived,
        "totalFollowers" to totalFollowers,
        "totalFollowing" to totalFollowing
    )
}

fun Map<String, Any?>.toUser(uid: String): User {
    return User(
        uid = uid,
        name = this["name"] as? String,
        username = this["username"] as? String ?: "usuario",
        bio = this["bio"] as? String,
        email = this["email"] as? String,
        photoUrl = this["photoUrl"] as? String,
        themeColor = this["themeColor"] as? String ?: "SYSTEM",
        totalPlacesCount = (this["totalPlacesCount"] as? Number)?.toInt() ?: 0,
        totalLikesReceived = (this["totalLikesReceived"] as? Number)?.toInt() ?: 0,
        totalFollowers = (this["totalFollowers"] as? Number)?.toInt() ?: 0,
        totalFollowing = (this["totalFollowing"] as? Number)?.toInt() ?: 0
    )
}