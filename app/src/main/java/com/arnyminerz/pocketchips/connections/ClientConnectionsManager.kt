package com.arnyminerz.pocketchips.connections

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.pocketchips.communications.Serializable
import com.arnyminerz.pocketchips.communications.SerializedObject
import com.arnyminerz.pocketchips.communications.Serializer
import com.arnyminerz.pocketchips.communications.deserialize
import com.arnyminerz.pocketchips.communications.sendPayload
import com.arnyminerz.pocketchips.game.requests.Request
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

abstract class ClientConnectionsManager(application: Application) :
    ConnectionsManager(application) {
    companion object {
        private const val TAG = "ClientConnectionsManager"
    }

    protected val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.i(TAG, "Received payload from $endpointId")
            if (payload.type == Payload.Type.BYTES) {
                val bytes = payload.asBytes()!!
                Log.d(TAG, "Payload bytes. Deserializing...")
                val deserialized = bytes.deserialize()
                Log.d(TAG, "Running payload callbacks (${payloadCallbacks.size})...")
                payloadCallbacks.forEach { (_, c) -> c(endpointId, deserialized) }
                Log.d(TAG, "Processing payload...")
                processPayload(endpointId, deserialized)
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Since we are working with byte transfers, this can be ignored
        }
    }

    protected val connectedToMutable = MutableLiveData<String?>()
    val connectedTo: LiveData<String?> get() = connectedToMutable

    @Volatile
    var payloadCallbacks =
        mapOf<String, (endpointId: String, payload: SerializedObject) -> Unit>()


    /** Gets called whenever the host sends data to the device. */
    protected open fun processPayload(endpointId: String, obj: SerializedObject) {
        Log.d(TAG, "Processing payload of data from $endpointId...")
    }

    /**
     * Sends the given payload to all the connected devices.
     * @throws IllegalStateException If currently not connected to any device.
     */
    fun sendPayload(bytes: ByteArray) {
        val endpointId = connectedToMutable.value
            ?: throw IllegalStateException("Currently not connected to any host device. Payload cannot be sent.")

        sendPayload(endpointId, bytes)
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
     * @throws IllegalStateException If currently not connected to any device.
     */
    fun sendPayload(payload: SerializedObject) {
        val endpointId = connectedToMutable.value
            ?: throw IllegalStateException("Currently not connected to any host device. Payload cannot be sent.")

        sendPayload(endpointId, payload)
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
    fun sendPayload(obj: Serializable) = sendPayload(obj.serialize())

    /**
     * Sends the given payload to given connected device.
     */
    fun sendPayload(endpointId: String, obj: Serializable) =
        sendPayload(endpointId, obj.serialize())

    /**
     * Sends a payload that expects a response from the host. Blocks thread until timeout or host
     * sends a response.
     * @throws NullPointerException If could not get access to the serializer.
     * @throws TimeoutCancellationException If [timeMillis] has passed before getting a response
     * from the host.
     */
    suspend inline fun <reified R : Serializable, reified S : Serializer<R>> sendPayloadResponse(
        request: Request<R, S>,
        timeMillis: Long = 10_000
    ): R {
        // Add listener
        Log.v("CCM", "Waiting for response of ${R::class.simpleName}...")
        // return withTimeout(timeMillis) {
            val id = UUID.randomUUID().toString()
            Log.v("CCM", "  Request ID: $id")

            return suspendCancellableCoroutine { cont ->
                Log.v("CCM", "Adding payload callback for request response ($id)...")
                payloadCallbacks = payloadCallbacks.toMutableMap().apply {
                    set(id) { _, payload ->
                        Log.v("CCM", "Received awaiting data from host: $payload")
                        payloadCallbacks = payloadCallbacks.toMutableMap().apply { remove(id) }
                        payload.deserializeOrNull<R, S>()?.let { cont.resume(it) }
                    }
                }

                // Send the payload data
                Log.v("CCM", "Sending request payload...")
                sendPayload(request)

                cont.invokeOnCancellation {
                    Log.v("CCM", "Request timed out, removing payload callback...")
                    payloadCallbacks = payloadCallbacks.toMutableMap().apply { remove(id) }
                }
            }
        // }
    }
}