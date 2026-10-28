package com.ramirezpenayo.placeshare.core.domain.model

enum class NotificationType {
    LIKE,
    COMMENT
}

data class NotificationItem(
    val id: String = "",
    val recipientUserId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val type: NotificationType = NotificationType.LIKE,
    val postId: String = "",
    val postTitle: String = "",
    val postPhotoUrl: String = "",
    val message: String = "",
    val createdAt: Long = 0L,
    val isRead: Boolean = false
)
