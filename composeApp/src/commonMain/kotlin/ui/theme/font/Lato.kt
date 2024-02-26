package ui.theme.font

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import pocketchips.composeapp.generated.resources.Res

object Lato {
    val black: FontFamily
        @Composable
        get() = buildFont(Res.font.lato_black)

    val blackItalic: FontFamily
        @Composable
        get() = buildFont(Res.font.lato_blackitalic)

    val bold: FontFamily
        @Composable
        get() = buildFont(Res.font.lato_bold)

    val boldItalic: FontFamily
        @Composable
        get() = buildFont(Res.font.lato_bolditalic)

    val italic: FontFamily
        @Composable
        get() = buildFont(Res.font.lato_italic)

    val light: FontFamily
        @Composable
        get() = buildFont(Res.font.lato_light)

    val lightItalic: FontFamily
        @Composable
        get() = buildFont(Res.font.lato_lightitalic)

    val regular: FontFamily
        @Composable
        get() = buildFont(Res.font.lato_regular)

    val thin: FontFamily
        @Composable
        get() = buildFont(Res.font.lato_thin)

    val thinItalic: FontFamily
        @Composable
        get() = buildFont(Res.font.lato_thinitalic)
}
