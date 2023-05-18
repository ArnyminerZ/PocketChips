package com.arnyminerz.pocketchips.connections

import android.app.Application
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.pocketchips.BuildConfig
import com.arnyminerz.pocketchips.storage.PREF_NAME
import com.arnyminerz.pocketchips.storage.PrefStorage
import com.arnyminerz.pocketchips.utils.async
import com.arnyminerz.pocketchips.utils.context
import com.arnyminerz.pocketchips.utils.get
import com.arnyminerz.pocketchips.utils.remove
import com.arnyminerz.pocketchips.utils.set
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.flow.first
import java.util.concurrent.locks.ReentrantLock

/**
 * Provides some utility functions to help interacting with the Nearby Connections API.
 * @see <a href="https://developers.google.com/nearby/connections/overview">Documentation</a>
 */
abstract class HostConnectionsManager(application: Application): AndroidViewModel(application) {
    companion object {
        private const val SERVICE_ID = BuildConfig.APPLICATION_ID

        private const val TAG = "HostConnectionsManager"

        /** Returns all the permissions required for the currently running Android Version. */
        val PERMISSIONS_REQUIRED: Array<String>
            get() {
                val permissions = mutableSetOf(
                    android.Manifest.permission.ACCESS_WIFI_STATE,
                    android.Manifest.permission.CHANGE_WIFI_STATE,
                )

                if (SDK_INT <= Build.VERSION_CODES.R) { // 30
                    permissions.add(android.Manifest.permission.BLUETOOTH)
                    permissions.add(android.Manifest.permission.BLUETOOTH_ADMIN)
                }
                if (SDK_INT <= Build.VERSION_CODES.P) { // 28
                    permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                }
                if (SDK_INT >= Build.VERSION_CODES.Q && SDK_INT <= Build.VERSION_CODES.S) { // 29-31
                    permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }
                if (SDK_INT >= Build.VERSION_CODES.S) { // 31
                    permissions.add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
                    permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
                    permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
                }
                if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // 33
                    permissions.add(android.Manifest.permission.NEARBY_WIFI_DEVICES)
                }
                return permissions.toTypedArray()
            }
    }

    /** Provides access to the Nearby Connections API lazily. */
    private val connectionsClient by lazy { Nearby.getConnectionsClient(context) }

    private val connectionLifecycleCallback = object: ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            // Add the endpoint to awaitingEndpoints
            addAwaiting(endpointId, connectionInfo)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    // Add device to the list of connected endpoints
                    addConnected(endpointId)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    // Remove from connected just in case
                    removeConnected(endpointId)
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    // TODO: Create holder for error messages
                    // Remove from connected just in case
                    removeConnected(endpointId)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            removeConnected(endpointId)
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                Log.i(TAG, "Received data from $endpointId.")
                connectedEndpointsMutable.get(connectedEndpointsLock, endpointId)?.postValue(payload.asBytes())
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Since we are working with byte transfers, this can be ignored
        }
    }

    /** Locks [awaitingEndpointsMutable] so that multiple threads don't update the value at the same time. */
    private val awaitingEndpointsLock = ReentrantLock()
    private val awaitingEndpointsMutable = MutableLiveData<Map<String, ConnectionInfo>>()
    /** Holds the devices that are awaiting for authorization. */
    val awaitingEndpoints: LiveData<Map<String, ConnectionInfo>> get() = awaitingEndpointsMutable

    private val connectedEndpointsLock = ReentrantLock()
    private val connectedEndpointsMutable = MutableLiveData<Map<String, MutableLiveData<ByteArray>>>()
    /** Holds the devices that have been successfully connected to the device. */
    val connectedEndpoints: LiveData<Map<String, MutableLiveData<ByteArray>>> get() = connectedEndpointsMutable

    private val isAdvertisingMutable = MutableLiveData(false)
    val isAdvertising: LiveData<Boolean> get() = isAdvertisingMutable

    private val operationPendingMutable = MutableLiveData(false)
    val operationPending: LiveData<Boolean> get() = operationPendingMutable


    /** The display name of the current device for advertising. */
    private suspend fun getName(): String =
        PrefStorage.getInstance(context)[PREF_NAME].first()

    /** Adds the given endpoint to [awaitingEndpointsMutable] */
    private fun addAwaiting(endpointId: String, connectionInfo: ConnectionInfo) {
        awaitingEndpointsMutable.set(awaitingEndpointsLock, endpointId, connectionInfo)
    }

    /** Removes the given endpoint from [awaitingEndpointsMutable] */
    private fun removeAwaiting(endpointId: String) {
        awaitingEndpointsMutable.remove(awaitingEndpointsLock, endpointId)
    }

    /** Adds the given endpoint to [connectedEndpointsMutable] */
    private fun addConnected(endpointId: String) {
        connectedEndpointsMutable.set(awaitingEndpointsLock, endpointId, MutableLiveData())
    }

    /** Removes the given endpoint from [connectedEndpointsMutable] */
    private fun removeConnected(endpointId: String) {
        connectedEndpointsMutable.remove(awaitingEndpointsLock, endpointId)
    }

    /** Authorizes a device from [awaitingEndpoints] to be connected. */
    fun authorizeAwaiting(endpointId: String) {
        connectionsClient.acceptConnection(endpointId, payloadCallback)
        removeAwaiting(endpointId)
    }

    /** Authorizes a device from [awaitingEndpoints] to be connected. */
    fun rejectAwaiting(endpointId: String) {
        connectionsClient.rejectConnection(endpointId)
        removeAwaiting(endpointId)
    }

    /** Gets called when the server starts advertising. */
    open fun onStartAdvertising() {
        Log.i(TAG, "Started advertising...")
        operationPendingMutable.postValue(false)
        isAdvertisingMutable.postValue(true)
    }

    /** Gets called when the server could not start advertising. */
    open fun onStartAdvertisingError(error: Exception) {
        Log.e(TAG, "Could not start advertising.", error)
        operationPendingMutable.postValue(false)
        isAdvertisingMutable.postValue(false)
    }

    /**
     * Starts advertising for new devices to connect.
     * @see onStartAdvertising
     * @see onStartAdvertisingError
     * @see isAdvertising
     */
    fun startAdvertising() = async {
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_STAR)
            .build()

        operationPendingMutable.postValue(true)
        connectionsClient
            .startAdvertising(getName(), SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
            .addOnSuccessListener { onStartAdvertising() }
            .addOnFailureListener { onStartAdvertisingError(it) }
    }

    /** Stops advertising for nearby devices. */
    fun stopAdvertising() = async {
        operationPendingMutable.postValue(true)
        connectionsClient.stopAdvertising()
        operationPendingMutable.postValue(false)
        isAdvertisingMutable.postValue(false)
    }
}