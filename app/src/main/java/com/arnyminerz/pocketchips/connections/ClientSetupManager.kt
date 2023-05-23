package com.arnyminerz.pocketchips.connections

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.pocketchips.communications.SerializedObject
import com.arnyminerz.pocketchips.game.GameSettings
import com.arnyminerz.pocketchips.utils.async
import com.arnyminerz.pocketchips.utils.remove
import com.arnyminerz.pocketchips.utils.set
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import java.util.concurrent.locks.ReentrantLock

class ClientSetupManager(application: Application) : ClientConnectionsManager(application) {
    companion object {
        private const val TAG = "ClientSetupManager"
    }

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
            requestingConnectionToMutable.postValue(endpointId)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.i(TAG, "Connected to endpoint $endpointId!")
                    availableEndpointsMutable.remove(availableEndpointsLock, endpointId)
                    connectedToMutable.postValue(endpointId)
                    requestingConnectionToMutable.postValue(null)

                    // Once connected, stop discovery
                    stopDiscovery()
                }

                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.i(TAG, "Connection to endpoint $endpointId rejected!")
                    connectedToMutable.postValue(null)
                    connectedToInfoMutable.postValue(null)
                    requestingConnectionToMutable.postValue(null)
                }

                ConnectionsStatusCodes.STATUS_ERROR -> {
                    // TODO: Create holder for error messages
                    Log.e(TAG, "Could not connect to $endpointId. Error: ${result.status.statusMessage}")
                    connectedToMutable.postValue(null)
                    connectedToInfoMutable.postValue(null)
                    requestingConnectionToMutable.postValue(null)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.w(TAG, "Disconnected from $endpointId")
            connectedToMutable.postValue(null)
            connectedToInfoMutable.postValue(null)
            requestingConnectionToMutable.postValue(null)
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

    private val requestingConnectionToMutable = MutableLiveData<String?>()
    val requestingConnectionTo: LiveData<String?> get() = requestingConnectionToMutable

    private val connectedToInfoMutable = MutableLiveData<ConnectionInfo?>()
    val connectedToInfo: LiveData<ConnectionInfo?> get() = connectedToInfoMutable

    private val gameSettingsMutable = MutableLiveData<GameSettings?>()
    val gameSettings: LiveData<GameSettings?> get() = gameSettingsMutable


    /** Gets called when the device requests connection to another one. */
    fun onRequestedConnection(endpointId: String) {
        Log.i(TAG, "Requested connection to $endpointId")
        operationPendingMutable.postValue(false)
        requestingConnectionToMutable.postValue(endpointId)
    }

    /** Gets called when the device requests connection to another one. */
    fun onRequestedConnectionError(endpointId: String, error: Exception) {
        Log.i(TAG, "Could not request connection to $endpointId.", error)
        operationPendingMutable.postValue(false)
        requestingConnectionToMutable.postValue(null)
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
        Log.i(TAG, "Started discovery...")
        operationPendingMutable.postValue(false)
        isDiscoveringMutable.postValue(true)
    }

    /** Gets called when the device could not start discovering. */
    fun onStartDiscoveryError(error: Exception) {
        Log.e(TAG, "Could not start discovery.", error)
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

    override fun processPayload(endpointId: String, obj: SerializedObject) {
        val gameSettings = obj.deserializeOrNull<GameSettings, GameSettings.Companion>()
        if (gameSettings != null) {
            Log.i(TAG, "Received GameSettings! Info: $gameSettings")
            gameSettingsMutable.postValue(gameSettings)
        } else
            Log.i(TAG, "Received data from $endpointId: $obj")
    }
}