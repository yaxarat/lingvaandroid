package dev.atajan.lingva_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.atajan.lingva_android.api.entities.LanguageEntity

@Composable
fun LanguageListPopUp(openDialog: MutableState<Boolean>, languageList: List<LanguageEntity>) {
    if (openDialog.value) {
        Dialog(onDismissRequest = { openDialog.value = false }) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f)
                    .background(
                        color = MaterialTheme.colors.background,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    itemsIndexed(items = languageList) { index, languageEntity ->
                        Text(
                            text = languageEntity.name,
                            style = MaterialTheme.typography.button,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp)
                        )
                        if (index < languageList.size) {
                            Divider(
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
