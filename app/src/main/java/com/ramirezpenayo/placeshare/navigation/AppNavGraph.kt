package com.ramirezpenayo.placeshare.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.ramirezpenayo.placeshare.feature.auth.AuthRoute
import com.ramirezpenayo.placeshare.feature.home.HomeScreen

object Rutas {
    const val AUTH = "auth"
    const val HOME = "home"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val inicio = if (FirebaseAuth.getInstance().currentUser != null) {
        Rutas.HOME
    } else {
        Rutas.AUTH
    }

    NavHost(navController = navController, startDestination = inicio) {
        composable(Rutas.AUTH) {
            AuthRoute(
                onAuthSuccess = {
                    navController.navigate(Rutas.HOME) {
                        popUpTo(Rutas.AUTH) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.HOME) {
            HomeScreen(
                onNavigateToThemeSettings = {
                    // Navegar a la pantalla de configuración de tema
                },
                onSignOutSuccess = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Rutas.AUTH) {
                        popUpTo(Rutas.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
