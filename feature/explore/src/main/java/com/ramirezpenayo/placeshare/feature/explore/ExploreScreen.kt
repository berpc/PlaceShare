package com.ramirezpenayo.placeshare.feature.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramirezpenayo.placeshare.feature.explore.component.CommentsBottomSheet
import com.ramirezpenayo.placeshare.feature.explore.component.ExploreCard

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val places by viewModel.places.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    val selectedPlace by viewModel.selectedPlaceForComments.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isSendingComment by viewModel.isSendingComment.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Explorar", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            places.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Todavía no hay lugares públicos de otros usuarios.")
            }

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(places, key = { it.id }) { place ->
                    ExploreCard(
                        place = place,
                        liked = viewModel.isLiked(place),
                        onLike = { viewModel.toggleLike(place) },
                        onCommentClick = { viewModel.openComments(place) }
                    )
                }
            }
        }
    }

    selectedPlace?.let { place ->
        CommentsBottomSheet(
            placeTitle = place.title,
            comments = comments,
            isSending = isSendingComment,
            onDismiss = viewModel::closeComments,
            onSendComment = viewModel::sendComment
        )
    }
}
