package com.arnyminerz.pocketchips.game

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import com.arnyminerz.pocketchips.communications.Serializable
import com.arnyminerz.pocketchips.communications.SerializedObject
import com.arnyminerz.pocketchips.communications.Serializer
import com.arnyminerz.pocketchips.communications.serialized

data class GameSettings(
    val smallBlind: UInt = 1U,
    val bigBlind: UInt = smallBlind * 2U,
    val playDirection: PlayDirection = PlayDirection.CW
): Serializable {
    companion object: Serializer<GameSettings> {
        @VisibleForTesting(PRIVATE) const val KEY_SMALL_BLIND = "sb"
        @VisibleForTesting(PRIVATE) const val KEY_BIG_BLIND = "bb"
        @VisibleForTesting(PRIVATE) const val KEY_PLAY_DIRECTION = "pd"

        /**
         * Converts a [SerializedObject] into [Game].
         * @throws NoSuchElementException If [serialized]'s structure is not valid.
         * @throws NumberFormatException If [serialized] contains invalid numbers or broken data.
         */
        override fun fromSerializedObject(serialized: SerializedObject) =
            GameSettings(
                serialized[KEY_SMALL_BLIND].toUInt(),
                serialized[KEY_BIG_BLIND].toUInt(),
                serialized[KEY_PLAY_DIRECTION].let { PlayDirection.valueOf(it) }
            )
    }

    override fun serialize(): SerializedObject = serialized(
        KEY_SMALL_BLIND to smallBlind.toString(),
        KEY_BIG_BLIND to bigBlind.toString(),
        KEY_PLAY_DIRECTION to playDirection.name
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameSettings

        if (smallBlind != other.smallBlind) return false
        if (bigBlind != other.bigBlind) return false
        if (playDirection != other.playDirection) return false

        return true
    }

    override fun hashCode(): Int {
        var result = smallBlind.hashCode()
        result = 31 * result + bigBlind.hashCode()
        result = 31 * result + playDirection.hashCode()
        return result
    }


    enum class PlayDirection { CW, CCW }

}
