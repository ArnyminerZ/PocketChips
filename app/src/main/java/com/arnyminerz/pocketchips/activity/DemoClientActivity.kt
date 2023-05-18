package com.arnyminerz.pocketchips.activity

import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.arnyminerz.pocketchips.R
import com.arnyminerz.pocketchips.connections.ClientConnectionsManager
import com.arnyminerz.pocketchips.connections.ConnectionsManager
import com.arnyminerz.pocketchips.databinding.DemoClientActivityBinding
import com.arnyminerz.pocketchips.ui.list.AvailableEndpointsAdapter

class DemoClientActivity : AppCompatActivity() {
    private val model: ClientConnectionsManager by viewModels()

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
}