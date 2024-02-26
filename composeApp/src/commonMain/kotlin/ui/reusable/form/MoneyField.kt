package ui.reusable.form

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun MoneyField(
    value: UInt,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    unit: Char = '€'
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = "$value $unit",
        onValueChange = { text ->
            var removeLast = false
            if (!text.contains(unit)) {
                // the user has removed the euro sign, remove the last number instead
                removeLast = true
            }
            val money = text
                // trim leading 0
                .trimStart('0')
                // remove non numeric characters using regex
                .replace(Regex("[^0-9]"), "")
                // remove the last number if the user removed the euro sign
                .dropLast(if (removeLast) 1 else 0)
                // convert to UInt
                .toUIntOrNull()
            // or default to 0
                ?: 0u

            onValueChange(money.toInt())
        },
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions {
            keyboardController?.hide()
        },
        singleLine = true
    )
}
