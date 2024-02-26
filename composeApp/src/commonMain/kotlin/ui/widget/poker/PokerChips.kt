package ui.widget.poker

import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import ui.widget.poker.PokerUI.chipColors
import kotlin.math.roundToInt

private const val chipWidth = 300f
private const val chipHeight = 50f
private const val randomPercent = 3

@Composable
fun PokerChips(amount: Int, modifier: Modifier = Modifier) {
    // First, calculate the number of chips of each type for the amount given.
    // Take the index of chipColors as the coin value, and generate a list of the amounts for each
    //   index of chipColors.
    var mutableAmount = amount
    val chipAmounts = chipColors.sortedByDescending { it.value }.associateWith { (chipValue, _) ->
        val amountOfThisChip = mutableAmount / chipValue
        mutableAmount -= amountOfThisChip * chipValue
        amountOfThisChip
    }

    // Offset all chips by the maximum random factor to the right, so that the chips are not
    // out of bounds.
    val xOffset = randomPercent / 100f * chipWidth
    println("xOffset: $xOffset")
    Canvas(modifier) {
        // TODO: Draw amounts correctly
        translate(left = xOffset) {
            drawChip(0, 0, chipWidth, chipHeight, chipColors[1].color, randomPercent)
            drawChip(0, 1, chipWidth, chipHeight, chipColors[1].color, randomPercent)
            drawChip(0, 2, chipWidth, chipHeight, chipColors[2].color, randomPercent)
            drawChip(0, 3, chipWidth, chipHeight, chipColors[2].color, randomPercent)
            drawChip(0, 4, chipWidth, chipHeight, chipColors[2].color, randomPercent)
            drawChip(0, 5, chipWidth, chipHeight, chipColors[3].color, randomPercent)
            drawChip(0, 6, chipWidth, chipHeight, chipColors[4].color, randomPercent)
        }
    }
}
