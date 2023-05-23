package com.arnyminerz.pocketchips.game.requests

import com.arnyminerz.pocketchips.communications.Serializable
import com.arnyminerz.pocketchips.communications.SerializedObject
import com.arnyminerz.pocketchips.communications.Serializer
import com.arnyminerz.pocketchips.communications.serialized

open class Request<R : Serializable, S : Serializer<R>>(
    val name: String
) : Serializable {
    companion object : Serializer<Request<*, *>> {
        override fun fromSerializedObject(serialized: SerializedObject): Request<Serializable, Serializer<Serializable>> =
            Request(serialized["name"])
    }

    override fun serialize(): SerializedObject =
        serialized("name" to name)

}
