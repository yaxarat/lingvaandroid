package dev.atajan.lingva_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.atajan.lingva_android.datastore.IS_DARK_THEME
import dev.atajan.lingva_android.datastore.dataStore
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel
import dev.atajan.lingva_android.ui.screens.TranslatenScreen
import dev.atajan.lingva_android.ui.theme.LingvaandroidTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val translateScreenViewModel: TranslateScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkTheme = applicationContext.dataStore.data
                .map { preferences ->
                    preferences[IS_DARK_THEME] ?: false
                }
                .collectAsState(false)

            LingvaandroidTheme(darkTheme = isDarkTheme.value) {
                TranslatenScreen(
                    viewModel = translateScreenViewModel,
                    isDarkTheme = isDarkTheme,
                    toggleTheme = this::toggleTheme
                )
            }
        }
    }

    private fun toggleTheme() {
        lifecycleScope.launch {
            applicationContext.dataStore.edit { preferences ->
                val currentTheme = preferences[IS_DARK_THEME] ?: false
                preferences[IS_DARK_THEME] = !currentTheme
            }
        }
    }
}
