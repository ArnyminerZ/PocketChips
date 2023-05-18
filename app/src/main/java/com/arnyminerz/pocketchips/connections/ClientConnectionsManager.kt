package com.arnyminerz.pocketchips.connections

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.pocketchips.utils.async
import com.arnyminerz.pocketchips.utils.context
import com.arnyminerz.pocketchips.utils.get
import com.arnyminerz.pocketchips.utils.remove
import com.arnyminerz.pocketchips.utils.set
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import java.util.concurrent.locks.ReentrantLock

class ClientConnectionsManager(application: Application): ConnectionsManager(application) {
    companion object {
        private const val TAG = "ClientConnectionsManager"
    }

    /** Provides access to the Nearby Connections API lazily. */
    private val connectionsClient by lazy { Nearby.getConnectionsClient(context) }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
            Log.i(TAG, "Found new endpoint: $endpointId")
            availableEndpointsMutable.set(availableEndpointsLock, endpointId, endpointInfo)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.i(TAG, "Lost endpoint: $endpointId")
            availableEndpointsMutable.remove(availableEndpointsLock, endpointId)
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.i(TAG, "Initiated connection to endpoint $endpointId. Name = ${connectionInfo.endpointName}")
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            connectedToInfoMutable.postValue(connectionInfo)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.i(TAG, "Connected to endpoint $endpointId!")
                    availableEndpointsMutable.remove(availableEndpointsLock, endpointId)
                    connectedToMutable.postValue(endpointId)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.i(TAG, "Connection to endpoint $endpointId rejected!")
                    connectedToMutable.postValue(null)
                    connectedToInfoMutable.postValue(null)
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    // TODO: Create holder for error messages
                    Log.e(TAG, "Could not connect to $endpointId. Error: ${result.status.statusMessage}")
                    connectedToMutable.postValue(null)
                    connectedToInfoMutable.postValue(null)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.w(TAG, "Disconnected from $endpointId")
            connectedToMutable.postValue(null)
            connectedToInfoMutable.postValue(null)
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                Log.i(TAG, "Received data from $endpointId: ${payload.asBytes()?.toString()}")
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Since we are working with byte transfers, this can be ignored
        }
    }

    /** Locks [availableEndpointsMutable] so that multiple threads don't update the value at the same time. */
    private val availableEndpointsLock = ReentrantLock()
    private val availableEndpointsMutable = MutableLiveData<Map<String, DiscoveredEndpointInfo>>()
    /** Holds the devices that are available to connect. */
    val availableEndpoints: LiveData<Map<String, DiscoveredEndpointInfo>> get() = availableEndpointsMutable

    private val isDiscoveringMutable = MutableLiveData(false)
    val isDiscovering: LiveData<Boolean> get() = isDiscoveringMutable

    private val operationPendingMutable = MutableLiveData(false)
    val operationPending: LiveData<Boolean> get() = operationPendingMutable

    private val connectedToMutable = MutableLiveData<String?>()
    val connectedTo: LiveData<String?> get() = connectedToMutable

    private val connectedToInfoMutable = MutableLiveData<ConnectionInfo?>()
    val connectedToInfo: LiveData<ConnectionInfo?> get() = connectedToInfoMutable


    /** Gets called when the device requests connection to another one. */
    fun onRequestedConnection(endpointId: String) {
        Log.i(TAG, "Requested connection to $endpointId")
        operationPendingMutable.postValue(false)
    }

    /** Gets called when the device requests connection to another one. */
    fun onRequestedConnectionError(endpointId: String, error: Exception) {
        Log.i(TAG, "Could not request connection to $endpointId.", error)
        operationPendingMutable.postValue(false)
    }

    fun requestConnection(endpointId: String) = async {
        Log.i(TAG, "Requesting connection to $endpointId")
        operationPendingMutable.postValue(true)
        connectionsClient
            .requestConnection(getName(), endpointId, connectionLifecycleCallback)
            .addOnSuccessListener { onRequestedConnection(endpointId) }
            .addOnFailureListener { onRequestedConnectionError(endpointId, it) }
    }

    /** Gets called when the device starts discovering. */
    fun onStartDiscovery() {
        Log.i(TAG, "Started advertising...")
        operationPendingMutable.postValue(false)
        isDiscoveringMutable.postValue(true)
    }

    /** Gets called when the device could not start discovering. */
    fun onStartDiscoveryError(error: Exception) {
        Log.e(TAG, "Could not start advertising.", error)
        operationPendingMutable.postValue(false)
        isDiscoveringMutable.postValue(false)
    }

    fun startDiscovery() = async {
        Log.i(TAG, "Starting discovery...")
        operationPendingMutable.postValue(true)
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build()
        connectionsClient
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener { onStartDiscovery() }
            .addOnFailureListener { onStartDiscoveryError(it) }
    }

    fun stopDiscovery() = async {
        Log.i(TAG, "Stopping discovery...")
        operationPendingMutable.postValue(true)
        connectionsClient.stopDiscovery()
        operationPendingMutable.postValue(false)
        isDiscoveringMutable.postValue(false)
    }
}