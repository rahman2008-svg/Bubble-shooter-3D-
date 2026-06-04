package com.example.game

data class Bubble(
    var row: Int = -1,
    var col: Int = -1,
    var x: Float = 0f,
    var y: Float = 0f,
    val color: BubbleColor,
    var isPopping: Boolean = false,
    var popScale: Float = 1.0f,
    var isDislodging: Boolean = false
)

enum class BubbleColor(val displayColor: Int, val nameString: String) {
    RED(0xFFF43F5E.toInt(), "Rose"),
    BLUE(0xFF6366F1.toInt(), "Indigo"),
    GREEN(0xFF10B981.toInt(), "Emerald"),
    YELLOW(0xFFF59E0B.toInt(), "Amber"),
    PURPLE(0xFF8B5CF6.toInt(), "Violet"),
    ORANGE(0xFFF97316.toInt(), "Orange"),
    CYAN(0xFF0EA5E9.toInt(), "Sky Blue");

    companion object {
        fun getRandomColor(count: Int = 5): BubbleColor {
            val colors = values()
            val limit = count.coerceIn(3, colors.size)
            return colors[(0 until limit).random()]
        }
    }
}
