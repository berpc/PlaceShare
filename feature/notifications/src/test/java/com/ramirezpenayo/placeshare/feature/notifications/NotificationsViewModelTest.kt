package com.ramirezpenayo.placeshare.feature.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ramirezpenayo.placeshare.core.domain.model.NotificationItem
import com.ramirezpenayo.placeshare.core.domain.model.NotificationType
import com.ramirezpenayo.placeshare.core.domain.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private val fakeRepository = FakeNotificationRepository()
    private val mockAuth: FirebaseAuth = mock(FirebaseAuth::class.java)
    private val mockUser: FirebaseUser = mock(FirebaseUser::class.java)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("user_abc")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = NotificationsViewModel(fakeRepository, mockAuth)

    @Test
    fun `al iniciar el viewModel se obtienen las notificaciones y el conteo no leido`() = runTest {
        fakeRepository.notificationsList = listOf(
            fakeNotification("1", isRead = false),
            fakeNotification("2", isRead = true)
        )
        fakeRepository.unreadCountValue = 1

        val viewModel = createViewModel()

        assertEquals(2, viewModel.notifications.value.size)
        assertEquals(1, viewModel.unreadCount.value)
    }

    @Test
    fun `marcar como leida invoca el repositorio`() = runTest {
        val viewModel = createViewModel()

        viewModel.markAsRead("1")

        assertTrue(fakeRepository.markedAsReadIds.contains("1"))
    }

    @Test
    fun `marcar todas como leidas invoca el repositorio con el uid`() = runTest {
        val viewModel = createViewModel()

        viewModel.markAllAsRead()

        assertTrue(fakeRepository.markedAllAsReadUserIds.contains("user_abc"))
    }
}

private class FakeNotificationRepository : NotificationRepository {
    var notificationsList = listOf<NotificationItem>()
    var unreadCountValue = 0

    val markedAsReadIds = mutableListOf<String>()
    val markedAllAsReadUserIds = mutableListOf<String>()
    val deletedIds = mutableListOf<String>()

    override fun getNotifications(userId: String): Flow<List<NotificationItem>> {
        return flowOf(notificationsList)
    }

    override fun getUnreadCount(userId: String): Flow<Int> {
        return flowOf(unreadCountValue)
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        markedAsReadIds.add(notificationId)
        return Result.success(Unit)
    }

    override suspend fun markAllAsRead(userId: String): Result<Unit> {
        markedAllAsReadUserIds.add(userId)
        return Result.success(Unit)
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        deletedIds.add(notificationId)
        return Result.success(Unit)
    }

    override suspend fun createNotification(notification: NotificationItem): Result<Unit> {
        notificationsList = notificationsList + notification
        return Result.success(Unit)
    }

    override suspend fun deleteAllNotifications(userId: String): Result<Unit> {
        notificationsList = emptyList()
        return Result.success(Unit)
    }
}

private fun fakeNotification(id: String, isRead: Boolean) = NotificationItem(
    id = id,
    recipientUserId = "user_abc",
    senderId = "sender_1",
    senderName = "Carlos",
    type = NotificationType.LIKE,
    postId = "post_100",
    postTitle = "Foto en la playa",
    postPhotoUrl = "",
    message = "Le gusto tu foto",
    createdAt = System.currentTimeMillis(),
    isRead = isRead
)
