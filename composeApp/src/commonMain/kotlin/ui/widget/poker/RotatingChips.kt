package ui.widget.poker

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import utils.toRadians
import kotlin.math.cos
import kotlin.math.sin

const val CHIP_TEXT_PADDING = 20f

@Composable
fun RotatingChips(
    amount: Int,
    modifier: Modifier = Modifier,
    labelTextStyle: TextStyle = MaterialTheme.typography.labelLarge,
    chipLabel: (chip: Chip, amount: Int) -> String = { chip, amm -> "$amm x ${chip.value} €" }
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        infiniteRepeatable(tween(45_000, easing = LinearEasing), RepeatMode.Restart)
    )

    var mutableAmount = amount
    val chipAmounts =
        PokerUI.chipColors.sortedByDescending { it.value }.associateWith { (chipValue, _) ->
            val amountOfThisChip = mutableAmount / chipValue
            mutableAmount -= amountOfThisChip * chipValue
            amountOfThisChip
        }
    val nonEmptyChips = chipAmounts.filter { it.value > 0 }
    val dDegrees = 360f / nonEmptyChips.size

    val textMeasurer = rememberTextMeasurer()
    val chipsLayoutResult = remember(nonEmptyChips, labelTextStyle) {
        nonEmptyChips.map { (chip, amount) ->
            textMeasurer.measure(chipLabel(chip, amount), labelTextStyle)
        }
    }
    val chipRadius = chipsLayoutResult.maxOf {
        maxOf(it.size.width, it.size.height).toFloat()
    } / 2 + CHIP_TEXT_PADDING

    Canvas(Modifier.aspectRatio(1f).then(modifier)) {
        val distance = size.width / 2 - chipRadius
        translate(
            top = size.width / 2,
            left = size.height / 2
        ) {
            for ((index, entry) in nonEmptyChips.entries.withIndex()) {
                val (chip, _) = entry
                val degrees = index * dDegrees + rotation
                val angle = -degrees + 90
                val x = cos(angle.toRadians()) * distance
                val y = sin(angle.toRadians()) * distance

                drawCircle(
                    chip.color,
                    radius = chipRadius,
                    center = Offset(x, y)
                )

                val textColor = if (chip.color.luminance() > 0.5f) {
                    Color.Black
                } else {
                    Color.White
                }
                val textLayoutResult = chipsLayoutResult[index]
                drawText(
                    textLayoutResult,
                    color = textColor,
                    topLeft = Offset(
                        x = x - textLayoutResult.size.width / 2,
                        y = y - textLayoutResult.size.height / 2
                    )
                )
            }
        }
    }
}
