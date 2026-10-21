package com.ramirezpenayo.placeshare.feature.profile.navigation

sealed class ProfileScreenRoute(val route: String) {
    data object Profile : ProfileScreenRoute("profile_main")
    data object EditProfile : ProfileScreenRoute("profile_edit")
}