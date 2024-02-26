package screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.russhwolf.settings.ExperimentalSettingsApi
import connectivity.ConnectivityManager
import connectivity.DiscoveredEndpoint
import data.Player
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import storage.SettingsKeys
import storage.settings

class GameCreationModel : ScreenModel {
    val playersList = MutableStateFlow(emptyList<Player>())

    /**
     * Requests to connect to the given endpoint.
     */
    fun connect(displayName: String, endpoint: DiscoveredEndpoint) = screenModelScope.async {
        ConnectivityManager.connect(displayName, endpoint)
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun ensureHost() {
        if (playersList.value.find { it.isHost } == null) {
            newPlayer(
                displayName = settings.getString(SettingsKeys.DISPLAY_NAME, "Game Host"),
                money = settings.getInt(SettingsKeys.STARTING_MONEY, 0).toUInt(),
                isHost = true
            )
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun newPlayer(
        displayName: String = "Player ${playersList.value.size + 1}",
        money: UInt = settings.getInt(SettingsKeys.STARTING_MONEY, 0).toUInt(),
        isHost: Boolean = false
    ) = screenModelScope.launch {
        val newPlayer = Player(displayName, money, isHost)
        playersList.value = playersList.value + newPlayer
    }

    fun updatePlayer(
        player: Player,
        displayName: String = player.displayName,
        money: UInt = player.money
    ): Player {
        val updatedPlayer = player.copy(displayName = displayName, money = money)
        playersList.value = playersList.value.map { if (it == player) updatedPlayer else it }
        return updatedPlayer
    }
}
