package com.arnyminerz.pocketchips.communications

interface Serializer <T: Any> {
    fun fromSerializedObject(serialized: SerializedObject): T
}
