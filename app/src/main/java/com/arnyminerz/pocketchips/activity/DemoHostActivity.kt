package com.arnyminerz.pocketchips.activity

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.arnyminerz.pocketchips.R
import com.arnyminerz.pocketchips.connections.ConnectionsManager
import com.arnyminerz.pocketchips.connections.HostConnectionsManager
import com.arnyminerz.pocketchips.databinding.DemoHostActivityBinding
import com.arnyminerz.pocketchips.game.GameSettings
import com.arnyminerz.pocketchips.ui.list.AwaitingEndpointsAdapter
import com.arnyminerz.pocketchips.ui.list.ConnectedEndpointsAdapter

class DemoHostActivity : AppCompatActivity() {
    private val model: HostConnectionsManager by viewModels()

    private val permissionRequestLauncher = registerForActivityResult(RequestMultiplePermissions()) {
        model.allPermissionsGranted.postValue(it.all { (_, granted) -> granted })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set some default values for GameSettings
        model.gameSettings = GameSettings()

        val binding = DataBindingUtil
            .setContentView<DemoHostActivityBinding>(this, R.layout.demo_host_activity)
            .also {
                it.lifecycleOwner = this
                it.model = model
            }

        val availableDevicesAdapter = AwaitingEndpointsAdapter(model)
        binding.availableDevices.layoutManager = LinearLayoutManager(this)
        binding.availableDevices.adapter = availableDevicesAdapter

        val connectedDevicesAdapter = ConnectedEndpointsAdapter()
        binding.connectedDevices.layoutManager = LinearLayoutManager(this)
        binding.connectedDevices.adapter = connectedDevicesAdapter

        model.awaitingEndpoints.observe(this) {
            availableDevicesAdapter.submitList(it.toList())
        }
        model.connectedEndpoints.observe(this) { map ->
            connectedDevicesAdapter.submitList(map.map { it.key })
        }

        model.allPermissionsGranted.observe(this) { granted ->
            if (!granted) permissionRequestLauncher.launch(ConnectionsManager.PERMISSIONS_REQUIRED)
        }
    }

    override fun onResume() {
        super.onResume()

        model.updateAllPermissionsGranted()
    }
}