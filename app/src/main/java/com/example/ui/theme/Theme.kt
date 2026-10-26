package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

@Composable
fun QuantiaTheme(
    preset: ExpressiveThemePreset = ExpressiveThemePreset.SAGE_LEAFIT,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> when (preset) {
            ExpressiveThemePreset.SAGE_LEAFIT -> if (darkTheme) SageLeafitDarkScheme else SageLeafitLightScheme
            ExpressiveThemePreset.VIOLET_PLAYGROUND -> if (darkTheme) VioletPlaygroundDarkScheme else VioletPlaygroundLightScheme
            ExpressiveThemePreset.WARM_TERRACOTTA -> if (darkTheme) WarmTerracottaDarkScheme else WarmTerracottaLightScheme
            ExpressiveThemePreset.DEEP_MINT -> if (darkTheme) DeepMintDarkScheme else DeepMintLightScheme
        }
    }

    val expressivePalette = when (preset) {
        ExpressiveThemePreset.SAGE_LEAFIT -> if (darkTheme) SageLeafitDarkExpressive else SageLeafitLightExpressive
        ExpressiveThemePreset.VIOLET_PLAYGROUND -> if (darkTheme) VioletPlaygroundDarkExpressive else VioletPlaygroundLightExpressive
        ExpressiveThemePreset.WARM_TERRACOTTA -> if (darkTheme) WarmTerracottaDarkExpressive else WarmTerracottaLightExpressive
        ExpressiveThemePreset.DEEP_MINT -> if (darkTheme) DeepMintDarkExpressive else DeepMintLightExpressive
    }

    CompositionLocalProvider(LocalExpressivePalette provides expressivePalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = QuantiaTypography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    QuantiaTheme(
        preset = ExpressiveThemePreset.SAGE_LEAFIT,
        themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
        useDynamicColor = false,
        content = content
    )
}
