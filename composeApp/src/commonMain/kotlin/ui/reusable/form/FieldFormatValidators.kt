package ui.reusable.form

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import pocketchips.composeapp.generated.resources.Res

object FieldFormatValidators {
    data object Email : FieldFormatValidator.Regex() {
        override val pattern: Regex = "[\\w.]{1,255}@[\\w.]{1,255}".toRegex()

        @Composable
        override fun error(): String = stringResource(Res.string.validation_email)
    }
}
