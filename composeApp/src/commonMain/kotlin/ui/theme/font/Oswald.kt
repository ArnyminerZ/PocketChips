package ui.theme.font

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import pocketchips.composeapp.generated.resources.Res

object Oswald {
    val bold: FontFamily
        @Composable
        get() = buildFont(Res.font.oswald_bold)

    val extraLight: FontFamily
        @Composable
        get() = buildFont(Res.font.oswald_extralight)

    val light: FontFamily
        @Composable
        get() = buildFont(Res.font.oswald_light)

    val medium: FontFamily
        @Composable
        get() = buildFont(Res.font.oswald_medium)

    val regular: FontFamily
        @Composable
        get() = buildFont(Res.font.oswald_regular)

    val semiBold: FontFamily
        @Composable
        get() = buildFont(Res.font.oswald_semibold)
}
