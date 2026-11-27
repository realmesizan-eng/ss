package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmeraldPrimary,
    onPrimary = Color(0xFF003825),
    primaryContainer = DarkEmeraldContainer,
    onPrimaryContainer = Color(0xFFAAFFD9),
    secondary = Color(0xFFFFB59D),
    onSecondary = Color(0xFF561F0B),
    secondaryContainer = Color(0xFF773218),
    onSecondaryContainer = Color(0xFFFFDBD0),
    tertiary = Color(0xFFFFCC77),
    onTertiary = Color(0xFF472B00),
    tertiaryContainer = Color(0xFF643F00),
    onTertiaryContainer = Color(0xFFFFE8B6),
    background = DarkBackground,
    onBackground = Color(0xFFE5EDE9),
    surface = DarkSurface,
    onSurface = Color(0xFFE5EDE9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBBC9C2),
    outline = Color(0xFF4D6058)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = TerracottaSecondary,
    onSecondary = Color.White,
    secondaryContainer = TerracottaContainer,
    onSecondaryContainer = OnTerracottaContainer,
    tertiary = MarigoldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = MarigoldContainer,
    onTertiaryContainer = OnMarigoldContainer,
    background = WarmBackground,
    onBackground = Color(0xFF1A221E),
    surface = WarmSurface,
    onSurface = Color(0xFF1A221E),
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = Color(0xFF4F5954),
    outline = WarmOutline
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PathshalaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
