package com.ramirezpenayo.placeshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramirezpenayo.placeshare.core.ui.theme.MyApplicationTheme
import com.ramirezpenayo.placeshare.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 1. Inyectamos un ViewModel global (o puedes crear un MainViewModel para esto)
            // Si prefieres, puedes exponer el estado del usuario actual aquí.
            val viewModel: MainViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            // 2. Extraemos la preferencia de Firestore del usuario (por defecto "SYSTEM")
            val themePreference = uiState.user?.themeColor?.uppercase() ?: "SYSTEM"

            // 3. Traducimos el String a un booleano para tu MyApplicationTheme
            val systemInDark = isSystemInDarkTheme()
            val darkTheme = when (themePreference) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemInDark // Si es "SYSTEM", usa la configuración del teléfono
            }

            // 4. Se lo pasamos a tu tema existente
            MyApplicationTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}