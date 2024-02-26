package ui.widget.poker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Generates a gradient for the border of a poker chip.
 * @param color The color of the chip.
 * @param startX The x position of the start of the gradient.
 * @param endX The x position of the end of the gradient.
 * @param stripes The number of stripes in the gradient.
 */
private fun generateChipBorderGradient(
    color: Color,
    startX: Float,
    endX: Float,
    stripes: Int = 3,
    randomPercent: Int = 30
): Brush {
    // calculate the complementary color for the chip
    val negativeChipColor = Color(
        red = 1f - color.red,
        green = 1f - color.green,
        blue = 1f - color.blue
    )
    /*val negativeChipColor = if ((color.red + color.green + color.blue) / 3 > 128) {
        Color(0xFF757575)
    } else {
        Color(0xFFEEEEEE)
    }*/

    val pieces = stripes * 2 + 1
    val dx = 1f / pieces
    val randomRange = (-randomPercent..randomPercent).random().toFloat() / 100f
    val xOffset = randomRange * dx
    var isTransparent = true
    val colorStops = mutableListOf<Pair<Float, Color>>()
    for (i in 0 until pieces) {
        var x = i * dx
        if (i != 0) x -= xOffset
        colorStops += x to if (isTransparent) negativeChipColor else Color.Transparent
        colorStops += x to if (isTransparent) Color.Transparent else negativeChipColor
        isTransparent = !isTransparent
    }
    return Brush.horizontalGradient(
        colorStops = colorStops.toTypedArray(),
        startX = startX,
        endX = endX
    )
}

/**
 * Draws a poker chip in the current DrawScope.
 * @param x The x index of the tower where the chip is located.
 * @param y The y position of the tower where the chip is located.
 * @param width The width of the chip in pixels.
 * @param height The height of the chip in pixels.
 * @param randomPercent The maximum random percentage to offset the chip horizontally.
 */
fun DrawScope.drawChip(
    x: Int,
    y: Int,
    width: Float,
    height: Float,
    color: Color,
    randomPercent: Int,
    minOffset: Float = 20f
) {
    val randomHorizontalOffset = (-randomPercent..randomPercent).random().toFloat() / 100f

    // chips should be drawn from the bottom up, so obtain the adjusted y value from the canvas' height
    val top = size.height - (y + 1) * height
    val left = x * width + minOf(minOffset, randomHorizontalOffset * width)

    // Draw the chip base color
    drawRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(width, height)
    )

    // Draw the chip border
    drawRect(
        generateChipBorderGradient(
            color = color,
            startX = left,
            endX = left + width,
            randomPercent = 50
        ),
        topLeft = Offset(left, top),
        size = Size(width, height)
    )

    // Draw the shade
    drawRect(
        Brush.horizontalGradient(
            0f to Color.Black.copy(alpha = .1f),
            .5f to Color.Black.copy(alpha = .2f),
            1f to Color.Black.copy(alpha = .1f),
            startX = left,
            endX = left + width
        ),
        topLeft = Offset(left, top),
        size = Size(width, height)
    )
    drawRect(
        Brush.verticalGradient(
            0f to Color.Black.copy(alpha = .05f),
            .5f to Color.Black.copy(alpha = .15f),
            1f to Color.Black.copy(alpha = .05f),
            startY = top,
            endY = top + height
        ),
        topLeft = Offset(left, top),
        size = Size(width, height)
    )
}
