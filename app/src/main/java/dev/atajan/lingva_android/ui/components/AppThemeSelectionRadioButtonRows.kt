package dev.atajan.lingva_android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.ui.theme.ThemingOptions

@Composable
fun AppThemeSelectionRadioButtonRows(
    toggleTheme: (ThemingOptions) -> Unit,
    getCurrentTheme: () -> ThemingOptions
) {
    var radioButtonState by remember { mutableStateOf(getCurrentTheme.invoke().name) }

    Text(
        text = "App Theme",
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(16.dp)
    )

    Column(modifier = Modifier.selectableGroup()) {
        ThemingOptions.values().forEach { option ->
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                RadioButton(
                    selected = radioButtonState == option.name,
                    onClick = {
                        toggleTheme(ThemingOptions.valueOf(option.name))
                        radioButtonState = option.name
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onBackground,
                        disabledColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = option.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}