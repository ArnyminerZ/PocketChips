package com.arnyminerz.pocketchips

import com.arnyminerz.pocketchips.communications.serialized
import com.arnyminerz.pocketchips.game.GameSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class GameSettingsTest {
    @Test
    fun test_GameSettings_serialization() {
        val gameSettings = GameSettings(1U, 2U, GameSettings.PlayDirection.CW)
        val serialized = gameSettings.serialize()

        assertEquals(3, serialized.size)
        assertEquals("1", serialized[GameSettings.KEY_SMALL_BLIND])
        assertEquals("2", serialized[GameSettings.KEY_BIG_BLIND])
        assertEquals("CW", serialized[GameSettings.KEY_PLAY_DIRECTION])
    }

    @Test
    fun test_GameSettings_deserialization() {
        val serialized = serialized(
            GameSettings.KEY_SMALL_BLIND to "3",
            GameSettings.KEY_BIG_BLIND to "6",
            GameSettings.KEY_PLAY_DIRECTION to "CCW"
        )
        val gameSettings = GameSettings.fromSerializedObject(serialized)

        assertEquals(3U, gameSettings.smallBlind)
        assertEquals(6U, gameSettings.bigBlind)
        assertEquals(GameSettings.PlayDirection.CCW, gameSettings.playDirection)
    }

    @Test
    fun test_GameSettings_equals() {
        val gameSettings1 = GameSettings(1U, 2U, GameSettings.PlayDirection.CCW)
        val gameSettings2 = GameSettings(1U, 2U, GameSettings.PlayDirection.CCW)
        val gameSettings3 = GameSettings(2U, 2U, GameSettings.PlayDirection.CW)
        val gameSettings4 = GameSettings(2U, 1U, GameSettings.PlayDirection.CCW)

        assertEquals(gameSettings1, gameSettings2)
        assertNotEquals(gameSettings1, gameSettings3)
        assertNotEquals(gameSettings1, gameSettings4)
        assertNotEquals(gameSettings3, gameSettings4)
    }
}