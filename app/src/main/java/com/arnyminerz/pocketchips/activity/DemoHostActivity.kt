package com.arnyminerz.pocketchips.activity

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.arnyminerz.pocketchips.R
import com.arnyminerz.pocketchips.connections.HostConnectionsManager
import com.arnyminerz.pocketchips.databinding.DemoHostActivityBinding
import com.arnyminerz.pocketchips.viewmodel.ConnectionsHostViewModel

class DemoHostActivity : AppCompatActivity() {
    private val model: ConnectionsHostViewModel by viewModels()

    private val permissionRequestLauncher = registerForActivityResult(RequestMultiplePermissions()) {
        model.allPermissionsGranted.postValue(it.all { (_, granted) -> granted })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil
            .setContentView<DemoHostActivityBinding>(this, R.layout.demo_host_activity)
            .also {
                it.lifecycleOwner = this
                it.model = model
            }

        model.allPermissionsGranted.observe(this) { granted ->
            if (!granted) permissionRequestLauncher.launch(HostConnectionsManager.PERMISSIONS_REQUIRED)
        }
    }

    override fun onResume() {
        super.onResume()

        model.updateAllPermissionsGranted()
    }
}