package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RoyalPurpleLight,
    onPrimary = Color(0xFF1E1035),
    primaryContainer = RoyalPurpleDark,
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = TrophyGoldLight,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = TrophyGoldDark,
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = PartyPinkLight,
    onTertiary = Color(0xFF4A0425),
    background = DarkBg,
    onBackground = Color(0xFFF3F0F8),
    surface = DarkSurface,
    onSurface = Color(0xFFF3F0F8),
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = Color(0xFFD4CEE8),
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF3B0764),
    secondary = TrophyGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = PartyPink,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = Color(0xFF1E1035),
    surface = LightSurface,
    onSurface = Color(0xFF1E1035),
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = Color(0xFF4C4368),
    outline = Color(0xFFE2DDF5)
)

@Composable
fun BekasaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our rich custom game palette by default
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
