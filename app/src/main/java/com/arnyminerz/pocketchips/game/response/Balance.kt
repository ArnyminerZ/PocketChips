package com.arnyminerz.pocketchips.game.response

import com.arnyminerz.pocketchips.communications.Serializable
import com.arnyminerz.pocketchips.communications.SerializedObject
import com.arnyminerz.pocketchips.communications.Serializer
import com.arnyminerz.pocketchips.communications.serialized

data class Balance(
    val amount: UInt
): Serializable {
    companion object: Serializer<Balance> {
        override fun fromSerializedObject(serialized: SerializedObject): Balance =
            Balance(
                serialized["amount"].toUInt()
            )
    }

    override fun serialize(): SerializedObject =
        serialized("amount" to amount.toString())
}
