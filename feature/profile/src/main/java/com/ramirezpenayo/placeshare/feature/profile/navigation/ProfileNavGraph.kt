package com.ramirezpenayo.placeshare.feature.profile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ramirezpenayo.placeshare.feature.profile.presentation.edit_profile.EditProfileRoute
import com.ramirezpenayo.placeshare.feature.profile.presentation.profile.ProfileRoute

@Composable
fun ProfileNavGraph(
    onNavigateToThemeSettings: () -> Unit,
    onSignOutSuccess: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = ProfileScreenRoute.Profile.route
    ) {
        // Pantalla principal del perfil (la que tiene los contadores y opciones estilo TikTok)
        composable(ProfileScreenRoute.Profile.route) {
            ProfileRoute(
                onNavigateToEditProfile = {
                    navController.navigate(ProfileScreenRoute.EditProfile.route)
                },
                onNavigateToThemeSettings = onNavigateToThemeSettings,
                onSignOutSuccess = onSignOutSuccess
            )
        }

        // Pantalla de editar perfil
        composable(ProfileScreenRoute.EditProfile.route) {
            EditProfileRoute(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}