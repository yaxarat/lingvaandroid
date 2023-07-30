package dev.atajan.lingva_android.common.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    currentCustomLingvaServerUrl: String,
    liveTranslateEnabled: Boolean,
    onToggleLiveTranslate: (Boolean) -> Unit,
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            bottom = 16.dp,
                        )
                        .fillMaxWidth()
                ) {
                    Text(
                        text = context.getString(R.string.toggle_live_translate),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )

                    Switch(
                        modifier = Modifier.semantics { contentDescription = "toggle live translate" },
                        checked = liveTranslateEnabled,
                        onCheckedChange = {
                            onToggleLiveTranslate(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                        )
                    )
                }

                BottomSheetSectionHeader(context.getString(R.string.advanced_settings_title))
                SettingsBottomSheetOutlinedTextField(
                    label = context.getString(R.string.custom_lingva_instance_address),
                    currentTextFieldValue = customLingvaServerUrl,
                    hint = currentCustomLingvaServerUrl,
                    onValueChange = { customLingvaServerUrl.value = it },
                )
            }
        }
    ) { }
}