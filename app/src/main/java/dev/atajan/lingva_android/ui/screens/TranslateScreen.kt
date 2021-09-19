package dev.atajan.lingva_android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.ui.components.LanguageSelectionBar

@Composable
fun TranslatenScreen(viewModel: TranslateScreenViewModel) {
//    viewModel.listLanguages()
    viewModel.testTranslate()

    Column(modifier = Modifier.fillMaxSize()) {
        LanguageSelectionBar(modifier = Modifier.padding(all = 16.dp))

        var text = remember { mutableStateOf("") }

        OutlinedTextField(
            value = text.value,
            onValueChange = { text.value = it },
            label = { Text("English") },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .padding(all = 16.dp),
            maxLines = 4
        )

        OutlinedTextField(
            value = text.value,
            onValueChange = { text.value = it },
            label = { Text("English") },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            maxLines = 4
        )
    }
}
