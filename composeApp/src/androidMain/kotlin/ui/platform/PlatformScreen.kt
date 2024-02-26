package ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

actual object PlatformScreen {
    @Composable
    actual fun getScreenWidth(): Int {
        return with(LocalDensity.current) {
            LocalConfiguration.current.screenWidthDp.dp.roundToPx()
        }
    }

    @Composable
    actual fun getScreenHeight(): Int {
        return with(LocalDensity.current) {
            LocalConfiguration.current.screenHeightDp.dp.roundToPx()
        }
    }
}
