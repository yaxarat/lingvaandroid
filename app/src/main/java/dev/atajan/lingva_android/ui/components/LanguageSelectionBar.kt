package dev.atajan.lingva_android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brightness4
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
    sourceLanguage: MutableState<LanguageEntity>,
    targetLanguage: MutableState<LanguageEntity>,
    isDarkTheme: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val sourceLanguagesPopUpShown = remember { mutableStateOf(false) }
    val targetLanguagesPopUpShown = remember { mutableStateOf(false) }

    Row(modifier = modifier.height(40.dp)) {
        Box(modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight()) {
            Surface(
                modifier = Modifier.fillMaxSize().clickable {
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
                    Text(targetLanguage.value.name, modifier = Modifier.padding(end = 20.dp))
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
                    Text(sourceLanguage.value.name, modifier = Modifier.padding(start = 20.dp))
                }
            }

            LanguageListPopUp(sourceLanguagesPopUpShown, supportedLanguages.value, sourceLanguage)

            // Drop the first language, "Detect", since it won't make sense for target language
            LanguageListPopUp(
                targetLanguagesPopUpShown,
                supportedLanguages.value.drop(1),
                targetLanguage
            )
        }

        IconButton(
            onClick = { isDarkTheme.value = !isDarkTheme.value },
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Brightness4,
                contentDescription = "Add new subscription.",
                tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
