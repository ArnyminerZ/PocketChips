package com.arnyminerz.pocketchips.connections

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.pocketchips.communications.Serializable
import com.arnyminerz.pocketchips.communications.SerializedObject
import com.arnyminerz.pocketchips.communications.deserialize
import com.arnyminerz.pocketchips.communications.sendPayload
import com.arnyminerz.pocketchips.game.GameSettings
import com.arnyminerz.pocketchips.game.requests.REQUEST_STATUS
import com.arnyminerz.pocketchips.game.requests.Request
import com.arnyminerz.pocketchips.game.response.GameStatus
import com.arnyminerz.pocketchips.utils.async
import com.arnyminerz.pocketchips.utils.get
import com.arnyminerz.pocketchips.utils.remove
import com.arnyminerz.pocketchips.utils.set
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import java.util.concurrent.locks.ReentrantLock

/**
 * Provides some utility functions to help interacting with the Nearby Connections API.
 * @see <a href="https://developers.google.com/nearby/connections/overview">Documentation</a>
 */
class HostConnectionsManager(application: Application) : ConnectionsManager(application) {
    companion object {
        private const val TAG = "HostConnectionsManager"
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            // Add the endpoint to awaitingEndpoints
            Log.i(
                TAG,
                "Initiated connection to endpoint $endpointId. Name = ${connectionInfo.endpointName}"
            )
            addAwaiting(endpointId, connectionInfo)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    // Add device to the list of connected endpoints
                    Log.i(TAG, "Connected with endpoint $endpointId!")
                    addConnected(endpointId)
                }

                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    // Remove from connected just in case
                    Log.i(TAG, "Connection with endpoint $endpointId rejected!")
                    removeConnected(endpointId)
                }

                ConnectionsStatusCodes.STATUS_ERROR -> {
                    // TODO: Create holder for error messages
                    // Remove from connected just in case
                    Log.e(
                        TAG,
                        "Could not connect with $endpointId. Error: ${result.status.statusMessage}"
                    )
                    removeConnected(endpointId)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.w(TAG, "Disconnected with $endpointId")
            removeConnected(endpointId)
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                Log.i(TAG, "Received data from $endpointId.")
                val bytes: ByteArray = payload.asBytes()!!
                onPayloadReceived?.invoke(endpointId, bytes)
                connectedEndpointsMutable.get(connectedEndpointsLock, endpointId)?.postValue(bytes)

                processReceivedPayload(endpointId, bytes.deserialize())
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
    private val connectedEndpointsMutable =
        MutableLiveData<Map<String, MutableLiveData<ByteArray>>>()

    /** Holds the devices that have been successfully connected to the device. */
    val connectedEndpoints: LiveData<Map<String, MutableLiveData<ByteArray>>> get() = connectedEndpointsMutable

    private val isAdvertisingMutable = MutableLiveData(false)
    val isAdvertising: LiveData<Boolean> get() = isAdvertisingMutable

    private val operationPendingMutable = MutableLiveData(false)
    val operationPending: LiveData<Boolean> get() = operationPendingMutable

    /** Gets called whenever a connected device sends some data. */
    var onPayloadReceived: ((endpointId: String, data: ByteArray) -> Unit)? = null

    var gameSettings: GameSettings? = null


    /** Adds the given endpoint to [awaitingEndpointsMutable] */
    private fun addAwaiting(endpointId: String, connectionInfo: ConnectionInfo) {
        awaitingEndpointsMutable.set(awaitingEndpointsLock, endpointId, connectionInfo)
    }

    /** Removes the given endpoint from [awaitingEndpointsMutable] */
    private fun removeAwaiting(endpointId: String) {
        awaitingEndpointsMutable.remove(awaitingEndpointsLock, endpointId)
    }

    /** Adds the given endpoint to [connectedEndpointsMutable], then sends all the game information */
    private fun addConnected(endpointId: String) {
        connectedEndpointsMutable.set(awaitingEndpointsLock, endpointId, MutableLiveData())
        sendPayload(endpointId, gameSettings!!)
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
    fun onStartAdvertising() {
        Log.i(TAG, "Started advertising...")
        operationPendingMutable.postValue(false)
        isAdvertisingMutable.postValue(true)
    }

    /** Gets called when the server could not start advertising. */
    fun onStartAdvertisingError(error: Exception) {
        Log.e(TAG, "Could not start advertising.", error)
        operationPendingMutable.postValue(false)
        isAdvertisingMutable.postValue(false)
    }

    /**
     * Starts advertising for new devices to connect.
     * @throws IllegalStateException If [gameSettings] has not been initialized.
     * @see onStartAdvertising
     * @see onStartAdvertisingError
     * @see isAdvertising
     */
    fun startAdvertising() = async {
        if (gameSettings == null)
            throw IllegalStateException("Tried to start advertising without initializing gameSettings.")

        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        operationPendingMutable.postValue(true)
        connectionsClient
            .startAdvertising(
                getName(),
                SERVICE_ID,
                connectionLifecycleCallback,
                advertisingOptions
            )
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

    /**
     * Sends the given payload to all the connected devices.
     * @throws IllegalStateException If there are no connected devices.
     */
    fun sendPayload(bytes: ByteArray) {
        val payload = Payload.fromBytes(bytes)
        val endpoints = connectedEndpointsMutable.get(connectedEndpointsLock)
        if (endpoints?.isNotEmpty() != true)
            throw IllegalStateException("Currently there are no connected devices. Payload cannot be sent.")

        val endpointIds = endpoints.keys.toList()
        Log.d(TAG, "Sending payload to endpoints ($endpointIds): $bytes")
        connectionsClient.sendPayload(endpointIds, payload)
    }

    /**
     * Sends the given payload to given connected device.
     */
    fun sendPayload(endpointId: String, bytes: ByteArray) {
        val payload = Payload.fromBytes(bytes)
        Log.d(TAG, "Sending payload to endpoint ($endpointId): $bytes")
        connectionsClient.sendPayload(endpointId, payload)
    }

    /**
     * Sends the given payload to all the connected devices.
     * @throws IllegalStateException If there are no connected devices.
     */
    fun sendPayload(serializedObject: SerializedObject) {
        val endpoints = connectedEndpointsMutable.get(connectedEndpointsLock)
        if (endpoints?.isNotEmpty() != true)
            throw IllegalStateException("Currently there are no connected devices. Payload cannot be sent.")

        val endpointIds = endpoints.keys.toList()
        Log.d(TAG, "Sending payload to endpoints ($endpointIds)")
        connectionsClient.sendPayload(endpointIds, serializedObject)
    }

    /**
     * Sends the given payload to given connected device.
     */
    fun sendPayload(endpointId: String, serializedObject: SerializedObject) {
        Log.d(TAG, "Sending payload to endpoint ($endpointId)")
        connectionsClient.sendPayload(endpointId, serializedObject)
    }

    /**
     * Sends the given payload to all the connected devices.
     * @throws IllegalStateException If there are no connected devices.
     */
    fun sendPayload(obj: Serializable) {
        sendPayload(obj.serialize())
    }

    /**
     * Sends the given payload to given connected device.
     */
    fun sendPayload(endpointId: String, obj: Serializable) {
        sendPayload(endpointId, obj.serialize())
    }

    /**
     * Processes some received payload, and gives answer if necessary.
     */
    private fun processReceivedPayload(endpointId: String, payload: SerializedObject) {
        val request = payload.deserializeOrNull<Request<*, *>, Request.Companion>()
        val shouldGiveResponse: Serializable? = when (request?.name) {
            REQUEST_STATUS -> GameStatus.Waiting
            else -> null
        }
        shouldGiveResponse?.let {
            sendPayload(endpointId, it)
        }
    }
}