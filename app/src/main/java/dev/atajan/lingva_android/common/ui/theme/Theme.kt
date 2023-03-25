package dev.atajan.lingva_android.common.ui.theme

import android.content.Context
import android.content.res.Configuration
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController

val canUseDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

fun isSystemInNightMode(context: Context): Boolean {
    return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        Configuration.UI_MODE_NIGHT_NO -> false
        else -> false
    }
}

enum class ThemingOptions {
    LIGHT, DARK, YOU
}

private val darkColorScheme = darkColorScheme(
    primary = LingvaGreen,
    primaryContainer = LingvaGreenLighter,
    secondary = LingvaDarkGray,
    secondaryContainer = AccentMagenta,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    background = Color.Black,
    onBackground = Color.White,
)

private val lightColorScheme = lightColorScheme(
    primary = LingvaGreenDarker,
    primaryContainer = LingvaGreenDarker,
    secondary = LingvaGray,
    secondaryContainer = AccentMagenta,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
)

@Composable
fun LingvaAndroidTheme(
    appTheme: ThemingOptions,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()

    val colorScheme = when (appTheme) {
        ThemingOptions.LIGHT -> lightColorScheme
        ThemingOptions.DARK -> darkColorScheme
        ThemingOptions.YOU -> {
            if (isSystemInDarkTheme()) dynamicDarkColorScheme(LocalContext.current) else dynamicLightColorScheme(LocalContext.current)
        }
    }

    systemUiController.setSystemBarsColor(
        color = colorScheme.background
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = material3Typography,
        content = content
    )
}
