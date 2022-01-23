package dev.atajan.lingva_android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.api.entities.LanguageEntity

@Composable
fun SelectDefaultLanguagesColumn(
    defaultSourceLanguage: MutableState<String>,
    defaultTargetLanguage: MutableState<String>,
    setDefaultSourceLanguage: (LanguageEntity) -> Unit,
    setDefaultTargetLanguage: (LanguageEntity) -> Unit,
    supportedLanguages: MutableState<List<LanguageEntity>>,
    toggleErrorDialogState: (Boolean) -> Unit,
) {
    val sourceLanguagesPopUpShown = remember { mutableStateOf(false) }
    val targetLanguagesPopUpShown = remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Default Source and Target Languages",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Source Language:",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp,
                )
            )

            Text(
                text = defaultSourceLanguage.value.ifEmpty { "Tap here to select" },
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        if (supportedLanguages.value.isNotEmpty()) {
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
                text = "Target Language:",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 16.dp,
                    bottom = 32.dp,
                )
            )

            Text(
                text = defaultTargetLanguage.value.ifEmpty { "Tap here to select" },
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 32.dp,
                    )
                    .clickable {
                        if (supportedLanguages.value.isNotEmpty()) {
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
        languageList = supportedLanguages.value,
    ) { selectedLanguage: LanguageEntity ->
        setDefaultSourceLanguage(selectedLanguage)
    }

    // Drop the first language, "Detect", since it won't make sense for target language
    LanguageListPopUp(
        openDialog = targetLanguagesPopUpShown,
        languageList = supportedLanguages.value.drop(1),
    ) { selectedLanguage: LanguageEntity ->
        setDefaultTargetLanguage(selectedLanguage)
    }
}