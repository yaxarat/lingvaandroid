package dev.atajan.lingva_android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.ui.components.LanguageSelectionBar

@Composable
fun TranslatenScreen(viewModel: TranslateScreenViewModel) {
    val scrollState = rememberScrollState(0)
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val textToTranslateMutableState = viewModel.textToTranslate

    Column(modifier = Modifier.fillMaxSize()) {
        LanguageSelectionBar(
            modifier = Modifier.padding(all = 16.dp),
            supportedLanguages = viewModel.supportedLanguages
        )

        OutlinedTextField(
            value = textToTranslateMutableState.value,
            onValueChange = { textToTranslateMutableState.value = it },
            label = { Text("Source text") },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .padding(all = 16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    softwareKeyboardController?.hide()
                    viewModel.translate()
                }
            ),
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        if (textToTranslateMutableState.value.isNotEmpty()) {
            Card(
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(2.dp, Color(0xFF61FD96)),
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
