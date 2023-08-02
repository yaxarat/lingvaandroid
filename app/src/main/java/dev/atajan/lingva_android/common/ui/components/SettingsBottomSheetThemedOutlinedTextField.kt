package dev.atajan.lingva_android.common.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.atajan.lingva_android.common.data.api.constants.LINGVA

@Composable
fun SettingsBottomSheetOutlinedTextField(
    label: String,
    currentTextFieldValue: MutableState<String>,
    hint: String,
    onValueChange: (String) -> Unit,
) {
    // TODO: add URL validation
    OutlinedTextField(
        value = currentTextFieldValue.value,
        placeholder = {
            Text(
                text = if (hint.isEmpty()) "Default: $LINGVA" else "Currently: $hint",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = ContentAlpha.disabled)
            )
        },
        onValueChange = { onValueChange(it) },
        label = {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Default
        ),
        textStyle = MaterialTheme.typography.labelLarge,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.disabled),
            focusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
            unfocusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.disabled),
            cursorColor = MaterialTheme.colorScheme.primary,
            textColor = MaterialTheme.colorScheme.onBackground,
        )
    )
}