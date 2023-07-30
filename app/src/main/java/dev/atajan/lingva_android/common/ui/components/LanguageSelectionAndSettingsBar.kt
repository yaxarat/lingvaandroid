package dev.atajan.lingva_android.common.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.R
import dev.atajan.lingva_android.common.domain.models.language.Language

@Composable
fun LanguageSelectionAndSettingsBar(
    supportedLanguages: List<Language>,
    sourceLanguage: Language,
    targetLanguage: Language,
    toggleErrorDialogState: (Boolean) -> Unit,
    onNewSourceLanguageSelected: (Language) -> Unit,
    onNewTargetLanguageSelected: (Language) -> Unit,
    middleIcon: ImageVector,
    onMiddleIconTap: () -> Unit,
    onEndIconTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .height(50.dp)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LanguageSelectionBar(
            supportedLanguages = supportedLanguages,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            toggleErrorDialogState = toggleErrorDialogState,
            onNewSourceLanguageSelected = onNewSourceLanguageSelected,
            onNewTargetLanguageSelected = onNewTargetLanguageSelected,
            middleIcon = middleIcon,
            onMiddleIconTap = onMiddleIconTap,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.85f)
        )

        IconButton(
            onClick = onEndIconTap,
            modifier = Modifier.fillMaxHeight()
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = context.getString(R.string.setting_icon_ax),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}