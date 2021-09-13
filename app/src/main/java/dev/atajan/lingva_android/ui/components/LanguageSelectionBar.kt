package dev.atajan.lingva_android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LanguageSelectionBar(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            shape = MaterialTheme.shapes.medium,
            color = Color.DarkGray
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Target", modifier = Modifier.padding(end = 16.dp))
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(0.55f).fillMaxHeight(),
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 15.dp,
                bottomStart = 4.dp,
                bottomEnd = 15.dp
            ),
            color = Color.Cyan
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Source", modifier = Modifier.padding(start = 16.dp))
            }
        }
    }
}

@Preview
@Composable
fun LanguageSelectionBarPreview() {
    LanguageSelectionBar(modifier = Modifier.height(40.dp))
}