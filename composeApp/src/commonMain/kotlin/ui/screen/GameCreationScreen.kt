package ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.BluetoothSearching
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.BluetoothDisabled
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.serialization.encodeValue
import com.russhwolf.settings.set
import connectivity.ConnectivityManager
import connectivity.ConnectivityState.IDLE
import connectivity.ConnectivityState.DISCOVERING
import connectivity.ConnectivityState.ADVERTISING
import connectivity.ConnectivityState.ADVERTISING_AND_DISCOVERING
import connectivity.DiscoveredEndpoint
import data.Game
import data.Player
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.compose.resources.stringResource
import pocketchips.composeapp.generated.resources.Res
import screenmodel.GameCreationModel
import storage.SettingsKeys
import storage.SettingsKeys.GAME_DATA
import storage.settings
import ui.platform.PlatformScreen
import ui.reusable.form.FormField
import ui.reusable.form.IconButtonBack
import ui.reusable.form.MoneyField
import ui.theme.font.Lato
import ui.theme.font.Oswald

class GameCreationScreen : Screen {
    companion object {
        private const val DEFAULT_DISPLAY_NAME = "Poker Chips"

        private const val ADVERTISEMENT_BUTTON_SIZE = 150
    }

    private lateinit var model: GameCreationModel

    @OptIn(ExperimentalSettingsApi::class)
    private val displayName: String by lazy {
        settings.getString(SettingsKeys.DISPLAY_NAME, DEFAULT_DISPLAY_NAME)
    }

    @Composable
    @OptIn(ExperimentalFoundationApi::class)
    override fun Content() {
        val scope = rememberCoroutineScope()

        model = rememberScreenModel { GameCreationModel() }

        DisposableEffect(Unit) {
            onDispose { ConnectivityManager.stop() }
        }

        val pagerState = rememberPagerState { 2 }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> ConnectionCard {
                        model.ensureHost()
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }

                    1 -> PlayersCard {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Card(
        modifier: Modifier = Modifier,
        containerColor: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 5.dp,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 56.dp)
                .then(modifier)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )
        }
    }

    @Composable
    private fun ConnectionCard(
        onUseThisDeviceRequested: () -> Unit = {}
    ) {
        val discoveredEndpoints by ConnectivityManager.discoveredEndpoints.collectAsState(emptyList())
        val connectedEndpoints by ConnectivityManager.connectedEndpoints.collectAsState(emptyList())

        val height = PlatformScreen.getScreenHeight()
        var topPadding by remember { mutableIntStateOf(height) }
        val animatedTopPadding by animateIntAsState(topPadding)
        LaunchedEffect(Unit) {
            topPadding = 0
        }

        Card(
            modifier = Modifier
                .offset {
                    IntOffset(0, animatedTopPadding)
                }
        ) {
            Text(
                text = stringResource(Res.string.create_game_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontFamily = Oswald.medium
            )
            Text(
                text = stringResource(Res.string.create_game_message),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center,
                fontFamily = Lato.regular
            )

            AdvertisementButton()

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(discoveredEndpoints) { endpoint ->
                    DiscoveredDeviceItem(endpoint, connectedEndpoints)
                }
            }

            TextButton(
                onClick = onUseThisDeviceRequested,
                modifier = Modifier.align(Alignment.End).padding(bottom = 4.dp, end = 8.dp)
            ) { Text(stringResource(Res.string.create_game_single)) }
        }
    }

    @OptIn(
        ExperimentalSettingsApi::class,
        ExperimentalSerializationApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    private fun PlayersCard(modifier: Modifier = Modifier, onBackRequested: () -> Unit) {
        val navigator = LocalNavigator.currentOrThrow

        val playersList by model.playersList.collectAsState(emptyList())
        var showingPlayer by remember { mutableStateOf<Player?>(null) }

        showingPlayer?.let { playerState ->
            var player by remember { mutableStateOf(playerState) }

            ModalBottomSheet(
                onDismissRequest = { showingPlayer = null },
                windowInsets = WindowInsets.safeDrawing
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.create_game_player_edit_title),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontFamily = Oswald.medium
                    )
                    AnimatedVisibility(
                        visible = player != playerState,
                    ) {
                        IconButton(
                            onClick = {
                                model.updatePlayer(playerState, player.displayName, player.money)
                                showingPlayer = null
                            }
                        ) {
                            Icon(Icons.Rounded.SaveAlt, stringResource(Res.string.save))
                        }
                    }
                }
                Text(
                    text = stringResource(
                        if (player.isHost) {
                            Res.string.create_game_player_edit_host_message
                        } else {
                            Res.string.create_game_player_edit_virtual_message
                        }
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(top = 8.dp, bottom = 8.dp),
                    textAlign = TextAlign.Center,
                    fontFamily = Lato.regular
                )
                FormField(
                    value = player.displayName,
                    onValueChange = {
                        if (player.isHost) {
                            settings[SettingsKeys.DISPLAY_NAME] = it
                        }
                        player = player.copy(displayName = it)
                    },
                    label = "Display Name",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                )
                MoneyField(
                    value = player.money,
                    onValueChange = {
                        if (player.isHost) {
                            settings[SettingsKeys.STARTING_MONEY] = it
                        }
                        player = player.copy(money = it.toUInt())
                    },
                    label = "Starting money",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        Card(
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButtonBack(onBackRequested)
                Text(
                    text = stringResource(Res.string.create_game_players_title),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontFamily = Oswald.medium
                )
                IconButton(
                    onClick = {
                        model.newPlayer()
                    }
                ) {
                    Icon(Icons.Rounded.Add, null)
                }
            }
            Text(
                text = stringResource(Res.string.create_game_players_virtual),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center,
                fontFamily = Lato.regular
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(playersList) { player ->
                    PlayerCard(
                        player = player,
                        modifier = Modifier
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                            .fillMaxWidth()
                    ) { showingPlayer = player }
                }
            }

            TextButton(
                onClick = {
                    val game = Game(Clock.System.now().toEpochMilliseconds(), playersList)
                    settings.encodeValue(Game.serializer(), GAME_DATA, game)
                    navigator.push(HomeScreen(game))
                },
                modifier = Modifier.align(Alignment.End).padding(bottom = 4.dp, end = 8.dp)
            ) { Text(stringResource(Res.string.create_game_create)) }
        }
    }

    @Composable
    private fun DiscoveredDeviceItem(
        endpoint: DiscoveredEndpoint,
        connectedEndpoints: List<String>
    ) {
        val isConnected = connectedEndpoints.contains(endpoint.endpointId)
        var isConnecting by remember { mutableStateOf(false) }
        OutlinedCard(
            enabled = isConnected.not() && !isConnecting,
            onClick = {
                isConnecting = true
                model.connect(displayName, endpoint).invokeOnCompletion {
                    isConnecting = false
                }
            },
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp).fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val transition = rememberInfiniteTransition(label = "connecting animation")
                val rotateAnimation by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Restart)
                )

                Icon(
                    imageVector = if (isConnected || isConnecting)
                        Icons.Outlined.Link
                    else
                        Icons.Outlined.LinkOff,
                    contentDescription = null,
                    modifier = if (isConnecting) Modifier.rotate(rotateAnimation) else Modifier
                )
                Text(
                    text = endpoint.endpointName,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                AnimatedVisibility(
                    visible = isConnecting,
                    enter = slideInHorizontally { it },
                    exit = slideOutHorizontally { it }
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun AdvertisementButton() {
        val hapticFeedback = LocalHapticFeedback.current
        val connectivityState by ConnectivityManager.state.collectAsState(IDLE)

        LaunchedEffect(connectivityState) {
            when (connectivityState) {
                IDLE -> {}
                else -> hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "connecting animation")
        val rotateAnimation by infiniteTransition.animateFloat(
            initialValue = -30f,
            targetValue = 30f,
            animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
        )
        val scaleAnimation by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse)
        )

        OutlinedCard(
            // Allow tapping when idle, or when just advertising (start discovery again)
            enabled = connectivityState == IDLE || connectivityState == ADVERTISING,
            onClick = {
                ConnectivityManager.advertise(displayName)
                ConnectivityManager.discover()
            },
            modifier = Modifier.padding(top = 24.dp).width(ADVERTISEMENT_BUTTON_SIZE.dp),
        ) {
            Box(modifier = Modifier.size(ADVERTISEMENT_BUTTON_SIZE.dp)) {
                AnimatedContent(
                    targetState = connectivityState,
                    modifier = Modifier.align(Alignment.Center)
                ) { state ->
                    Icon(
                        imageVector = when (state) {
                            IDLE -> Icons.Outlined.BluetoothDisabled
                            ADVERTISING -> Icons.Outlined.Bluetooth
                            DISCOVERING -> Icons.AutoMirrored.Outlined.BluetoothSearching
                            ADVERTISING_AND_DISCOVERING -> Icons.Outlined.Hub
                        },
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                            .then(
                                if (state == ADVERTISING_AND_DISCOVERING) {
                                    Modifier.rotate(rotateAnimation).scale(scaleAnimation)
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
            }
            AnimatedContent(
                targetState = connectivityState
            ) { state ->
                Text(
                    text = when (state) {
                        IDLE -> stringResource(Res.string.create_game_pair)
                        ADVERTISING -> stringResource(Res.string.create_game_advertising)
                        DISCOVERING -> stringResource(Res.string.create_game_discovering)
                        ADVERTISING_AND_DISCOVERING -> stringResource(Res.string.create_game_pairing)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    private fun PlayerCard(player: Player, modifier: Modifier = Modifier, onClick: () -> Unit) {
        OutlinedCard(
            onClick = onClick,
            enabled = player.isHost || player.isVirtual,
            modifier = modifier
        ) {
            Box(modifier = Modifier.aspectRatio(1f).fillMaxWidth().padding(8.dp)) {
                Image(
                    painter = player.painter,
                    contentDescription = player.displayName,
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = player.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis
                )
                if (player.isHost) {
                    Spacer(Modifier.width(4.dp))
                    Badge {
                        Text("You")
                    }
                }
            }
        }
    }
}
