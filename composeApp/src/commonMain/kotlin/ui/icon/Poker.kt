package ui.icon

import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val PokerIcons.Poker: ImageVector
    get() {
        if (_poker != null) {
            return _poker!!
        }
        _poker = ImageVector.Builder(
            name = "PokerIcons",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0F,
            viewportHeight = 24.0F,
        ).materialPath {
            verticalLineToRelative(0.0F)
            moveTo(5.0F, 2.0F)
            curveTo(3.347656F, 2.0F, 2.0F, 3.347656F, 2.0F, 5.0F)
            lineTo(2.0F, 17.0F)
            curveTo(2.0F, 18.652344F, 3.347656F, 20.0F, 5.0F, 20.0F)
            lineTo(7.375F, 20.0F)
            lineTo(15.65625F, 21.9375F)
            curveTo(15.878906F, 21.988281F, 16.09375F, 22.0F, 16.3125F, 22.0F)
            curveTo(17.679688F, 22.0F, 18.929688F, 21.042969F, 19.25F, 19.65625F)
            lineTo(21.9375F, 7.96875F)
            curveTo(22.304688F, 6.355469F, 21.265625F, 4.742188F, 19.65625F, 4.375F)
            lineTo(15.5625F, 3.4375F)
            curveTo(15.035156F, 2.582031F, 14.078125F, 2.0F, 13.0F, 2.0F)

            moveTo(5.0F, 4.0F)
            lineTo(13.0F, 4.0F)
            curveTo(13.550781F, 4.0F, 14.0F, 4.449219F, 14.0F, 5.0F)
            lineTo(14.0F, 17.0F)
            curveTo(14.0F, 17.550781F, 13.550781F, 18.0F, 13.0F, 18.0F)
            lineTo(5.0F, 18.0F)
            curveTo(4.449219F, 18.0F, 4.0F, 17.550781F, 4.0F, 17.0F)
            lineTo(4.0F, 5.0F)
            curveTo(4.0F, 4.449219F, 4.449219F, 4.0F, 5.0F, 4.0F)

            moveTo(16.0F, 5.59375F)
            lineTo(19.21875F, 6.3125F)
            curveTo(19.757812F, 6.433594F, 20.089844F, 6.996094F, 19.96875F, 7.53125F)
            lineTo(17.28125F, 19.21875F)
            curveTo(17.15625F, 19.753906F, 16.621094F, 20.089844F, 16.09375F, 19.96875F)
            lineTo(14.46875F, 19.59375F)
            curveTo(15.375F, 19.078125F, 16.0F, 18.117188F, 16.0F, 17.0F)

            moveTo(9.0F, 6.0F)
            curveTo(9.0F, 6.0F, 5.0F, 9.238281F, 5.0F, 11.0625F)
            curveTo(5.0F, 12.277344F, 5.988281F, 13.28125F, 7.1875F, 13.28125F)
            curveTo(7.605469F, 13.28125F, 8.019531F, 13.132813F, 8.375F, 12.90625F)
            lineTo(7.65625F, 15.0F)
            lineTo(10.34375F, 15.0F)
            lineTo(9.625F, 12.9375F)
            curveTo(9.953125F, 13.148438F, 10.351563F, 13.28125F, 10.8125F, 13.28125F)
            curveTo(12.011719F, 13.28125F, 13.0F, 12.277344F, 13.0F, 11.0625F)
            curveTo(13.0F, 9.242188F, 9.0F, 6.0F, 9.0F, 6.0F)

            close()
        }.build()
        return _poker!!
    }
private var _poker: ImageVector? = null
