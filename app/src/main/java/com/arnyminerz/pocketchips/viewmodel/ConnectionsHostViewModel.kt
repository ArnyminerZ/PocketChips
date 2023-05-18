package com.arnyminerz.pocketchips.viewmodel

import android.app.Application
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.pocketchips.connections.HostConnectionsManager
import com.arnyminerz.pocketchips.utils.async
import com.arnyminerz.pocketchips.utils.context

class ConnectionsHostViewModel(application: Application): HostConnectionsManager(application) {
    companion object {
        private const val TAG = "ConnectionsHostViewModel"
    }

    val allPermissionsGranted = MutableLiveData(false)


    fun updateAllPermissionsGranted() = async {
        val granted = PERMISSIONS_REQUIRED.all { permission ->
            (ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED).also {
                if (!it) Log.w(TAG, "Permission not granted: $permission")
            }
        }
        allPermissionsGranted.postValue(granted)
    }
}
