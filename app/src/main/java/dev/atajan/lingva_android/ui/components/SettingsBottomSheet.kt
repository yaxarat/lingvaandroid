package dev.atajan.lingva_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import dev.atajan.lingva_android.common.data.api.entities.LanguageEntity
import dev.atajan.lingva_android.ui.theme.ThemingOptions

@ExperimentalMaterialApi
@Composable
fun SettingsBottomSheet(
    modalBottomSheetState: ModalBottomSheetState,
    toggleTheme: (ThemingOptions) -> Unit,
    getCurrentTheme: () -> ThemingOptions,
    setDefaultSourceLanguage: (LanguageEntity) -> Unit,
    setDefaultTargetLanguage: (LanguageEntity) -> Unit,
    supportedLanguages: List<LanguageEntity>,
    defaultSourceLanguage: String,
    defaultTargetLanguage: String,
    toggleErrorDialogState: (Boolean) -> Unit,
) {

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            Column(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
            ) {
                AppThemeSelectionRadioButtonRows(
                    toggleTheme = toggleTheme,
                    getCurrentTheme = getCurrentTheme
                )

                SelectDefaultLanguagesColumn(
                    defaultSourceLanguage = defaultSourceLanguage,
                    defaultTargetLanguage = defaultTargetLanguage,
                    setDefaultSourceLanguage = setDefaultSourceLanguage,
                    setDefaultTargetLanguage = setDefaultTargetLanguage,
                    supportedLanguages = supportedLanguages,
                    toggleErrorDialogState = toggleErrorDialogState,
                )
            }
        }
    ) { }
}