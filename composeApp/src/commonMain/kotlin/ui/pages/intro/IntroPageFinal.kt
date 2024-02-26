package ui.pages.intro

import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pocketchips.composeapp.generated.resources.Res

object IntroPageFinal : IntroPage(
    image = { painterResource(Res.drawable.icons8_poker) },
    title = { stringResource(Res.string.intro_final_title) },
    message = { stringResource(Res.string.intro_final_message) }
)
