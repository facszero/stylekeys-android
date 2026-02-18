package com.stylekeys

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

/**
 * StyleKeyboardService — Servicio IME (Input Method Editor).
 *
 * Flujo de uso:
 * 1. El usuario activa StyleKeys desde el selector de teclado.
 * 2. Ve una barra de estilos + campo de texto + vista previa.
 * 3. Elige un estilo, escribe, y toca "Insertar".
 * 4. El texto transformado se inyecta directamente al campo activo.
 * 5. Puede volver al teclado anterior con el botón "Cambiar teclado".
 */
class StyleKeyboardService : InputMethodService() {

    // ─────────────────────────────────────────────────────────────────────────
    // Estado interno
    // ─────────────────────────────────────────────────────────────────────────

    private var selectedStyle: TextStyler.Style = TextStyler.Style.BOLD
    private val styleChips = mutableListOf<TextView>()

    // Referencias a vistas (no null después de onCreateInputView)
    private lateinit var etInput: EditText
    private lateinit var tvPreview: TextView
    private lateinit var btnInsert: Button
    private lateinit var btnClear: Button
    private lateinit var btnSwitchKeyboard: Button
    private lateinit var chipsContainer: LinearLayout

    // ─────────────────────────────────────────────────────────────────────────
    // Ciclo de vida del IME
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreateInputView(): View {
        val keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)
        bindViews(keyboardView)
        buildStyleChips()
        setupListeners()
        return keyboardView
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Limpiar el campo al activar el teclado si no es un reinicio
        if (!restarting) {
            etInput.text.clear()
            updatePreview("")
        }
        // Darle foco al EditText interno para capturar escritura física/virtual
        etInput.requestFocus()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Construcción de UI
    // ─────────────────────────────────────────────────────────────────────────

    private fun bindViews(root: View) {
        etInput = root.findViewById(R.id.et_input)
        tvPreview = root.findViewById(R.id.tv_preview)
        btnInsert = root.findViewById(R.id.btn_insert)
        btnClear = root.findViewById(R.id.btn_clear)
        btnSwitchKeyboard = root.findViewById(R.id.btn_switch_keyboard)
        chipsContainer = root.findViewById(R.id.style_chips_container)
    }

    /**
     * Genera dinámicamente un chip por cada estilo disponible.
     * Usar chips programáticos evita dependencia de la librería Chip de Material
     * y da control total sobre el diseño.
     */
    private fun buildStyleChips() {
        chipsContainer.removeAllViews()
        styleChips.clear()

        TextStyler.allStyles().forEachIndexed { index, style ->
            val chip = TextView(this).apply {
                text = style.label
                textSize = 14f
                setPadding(
                    dpToPx(14), dpToPx(8),
                    dpToPx(14), dpToPx(8)
                )
                setOnClickListener { selectStyle(style) }
            }
            chipsContainer.addView(chip)
            styleChips.add(chip)
        }

        // Seleccionar el primero por defecto
        selectStyle(TextStyler.allStyles().first())
    }

    private fun setupListeners() {
        // Actualizar preview en tiempo real mientras el usuario escribe
        etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePreview(s?.toString() ?: "")
            }
        })

        // Insertar texto estilizado en la app host
        btnInsert.setOnClickListener {
            val rawText = etInput.text.toString()
            if (rawText.isNotBlank()) {
                val styledText = TextStyler.apply(rawText, selectedStyle)
                commitText(styledText)
                etInput.text.clear()
            }
        }

        // Limpiar campo
        btnClear.setOnClickListener {
            etInput.text.clear()
        }

        // Mostrar selector de teclado del sistema
        btnSwitchKeyboard.setOnClickListener {
            switchToLastInputMethod()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lógica de estilos
    // ─────────────────────────────────────────────────────────────────────────

    private fun selectStyle(style: TextStyler.Style) {
        selectedStyle = style

        // Actualizar apariencia de todos los chips
        TextStyler.allStyles().forEachIndexed { index, s ->
            val chip = styleChips.getOrNull(index) ?: return@forEachIndexed
            val isSelected = s == selectedStyle
            chip.background = ContextCompat.getDrawable(
                this,
                if (isSelected) R.drawable.bg_chip_selected else R.drawable.bg_chip_normal
            )
            chip.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (isSelected) R.color.style_chip_text_selected else R.color.style_chip_text
                )
            )
        }

        // Refrescar preview con el nuevo estilo
        updatePreview(etInput.text.toString())
    }

    private fun updatePreview(rawText: String) {
        tvPreview.text = if (rawText.isBlank()) {
            TextStyler.apply(selectedStyle.preview, selectedStyle)
        } else {
            TextStyler.apply(rawText, selectedStyle)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Comunicación con la app host
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Envía el texto transformado al campo activo de la app host (LinkedIn, etc).
     * commitText(text, 1) inserta el texto y mueve el cursor al final.
     */
    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    /**
     * Vuelve al teclado que estaba activo antes de StyleKeys.
     * Si no hay uno previo, muestra el selector del sistema.
     */
    private fun switchToLastInputMethod() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val success = imm.switchToLastInputMethod(window.window?.attributes?.token)
        if (!success) {
            imm.showInputMethodPicker()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────────────────────────────────

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
