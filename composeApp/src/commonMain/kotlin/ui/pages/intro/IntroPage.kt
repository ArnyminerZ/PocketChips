package ui.pages.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow

open class IntroPage(
    val image: @Composable () -> Painter,
    val title: @Composable () -> String,
    val message: @Composable () -> String
) {
    val canGoNext = MutableStateFlow(true)

    @Composable
    fun ColumnScope.PageContent(
        imageAlpha: Float = 1f,
        imageModifier: Modifier = Modifier,
        titleModifier: Modifier = Modifier,
        messageModifier: Modifier = Modifier,
        messageTextStyle: TextStyle = MaterialTheme.typography.titleLarge
    ) {
        Image(
            painter = image(),
            contentDescription = title(),
            alpha = imageAlpha,
            modifier = Modifier
                .padding(top = 128.dp)
                .size(128.dp)
                .then(imageModifier)
        )
        Text(
            text = title(),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 64.dp)
                .padding(top = 32.dp)
                .then(titleModifier)
        )
        Text(
            text = message(),
            style = messageTextStyle,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 32.dp)
                .padding(top = 12.dp, bottom = 64.dp)
                .then(messageModifier)
        )
    }

    @Composable
    open fun ColumnScope.Content() {
        PageContent()
    }
}
