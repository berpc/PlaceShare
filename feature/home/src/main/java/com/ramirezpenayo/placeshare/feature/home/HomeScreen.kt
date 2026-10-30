package com.ramirezpenayo.placeshare.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ramirezpenayo.placeshare.feature.explore.ExploreScreen
import com.ramirezpenayo.placeshare.feature.home.component.PlaceCard
import com.ramirezpenayo.placeshare.feature.notifications.NotificationsScreen
import com.ramirezpenayo.placeshare.feature.notifications.NotificationsViewModel
import com.ramirezpenayo.placeshare.feature.places.AddPlaceScreen
import com.ramirezpenayo.placeshare.feature.profile.navigation.ProfileNavGraph

@Composable
fun HomeScreen(
    onNavigateToThemeSettings: () -> Unit = {},
    onSignOutSuccess: () -> Unit = {}
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = rutaActual == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = HomeTab.Visits.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(HomeTab.Visits.route) {
                VisitsNavHost()
            }
            composable(HomeTab.Maps.route) {
                MapScreen()
            }
            composable(HomeTab.Explore.route) {
                ExploreScreen()
            }
            composable(HomeTab.Profile.route) {
                ProfileNavGraph(
                    onNavigateToThemeSettings = onNavigateToThemeSettings,
                    onSignOutSuccess = onSignOutSuccess
                )
            }
        }
    }
}

@Composable
private fun VisitsNavHost() {
    val visitsNavController = rememberNavController()

    NavHost(
        navController = visitsNavController,
        startDestination = "places_list"
    ) {
        composable("places_list") {
            PlacesListScreen(
                onPlaceClick = { id ->
                    visitsNavController.navigate("place/$id")
                },
                onAddPlaceClick = {
                    visitsNavController.navigate("add_place")
                },
                onNotificationsClick = {
                    visitsNavController.navigate("notifications")
                }
            )
        }
        composable("notifications") {
            NotificationsScreen(
                onBackClick = { visitsNavController.popBackStack() }
            )
        }
        composable(
            route = "place/{placeId}",
            arguments = listOf(navArgument("placeId") { type = NavType.StringType })
        ) { entry ->
            PlaceDetailScreen(
                placeId = entry.arguments?.getString("placeId") ?: "",
                onBack = { visitsNavController.popBackStack() }
            )
        }
        composable("add_place") {
            AddPlaceScreen(
                onSaved = { visitsNavController.popBackStack() },
                onBackClick = { visitsNavController.popBackStack() }
            )
        }
    }
}

@Composable
fun PlacesListScreen(
    onPlaceClick: (String) -> Unit,
    onAddPlaceClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel()
) {
    val places by viewModel.places.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val unreadCount by notificationsViewModel.unreadCount.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlaceClick) {
                Icon(Icons.Default.Add, contentDescription = "Agregar lugar")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mis lugares",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "${places.size} guardados",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                }

                IconButton(onClick = onNotificationsClick) {
                    if (unreadCount > 0) {
                        BadgedBox(
                            badge = { Badge { Text("$unreadCount") } }
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notificaciones"
                        )
                    }
                }
            }

            when {
                loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                places.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Todavía no guardaste lugares.\nTocá + para agregar el primero.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(places, key = { it.id }) { place ->
                        PlaceCard(place = place, onClick = { onPlaceClick(place.id) })
                    }
                }
            }
        }
    }
}
