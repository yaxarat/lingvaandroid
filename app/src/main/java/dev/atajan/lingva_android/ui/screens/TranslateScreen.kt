package dev.atajan.lingva_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.ui.components.LanguageSelectionBar

@Composable
fun TranslatenScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        LanguageSelectionBar(modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp).fillMaxHeight(0.08f))

        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f).padding(horizontal = 16.dp).padding(bottom = 16.dp), backgroundColor = Color.Green, shape = MaterialTheme.shapes.medium) {

        }

        Card(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(bottom = 16.dp), backgroundColor = Color.Red, shape = MaterialTheme.shapes.medium) {

        }
    }
}
