import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.util.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ColorConverterApp : JFrame("Color Converter") {
    private val colorConverter = ColorConverter()
    private var isUpdating = false;

    private var rgbColor = listOf(0, 0, 0)
    private var cmykColor = listOf(0.0, 0.0, 0.0, 1.0)
    private var hsvColor = listOf(0.0, 0.0, 0.0)

    private val rgbFields = rgbColor.map { JTextField(5) }
    private val cmykFields = cmykColor.map { JTextField(5) }
    private val hsvFields = hsvColor.map { JTextField(5) }

    private val rgbSliders = List(3) { createSlider(0, 255) }
    private val cmykSliders = List(4) { createSlider(0, 100) }
    private val hsvSliders = listOf(createSlider(0, 360), createSlider(0, 100), createSlider(0, 100))

    private val colorDisplayPanel = JPanel().apply {
        preferredSize = Dimension(100, 100)
        background = Color.BLACK
    }

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        layout = GridLayout(6, 1)

        val rgbPanel = JPanel().apply {
            add(JLabel("RGB:"))
            rgbFields.forEach { add(it) }
            rgbSliders.forEach { add(it) }
        }
        add(rgbPanel)

        val cmykPanel = JPanel().apply {
            add(JLabel("CMYK:"))
            cmykFields.forEach { add(it) }
            cmykSliders.forEach { add(it) }
        }
        add(cmykPanel)

        val hsvPanel = JPanel().apply {
            add(JLabel("HSV:"))
            hsvFields.forEach { add(it) }
            hsvSliders.forEach { add(it) }
        }
        add(hsvPanel)

        add(colorDisplayPanel)

        val colorChooserButton = JButton("Choose color").apply {
            addActionListener { chooseColor() }
        }
        add(colorChooserButton)

        rgbFields.forEach { addDocumentListener(it, ColorScheme.RGB) }
        cmykFields.forEach { addDocumentListener(it, ColorScheme.CMYK) }
        hsvFields.forEach { addDocumentListener(it, ColorScheme.HSV) }

        rgbSliders.forEachIndexed { i, slider -> addSliderListener(slider, rgbFields[i], ColorScheme.RGB) }
        cmykSliders.forEachIndexed { i, slider -> addSliderListener(slider, cmykFields[i], ColorScheme.CMYK) }
        hsvSliders.forEachIndexed { i, slider -> addSliderListener(slider, hsvFields[i], ColorScheme.HSV) }

        updateFields()
        updateSliders()
        updateColorDisplay()
        pack()
        isVisible = true
    }

    private fun chooseColor() {
        val color = JColorChooser.showDialog(this, "Choose color", null)
        if (color != null) {
            rgbColor = listOf(color.red, color.green, color.blue)
            updateColorPanelFromRgb()
            updateFields()
            updateSliders()
            updateColorDisplay()
        }
    }

    private fun addDocumentListener(field: JTextField, changed: ColorScheme) {
        field.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = updateColors(changed)
            override fun removeUpdate(e: DocumentEvent?) = updateColors(changed)
            override fun changedUpdate(e: DocumentEvent?) = updateColors(changed)
        })
    }

    private fun addSliderListener(slider: JSlider, field: JTextField, changed: ColorScheme) {
        slider.addChangeListener {
            if (!isUpdating) {
                field.text = slider.value.toString()
            }
        }
    }

    private fun createSlider(min: Int, max: Int): JSlider =
        JSlider(min, max)
            .apply {
                majorTickSpacing = (max - min) / 5
                paintTicks = true
                paintLabels = true
            }


    private fun updateColors(changed: ColorScheme) {
        if (isUpdating) return
        SwingUtilities.invokeLater {
            isUpdating = true;
            try {
                when (changed) {
                    ColorScheme.RGB -> {
                        if (rgbFields.any { it.text.isEmpty() }) {
                            return@invokeLater
                        }
                        val (r, g, b) = rgbFields.map { it.text.toInt() }
                        rgbColor = listOf(r, g, b)
                        updateColorPanelFromRgb()
                        updateCMYKField()
                        updateHSVField()
                    }

                    ColorScheme.CMYK -> {
                        if (cmykFields.any { it.text.isEmpty() }) {
                            return@invokeLater
                        }
                        val (c, m, y, k) = cmykFields.map { it.text.toDouble() / 100 }
                        val (r, g, b) = colorConverter.cmykToRgb(c, m, y, k)
                        rgbColor = listOf(r, g, b)
                        cmykColor = listOf(c, m, y, k)
                        hsvColor = colorConverter.rgbToHsv(r, g, b)
                        updateRGBField()
                        updateHSVField()
                    }

                    ColorScheme.HSV -> {
                        if (hsvFields.any { it.text.isEmpty() }) {
                            return@invokeLater
                        }
                        var (h, s, v) = hsvFields.map { it.text.toDouble() }
                        s /= 100
                        v /= 100
                        val (r, g, b) = colorConverter.hsvToRgb(h, s, v)
                        rgbColor = listOf(r, g, b)
                        cmykColor = colorConverter.rgbToCmyk(r, g, b)
                        hsvColor = listOf(h, s, v)
                        updateRGBField()
                        updateCMYKField()
                    }
                }
                updateSliders()
                updateColorDisplay()
            } catch (e: NumberFormatException) {
                println(e.toString())
            } finally {
                isUpdating = false;
            }
        }
    }

    private fun updateRGBField() = updateField(rgbFields, rgbColor)

    private fun updateCMYKField() = updateField(cmykFields, cmykColor.map { it * 100 })

    private fun updateHSVField() {
        val (h, s, v) = hsvColor
        updateField(hsvFields, listOf(h, s * 100, v * 100))
    }

    private fun updateFields() {
        updateRGBField()
        updateCMYKField()
        updateHSVField()
    }

    private fun updateField(fields: List<JTextField>, values: List<Number>) {
        fields.forEachIndexed { i, field ->
            field.text = String.format(if (values[i] is Double) "%.2f" else "%d", values[i])
        }
        println("updated for $values")
    }

    private fun updateColorDisplay() {
        val (r, g, b) = rgbColor
        if (r in 0..255 && g in 0..255 && b in 0..255) {  // Check if r, g, b are within the valid range
            colorDisplayPanel.background = Color(r, g, b)
            colorDisplayPanel.repaint()
        } else {
            println("RGB values out of range: r=$r, g=$g, b=$b")  // Optional: Log the invalid values
        }
    }


    private fun updateSliders() {
        val (h, s, v) = hsvColor
        mapOf(
            rgbSliders to rgbColor,
            cmykSliders to cmykColor.map { it * 100 },
            hsvSliders to listOf(h, s * 100, v * 100)
        ).forEach { (sliders, values) ->
            sliders.forEachIndexed { i, slider ->
                slider.value = values[i].toInt()
            }
        }
    }

    private fun updateColorPanelFromRgb() {
        val (r, g, b) = rgbColor
        cmykColor = colorConverter.rgbToCmyk(r, g, b)
        hsvColor = colorConverter.rgbToHsv(r, g, b)
    }
}

fun main() {
    Locale.setDefault(Locale.ENGLISH)
    SwingUtilities.invokeLater {
        ColorConverterApp()
    }
}
