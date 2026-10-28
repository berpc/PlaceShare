package com.ramirezpenayo.placeshare.feature.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ramirezpenayo.placeshare.core.domain.model.NotificationItem
import com.ramirezpenayo.placeshare.core.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val currentUid: String? get() = auth.currentUser?.uid

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        val uid = currentUid ?: run {
            _loading.value = false
            return
        }

        viewModelScope.launch {
            notificationRepository.getNotifications(uid).collect { list ->
                _loading.value = false
                _notifications.value = list
            }
        }

        viewModelScope.launch {
            notificationRepository.getUnreadCount(uid).collect { count ->
                _unreadCount.value = count
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        val uid = currentUid ?: return
        viewModelScope.launch {
            notificationRepository.markAllAsRead(uid)
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
        }
    }

    fun deleteAllNotifications() {
        val uid = currentUid ?: return
        viewModelScope.launch {
            notificationRepository.deleteAllNotifications(uid)
        }
    }

    fun triggerSystemNotification(context: Context, notif: NotificationItem) {
        val title = if (notif.type == com.ramirezpenayo.placeshare.core.domain.model.NotificationType.LIKE) {
            "❤️ ¡A ${notif.senderName} le gustó tu publicación!"
        } else {
            "💬 ${notif.senderName} comentó en tu publicación"
        }
        val msg = "${notif.postTitle}: ${notif.message}"
        SystemNotificationHelper.showSystemNotification(context, title, msg)
    }

    fun sendTestSystemNotification(context: Context) {
        SystemNotificationHelper.showSystemNotification(
            context = context,
            title = "PlaceShare - Notificación de prueba",
            message = "Un usuario le dio Me Gusta a tu foto compartida"
        )
    }
}
