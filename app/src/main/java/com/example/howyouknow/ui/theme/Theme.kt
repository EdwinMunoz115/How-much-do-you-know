package com.example.howyouknow.ui.theme

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
    primary = AquaDark,
    secondary = SlateBlueDark,
    tertiary = LimeSparkDark,
    background = DeepNavyDark,
    surface = DeepNavy,
    surfaceVariant = SlateBlue,
    onPrimary = DeepNavyDark,
    onSecondary = DeepNavyDark,
    onTertiary = DeepNavyDark,
    onBackground = Cloud,
    onSurface = Cloud
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    secondary = SlateBlue,
    tertiary = RoseGlow,
    background = Cloud,
    surface = Color.White,
    surfaceVariant = Mist,
    onPrimary = Cloud,
    onSecondary = Cloud,
    onTertiary = Cloud,
    onBackground = Ink,
    onSurface = Ink
)

@Composable
fun HowYouKnowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
