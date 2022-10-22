package dev.atajan.lingva_android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.R
import dev.atajan.lingva_android.ui.components.ErrorNotificationDialog
import dev.atajan.lingva_android.ui.components.LanguageSelectionBar
import dev.atajan.lingva_android.ui.components.SettingsBottomSheet
import dev.atajan.lingva_android.ui.components.TitleBar
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.ClearInputField
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.CopyTextToClipboard
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.DefaultSourceLanguageSelected
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.DefaultTargetLanguageSelected
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.OnTextToTranslateChange
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.SetNewSourceLanguage
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.SetNewTargetLanguage
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.ShowErrorDialog
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.ToggleAppTheme
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.Translate
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.TrySwapLanguages
import dev.atajan.lingva_android.ui.theme.ThemingOptions
import dev.atajan.lingva_android.ui.theme.mediumRoundedCornerShape
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun TranslationScreen(
    viewModel: TranslateScreenViewModel,
    getCurrentTheme: () -> ThemingOptions
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState(0)
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = { it != ModalBottomSheetValue.HalfExpanded }
    )
    val translationScreenState by viewModel.states.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TitleBar(
            title = context.getString(R.string.app_name),
            onEndIconTap = {
                scope.launch {
                    if (modalBottomSheetState.isVisible) {
                        modalBottomSheetState.hide()
                    } else {
                        modalBottomSheetState.show()
                    }
                }
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .padding(all = 16.dp)
        ) {
            OutlinedTextField(
                value = translationScreenState.textToTranslate,
                onValueChange = { newValue: String ->
                    viewModel.send(OnTextToTranslateChange(newValue))
                },
                label = {
                    Text(
                        text = context.getString(R.string.source_text),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxSize(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                textStyle = MaterialTheme.typography.titleMedium,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.disabled),
                    focusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.disabled),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onBackground,
                )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                AnimatedVisibility(translationScreenState.textToTranslate.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                softwareKeyboardController?.hide()
                                viewModel.send(ClearInputField)
                            },
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = context.getString(R.string.delete_icon_ax),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        IconButton(
                            onClick = {
                                softwareKeyboardController?.hide()
                                viewModel.send(Translate)
                            },
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Translate,
                                contentDescription = context.getString(R.string.translate_icon_ax),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(translationScreenState.translatedText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
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
                            translationScreenState.translatedText,
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

        LanguageSelectionBar(
            modifier = Modifier.padding(all = 16.dp),
            supportedLanguages = translationScreenState.supportedLanguages,
            sourceLanguage = translationScreenState.sourceLanguage,
            targetLanguage = translationScreenState.targetLanguage,
            toggleErrorDialogState = {
                viewModel.send(ShowErrorDialog(it))
            },
            middleIcon = Icons.Rounded.SwapHoriz,
            onMiddleIconTap = { viewModel.send(TrySwapLanguages) },
            onNewSourceLanguageSelected = { viewModel.send(SetNewSourceLanguage(it)) },
            onNewTargetLanguageSelected = { viewModel.send(SetNewTargetLanguage(it)) }
        )
    }

    SettingsBottomSheet(
        modalBottomSheetState = modalBottomSheetState,
        toggleTheme = {
            viewModel.send(ToggleAppTheme(it))
        },
        getCurrentTheme = getCurrentTheme,
        setDefaultSourceLanguage = {
            viewModel.send(DefaultSourceLanguageSelected(it))
        },
        setDefaultTargetLanguage = {
            viewModel.send(DefaultTargetLanguageSelected(it))
        },
        supportedLanguages = translationScreenState.supportedLanguages,
        defaultSourceLanguage = translationScreenState.defaultSourceLanguage,
        defaultTargetLanguage = translationScreenState.defaultTargetLanguage,
        toggleErrorDialogState = {
            viewModel.send(ShowErrorDialog(it))
        }
    )

    ErrorNotificationDialog(translationScreenState.errorDialogState) {
        viewModel.send(ShowErrorDialog(false))
    }
}
