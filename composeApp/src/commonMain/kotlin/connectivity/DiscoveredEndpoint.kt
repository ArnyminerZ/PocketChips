package connectivity

data class DiscoveredEndpoint(
    val endpointId: String,
    val endpointName: String,
    val endpointInfo: ByteArray,
    val serviceId: String
) {
    companion object;

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DiscoveredEndpoint

        if (endpointId != other.endpointId) return false
        if (endpointName != other.endpointName) return false
        if (!endpointInfo.contentEquals(other.endpointInfo)) return false
        return serviceId == other.serviceId
    }

    override fun hashCode(): Int {
        var result = endpointId.hashCode()
        result = 31 * result + endpointName.hashCode()
        result = 31 * result + endpointInfo.contentHashCode()
        result = 31 * result + serviceId.hashCode()
        return result
    }
}
