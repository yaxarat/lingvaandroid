package dev.atajan.lingva_android

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import dev.atajan.lingva_android.common.ui.theme.LingvaAndroidTheme
import dev.atajan.lingva_android.common.ui.theme.ThemingOptions
import dev.atajan.lingva_android.common.ui.theme.canUseDynamicColor
import dev.atajan.lingva_android.common.ui.theme.selectedThemeFlow
import dev.atajan.lingva_android.quicktranslatefeature.screens.QuickTranslateScreen
import dev.atajan.lingva_android.quicktranslatefeature.screens.QuickTranslateScreenViewModel

@ExperimentalMaterialApi
@AndroidEntryPoint
class QuickTranslateActivity : ComponentActivity() {

    private val viewModel: QuickTranslateScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val selectedTheme: String? by selectedThemeFlow(applicationContext)
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
                QuickTranslateScreen(
                    textToTranslate = getTextToTranslate(),
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }


    private fun getTextToTranslate(): String {
        val textToQuickTranslate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }

        return (textToQuickTranslate ?: intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)) as String
    }
}