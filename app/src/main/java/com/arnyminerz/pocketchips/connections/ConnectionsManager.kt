package com.arnyminerz.pocketchips.connections

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.pocketchips.BuildConfig
import com.arnyminerz.pocketchips.storage.PREF_NAME
import com.arnyminerz.pocketchips.storage.PrefStorage
import com.arnyminerz.pocketchips.utils.async
import com.arnyminerz.pocketchips.utils.context
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.flow.first

abstract class ConnectionsManager(application: Application): AndroidViewModel(application) {
    companion object {
        private const val TAG = "ConnectionsManager"

        /** Returns all the permissions required for the currently running Android Version. */
        @JvmStatic
        val PERMISSIONS_REQUIRED: Array<String>
            get() {
                val permissions = mutableSetOf(
                    android.Manifest.permission.ACCESS_WIFI_STATE,
                    android.Manifest.permission.CHANGE_WIFI_STATE,
                )

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) { // 30
                    permissions.add(android.Manifest.permission.BLUETOOTH)
                    permissions.add(android.Manifest.permission.BLUETOOTH_ADMIN)
                }
                // if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // 28
                    permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                // }
                // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) { // 29-31
                    permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
                // }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // 31
                    permissions.add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
                    permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
                    permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // 33
                    permissions.add(android.Manifest.permission.NEARBY_WIFI_DEVICES)
                }
                return permissions.toTypedArray()
            }

        @JvmStatic
        protected val STRATEGY: Strategy = Strategy.P2P_STAR

        @JvmStatic
        protected val SERVICE_ID = BuildConfig.APPLICATION_ID
    }

    val allPermissionsGranted = MutableLiveData(false)


    /** The display name of the current device for advertising. */
    protected suspend fun getName(): String =
        PrefStorage.getInstance(context)[PREF_NAME].first()

    fun updateAllPermissionsGranted() = async {
        val granted = PERMISSIONS_REQUIRED.all { permission ->
            (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED).also {
                if (!it) Log.w(TAG, "Permission not granted: $permission")
            }
        }
        allPermissionsGranted.postValue(granted)
    }
}