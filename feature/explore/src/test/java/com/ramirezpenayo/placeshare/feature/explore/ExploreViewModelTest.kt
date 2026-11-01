package com.ramirezpenayo.placeshare.feature.explore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ramirezpenayo.placeshare.core.domain.model.Comment
import com.ramirezpenayo.placeshare.core.domain.model.ExplorePlace
import com.ramirezpenayo.placeshare.core.domain.repository.ExploreRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    private val fakeRepository = FakeExploreRepository()
    private val mockAuth: FirebaseAuth = mock(FirebaseAuth::class.java)
    private val mockUser: FirebaseUser = mock(FirebaseUser::class.java)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("user_exp")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ExploreViewModel(fakeRepository, mockAuth)

    @Test
    fun `al iniciar el viewModel se obtienen las publicaciones publicas`() = runTest {
        fakeRepository.publicPlacesList = listOf(
            fakeExplorePlace("exp_1", "Lugar Publico 1")
        )

        val viewModel = createViewModel()

        assertEquals(1, viewModel.places.value.size)
        assertEquals("Lugar Publico 1", viewModel.places.value[0].title)
        assertFalse(viewModel.loading.value)
    }

    @Test
    fun `openComments abre el lugar seleccionado y carga comentarios`() = runTest {
        val place = fakeExplorePlace("exp_1", "Lugar 1")
        fakeRepository.commentsMap["exp_1"] = listOf(
            Comment("c1", "user_1", "Ana", "Gran lugar", System.currentTimeMillis())
        )

        val viewModel = createViewModel()
        viewModel.openComments(place)

        assertEquals("exp_1", viewModel.selectedPlaceForComments.value?.id)
        assertEquals(1, viewModel.comments.value.size)
        assertEquals("Gran lugar", viewModel.comments.value[0].text)
    }

    @Test
    fun `closeComments limpia el lugar seleccionado y lista de comentarios`() = runTest {
        val place = fakeExplorePlace("exp_1", "Lugar 1")
        val viewModel = createViewModel()
        viewModel.openComments(place)

        viewModel.closeComments()

        assertNull(viewModel.selectedPlaceForComments.value)
        assertTrue(viewModel.comments.value.isEmpty())
    }
}

private class FakeExploreRepository : ExploreRepository {
    var publicPlacesList = listOf<ExplorePlace>()
    val commentsMap = mutableMapOf<String, List<Comment>>()

    override fun getPublicPlaces(currentUid: String?): Flow<List<ExplorePlace>> {
        return flowOf(publicPlacesList)
    }

    override suspend fun toggleLike(
        placeId: String,
        userId: String,
        isLiked: Boolean
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override fun getComments(placeId: String): Flow<List<Comment>> {
        return flowOf(commentsMap[placeId] ?: emptyList())
    }

    override suspend fun addComment(
        placeId: String,
        userId: String,
        userName: String,
        text: String
    ): Result<Unit> {
        val newComment = Comment(
            id = "c_${System.currentTimeMillis()}",
            userId = userId,
            userName = userName,
            text = text,
            createdAt = System.currentTimeMillis()
        )
        val list = commentsMap[placeId] ?: emptyList()
        commentsMap[placeId] = list + newComment
        return Result.success(Unit)
    }
}

private fun fakeExplorePlace(id: String, title: String) = ExplorePlace(
    id = id,
    userId = "owner_1",
    userName = "Propietario",
    title = title,
    note = "Nota",
    photoUrl = "",
    lat = 0.0,
    lng = 0.0,
    createdAt = System.currentTimeMillis(),
    likedBy = emptyList(),
    commentsCount = 0
)
