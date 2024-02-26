package ui.pages.intro

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pocketchips.composeapp.generated.resources.Res

object IntroPageWelcome : IntroPage(
    image = { painterResource(Res.drawable.icons8_roulette_chips) },
    title = { stringResource(Res.string.intro_welcome_title) },
    message = { stringResource(Res.string.intro_welcome_message) }
) {
    @Composable
    override fun ColumnScope.Content() {
        var alpha by remember { mutableStateOf(0f) }
        val alphaAnimation by animateFloatAsState(
            targetValue = alpha,
            animationSpec = tween(1200)
        )
        var imageOffset by remember { mutableStateOf((-50).dp) }
        val imageOffsetAnimation by animateDpAsState(
            targetValue = imageOffset,
            animationSpec = tween(1200)
        )
        var textOffset by remember { mutableStateOf((-300).dp) }
        val textOffsetAnimation by animateDpAsState(
            targetValue = textOffset,
            animationSpec = tween(1200)
        )
        LaunchedEffect(Unit) {
            alpha = 1f
            imageOffset = 0.dp
            textOffset = 0.dp
        }

        PageContent(
            imageAlpha = alphaAnimation,
            imageModifier = Modifier.offset {
                IntOffset(0, imageOffsetAnimation.roundToPx())
            },
            titleModifier = Modifier.offset {
                IntOffset(textOffsetAnimation.roundToPx(), 0)
            },
            messageModifier = Modifier.offset {
                IntOffset(textOffsetAnimation.roundToPx(), 0)
            }
        )
    }
}
