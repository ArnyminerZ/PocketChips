package connectivity

import kotlinx.coroutines.flow.MutableStateFlow

actual object ConnectivityManager {
    // TODO: https://developers.google.com/nearby/connections/swift/get-started

    actual val discoveredEndpoints: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    actual fun advertise(displayName: String) {
        TODO("Not yet implemented")
    }

    actual fun discover() {
        TODO("Not yet implemented")
    }
}
