package com.arnyminerz.pocketchips.communications

import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.tasks.Task

data class SerializedObject(
    @get:VisibleForTesting(PRIVATE)
    val data: Map<String, String>
) {
    companion object {
        /**
         * Stores all the characters that are not allowed to be contained in the serialized object
         * because they are used for serialization and deserialization.
         */
        private const val FORBIDDEN_CHARS = "|="
    }

    init {
        val reg = Regex("[$FORBIDDEN_CHARS]+", setOf(RegexOption.MULTILINE))
        val keys = data.keys.joinToString()
        val values = data.values.joinToString()
        if (reg.containsMatchIn(keys) || reg.containsMatchIn(values))
            throw IllegalArgumentException("The serialized object can't contain any of the following characters: $FORBIDDEN_CHARS")
    }

    val bytes: ByteArray by lazy {
        data.toList()
            .joinToString("|") { (k, v) -> "$k=$v" }
            .encodeToByteArray()
    }

    val size: Int = data.size


    override fun equals(other: Any?): Boolean {
        if (other is SerializedObject)
            return data.all { other.data[it.key] == it.value }
        if (other is Map<*, *>)
            return data.all { other[it.key] == it.value }
        return false
    }

    override fun hashCode(): Int = data.hashCode()

    /**
     * Gets the value stored at [key].
     * @throws NoSuchElementException If there's not an element on the given key.
     */
    operator fun get(key: String) = data.getValue(key)

    /**
     * Tries deserializing `this` using the given serializer type. Note that the serializer must
     * be an Object (`companion object`).
     */
    inline fun <T : Any, reified S : Serializer<T>> deserializeOrNull(): T? {
        // Use reflection to access the companion
        val serializer = S::class.objectInstance
        // Run deserialization
        return try {
            serializer?.fromSerializedObject(this)
        } catch (e: NoSuchElementException) {
            null
        } catch (e: NumberFormatException) {
            null
        }
    }
}

/**
 * Instantiates a new [SerializedObject] from the data passed.
 * @throws IllegalArgumentException If passed illegal data. See [SerializedObject]'s constructor.
 */
fun serialized(vararg pairs: Pair<String, String>) = SerializedObject(mapOf(*pairs))

/**
 * Converts a [ByteArray] obtained from [SerializedObject.bytes] and converts it back again to
 * [SerializedObject].
 */
fun ByteArray.deserialize(): SerializedObject =
    decodeToString()
        .split("|").mapNotNull { pair ->
            val splitPair = pair.split("=")
            if (splitPair.size != 2) return@mapNotNull null
            splitPair[0] to splitPair[1]
        }
        .toMap()
        .let { SerializedObject(it) }

/**
 * Sends a [Payload] to a given set of remote endpoints. Payloads can only be sent to remote
 * endpoints once a notice of connection acceptance has been delivered via
 * [ConnectionLifecycleCallback.onConnectionResult].
 *
 * Possible result status codes include:
 * - [ConnectionsStatusCodes.STATUS_OUT_OF_ORDER_API_CALL] if the device has not first performed
 *   advertisement or discovery (to set the Strategy).
 * - [ConnectionsStatusCodes.STATUS_ENDPOINT_UNKNOWN] if there's no active (or pending) connection
 *   to the remote endpoint.
 * - [ConnectionsStatusCodes.STATUS_OK] if none of the above errors occurred. Note that this
 *   indicates that Nearby Connections will attempt to send the Payload, but not that the send has
 *   successfully completed yet. Errors might still occur during transmission (and at different
 *   times for different endpoints), and will be delivered via
 *   [PayloadCallback.onPayloadTransferUpdate].
 * @param endpointIds The identifiers for the remote endpoints to which the payload should be sent.
 * @param serializedObject The Payload to be sent.
 * @return [Task] to access the status of the operation when available.
 */
fun ConnectionsClient.sendPayload(
    endpointIds: List<String>,
    serializedObject: SerializedObject
): Task<Void> {
    val payload = Payload.fromBytes(serializedObject.bytes)
    return sendPayload(endpointIds, payload)
}

/**
 * Sends a [Payload] to a remote endpoint. Payloads can only be sent to remote endpoints once a
 * notice of connection acceptance has been delivered via
 * [ConnectionLifecycleCallback.onConnectionResult].
 *
 * Possible result status codes include:
 * - [ConnectionsStatusCodes.STATUS_OUT_OF_ORDER_API_CALL] if the device has not first performed
 *   advertisement or discovery (to set the Strategy).
 * - [ConnectionsStatusCodes.STATUS_ENDPOINT_UNKNOWN] if there's no active (or pending) connection
 *   to the remote endpoint.
 * - [ConnectionsStatusCodes.STATUS_OK] if none of the above errors occurred. Note that this
 *   indicates that Nearby Connections will attempt to send the Payload, but not that the send has
 *   successfully completed yet. Errors might still occur during transmission (and at different
 *   times for different endpoints), and will be delivered via
 *   [PayloadCallback.onPayloadTransferUpdate].
 * @param endpointId The identifier for the remote endpoints to which the payload should be sent.
 * @param serializedObject The Payload to be sent.
 * @return [Task] to access the status of the operation when available.
 */
fun ConnectionsClient.sendPayload(
    endpointId: String,
    serializedObject: SerializedObject
): Task<Void> {
    val payload = Payload.fromBytes(serializedObject.bytes)
    return sendPayload(endpointId, payload)
}

inline fun <T : Serializable, reified S : Serializer<T>> Intent.getSerializedExtra(name: String): T? {
    if (!hasExtra(name)) return null
    val bytes = getByteArrayExtra(name) ?: return null
    val obj = bytes.deserialize()

    return S::class.objectInstance?.fromSerializedObject(obj)
}

fun <T : Serializable> Intent.putExtra(name: String, value: T) =
    putExtra(name, value.serialize().bytes)
