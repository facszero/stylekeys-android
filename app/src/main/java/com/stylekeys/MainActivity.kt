package com.stylekeys

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

/**
 * MainActivity — Pantalla de configuración inicial.
 *
 * Guía al usuario en 2 pasos:
 * 1. Activar StyleKeys en ajustes del sistema
 * 2. Seleccionarlo como teclado activo
 *
 * También muestra un catálogo de los estilos disponibles.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupStepButtons()
        renderStylesDemo()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar estado de los pasos según configuración actual
        checkSetupStatus()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pasos de configuración
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupStepButtons() {
        // Paso 1: abrir Settings → Teclados virtuales
        findViewById<Button>(R.id.btn_step1).setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        // Paso 2: mostrar el selector de teclado del sistema
        findViewById<Button>(R.id.btn_step2).setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }
    }

    private fun checkSetupStatus() {
        val isEnabled = isKeyboardEnabled()
        val cardReady = findViewById<MaterialCardView>(R.id.card_ready)
        cardReady.visibility = if (isEnabled) View.VISIBLE else View.GONE
    }

    /**
     * Verifica si StyleKeys está habilitado en la lista de IMEs activos del sistema.
     */
    private fun isKeyboardEnabled(): Boolean {
        val enabledInputMethods = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_INPUT_METHODS
        ) ?: return false
        return enabledInputMethods.contains(packageName)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Demo de estilos
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Genera una vista previa de todos los estilos usando el texto de muestra
     * propio de cada estilo y lo muestra en el TextView de demo.
     */
    private fun renderStylesDemo() {
        val demoWord = "StyleKeys"
        val sb = StringBuilder()

        TextStyler.allStyles().forEach { style ->
            val transformed = TextStyler.apply(demoWord, style)
            sb.append("${style.label.replace(Regex("[𝐁𝐼𝑩𝙼𝔉𝒮𝔻𝖲𝗦𝘚𝙎]"), "")}:  $transformed\n")
        }

        findViewById<TextView>(R.id.tv_styles_demo).text = sb.toString().trimEnd()
    }
}
