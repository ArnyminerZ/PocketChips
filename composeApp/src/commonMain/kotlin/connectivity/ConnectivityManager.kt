package connectivity

import kotlinx.coroutines.flow.MutableStateFlow

expect object ConnectivityManager {
    val discoveredEndpoints: MutableStateFlow<List<DiscoveredEndpoint>>
    val connectedEndpoints: MutableStateFlow<List<String>>

    val state: MutableStateFlow<ConnectivityState>

    fun advertise(displayName: String)

    fun discover()

    fun stop()

    suspend fun connect(displayName: String, endpoint: DiscoveredEndpoint)
}
