package com.ramirezpenayo.placeshare.feature.home.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ramirezpenayo.placeshare.core.domain.model.Place

@Composable
fun PlaceCard(place: Place, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (place.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = place.photoUrl,
                    contentDescription = place.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = place.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (place.note.isNotBlank()) {
                    Text(
                        text = place.note,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    text = formatDate(place.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
