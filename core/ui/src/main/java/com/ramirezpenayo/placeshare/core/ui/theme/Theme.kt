package com.ramirezpenayo.placeshare.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ramirezpenayo.placeshare.core.ui.theme.DarkPrimary
import com.ramirezpenayo.placeshare.core.ui.theme.WhitePrimary

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkLight,
    tertiary = BluePrimary,
    onSurfaceVariant = WhitePrimary,
    onPrimary = WhitePrimary,
    onSecondary = DarkPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = WhitePrimary,
    secondary = WhiteLight,
    tertiary = BluePrimary,
    onSurfaceVariant = DarkPrimary,
    onPrimary = WhitePrimary,
    onSecondary = DarkPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}