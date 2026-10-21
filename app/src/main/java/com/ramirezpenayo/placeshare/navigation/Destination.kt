package com.ramirezpenayo.placeshare.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Destination("home", "Home", Icons.Default.Home)
    object Map : Destination("map", "Map", Icons.Default.LocationOn)
    object Add : Destination("add", "Add", Icons.Default.Add)
    object Explore : Destination("explore", "Explore", Icons.Default.Search)
    object Profile : Destination("profile", "Profile", Icons.Default.Person)

    companion object {
        val bottomBarItems = listOf(Home, Map, Add, Explore, Profile)
    }
}