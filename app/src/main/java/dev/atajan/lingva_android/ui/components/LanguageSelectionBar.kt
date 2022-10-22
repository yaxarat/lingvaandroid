package dev.atajan.lingva_android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.R
import dev.atajan.lingva_android.api.entities.LanguageEntity

@Composable
fun LanguageSelectionBar(
    supportedLanguages: List<LanguageEntity>,
    sourceLanguage: LanguageEntity,
    targetLanguage: LanguageEntity,
    toggleErrorDialogState: (Boolean) -> Unit,
    onNewSourceLanguageSelected: (LanguageEntity) -> Unit,
    onNewTargetLanguageSelected: (LanguageEntity) -> Unit,
    middleIcon: ImageVector,
    onMiddleIconTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sourceLanguagesPopUpShown = remember { mutableStateOf(false) }
    val targetLanguagesPopUpShown = remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .height(50.dp)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(130.dp)
                .clickable {
                    if (supportedLanguages.isNotEmpty()) {
                        sourceLanguagesPopUpShown.value = true
                    } else {
                        toggleErrorDialogState(true)
                    }
                },
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondary
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (sourceLanguage.name == "Detect") {
                        context.getString(R.string.detect_language)
                    } else {
                        sourceLanguage.name
                    },
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
        }

        IconButton(
            onClick = onMiddleIconTap,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.25f)
        ) {
            Icon(
                imageVector = middleIcon,
                contentDescription = context.getString(R.string.swap_icon_ax),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxSize()
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(130.dp)
                .clickable {
                    if (supportedLanguages.isNotEmpty()) {
                        targetLanguagesPopUpShown.value = true
                    } else {
                        toggleErrorDialogState(true)
                    }
                },
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondary
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = targetLanguage.name,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
        }
    }

    LanguageListPopUp(
        openDialog = sourceLanguagesPopUpShown,
        languageList = supportedLanguages
    ) { onNewSourceLanguageSelected(it) }

    // Drop the first language, "Detect", since it won't make sense for target language
    LanguageListPopUp(
        openDialog = targetLanguagesPopUpShown,
        languageList = supportedLanguages.drop(1)
    ) { onNewTargetLanguageSelected(it) }
}
