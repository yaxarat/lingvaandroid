package dev.atajan.lingva_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.atajan.lingva_android.datastore.IS_DARK_THEME
import dev.atajan.lingva_android.datastore.dataStore
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel
import dev.atajan.lingva_android.ui.screens.TranslationScreen
import dev.atajan.lingva_android.ui.theme.LingvaAndroidTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val translateScreenViewModel: TranslateScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val appTheme = isSystemInDarkTheme().let {
                getAppTheme { it }.collectAsState(initial = it)
            }

            LingvaAndroidTheme(appTheme) {
                TranslationScreen(
                    viewModel = translateScreenViewModel,
                    toggleTheme = this::toggleAppTheme
                )
            }
        }
    }

    private fun getAppTheme(isSystemInDarkTheme: () -> Boolean): Flow<Boolean> {
        return applicationContext.dataStore.data
            .map { preferences ->
                preferences[IS_DARK_THEME] ?: isSystemInDarkTheme.invoke()
            }
    }

    private fun toggleAppTheme() {
        lifecycleScope.launch {
            applicationContext.dataStore.edit { preferences ->
                val currentTheme = preferences[IS_DARK_THEME] ?: false
                preferences[IS_DARK_THEME] = !currentTheme
            }
        }
    }
}
