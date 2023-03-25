package dev.atajan.lingva_android.common.ui.components

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
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.R
import dev.atajan.lingva_android.common.ui.theme.ThemingOptions
import dev.atajan.lingva_android.common.ui.theme.canUseDynamicColor

@Composable
fun AppThemeSelectionRadioButtonRows(
    toggleTheme: (ThemingOptions) -> Unit,
    currentTheme: MutableState<ThemingOptions>
) {
    val context = LocalContext.current

    Text(
        text = context.getString(R.string.app_theme_setting_title),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(16.dp)
    )

    Column(modifier = Modifier.selectableGroup()) {
        ThemingOptions.values()
            .forEach { option ->
                if (option == ThemingOptions.YOU) {
                    if (canUseDynamicColor) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            RadioButton(
                                selected = currentTheme.value.name == option.name,
                                onClick = {
                                    toggleTheme(ThemingOptions.valueOf(option.name))
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onBackground,
                                    disabledColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Text(
                                text = option.name.uppercase() + " - " + context.getString(R.string.material_you_descriptor),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        RadioButton(
                            selected = currentTheme.value.name == option.name,
                            onClick = {
                                toggleTheme(ThemingOptions.valueOf(option.name))
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
}