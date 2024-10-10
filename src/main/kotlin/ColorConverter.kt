import kotlin.math.abs
import kotlin.math.roundToInt

class ColorConverter {
    fun rgbToCmyk(r: Int, g: Int, b: Int): List<Double> {
        if (listOf(r, g, b).all { it == 0 }) {
            return listOf(0.0, 0.0, 0.0, 1.0)
        }
        val rNorm = r / 255.0
        val gNorm = g / 255.0
        val bNorm = b / 255.0

        val k = 1 - maxOf(rNorm, gNorm, bNorm)
        val c = (1 - rNorm - k) / (1 - k)
        val m = (1 - gNorm - k) / (1 - k)
        val y = (1 - bNorm - k) / (1 - k)

        return listOf(c, m, y, k)
    }

    fun cmykToRgb(c: Double, m: Double, y: Double, k: Double): List<Int> {
        val r = (255 * (1 - c) * (1 - k)).roundToInt()
        val g = (255 * (1 - m) * (1 - k)).roundToInt()
        val b = (255 * (1 - y) * (1 - k)).roundToInt()

        return listOf(r, g, b)
    }

    fun rgbToHsv(r: Int, g: Int, b: Int): List<Double> {
        val rNorm = r / 255.0
        val gNorm = g / 255.0
        val bNorm = b / 255.0

        val max = maxOf(rNorm, gNorm, bNorm)
        val min = minOf(rNorm, gNorm, bNorm)
        val delta = max - min

        val v = max
        val s = if (max == 0.0) 0.0 else delta / max

        val h = when {
            delta == 0.0 -> 0.0
            max == rNorm -> (60 * ((gNorm - bNorm) / delta + 6)) % 360
            max == gNorm -> 60 * ((bNorm - rNorm) / delta + 2)
            else -> 60 * ((rNorm - gNorm) / delta + 4)
        }

        return listOf(h, s, v)
    }

    fun hsvToRgb(h: Double, s: Double, v: Double): List<Int> {
        val c = v * s
        val x = c * (1 - abs((h / 60) % 2 - 1))
        val m = v - c

        val (r1, g1, b1) = when {
            h < 60 -> listOf(c, x, 0.0)
            h < 120 -> listOf(x, c, 0.0)
            h < 180 -> listOf(0.0, c, x)
            h < 240 -> listOf(0.0, x, c)
            h < 300 -> listOf(x, 0.0, c)
            else -> listOf(c, 0.0, x)
        }

        val r = ((r1 + m) * 255).roundToInt()
        val g = ((g1 + m) * 255).roundToInt()
        val b = ((b1 + m) * 255).roundToInt()

        return listOf(r, g, b)
    }
}