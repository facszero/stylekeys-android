package com.stylekeys

import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class StyleKeyboardService : InputMethodService() {

    private var selectedStyle: TextStyler.Style = TextStyler.Style.BOLD
    private val styleChips = mutableListOf<TextView>()

    private lateinit var etDisplay: EditText
    private lateinit var tvPreview: TextView
    private lateinit var btnInsert: Button
    private lateinit var btnClear: Button
    private lateinit var btnSwitchKeyboard: Button
    private lateinit var btnOpenKeyboard: ImageButton
    private lateinit var chipsContainer: LinearLayout

    override fun onCreateInputView(): View {
        val root = layoutInflater.inflate(R.layout.keyboard_view, null)

        etDisplay         = root.findViewById(R.id.et_input)
        tvPreview         = root.findViewById(R.id.tv_preview)
        btnInsert         = root.findViewById(R.id.btn_insert)
        btnClear          = root.findViewById(R.id.btn_clear)
        btnSwitchKeyboard = root.findViewById(R.id.btn_switch_keyboard)
        btnOpenKeyboard   = root.findViewById(R.id.btn_open_keyboard)
        chipsContainer    = root.findViewById(R.id.style_chips_container)

        etDisplay.isFocusable = false
        etDisplay.isFocusableInTouchMode = false

        buildStyleChips()
        setupListeners()
        return root
    }

    // Se llama cada vez que la ventana del teclado se vuelve visible
    // — momento ideal para leer el resultado del dialog
    override fun onWindowShown() {
        super.onWindowShown()
        checkForPendingResult()
    }

    private fun checkForPendingResult() {
        val prefs = getSharedPreferences(
            InputDialogActivity.PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(InputDialogActivity.KEY_HAS_RESULT, false)) {
            val text = prefs.getString(InputDialogActivity.KEY_RESULT_TEXT, "") ?: ""
            // Limpiar inmediatamente para no reusar en la próxima apertura
            prefs.edit()
                .remove(InputDialogActivity.KEY_RESULT_TEXT)
                .putBoolean(InputDialogActivity.KEY_HAS_RESULT, false)
                .apply()
            // Actualizar la UI del teclado
            etDisplay.setText(text)
            updatePreview(text)
        }
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (!restarting) { etDisplay.text.clear(); updatePreview("") }
    }

    private fun buildStyleChips() {
        chipsContainer.removeAllViews()
        styleChips.clear()
        TextStyler.allStyles().forEach { style ->
            val chip = TextView(this).apply {
                text = style.label
                textSize = 14f
                setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8))
                setOnClickListener { selectStyle(style) }
            }
            chipsContainer.addView(chip)
            styleChips.add(chip)
        }
        selectStyle(TextStyler.allStyles().first())
    }

    private fun setupListeners() {
        etDisplay.setOnClickListener { openInputDialog() }
        btnOpenKeyboard.setOnClickListener { openInputDialog() }

        btnInsert.setOnClickListener {
            val text = etDisplay.text.toString()
            if (text.isNotBlank()) {
                currentInputConnection?.commitText(TextStyler.apply(text, selectedStyle), 1)
                etDisplay.text.clear()
                updatePreview("")
            }
        }

        btnClear.setOnClickListener {
            etDisplay.text.clear()
            updatePreview("")
        }

        btnSwitchKeyboard.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (!imm.switchToLastInputMethod(window.window?.attributes?.token))
                imm.showInputMethodPicker()
        }
    }

    private fun openInputDialog() {
        startActivity(Intent(this, InputDialogActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(InputDialogActivity.EXTRA_CURRENT_TEXT, etDisplay.text.toString())
        })
    }

    private fun selectStyle(style: TextStyler.Style) {
        selectedStyle = style
        TextStyler.allStyles().forEachIndexed { index, s ->
            val chip = styleChips.getOrNull(index) ?: return@forEachIndexed
            val sel = s == selectedStyle
            chip.background = ContextCompat.getDrawable(this,
                if (sel) R.drawable.bg_chip_selected else R.drawable.bg_chip_normal)
            chip.setTextColor(ContextCompat.getColor(this,
                if (sel) R.color.style_chip_text_selected else R.color.style_chip_text))
        }
        updatePreview(etDisplay.text.toString())
    }

    private fun updatePreview(rawText: String) {
        tvPreview.text = if (rawText.isBlank())
            TextStyler.apply(selectedStyle.preview, selectedStyle)
        else TextStyler.apply(rawText, selectedStyle)
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()
}
