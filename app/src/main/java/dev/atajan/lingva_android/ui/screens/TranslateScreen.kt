package dev.atajan.lingva_android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.ui.components.LanguageSelectionBar

@Composable
fun TranslatenScreen(
    viewModel: TranslateScreenViewModel,
    isDarkTheme: State<Boolean>,
    toggleTheme: () -> Unit
) {
    val scrollState = rememberScrollState(0)
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val textToTranslateMutableState = viewModel.textToTranslate

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        LanguageSelectionBar(
            modifier = Modifier.padding(all = 16.dp),
            supportedLanguages = viewModel.supportedLanguages,
            sourceLanguage = viewModel.sourceLanguage,
            targetLanguage = viewModel.targetLanguage,
            isDarkTheme = isDarkTheme,
            toggleTheme = toggleTheme
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .padding(all = 16.dp)
        ) {
            OutlinedTextField(
                value = textToTranslateMutableState.value,
                onValueChange = { textToTranslateMutableState.value = it },
                label = { Text("Source text") },
                modifier = Modifier.fillMaxSize(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        softwareKeyboardController?.hide()
                        viewModel.translate()
                    }
                ),
                textStyle = TextStyle(color = MaterialTheme.colors.onSurface)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.End
            ) {
                Spacer(modifier = Modifier.fillMaxSize(0.80f))
                IconButton(
                    onClick = {
                        softwareKeyboardController?.hide()
                        viewModel.translate()
                    },
                    modifier = Modifier.fillMaxHeight().padding(bottom = 16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Translate,
                        contentDescription = "Translate",
                        tint = if (isDarkTheme.value) Color.White else Color.Black,
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        if (viewModel.translatedText.value.isNotEmpty()) {
            Card(
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(2.dp, MaterialTheme.colors.primary),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 16.dp),
                elevation = 0.dp
            ) {
                Text(
                    viewModel.translatedText.value,
                    modifier = Modifier.padding(16.dp).verticalScroll(scrollState)
                )
            }
        }
    }
}
