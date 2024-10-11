import java.awt.*
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ColorConverterApp : JFrame("Color Converter") {
    private val colorConverter = ColorConverter()
    private var isUpdating = false

    private var rgbColor = listOf(0, 0, 0)
    private var cmykColor = listOf(0, 0, 0, 100)
    private var hsvColor = listOf(0, 0, 0)

    private val rgbFields = rgbColor.map { JTextField(5) }
    private val cmykFields = cmykColor.map { JTextField(5) }
    private val hsvFields = hsvColor.map { JTextField(5) }

    private val rgbSliders = List(3) { createSlider(0, 255) }
    private val cmykSliders = List(4) { createSlider(0, 100) }
    private val hsvSliders = listOf(createSlider(0, 360), createSlider(0, 100), createSlider(0, 100))

    private val colorDisplayPanel = JPanel().apply {
        preferredSize = Dimension(150, 150)
        background = Color.BLACK
    }

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        layout = GridBagLayout()
        val gbc = GridBagConstraints().apply { fill = GridBagConstraints.BOTH }

        addPanelWithBorder("RGB", rgbFields, rgbSliders, gbc, 0)
        addPanelWithBorder("CMYK", cmykFields, cmykSliders, gbc, 1)
        addPanelWithBorder("HSV", hsvFields, hsvSliders, gbc, 2)

        gbc.gridx = 0
        gbc.gridy = 3
        gbc.gridwidth = 2
        gbc.insets = Insets(10, 10, 10, 10)
        add(colorDisplayPanel, gbc)

        val colorChooserButton = JButton("Choose Color").apply {
            addActionListener { chooseColor() }
        }

        gbc.gridy = 4
        gbc.gridwidth = 1
        gbc.insets = Insets(0, 10, 10, 10)
        add(colorChooserButton, gbc)

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

    private fun addPanelWithBorder(
        title: String,
        fields: List<JTextField>,
        sliders: List<JSlider>,
        gbc: GridBagConstraints,
        yPosition: Int
    ) {
        val panel = JPanel(GridBagLayout()).apply {
            border = TitledBorder(title)
        }

        val gbcInner = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            insets = Insets(2, 5, 2, 5)
        }

        fields.forEachIndexed { i, field ->
            gbcInner.gridx = 0
            panel.add(field, gbcInner)
            gbcInner.gridx = 1
            panel.add(sliders[i], gbcInner)
            gbcInner.gridy++
        }

        gbc.gridx = 0
        gbc.gridy = yPosition
        gbc.gridwidth = 2
        add(panel, gbc)
    }

    private fun chooseColor() {
        val color = JColorChooser.showDialog(this, "Choose Color", null)
        if (color != null) {
            rgbColor = listOf(color.red, color.green, color.blue)
            updateSchemesComponentsFromRgb()
            updateFields()
            updateSliders()
            updateColorDisplay()
        }
    }

    private fun addDocumentListener(field: JTextField, scheme: ColorScheme) {
        field.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = updateColors(scheme)
            override fun removeUpdate(e: DocumentEvent?) = updateColors(scheme)
            override fun changedUpdate(e: DocumentEvent?) = updateColors(scheme)
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
        JSlider(min, max).apply {
            majorTickSpacing = (max - min) / 5
            paintTicks = true
            paintLabels = true
        }

    private fun updateColors(changed: ColorScheme) {
        if (isUpdating) return

        SwingUtilities.invokeLater {
            isUpdating = true
            try {
                when (changed) {
                    ColorScheme.RGB -> {
                        if (inputIsInvalid(rgbFields)) return@invokeLater
                        updateRGBFromFields()
                        updateSchemesComponentsFromRgb()
                    }

                    ColorScheme.CMYK -> {
                        if (inputIsInvalid(cmykFields)) return@invokeLater
                        updateRGBFromCmyk()
                    }

                    ColorScheme.HSV -> {
                        if (inputIsInvalid(hsvFields)) return@invokeLater
                        updateRGBFromHsv()
                    }
                }

                updateAllFieldsAndDisplay()
            } catch (e: NumberFormatException) {
                println(e.toString())
            } finally {
                isUpdating = false
            }
        }
    }
    private fun inputIsInvalid(fields: List<JTextField>): Boolean {
        return if (fields.any { it.text.isNullOrBlank() }) {
            fields.forEach { it.text = "0" }
            updateFields()
            true
        } else {
            false
        }
    }
    private fun updateRGBFromFields() {
        val (r, g, b) = rgbFields.map { it.text.toIntOrNull()?.coerceIn(0, 255) ?: 0 }
        rgbColor = listOf(r, g, b)
    }
    private fun updateRGBFromCmyk() {
        val (c, m, y, k) = cmykFields.map { it.text.toIntOrNull()?.coerceIn(0, 100) ?: 0 }
        rgbColor = colorConverter.cmykToRgb(c, m, y, k)
        cmykColor = listOf(c, m, y, k)
        hsvColor = colorConverter.rgbToHsv(rgbColor[0], rgbColor[1], rgbColor[2])
    }
    private fun updateRGBFromHsv() {
        val (h, s, v) = hsvFields.mapIndexed { index, field ->
            val value = field.text.toIntOrNull() ?: 0
            when (index) {
                0 -> value.coerceIn(0, 360) // Hue range: 0-360
                else -> value.coerceIn(0, 100) // Saturation and Value range: 0-100
            }
        }
        rgbColor = colorConverter.hsvToRgb(h, s, v)
        cmykColor = colorConverter.rgbToCmyk(rgbColor[0], rgbColor[1], rgbColor[2])
        hsvColor = listOf(h, s, v)
    }
    private fun updateAllFieldsAndDisplay() {
        updateFields()
        updateSliders()
        updateColorDisplay()
    }
    private fun updateRGBField() = updateField(rgbFields, rgbColor)

    private fun updateCMYKField() = updateField(cmykFields, cmykColor)

    private fun updateHSVField() = updateField(hsvFields, hsvColor)

    private fun updateFields() {
        updateRGBField()
        updateCMYKField()
        updateHSVField()
    }

    private fun updateField(fields: List<JTextField>, values: List<Int>) {
        fields.forEachIndexed { i, field ->
            field.text = values[i].toString()
        }
    }

    private fun updateColorDisplay() {
        rgbColor.validateComponents()
        val (r, g, b) = rgbColor
        colorDisplayPanel.background = Color(r, g, b)
        colorDisplayPanel.repaint()
    }

    private fun List<Int>.validateComponents() {
        rgbColor = this.map { it.coerceIn(0, 255) }
        updateRGBField()
        updateCMYKField()
        updateHSVField()
    }

    private fun updateSliders() {
        val (h, s, v) = hsvColor
        mapOf(
            rgbSliders to rgbColor,
            cmykSliders to cmykColor,
            hsvSliders to listOf(h, s, v)
        ).forEach { (sliders, values) ->
            sliders.forEachIndexed { i, slider ->
                slider.value = values[i]
            }
        }
    }

    private fun updateSchemesComponentsFromRgb() {
        val (r, g, b) = rgbColor
        cmykColor = colorConverter.rgbToCmyk(r, g, b).map { it }
        hsvColor = colorConverter.rgbToHsv(r, g, b).map { it }
    }
}

fun main() {
    SwingUtilities.invokeLater { ColorConverterApp() }
}
