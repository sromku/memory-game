import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Bounding box of an SVG path's control points, enough to tell where a traced part sits.
 * Handles the full command set (absolute and relative); arcs contribute their end points.
 */
object SvgPathBounds {
    data class Box(val left: Float, val top: Float, val right: Float, val bottom: Float) {
        val width get() = right - left
        val height get() = bottom - top
        val centerX get() = (left + right) / 2
        val centerY get() = (top + bottom) / 2
        val area get() = width * height
        fun contains(x: Float, y: Float) = x in left..right && y in top..bottom
    }

    private val token = Regex("[MmLlHhVvCcSsQqTtAaZz]|-?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][-+]?\\d+)?")

    fun of(d: String): Box {
        val tokens = token.findAll(d).map { it.value }.toList()
        var i = 0
        var cmd = 'M'
        var x = 0f; var y = 0f; var startX = 0f; var startY = 0f
        var l = Float.MAX_VALUE; var t = Float.MAX_VALUE; var r = -Float.MAX_VALUE; var b = -Float.MAX_VALUE
        fun see(px: Float, py: Float) { l = min(l, px); t = min(t, py); r = max(r, px); b = max(b, py) }
        fun num() = tokens[i++].toFloat()
        while (i < tokens.size) {
            val tk = tokens[i]
            if (tk.length == 1 && tk[0].isLetter()) { cmd = tk[0]; i++; if (cmd == 'Z' || cmd == 'z') { x = startX; y = startY; continue } }
            val rel = cmd.isLowerCase()
            when (cmd.uppercaseChar()) {
                'M' -> { val nx = num(); val ny = num(); x = if (rel) x + nx else nx; y = if (rel) y + ny else ny; startX = x; startY = y; see(x, y); cmd = if (rel) 'l' else 'L' }
                'L' -> { val nx = num(); val ny = num(); x = if (rel) x + nx else nx; y = if (rel) y + ny else ny; see(x, y) }
                'H' -> { val nx = num(); x = if (rel) x + nx else nx; see(x, y) }
                'V' -> { val ny = num(); y = if (rel) y + ny else ny; see(x, y) }
                'C' -> { repeat(2) { val cx = num(); val cy = num(); see(if (rel) x + cx else cx, if (rel) y + cy else cy) }; val nx = num(); val ny = num(); x = if (rel) x + nx else nx; y = if (rel) y + ny else ny; see(x, y) }
                'S', 'Q' -> { val cx = num(); val cy = num(); see(if (rel) x + cx else cx, if (rel) y + cy else cy); val nx = num(); val ny = num(); x = if (rel) x + nx else nx; y = if (rel) y + ny else ny; see(x, y) }
                'T' -> { val nx = num(); val ny = num(); x = if (rel) x + nx else nx; y = if (rel) y + ny else ny; see(x, y) }
                'A' -> { i += 5; val nx = num(); val ny = num(); x = if (rel) x + nx else nx; y = if (rel) y + ny else ny; see(x, y) }
                else -> error("Unknown path command $cmd")
            }
        }
        if (abs(l) == Float.MAX_VALUE) return Box(0f, 0f, 0f, 0f)
        return Box(l, t, r, b)
    }
}
