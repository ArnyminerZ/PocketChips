package connectivity

import com.arnyminerz.pocketchips.applicationContext
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
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
import com.google.android.gms.nearby.connection.Strategy
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual object ConnectivityManager {
    private const val SERVICE_ID = "poker_chips"

    actual val discoveredEndpoints: MutableStateFlow<List<DiscoveredEndpoint>> =
        MutableStateFlow(emptyList())
    actual val connectedEndpoints: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    actual val state: MutableStateFlow<ConnectivityState> = MutableStateFlow(ConnectivityState.IDLE)

    private val client by lazy {
        Nearby.getConnectionsClient(applicationContext)
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            // Automatically accept connections
            client.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            val status = resolution.status
            when (val statusCode = status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Napier.i { "Connected to endpoint $endpointId" }
                    connectedEndpoints.value = connectedEndpoints.value + endpointId
                }

                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Napier.e { "Could not connect to endpoint $endpointId. Connection rejected." }
                }

                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Napier.e { "Could not connect to endpoint $endpointId: ${status.statusMessage}" }
                }

                else -> {
                    Napier.e { "Could not connect to endpoint $endpointId. Error (${statusCode}): ${status.statusMessage}" }
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Napier.i { "Disconnected from endpoint $endpointId" }
            connectedEndpoints.value = connectedEndpoints.value - endpointId
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Napier.d { "Found new endpoint. Id=$endpointId, name=${info.endpointName}" }
            val endpoint = DiscoveredEndpoint.build(endpointId, info)
            discoveredEndpoints.value = discoveredEndpoints.value + endpoint
        }

        override fun onEndpointLost(endpointId: String) {
            Napier.d { "Lost endpoint. Id=$endpointId" }
            discoveredEndpoints.value = discoveredEndpoints.value
                .toMutableList()
                .apply { removeIf { it.endpointId == endpointId } }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> {
                    val message = payload.asBytes()!!.decodeToString()
                    Napier.i { "Received message from $endpointId: $message" }
                }
                Payload.Type.FILE -> {
                    Napier.i { "Received file from $endpointId." }
                }
                Payload.Type.STREAM -> {
                    Napier.i { "Received stream from $endpointId." }
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Napier.i { "Payload transfer update ($endpointId)." }
        }
    }

    actual fun advertise(displayName: String) {
        // Do not start if already advertising
        if (state.value == ConnectivityState.ADVERTISING_AND_DISCOVERING) return
        if (state.value == ConnectivityState.ADVERTISING) return

        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()
        Napier.d { "Starting advertisement as \"$displayName\"..." }
        client
            .startAdvertising(
                displayName,
                SERVICE_ID,
                connectionLifecycleCallback,
                advertisingOptions
            )
            .addOnSuccessListener {
                Napier.i { "Advertisement started." }

                state.value = if (state.value == ConnectivityState.DISCOVERING)
                    ConnectivityState.ADVERTISING_AND_DISCOVERING
                else
                    ConnectivityState.ADVERTISING
            }
            .addOnFailureListener { error ->
                Napier.e(error) { "Could not start advertising for other devices." }

                state.value = if (state.value == ConnectivityState.ADVERTISING_AND_DISCOVERING)
                    ConnectivityState.DISCOVERING
                else
                    ConnectivityState.IDLE
            }
    }

    actual fun discover() {
        // Do not start if already discovering
        if (state.value == ConnectivityState.ADVERTISING_AND_DISCOVERING) return
        if (state.value == ConnectivityState.DISCOVERING) return

        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()
        Napier.d { "Starting discovery..." }
        client.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener {
                Napier.i { "Discovery started." }

                state.value = if (state.value == ConnectivityState.ADVERTISING)
                    ConnectivityState.ADVERTISING_AND_DISCOVERING
                else
                    ConnectivityState.DISCOVERING

                CoroutineScope(Dispatchers.IO).launch {
                    // Stop discovering after 10 seconds
                    delay(10000)
                    client.stopDiscovery()
                    Napier.i { "Discovery stopped after 10 seconds." }

                    state.value = if (state.value == ConnectivityState.ADVERTISING_AND_DISCOVERING)
                        ConnectivityState.ADVERTISING
                    else
                        ConnectivityState.IDLE
                }
            }
            .addOnFailureListener { error ->
                Napier.e(error) { "Could not start discovering devices." }

                state.value = if (state.value == ConnectivityState.ADVERTISING_AND_DISCOVERING)
                    ConnectivityState.ADVERTISING
                else
                    ConnectivityState.IDLE
            }
    }

    actual fun stop() {
        client.stopDiscovery()
        client.stopAdvertising()
    }

    actual suspend fun connect(displayName: String, endpoint: DiscoveredEndpoint) {
        return suspendCoroutine { cont ->
            Napier.d { "Requesting connection to endpoint ${endpoint.endpointId} as $displayName..." }
            client.requestConnection(displayName, endpoint.endpointId, connectionLifecycleCallback)
                .addOnSuccessListener {
                    Napier.i { "Requested endpoint connection successfully." }
                    cont.resume(Unit)
                }
                .addOnFailureListener { error ->
                    Napier.e(error) { "Could not connect to endpoint." }
                    cont.resumeWithException(error)
                }
        }
    }
}
