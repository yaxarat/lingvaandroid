package dev.atajan.lingva_android.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.atajan.lingva_android.common.domain.models.language.Language

@Composable
fun LanguageListPopUp(
    openDialog: MutableState<Boolean>,
    languageList: List<Language>,
    onLanguageSelected: ((Language) -> Unit),
) {
    if (languageList.isEmpty()) {
        openDialog.value = false
    } else {
        if (openDialog.value) {
            Dialog(onDismissRequest = { openDialog.value = false }) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.95f)
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        itemsIndexed(items = languageList) { index, language ->
                            Column(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .clickable {
                                        onLanguageSelected(language)
                                        openDialog.value = false
                                    }
                            ) {
                                Text(
                                    text = language.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 8.dp)

                                )
                                if (index < languageList.size) {
                                    Divider(
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
