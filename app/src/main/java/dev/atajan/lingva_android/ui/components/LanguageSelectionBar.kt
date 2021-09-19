package dev.atajan.lingva_android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.api.entities.LanguageEntity

@Composable
fun LanguageSelectionBar(
    supportedLanguages: MutableState<List<LanguageEntity>>,
    modifier: Modifier = Modifier
) {
    val sourceLanguagesPopUpShown = remember { mutableStateOf(false) }
    val targetLanguagesPopUpShown = remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth().height(40.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().clickable {
                targetLanguagesPopUpShown.value = true
            },
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
            modifier = Modifier.fillMaxWidth(0.55f).fillMaxHeight().clickable {
                sourceLanguagesPopUpShown.value = true
            },
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

        LanguageListPopUp(sourceLanguagesPopUpShown, supportedLanguages.value)

        // Drop the first language, "Detect", since it won't make sense for target language
        LanguageListPopUp(targetLanguagesPopUpShown, supportedLanguages.value.drop(1))
    }
}