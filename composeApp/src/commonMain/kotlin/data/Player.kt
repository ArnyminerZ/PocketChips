package data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import pocketchips.composeapp.generated.resources.Res

@Serializable
data class Player(
    val displayName: String,
    val money: UInt,
    val isHost: Boolean = false,
    val endpointId: String? = null,
    val image: String? = null
) {
    val painter: Painter
        @Composable
        get() = when (image) {
            else -> painterResource(Res.drawable.icons8_roulette_chips)
        }

    val isRemote: Boolean = endpointId != null

    val isVirtual: Boolean = !isRemote && !isHost

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Player

        if (displayName != other.displayName) return false
        if (money != other.money) return false
        if (endpointId != other.endpointId) return false
        if (isHost != other.isHost) return false
        return image == other.image
    }

    override fun hashCode(): Int {
        var result = displayName.hashCode()
        result = 31 * result + money.hashCode()
        result = 31 * result + (endpointId?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + isHost.hashCode()
        return result
    }
}
