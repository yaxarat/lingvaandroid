package dev.atajan.lingva_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.atajan.lingva_android.translatefeature.screens.TranslateScreenViewModel
import dev.atajan.lingva_android.translatefeature.screens.TranslationScreen
import dev.atajan.lingva_android.common.ui.theme.LingvaAndroidTheme
import dev.atajan.lingva_android.common.ui.theme.ThemingOptions
import dev.atajan.lingva_android.common.ui.theme.canUseDynamicColor
import dev.atajan.lingva_android.common.ui.theme.isSystemInNightMode
import dev.atajan.lingva_android.common.ui.theme.selectedThemeFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalMaterialApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var theme: MutableState<ThemingOptions> = mutableStateOf(
        if (canUseDynamicColor) {
            ThemingOptions.YOU
        } else if (isSystemInNightMode(this)) {
            ThemingOptions.DARK
        } else {
            ThemingOptions.LIGHT
        }
    )

    private val viewModel: TranslateScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectedThemeFlow(applicationContext)
            .onEach {
                it?.let { theme.value = ThemingOptions.valueOf(it) }
            }
            .launchIn(lifecycleScope)

        setContent {
            isSystemInDarkTheme()
            LingvaAndroidTheme(appTheme = theme.value) {
                TranslationScreen(
                    viewModel = viewModel,
                    currentTheme = theme
                )
            }
        }
    }
}
