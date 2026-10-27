package com.ramirezpenayo.placeshare.core.domain.repository

import com.ramirezpenayo.placeshare.core.domain.model.Comment
import com.ramirezpenayo.placeshare.core.domain.model.ExplorePlace
import kotlinx.coroutines.flow.Flow

interface ExploreRepository {
    fun getPublicPlaces(currentUid: String?): Flow<List<ExplorePlace>>
    suspend fun toggleLike(placeId: String, userId: String, isLiked: Boolean): Result<Unit>
    fun getComments(placeId: String): Flow<List<Comment>>
    suspend fun addComment(placeId: String, userId: String, userName: String, text: String): Result<Unit>
}
