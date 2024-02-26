package ui.theme.font

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

@Composable
internal fun buildFont(res: FontResource) = FontFamily(Font(res))
