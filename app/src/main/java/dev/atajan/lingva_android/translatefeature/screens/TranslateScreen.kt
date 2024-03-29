package dev.atajan.lingva_android.translatefeature.screens

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
import androidx.compose.material.icons.rounded.SpeakerNotes
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.R
import dev.atajan.lingva_android.common.ui.components.ErrorNotificationDialog
import dev.atajan.lingva_android.common.ui.components.LanguageSelectionAndSettingsBar
import dev.atajan.lingva_android.common.ui.components.SettingsBottomSheet
import dev.atajan.lingva_android.common.ui.theme.ThemingOptions
import dev.atajan.lingva_android.common.ui.theme.mediumRoundedCornerShape
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.ClearInputField
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.CopyTextToClipboard
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.DefaultSourceLanguageSelected
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.DefaultTargetLanguageSelected
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.DisplayPronunciation
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.ReadTextOutLoud
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SetNewSourceLanguage
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SetNewTargetLanguage
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.ShowErrorDialog
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.ToggleAppTheme
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.Translate
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.TrySwapLanguages
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.UserToggleLiveTranslate
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.UserUpdateCustomLingvaServerUrl
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun TranslationScreen(
    viewModel: TranslateScreenViewModel,
    currentTheme: MutableState<ThemingOptions>
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
    val customLingvaServerUrl = remember { mutableStateOf("") }
    val textToTranslate by viewModel.textToTranslate.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LanguageSelectionAndSettingsBar(
            supportedLanguages = translationScreenState.supportedLanguages,
            sourceLanguage = translationScreenState.sourceLanguage,
            targetLanguage = translationScreenState.targetLanguage,
            toggleErrorDialogState = {
                viewModel.send(ShowErrorDialog(it))
            },
            middleIcon = Icons.Rounded.SwapHoriz,
            onMiddleIconTap = { viewModel.send(TrySwapLanguages) },
            onNewSourceLanguageSelected = { viewModel.send(SetNewSourceLanguage(it)) },
            onNewTargetLanguageSelected = { viewModel.send(SetNewTargetLanguage(it)) },
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
                value = textToTranslate,
                onValueChange = {
                    viewModel.onTextToTranslateChange(it)
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
                AnimatedVisibility(textToTranslate.isNotEmpty()) {
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
                    .fillMaxHeight()
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
                            text = if (translationScreenState.displayPronunciation) {
                                translationScreenState.translatedTextPronunciation
                            } else {
                                translationScreenState.translatedText
                            },
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.send(ReadTextOutLoud)
                            },
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VolumeUp,
                                contentDescription = context.getString(R.string.text_to_speech_icon_ax),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.send(DisplayPronunciation)
                            },
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SpeakerNotes,
                                contentDescription = context.getString(R.string.text_to_speech_icon_ax),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.send(CopyTextToClipboard)
                            },
                            modifier = Modifier.padding(bottom = 16.dp),
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
        }
    }

    SettingsBottomSheet(
        modalBottomSheetState = modalBottomSheetState,
        toggleTheme = {
            viewModel.send(ToggleAppTheme(it))
        },
        currentTheme = currentTheme,
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
        },
        customLingvaServerUrl = customLingvaServerUrl,
        liveTranslateEnabled = translationScreenState.liveTranslationEnabled,
        onToggleLiveTranslate = { viewModel.send(UserToggleLiveTranslate(it)) },
        currentCustomLingvaServerUrl = translationScreenState.customLingvaServerUrl,
        context = context,
    )

    LaunchedEffect(!modalBottomSheetState.isVisible) {
        if (customLingvaServerUrl.value.isNotEmpty()) {
            viewModel.send(UserUpdateCustomLingvaServerUrl(customLingvaServerUrl.value))
            customLingvaServerUrl.value = ""
        }
    }

    ErrorNotificationDialog(translationScreenState.errorDialogState) {
        viewModel.send(ShowErrorDialog(false))
    }
}
