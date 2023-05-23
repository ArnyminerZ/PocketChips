package com.arnyminerz.pocketchips.connections

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.pocketchips.activity.DemoGameActivity
import com.arnyminerz.pocketchips.communications.getSerializedExtra
import com.arnyminerz.pocketchips.game.GameSettings
import com.arnyminerz.pocketchips.game.requests.BalanceRequest
import com.arnyminerz.pocketchips.game.requests.GameStatusRequest
import com.arnyminerz.pocketchips.game.response.Balance
import com.arnyminerz.pocketchips.game.response.GameStatus
import com.arnyminerz.pocketchips.utils.async
import kotlinx.coroutines.TimeoutCancellationException

class ClientGameManager(application: Application) : ClientConnectionsManager(application) {
    companion object {
        private const val TAG = "GameClientManager"
    }

    val isValid: Boolean get() = hostEndpointId != null && gameSettings != null

    var hostEndpointId: String? = null
        private set
    var gameSettings: GameSettings? = null
        private set

    private val balanceMutable: MutableLiveData<Balance> = MutableLiveData()
    val balance: LiveData<Balance> get() = balanceMutable

    private val gameStatusMutable: MutableLiveData<GameStatus> = MutableLiveData()
    val gameStatus: LiveData<GameStatus> get() = gameStatusMutable


    @MainThread
    fun initializeFromIntent(intent: Intent) {
        hostEndpointId = intent.getStringExtra(DemoGameActivity.EXTRA_HOST_ENDPOINT_ID)
        gameSettings = intent.getSerializedExtra<GameSettings, GameSettings.Companion>(DemoGameActivity.EXTRA_GAME_SETTINGS)

        connectedToMutable.value = hostEndpointId

        hostEndpointId?.let {
            connectionsClient.acceptConnection(it, payloadCallback)
        }
    }

    /** Asks the host whether the game has started. */
    fun requestGameStatus() = async {
        try {
            Log.d(TAG, "Requesting game status to host...")
            val gameStatus = sendPayloadResponse(GameStatusRequest)
            gameStatusMutable.postValue(gameStatus)
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Request timed out for game status.")
        }
    }

    /** Requests the amount of money this player has. */
    fun requestBalance() = async {
        try {
            Log.d(TAG, "Requesting balance to host...")
            val balance = sendPayloadResponse(BalanceRequest)
            balanceMutable.postValue(balance)
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Request timed out for balance.")
        }
    }
}
