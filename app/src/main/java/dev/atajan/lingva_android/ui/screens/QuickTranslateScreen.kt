package dev.atajan.lingva_android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowRightAlt
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.R
import dev.atajan.lingva_android.ui.components.ErrorNotificationDialog
import dev.atajan.lingva_android.ui.components.LanguageSelectionBar
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.CopyTextToClipboard
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.OnTextToTranslateChange
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.SetNewSourceLanguage
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.SetNewTargetLanguage
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.ShowErrorDialog
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.Translate
import dev.atajan.lingva_android.ui.theme.mediumRoundedCornerShape

@ExperimentalMaterialApi
@Composable
fun QuickTranslateScreen(
    textToTranslate: String,
    viewModel: QuickTranslateScreenViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState(0)
    val quickTranslateScreenState by viewModel.states.collectAsState()

    viewModel.send(OnTextToTranslateChange(textToTranslate))
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LanguageSelectionBar(
            modifier = Modifier.padding(all = 16.dp),
            supportedLanguages = quickTranslateScreenState.supportedLanguages,
            sourceLanguage = quickTranslateScreenState.sourceLanguage,
            targetLanguage = quickTranslateScreenState.targetLanguage,
            toggleErrorDialogState = {
                viewModel.send(ShowErrorDialog(it))
            },
            middleIcon = Icons.Rounded.ArrowRightAlt,
            onMiddleIconTap = { 
                // No action necessary for this user flow
            },
            onNewSourceLanguageSelected = {
                viewModel.send(SetNewSourceLanguage(it))
                viewModel.send(Translate)
            },
            onNewTargetLanguageSelected = {
                viewModel.send(SetNewTargetLanguage(it))
                viewModel.send(Translate)
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.40f)
                .padding(all = 16.dp)
        ) {
            OutlinedTextField(
                value = quickTranslateScreenState.textToTranslate,
                onValueChange = { },
                label = {
                    Text(
                        text = context.getString(R.string.source_text),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.titleMedium,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
                    focusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onBackground,
                ),
                readOnly = true
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .padding(all = 16.dp)
        ) {
            Card(
                shape = mediumRoundedCornerShape,
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                ),
                elevation = 0.dp,
                backgroundColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxSize(),
            ) {
                SelectionContainer {
                    Text(
                        quickTranslateScreenState.translatedText,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                IconButton(
                    onClick = {
                        viewModel.send(CopyTextToClipboard)
                    },
                    modifier = Modifier
                        .padding(bottom = 16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = context.getString(R.string.copy_icon_ax),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }

    ErrorNotificationDialog(quickTranslateScreenState.errorDialogState) {
        viewModel.send(ShowErrorDialog(false))
    }
}