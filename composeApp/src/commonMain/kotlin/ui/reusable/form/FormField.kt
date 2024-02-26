package ui.reusable.form

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import org.jetbrains.compose.resources.stringResource
import pocketchips.composeapp.generated.resources.Res

/**
 * Displays an outlined text field intended to be used in forms.
 * It forced to have just one line, and some utilities are provided.
 *
 * @param value The current value of the field, if null, the field will be empty.
 * @param onValueChange Will be called whenever the user types something in the field.
 * @param label The text to display on top of the field.
 * @param modifier If any, modifiers to apply to the field.
 * @param enabled If `true` the field is intractable, `false` disables the field. Default: `true`
 * @param error If not `null`, this text will be displayed in red under the field.
 * @param isPassword If true, the field will be considered a password input.
 * A show/hide password button will be displayed at the end of the field, and the characters will be obfuscated.
 * @param nextFocusRequester If any, what should be selected when tapping the "next" button in the keyboard, or when
 * pressing TAB.
 * @param onSubmit If any, will be called when the user presses the submit button in the software keyboard, or enter
 * in a hardware keyboard.
 * @param capitalization Can be provided to specify the capitalization options for the keyboard. Defaults to none.
 * @param supportingText If any, the text that will be displayed under the field for giving more information about the
 * field to the user.
 * Won't be displayed if [error] is not null.
 * @param isRequired If `true`, a red tick (`*`) will be displayed at the end of [label].
 * @param validator If not null, will be used for validating the current text. Will show
 */
@Composable
fun FormField(
    value: String?,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    error: String? = null,
    isPassword: Boolean = false,
    nextFocusRequester: FocusRequester? = null,
    onSubmit: (() -> Unit)? = null,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    supportingText: String? = null,
    keyboardType: KeyboardType? = null,
    isRequired: Boolean = false,
    validator: FieldFormatValidator? = null
) {
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    var showingPassword by remember { mutableStateOf(!isPassword) }

    val validationError = validator
        // Only validate if text has been introduced
        ?.takeIf { value != null }
        ?.takeUnless { it.validate(value) }
        ?.error()

    OutlinedTextField(
        value = value ?: "",
        onValueChange = onValueChange,
        modifier = modifier,
        label = {
            Text(
                text = buildAnnotatedString {
                    append(label)
                    if (isRequired) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                            append(" *")
                        }
                    }
                }
            )
        },
        enabled = enabled,
        readOnly = readOnly,
        singleLine = true,
        maxLines = 1,
        visualTransformation = if (showingPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) {
                KeyboardType.Password
            } else {
                keyboardType ?: KeyboardType.Text
            },
            imeAction = when {
                nextFocusRequester != null -> ImeAction.Next
                onSubmit != null -> ImeAction.Go
                else -> ImeAction.Done
            },
            capitalization = capitalization
        ),
        keyboardActions = KeyboardActions(
            onNext = { nextFocusRequester?.requestFocus() },
            onDone = { softwareKeyboardController?.hide() },
            onGo = { onSubmit?.invoke() }
        ),
        trailingIcon = (@Composable {
            IconButton(
                onClick = { showingPassword = !showingPassword }
            ) {
                Icon(
                    imageVector = if (showingPassword) {
                        Icons.Outlined.VisibilityOff
                    } else {
                        Icons.Outlined.Visibility
                    },
                    contentDescription = if (showingPassword) {
                        stringResource(Res.string.hide_password)
                    } else {
                        stringResource(Res.string.show_password)
                    }
                )
            }
        }).takeIf { isPassword },
        isError = error != null || validationError != null,
        supportingText = {
            AnimatedContent(
                targetState = error to validationError,
                transitionSpec = {
                    slideInVertically { -it } togetherWith slideOutVertically { -it }
                }
            ) { (err, validation) ->
                err?.let { Text(it) }
                    ?: validation?.let { Text(it) }
                    ?: supportingText?.let { Text(it) }
            }
        }
    )
}
