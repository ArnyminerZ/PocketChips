package ui.pages.intro

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pocketchips.composeapp.generated.resources.Res

object IntroPageSafety: IntroPage(
    image = { painterResource(Res.drawable.icons8_mental_health) },
    title = { stringResource(Res.string.intro_safety_title) },
    message = { stringResource(Res.string.intro_safety_message_1) + "\n\n" + stringResource(Res.string.intro_safety_message_2) }
) {
    @Composable
    override fun ColumnScope.Content() {
        PageContent(
            messageTextStyle = MaterialTheme.typography.titleMedium,
            messageModifier = Modifier.verticalScroll(rememberScrollState())
        )
    }
}
