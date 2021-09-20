package dev.atajan.lingva_android

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import dagger.hilt.android.AndroidEntryPoint
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel
import dev.atajan.lingva_android.ui.screens.TranslatenScreen
import dev.atajan.lingva_android.ui.theme.LingvaandroidTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val translateScreenViewModel: TranslateScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val window = window.apply {
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        setContent {
            val isDarkTheme = mutableStateOf(false)

            LingvaandroidTheme() {
                ThemedSystemUI(window) // Adjust system ui to match the app theme
                TranslatenScreen(
                    viewModel = translateScreenViewModel,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }

    @Composable
    private fun ThemedSystemUI(windows: Window) {
        windows.statusBarColor = MaterialTheme.colors.surface.toArgb()
        windows.navigationBarColor = MaterialTheme.colors.surface.toArgb()

        @Suppress("DEPRECATION")
        if (MaterialTheme.colors.surface.luminance() > 0.5f) {
            windows.decorView.systemUiVisibility =
                windows.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        @Suppress("DEPRECATION")
        if (MaterialTheme.colors.surface.luminance() > 0.5f) {
            windows.decorView.systemUiVisibility =
                windows.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }
}
