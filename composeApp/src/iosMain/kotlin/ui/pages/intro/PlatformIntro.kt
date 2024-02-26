package ui.pages.intro

import androidx.compose.runtime.Composable

actual object PlatformIntro {
    /**
     * A list of platform-specific intro pages.
     */
    actual val introPages: List<IntroPage> = emptyList()

    /**
     * Whether the intro should be shown.
     * This check is exclusively for the current platform, intro will always be shown the first time
     * the app is launched regardless of this value.
     */
    @Composable
    actual fun shouldShowIntro(): Boolean = false
}
