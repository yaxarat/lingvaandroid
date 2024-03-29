package dev.atajan.lingva_android.common.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.atajan.lingva_android.R
import dev.atajan.lingva_android.common.ui.theme.ThemingOptions
import dev.atajan.lingva_android.common.ui.theme.canUseDynamicColor

@Composable
fun AppThemeSelectionRadioButtonRows(
    toggleTheme: (ThemingOptions) -> Unit,
    currentTheme: MutableState<ThemingOptions>,
    context: Context
) {
    Column(modifier = Modifier.selectableGroup()) {
        ThemingOptions.values()
            .forEach { option ->
                if (option == ThemingOptions.YOU) {
                    if (canUseDynamicColor) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
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
                                )
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
                        modifier = Modifier.fillMaxWidth()
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
                            )
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