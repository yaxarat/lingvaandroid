package dev.atajan.lingva_android.common.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.R
import dev.atajan.lingva_android.common.domain.models.language.Language
import dev.atajan.lingva_android.common.ui.theme.ThemingOptions

@ExperimentalMaterialApi
@Composable
fun SettingsBottomSheet(
    modalBottomSheetState: ModalBottomSheetState,
    toggleTheme: (ThemingOptions) -> Unit,
    currentTheme: MutableState<ThemingOptions>,
    setDefaultSourceLanguage: (Language) -> Unit,
    setDefaultTargetLanguage: (Language) -> Unit,
    supportedLanguages: List<Language>,
    defaultSourceLanguage: String,
    defaultTargetLanguage: String,
    toggleErrorDialogState: (Boolean) -> Unit,
    customLingvaServerUrl: MutableState<String>,
    context: Context,
) {
    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            Column(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
            ) {
                BottomSheetSectionHeader(context.getString(R.string.app_theme_setting_title))
                AppThemeSelectionRadioButtonRows(
                    toggleTheme = toggleTheme,
                    currentTheme = currentTheme,
                    context = context
                )

                BottomSheetSectionHeader(context.getString(R.string.default_languages_title))
                SelectDefaultLanguagesColumn(
                    defaultSourceLanguage = defaultSourceLanguage,
                    defaultTargetLanguage = defaultTargetLanguage,
                    setDefaultSourceLanguage = setDefaultSourceLanguage,
                    setDefaultTargetLanguage = setDefaultTargetLanguage,
                    supportedLanguages = supportedLanguages,
                    toggleErrorDialogState = toggleErrorDialogState,
                    context = context
                )

                BottomSheetSectionHeader(context.getString(R.string.advanced_settings_title))
                SettingsBottomSheetOutlinedTextField(
                    label = context.getString(R.string.custom_lingva_instance_address),
                    currentTextFieldValue = customLingvaServerUrl,
                    onValueChange = { customLingvaServerUrl.value = it },
                )
            }
        }
    ) { }
}