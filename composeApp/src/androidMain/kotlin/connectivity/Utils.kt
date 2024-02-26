package connectivity

import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo

/**
 * Builds a new [DiscoveredEndpoint] from the given [endpointId] and [info].
 * Usually used in discovery callback to build new [DiscoveredEndpoint]s.
 */
fun DiscoveredEndpoint.Companion.build(
    endpointId: String,
    info: DiscoveredEndpointInfo
): DiscoveredEndpoint {
    return DiscoveredEndpoint(endpointId, info.endpointName, info.endpointInfo, info.serviceId)
}
