import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.Navigator
import ui.theme.AppTheme
import com.russhwolf.settings.ExperimentalSettingsApi
import storage.SettingsKeys
import storage.settings
import ui.pages.intro.PlatformIntro
import ui.screen.HomeScreen
import ui.screen.OnboardingScreen

@Composable
@OptIn(ExperimentalSettingsApi::class)
fun App() {
    AppTheme {
        val shownOnboarding = remember {
            settings.getBoolean(SettingsKeys.SHOWN_ONBOARDING, false)
        }

        Navigator(
            screen = if (!shownOnboarding || PlatformIntro.shouldShowIntro()) {
                OnboardingScreen()
            } else {
                HomeScreen()
            }
        )
    }
}
