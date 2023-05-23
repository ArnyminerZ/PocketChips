package com.arnyminerz.pocketchips.communications

interface Serializer <T: Any> {
    /**
     * Converts a [SerializedObject] into [T].
     * @throws NoSuchElementException If [serialized]'s structure is not valid.
     * @throws NumberFormatException If [serialized] contains invalid numbers or broken data.
     */
    fun fromSerializedObject(serialized: SerializedObject): T
}
