package dev.atajan.lingva_android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.ui.theme.ThemingOptions

@ExperimentalMaterialApi
@Composable
fun SettingsBottomSheet(
    modalBottomSheetState: ModalBottomSheetState,
    toggleTheme: (ThemingOptions) -> Unit,
    getCurrentTheme: () -> ThemingOptions
) {
    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            Column {
                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(16.dp)
                )
                // We have two radio buttons and only one can be selected
                var state by remember { mutableStateOf(getCurrentTheme.invoke().name) }
                // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
                Column(Modifier.selectableGroup()) {
                    for (theme in ThemingOptions.values()) {
                        RadioButton(
                            selected = state == theme.name,
                            onClick = {
                                toggleTheme(ThemingOptions.valueOf(theme.name))
                                state = theme.name
                            }
                        )
                    }
                }
            }
        }
    ) { }
}