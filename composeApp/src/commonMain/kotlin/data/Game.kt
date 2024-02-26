package data

import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val startTimestamp: Long,
    val players: List<Player>,
    val money: Long = 0L,
    val currency: Char = '€'
) {
    companion object {
        val none = Game(-1, emptyList())
    }

    fun isNone() = startTimestamp < 0 && players.isEmpty()
}
