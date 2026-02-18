package com.stylekeys

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class InputDialogActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CURRENT_TEXT = "current_text"
        const val PREFS_NAME = "stylekeys_prefs"
        const val KEY_RESULT_TEXT = "result_text"
        const val KEY_HAS_RESULT = "has_result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_dialog)

        // Forzar teclado visible al abrir
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        val etText    = findViewById<EditText>(R.id.et_dialog_input)
        val btnOk     = findViewById<Button>(R.id.btn_dialog_ok)
        val btnCancel = findViewById<Button>(R.id.btn_dialog_cancel)

        // Pre-cargar texto existente
        val currentText = intent.getStringExtra(EXTRA_CURRENT_TEXT) ?: ""
        if (currentText.isNotBlank()) {
            etText.setText(currentText)
            etText.setSelection(currentText.length)
        }

        // Forzar foco y teclado
        etText.requestFocus()

        btnOk.setOnClickListener {
            val result = etText.text.toString()
            // Guardar en SharedPreferences — canal confiable entre Activity y Service
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString(KEY_RESULT_TEXT, result)
                .putBoolean(KEY_HAS_RESULT, true)
                .apply()
            finish()
        }

        btnCancel.setOnClickListener { finish() }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Segunda llamada para garantizar que el teclado aparezca
            val et = findViewById<EditText>(R.id.et_dialog_input)
            et.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}
