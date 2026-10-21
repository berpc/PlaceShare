package com.ramirezpenayo.placeshare.feature.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

enum class HomeTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Visits("visits", "Mis lugares", Icons.Filled.Home),
    Explore("explore", "Explorar", Icons.Filled.Search),
    Maps("maps", "Mi mapa", Icons.Filled.Place),
    Profile("profile", "Perfil", Icons.Filled.Person)
}
