package ui.pages.intro

import androidx.compose.runtime.Composable

expect object PlatformIntro {
    /**
     * A list of platform-specific intro pages.
     */
    val introPages: List<IntroPage>

    /**
     * Whether the intro should be shown.
     * This check is exclusively for the current platform, intro will always be shown the first time
     * the app is launched regardless of this value.
     */
    @Composable
    fun shouldShowIntro(): Boolean
}
