package ui.platform

import androidx.compose.runtime.Composable

expect object PlatformScreen {
    @Composable
    fun getScreenWidth(): Int

    @Composable
    fun getScreenHeight(): Int
}
