package dev.atajan.lingva_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import dev.atajan.lingva_android.datastore.APP_THEME
import dev.atajan.lingva_android.datastore.dataStore
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel
import dev.atajan.lingva_android.ui.screens.TranslationScreen
import dev.atajan.lingva_android.ui.theme.LingvaAndroidTheme
import dev.atajan.lingva_android.ui.theme.ThemingOptions
import dev.atajan.lingva_android.ui.theme.canUseDynamicColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ExperimentalMaterialApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val translateScreenViewModel: TranslateScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val selectedTheme: String? by checkIfThemeAlreadySet()
                .collectAsState(initial = null)

            val appTheme: ThemingOptions = if (selectedTheme.isNullOrBlank()) {
                if (canUseDynamicColor) {
                    ThemingOptions.YOU
                } else if (isSystemInDarkTheme()) {
                    ThemingOptions.DARK
                } else {
                    ThemingOptions.LIGHT
                }
            } else {
                ThemingOptions.valueOf(selectedTheme!!)
            }

            LingvaAndroidTheme(appTheme = appTheme) {
                TranslationScreen(
                    viewModel = translateScreenViewModel,
                    getCurrentTheme = { appTheme }
                )
            }
        }
    }

    private fun checkIfThemeAlreadySet(): Flow<String?> {
        return applicationContext.dataStore.data
            .map { preferences ->
                preferences[APP_THEME]
            }
    }
}
