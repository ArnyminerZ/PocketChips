package com.arnyminerz.pocketchips.activity

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.arnyminerz.pocketchips.R
import com.arnyminerz.pocketchips.communications.putExtra
import com.arnyminerz.pocketchips.connections.ClientSetupManager
import com.arnyminerz.pocketchips.connections.ConnectionsManager
import com.arnyminerz.pocketchips.databinding.DemoClientActivityBinding
import com.arnyminerz.pocketchips.ui.list.AvailableEndpointsAdapter

class DemoClientActivity : AppCompatActivity() {
    private val model: ClientSetupManager by viewModels()

    private val permissionRequestLauncher = registerForActivityResult(RequestMultiplePermissions()) {
        model.allPermissionsGranted.postValue(it.all { (_, granted) -> granted })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil
            .setContentView<DemoClientActivityBinding>(this, R.layout.demo_client_activity)
            .also {
                it.lifecycleOwner = this
                it.model = model
                it.activity = this
            }

        (binding.discoveringIcon.drawable as AnimationDrawable).start()

        val availableEndpointsAdapter = AvailableEndpointsAdapter(model)
        binding.availableDevices.layoutManager = LinearLayoutManager(this)
        binding.availableDevices.adapter = availableEndpointsAdapter

        model.availableEndpoints.observe(this) {
            availableEndpointsAdapter.submitList(it.toList())
        }

        model.allPermissionsGranted.observe(this) { granted ->
            if (!granted) permissionRequestLauncher.launch(ConnectionsManager.PERMISSIONS_REQUIRED)
        }
    }

    override fun onResume() {
        super.onResume()

        model.updateAllPermissionsGranted()
    }

    fun launchGame() {
        val endpointId = model.connectedTo.value ?: return
        val gameSettings = model.gameSettings.value ?: return

        startActivity(
            Intent(this, DemoGameActivity::class.java).apply {
                putExtra(DemoGameActivity.EXTRA_HOST_ENDPOINT_ID, endpointId)
                putExtra(DemoGameActivity.EXTRA_GAME_SETTINGS, gameSettings)
            }
        )
    }
}