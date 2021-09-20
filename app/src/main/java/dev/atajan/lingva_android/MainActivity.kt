package dev.atajan.lingva_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dagger.hilt.android.AndroidEntryPoint
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel
import dev.atajan.lingva_android.ui.screens.TranslatenScreen
import dev.atajan.lingva_android.ui.theme.LingvaandroidTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val translateScreenViewModel: TranslateScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkTheme = remember { mutableStateOf(false) }

            LingvaandroidTheme(darkTheme = isDarkTheme.value) {
                TranslatenScreen(
                    viewModel = translateScreenViewModel,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}
