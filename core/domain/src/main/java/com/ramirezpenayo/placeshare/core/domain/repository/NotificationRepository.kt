package com.ramirezpenayo.placeshare.core.domain.repository

import com.ramirezpenayo.placeshare.core.domain.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<List<NotificationItem>>
    fun getUnreadCount(userId: String): Flow<Int>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun markAllAsRead(userId: String): Result<Unit>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    suspend fun deleteAllNotifications(userId: String): Result<Unit>
    suspend fun createNotification(notification: NotificationItem): Result<Unit>
}
