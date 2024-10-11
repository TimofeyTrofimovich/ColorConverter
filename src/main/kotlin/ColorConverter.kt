class ColorConverter {
    fun rgbToCmyk(r: Int, g: Int, b: Int): List<Int> {
        val rf = r / 255.0
        val gf = g / 255.0
        val bf = b / 255.0

        val k = 1 - maxOf(rf, gf, bf)
        if (k == 1.0) return listOf(0, 0, 0, 100)

        val c = ((1 - rf - k) / (1 - k)) * 100
        val m = ((1 - gf - k) / (1 - k)) * 100
        val y = ((1 - bf - k) / (1 - k)) * 100

        return listOf(c.toInt(), m.toInt(), y.toInt(), (k * 100).toInt())
    }

    fun cmykToRgb(c: Int, m: Int, y: Int, k: Int): List<Int> {
        val cRatio = c / 100.0
        val mRatio = m / 100.0
        val yRatio = y / 100.0
        val kRatio = k / 100.0

        val r = (255 * (1 - cRatio) * (1 - kRatio)).toInt()
        val g = (255 * (1 - mRatio) * (1 - kRatio)).toInt()
        val b = (255 * (1 - yRatio) * (1 - kRatio)).toInt()

        return listOf(r, g, b)
    }

    fun rgbToHsv(r: Int, g: Int, b: Int): List<Int> {
        val rf = r / 255.0
        val gf = g / 255.0
        val bf = b / 255.0

        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val delta = max - min

        val h = when {
            delta == 0.0 -> 0.0
            max == rf -> (60 * ((gf - bf) / delta) + 360) % 360
            max == gf -> (60 * ((bf - rf) / delta) + 120) % 360
            else -> (60 * ((rf - gf) / delta) + 240) % 360
        }

        val s = if (max == 0.0) 0.0 else delta / max * 100
        val v = max * 100

        return listOf(h.toInt(), s.toInt(), v.toInt())
    }

    fun hsvToRgb(h: Int, s: Int, v: Int): List<Int> {
        val sRatio = s / 100.0
        val vRatio = v / 100.0
        val c = vRatio * sRatio
        val x = c * (1 - Math.abs(h / 60.0 % 2 - 1))
        val m = vRatio - c

        val (rf, gf, bf) = when (h) {
            in 0..59 -> listOf(c, x, 0.0)
            in 60..119 -> listOf(x, c, 0.0)
            in 120..179 -> listOf(0.0, c, x)
            in 180..239 -> listOf(0.0, x, c)
            in 240..299 -> listOf(x, 0.0, c)
            else -> listOf(c, 0.0, x)
        }

        val r = ((rf + m) * 255).toInt()
        val g = ((gf + m) * 255).toInt()
        val b = ((bf + m) * 255).toInt()

        return listOf(r, g, b)
    }
}