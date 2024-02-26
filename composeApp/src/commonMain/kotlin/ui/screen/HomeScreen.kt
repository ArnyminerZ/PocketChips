package ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.SensorsOff
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.rounded.Euro
import androidx.compose.material.icons.rounded.EuroSymbol
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.serialization.nullableSerializedValue
import com.russhwolf.settings.serialization.serializedValue
import connectivity.ConnectivityManager
import data.Game
import data.Player
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import storage.SettingsKeys
import storage.settings
import ui.icon.Poker
import ui.icon.PokerIcons
import ui.theme.font.Oswald
import ui.widget.poker.RotatingChips
import utils.secondsToHumanReadable

class HomeScreen(
    private val game: Game = Game.none
) : Screen {
    @OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val game by settings.serializedValue(Game.serializer(), SettingsKeys.GAME_DATA, game)
        LaunchedEffect(game) {
            if (game.isNone()) {
                Napier.i { "Game is null, navigating to creation screen..." }
                navigator.push(GameCreationScreen())
            }
        }

        if (game.isNone()) return

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                Game(game)
            }
        }
    }

    @Composable
    private fun Game(game: Game) {
        var selectedPlayer by remember { mutableStateOf(game.players.first()) }

        val date = Instant.fromEpochMilliseconds(game.startTimestamp)
        var gameTime by remember { mutableStateOf(0L) }
        LaunchedEffect(Unit) {
            while (true) {
                val now = Clock.System.now()
                gameTime = now.epochSeconds - date.epochSeconds
                delay(1000)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DataChip(
                icon = Icons.Rounded.Timer,
                value = secondsToHumanReadable(gameTime),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            DataChip(
                icon = Icons.Rounded.EuroSymbol,
                value = "${game.money} ${game.currency}",
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            items(game.players.filterNot { it.isRemote }) { player ->
                PlayerChip(
                    icon = PokerIcons.Poker,
                    game = game,
                    player = player,
                    isSelected = selectedPlayer == player,
                    modifier = Modifier.fillMaxWidth().padding(4.dp)
                ) { selectedPlayer = player }
            }
        }
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            AnimatedContent(
                targetState = selectedPlayer,
                transitionSpec = {
                    fadeIn(tween(1000)) togetherWith fadeOut(tween(1000))
                }
            ) {
                RotatingChips(
                    amount = it.money.toInt(),
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                )
            }
        }
    }

    @Composable
    private fun DataChip(
        icon: ImageVector,
        value: String,
        modifier: Modifier = Modifier
    ) {
        OutlinedCard(modifier) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(24.dp)
                )
                Text(
                    text = value,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = Oswald.medium,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    private fun PlayerChip(
        icon: ImageVector,
        game: Game,
        player: Player,
        isSelected: Boolean,
        modifier: Modifier = Modifier,
        onclick: () -> Unit
    ) {
        val connectedEndpoints by ConnectivityManager.connectedEndpoints.collectAsState(emptyList())
        val elevation by animateDpAsState(
            targetValue = if (isSelected) 5.dp else 0.dp,
            animationSpec = tween(500)
        )

        OutlinedCard(
            onClick = onclick,
            elevation = CardDefaults.cardElevation(elevation),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                        .padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = player.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = Oswald.regular,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Icon(
                            imageVector = if (player.isVirtual)
                                Icons.Outlined.SmartToy
                            else if (player.isHost) {
                                Icons.Outlined.Person
                            } else {
                                if (connectedEndpoints.contains(player.endpointId)) {
                                    Icons.Outlined.Sensors
                                } else {
                                    Icons.Outlined.SensorsOff
                                }
                            },
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).padding(start = 4.dp)
                        )
                    }
                    Text(
                        text = "${player.money} ${game.currency}",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = Oswald.light,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
