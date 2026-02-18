package com.stylekeys

import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class InputDialogActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CURRENT_TEXT = "current_text"

        // Canal de comunicación directo entre Activity y Service
        // Al estar en el mismo proceso, esto es seguro y sin timing issues
        var pendingText: String? = null
        var onTextReady: ((String) -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_dialog)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val etText    = findViewById<EditText>(R.id.et_dialog_input)
        val btnOk     = findViewById<Button>(R.id.btn_dialog_ok)
        val btnCancel = findViewById<Button>(R.id.btn_dialog_cancel)

        // Pre-cargar texto existente
        val currentText = intent.getStringExtra(EXTRA_CURRENT_TEXT) ?: ""
        if (currentText.isNotBlank()) {
            etText.setText(currentText)
            etText.setSelection(currentText.length)
        }

        btnOk.setOnClickListener {
            val result = etText.text.toString()
            // Llamada directa al callback del Service (mismo proceso, mismo hilo)
            onTextReady?.invoke(result)
            finish()
        }

        btnCancel.setOnClickListener { finish() }
    }
}
