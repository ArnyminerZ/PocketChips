package com.arnyminerz.pocketchips.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.arnyminerz.pocketchips.connections.ClientGameManager
import com.arnyminerz.pocketchips.game.GameSettings

class DemoGameActivity: AppCompatActivity() {
    companion object {
        private const val TAG = "DemoGameActivity"

        /**
         * If this extra is passed, the Activity will act as a client, that talks with this
         * endpoint, which must act as a host.
         */
        const val EXTRA_HOST_ENDPOINT_ID = "host"

        /**
         * A serialized instance of [GameSettings]. **Required**
         */
        const val EXTRA_GAME_SETTINGS = "game_settings"
    }

    private val clientModel by viewModels<ClientGameManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        clientModel.initializeFromIntent(intent)

        if (!clientModel.isValid) {
            // Missing extras
            Log.w(TAG, "Intent not valid. Missing extras.")
            Log.w(TAG, "  hostEndpointId = ${clientModel.hostEndpointId}")
            Log.w(TAG, "  gameSettings = ${clientModel.gameSettings}")
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        clientModel.requestGameStatus()
    }
}