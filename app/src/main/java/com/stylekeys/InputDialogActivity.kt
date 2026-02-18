package com.stylekeys

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

/**
 * InputDialogActivity — Activity con tema Dialog para capturar texto.
 *
 * Por qué existe: un InputMethodService no puede mostrar un soft keyboard
 * para sus propios campos internos (ocupa el slot IME del sistema).
 * Una Activity con Theme.Dialog SÍ tiene su propia ventana con soporte
 * completo de soft keyboard. Es el patrón estándar para este caso.
 *
 * Flujo:
 * 1. StyleKeyboardService llama startActivity() con FLAG_ACTIVITY_NEW_TASK
 * 2. Esta Activity aparece como dialog flotante encima del teclado
 * 3. El usuario escribe con el teclado que el sistema asigna (Gboard etc.)
 * 4. Al confirmar, emite un Broadcast con el texto
 * 5. StyleKeyboardService lo recibe y lo muestra en su campo de display
 */
class InputDialogActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CURRENT_TEXT = "current_text"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_dialog)

        // Mostrar teclado automáticamente al abrir
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val etText = findViewById<EditText>(R.id.et_dialog_input)
        val btnOk  = findViewById<Button>(R.id.btn_dialog_ok)
        val btnCancel = findViewById<Button>(R.id.btn_dialog_cancel)

        // Pre-cargar texto existente si hay
        val currentText = intent.getStringExtra(EXTRA_CURRENT_TEXT) ?: ""
        if (currentText.isNotBlank()) {
            etText.setText(currentText)
            etText.setSelection(currentText.length)
        }

        btnOk.setOnClickListener {
            val result = etText.text.toString()
            val broadcast = Intent(StyleKeyboardService.ACTION_TEXT_ENTERED).apply {
                putExtra(StyleKeyboardService.EXTRA_TEXT, result)
                setPackage(packageName)
            }
            sendBroadcast(broadcast)
            finish()
        }

        btnCancel.setOnClickListener { finish() }
    }
}
