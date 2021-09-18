package dev.atajan.lingva_android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    Box(modifier = modifier.fillMaxWidth().height(40.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFE5E5E5)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Target", modifier = Modifier.padding(end = 20.dp))
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(0.55f).fillMaxHeight(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF61FD96)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Source", modifier = Modifier.padding(start = 20.dp))
            }
        }
    }
}

@Preview
@Composable
fun LanguageSelectionBarPreview() {
    LanguageSelectionBar(modifier = Modifier.height(40.dp))
}