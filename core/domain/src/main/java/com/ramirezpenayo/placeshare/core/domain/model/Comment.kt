package com.ramirezpenayo.placeshare.core.domain.model

data class Comment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val createdAt: Long = 0L
)
