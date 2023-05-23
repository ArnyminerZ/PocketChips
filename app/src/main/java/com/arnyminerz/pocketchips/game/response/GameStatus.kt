package com.arnyminerz.pocketchips.game.response

import com.arnyminerz.pocketchips.communications.Serializable
import com.arnyminerz.pocketchips.communications.SerializedObject
import com.arnyminerz.pocketchips.communications.Serializer
import com.arnyminerz.pocketchips.communications.serialized

sealed class GameStatus(
    val name: String
): Serializable {
    companion object: Serializer<GameStatus> {
        fun valueOf(name: String): GameStatus = when (name) {
            "started" -> Started
            "waiting" -> Waiting
            else -> throw IllegalArgumentException("\"$name\" is not a valid GameStatus.")
        }

        override fun fromSerializedObject(serialized: SerializedObject): GameStatus =
            valueOf(serialized["name"])
    }

    object Started: GameStatus("started")
    object Waiting: GameStatus("waiting")


    override fun serialize(): SerializedObject =
        serialized("name" to name)
}