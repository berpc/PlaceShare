package com.ramirezpenayo.placeshare.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.ramirezpenayo.placeshare.feature.auth.AuthRoute
import com.ramirezpenayo.placeshare.feature.home.HomeScreen
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ramirezpenayo.placeshare.feature.home.PlaceDetailScreen
import com.ramirezpenayo.placeshare.feature.places.AddPlaceScreen

object Rutas {
    const val AUTH = "auth"
    const val MAIN = "main"
}

@Composable
fun AppNavGraph() {
    val rootNavController = rememberNavController()
    val inicio = if (FirebaseAuth.getInstance().currentUser != null) {
        Rutas.MAIN
    } else {
        Rutas.AUTH
    }

    NavHost(navController = rootNavController, startDestination = inicio) {
        composable(Rutas.AUTH) {
            AuthRoute(
                onAuthSuccess = {
                    rootNavController.navigate(Rutas.MAIN) {
                        popUpTo(Rutas.AUTH) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.MAIN) {
            MainScaffold()
        }
    }
}

@Composable
private fun MainScaffold() {
    val tabNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val entry by tabNavController.currentBackStackEntryAsState()
                val currentRoute = entry?.destination?.route

                Destination.bottomBarItems.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            tabNavController.navigate(destination.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.title) },
                        label = { Text(destination.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(

            navController = tabNavController,
            //startDestination = Destination.Home.route,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(innerPadding)

        ) {
            composable(Destination.Home.route) {
                HomeScreen(
                    onPlaceClick = { id -> tabNavController.navigate("place/$id") }
                )
            }
            composable(
                route = "place/{placeId}",
                arguments = listOf(navArgument("placeId") { type = NavType.StringType })
            ) { entry ->
                PlaceDetailScreen(
                    placeId = entry.arguments?.getString("placeId") ?: "",
                    onBack = { tabNavController.popBackStack() }
                )
            }
            composable(Destination.Add.route) {
                AddPlaceScreen(
                    onSaved = {
                        tabNavController.navigate(Destination.Home.route) {
                            popUpTo(tabNavController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Destination.Map.route) { Text("Mapa (próximamente)") }
            composable(Destination.Explore.route) { Text("Explora (próximamente)") }
            composable(Destination.Profile.route) { Text("Perfil (próximamente)") }
        }
    }
}