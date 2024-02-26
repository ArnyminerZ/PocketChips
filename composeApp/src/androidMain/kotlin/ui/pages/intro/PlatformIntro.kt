package ui.pages.intro

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
actual object PlatformIntro {
    /**
     * A list of platform-specific intro pages.
     */
    actual val introPages: List<IntroPage> = listOf(IntroPagePermissions)

    /**
     * Whether the intro should be shown.
     * This check is exclusively for the current platform, intro will always be shown the first time
     * the app is launched regardless of this value.
     */
    @Composable
    actual fun shouldShowIntro(): Boolean {
        val context = LocalContext.current

        // Check if any of the permissions are not granted
        return IntroPagePermissions.permissions.any {
            ContextCompat.checkSelfPermission(context, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
}
