package dev.atajan.lingva_android.common.ui.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.R
import dev.atajan.lingva_android.common.domain.models.language.Language

@Composable
fun SelectDefaultLanguagesColumn(
    defaultSourceLanguage: String,
    defaultTargetLanguage: String,
    setDefaultSourceLanguage: (Language) -> Unit,
    setDefaultTargetLanguage: (Language) -> Unit,
    supportedLanguages: List<Language>,
    toggleErrorDialogState: (Boolean) -> Unit,
    context: Context
) {
    val sourceLanguagesPopUpShown = remember { mutableStateOf(false) }
    val targetLanguagesPopUpShown = remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = context.getString(R.string.default_language_source),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp,
                )
            )

            Text(
                text = defaultSourceLanguage.ifEmpty { context.getString(R.string.tap_to_select) },
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        if (supportedLanguages.isNotEmpty()) {
                            sourceLanguagesPopUpShown.value = true
                        } else {
                            toggleErrorDialogState(true)
                        }
                    }
            )
        }

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = context.getString(R.string.default_language_target),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp,
                )
            )

            Text(
                text = defaultTargetLanguage.ifEmpty { context.getString(R.string.tap_to_select) },
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        if (supportedLanguages.isNotEmpty()) {
                            targetLanguagesPopUpShown.value = true
                        } else {
                            toggleErrorDialogState(true)
                        }
                    }
            )
        }
    }

    LanguageListPopUp(
        openDialog = sourceLanguagesPopUpShown,
        languageList = supportedLanguages,
    ) { selectedLanguage: Language ->
        setDefaultSourceLanguage(selectedLanguage)
    }

    // Drop the first language, "Detect", since it won't make sense for target language
    LanguageListPopUp(
        openDialog = targetLanguagesPopUpShown,
        languageList = supportedLanguages.drop(1),
    ) { selectedLanguage: Language ->
        setDefaultTargetLanguage(selectedLanguage)
    }
}