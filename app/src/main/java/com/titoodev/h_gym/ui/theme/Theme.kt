package com.titoodev.h_gym.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeVariant {
    PINK, BLUE, WHITE, BLACK
}

private val PinkLightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = Color.White,
    primaryContainer = PinkContainerLight,
    onPrimaryContainer = PinkPrimary,
    background = PinkBackgroundLight,
    surface = PinkSurfaceLight,
    onBackground = Color(0xFF2B0A12),
    onSurface = Color(0xFF2B0A12),
    outline = Color(0xFFFFB3C6)
)

private val PinkDarkColorScheme = darkColorScheme(
    primary = PinkPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = PinkContainerDark,
    onPrimaryContainer = PinkPrimaryDark,
    background = PinkBackgroundDark,
    surface = PinkSurfaceDark,
    onBackground = Color(0xFFFFEBF0),
    onSurface = Color(0xFFFFEBF0),
    outline = Color(0xFF80002A)
)

private val BlueLightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueContainerLight,
    onPrimaryContainer = BluePrimary,
    background = BlueBackgroundLight,
    surface = BlueSurfaceLight,
    onBackground = Color(0xFF041428),
    onSurface = Color(0xFF041428),
    outline = Color(0xFF99C2FF)
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = Color.White,
    primaryContainer = BlueContainerDark,
    onPrimaryContainer = BluePrimaryDark,
    background = BlueBackgroundDark,
    surface = BlueSurfaceDark,
    onBackground = Color(0xFFEBF3FF),
    onSurface = Color(0xFFEBF3FF),
    outline = Color(0xFF004B99)
)

private val GreenLightColorScheme = lightColorScheme(
    primary = Color(0xFF649900),
    onPrimary = Color.White,
    primaryContainer = GreenContainerLight,
    onPrimaryContainer = Color(0xFF2D4700),
    background = GreenBackgroundLight,
    surface = GreenSurfaceLight,
    onBackground = Color(0xFF101B00),
    onSurface = Color(0xFF101B00),
    outline = Color(0xFFA3E600)
)

private val GreenDarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.Black,
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = GreenPrimaryDark,
    background = GreenBackgroundDark,
    surface = GreenSurfaceDark,
    onBackground = Color(0xFFF4FFE0),
    onSurface = Color(0xFFF4FFE0),
    outline = Color(0xFF4A6600)
)

private val WhiteLightColorScheme = lightColorScheme(
    primary = WhitePrimary,
    onPrimary = Color.White,
    primaryContainer = WhiteContainerLight,
    onPrimaryContainer = WhitePrimary,
    background = WhiteBackgroundLight,
    surface = WhiteSurfaceLight,
    onBackground = Color(0xFF1C1C1E),
    onSurface = Color(0xFF1C1C1E),
    outline = Color(0xFFD1D1D6)
)

private val WhiteDarkColorScheme = darkColorScheme(
    primary = WhitePrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = WhiteContainerDark,
    onPrimaryContainer = WhitePrimaryDark,
    background = WhiteBackgroundDark,
    surface = WhiteSurfaceDark,
    onBackground = Color(0xFFF2F2F7),
    onSurface = Color(0xFFF2F2F7),
    outline = Color(0xFF48484A)
)

@Composable
fun HGYMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeVariant: ThemeVariant = ThemeVariant.PINK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeVariant) {
        ThemeVariant.PINK -> if (darkTheme) PinkDarkColorScheme else PinkLightColorScheme
        ThemeVariant.BLUE -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
        ThemeVariant.BLACK -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
        ThemeVariant.WHITE -> if (darkTheme) WhiteDarkColorScheme else WhiteLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
