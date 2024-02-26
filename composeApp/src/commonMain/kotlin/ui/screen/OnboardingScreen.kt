package ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import pocketchips.composeapp.generated.resources.Res
import storage.SettingsKeys
import storage.settings
import ui.pages.intro.IntroPage
import ui.pages.intro.IntroPageFinal
import ui.pages.intro.IntroPageSafety
import ui.pages.intro.IntroPageWelcome
import ui.pages.intro.PlatformIntro

@OptIn(ExperimentalFoundationApi::class, ExperimentalSettingsApi::class)
class OnboardingScreen : Screen {
    private val pages: List<IntroPage> = listOf(
        IntroPageWelcome,
        IntroPageSafety,
        *PlatformIntro.introPages.toTypedArray(),
        IntroPageFinal
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val scope = rememberCoroutineScope()
        val pagerState = rememberPagerState { pages.size }

        val page = pages[pagerState.currentPage]
        val canGoNext by page.canGoNext.collectAsState(true)

        Scaffold(
            floatingActionButton = {
                val isLastPage = pagerState.currentPage + 1 >= pages.size

                AnimatedVisibility(
                    visible = canGoNext,
                    enter = slideInHorizontally { it },
                    exit = slideOutHorizontally { it }
                ) {
                    FloatingActionButton(
                        onClick = {
                            if (isLastPage) CoroutineScope(Dispatchers.IO).launch {
                                settings[SettingsKeys.SHOWN_ONBOARDING] = true
                                navigator.push(HomeScreen())
                            } else scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isLastPage)
                                Icons.Outlined.Done
                            else
                                Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = if (isLastPage) {
                                stringResource(Res.string.done)
                            } else {
                                stringResource(Res.string.next)
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = canGoNext,
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    with(pages[page]) { Content() }
                }
            }
        }
    }
}
