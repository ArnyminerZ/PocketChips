package ui.widget.poker

import androidx.compose.ui.graphics.Color

object PokerUI {
    val chipColors = listOf(
        Chip(1, Color(0xFFFAFAFA)), // white
        Chip(5, Color(0xFFEF5350)), // red
        Chip(10, Color(0xFFA1887F)), // brown
        Chip(25, Color(0xFF4CAF50)), // green
        Chip(50, Color(0xFF2196F3)), // blue
        Chip(100, Color(0xFF9C27B0)), // purple
        Chip(500, Color(0xFFFFC107)), // yellow
        Chip(1000, Color(0xFF795548)), // orange
    )
}
