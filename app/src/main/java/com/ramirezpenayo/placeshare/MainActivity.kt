package com.ramirezpenayo.placeshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ramirezpenayo.placeshare.core.ui.theme.MyApplicationTheme
import com.ramirezpenayo.placeshare.feature.auth.AuthRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AuthRoute(
                    onAuthSuccess = {
                        // TODO: Aquí luego implementaremos la navegación hacia el Home
                    }
                )
            }
        }
    }
}