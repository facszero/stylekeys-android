package com.stylekeys

/**
 * TextStyler — Motor de transformación de texto a variantes Unicode matemáticas.
 *
 * Utiliza los bloques de caracteres matemáticos de Unicode (U+1D400–U+1D7FF)
 * que son visualmente distintos pero se copian/pegan como texto plano en
 * cualquier plataforma que soporte UTF-8 (LinkedIn, Facebook, Instagram, etc).
 *
 * Cada estilo define offsets de code points para mayúsculas, minúsculas y dígitos,
 * más un mapa de excepciones para los caracteres que no siguen el patrón secuencial
 * (algunos slots en Unicode están ocupados por símbolos matemáticos históricos).
 */
object TextStyler {

    // ─────────────────────────────────────────────────────────────────────────
    // Definición de estilos
    // ─────────────────────────────────────────────────────────────────────────

    enum class Style(
        val label: String,
        val preview: String,
        val upperOffset: Int,
        val lowerOffset: Int,
        val digitOffset: Int,
        val exceptions: Map<Char, String> = emptyMap()
    ) {

        BOLD(
            label = "𝐁old",
            preview = "𝐀𝐁𝐂 𝐚𝐛𝐜",
            upperOffset = 0x1D400 - 'A'.code,
            lowerOffset = 0x1D41A - 'a'.code,
            digitOffset = 0x1D7CE - '0'.code
        ),

        ITALIC(
            label = "𝐼talic",
            preview = "𝐴𝐵𝐶 𝑎𝑏𝑐",
            upperOffset = 0x1D434 - 'A'.code,
            lowerOffset = 0x1D44E - 'a'.code,
            digitOffset = 0, // sin variante itálica para dígitos
            exceptions = mapOf(
                'h' to "\u210E",  // ℎ planck constant
                'I' to "\u2110"   // ℐ
            )
        ),

        BOLD_ITALIC(
            label = "𝑩old 𝑰talic",
            preview = "𝑨𝑩𝑪 𝒂𝒃𝒄",
            upperOffset = 0x1D468 - 'A'.code,
            lowerOffset = 0x1D482 - 'a'.code,
            digitOffset = 0
        ),

        SCRIPT(
            label = "𝒮cript",
            preview = "𝒜𝒝𝒞 𝒶𝒷𝒸",
            upperOffset = 0x1D49C - 'A'.code,
            lowerOffset = 0x1D4B6 - 'a'.code,
            digitOffset = 0,
            exceptions = mapOf(
                'B' to "\u212C",  // ℬ
                'E' to "\u2130",  // ℰ
                'F' to "\u2131",  // ℱ
                'H' to "\u210B",  // ℋ
                'I' to "\u2110",  // ℐ
                'L' to "\u2112",  // ℒ
                'M' to "\u2133",  // ℳ
                'R' to "\u211B",  // ℛ
                'e' to "\u212F",  // ℯ
                'g' to "\u210A",  // ℊ
                'o' to "\u2134"   // ℴ
            )
        ),

        FRAKTUR(
            label = "𝔉raktur",
            preview = "𝔄𝔅ℭ 𝔞𝔟𝔠",
            upperOffset = 0x1D504 - 'A'.code,
            lowerOffset = 0x1D51E - 'a'.code,
            digitOffset = 0,
            exceptions = mapOf(
                'C' to "\u212D",  // ℭ
                'H' to "\u210C",  // ℌ
                'I' to "\u2111",  // ℑ
                'R' to "\u211C",  // ℜ
                'Z' to "\u2128"   // ℨ
            )
        ),

        DOUBLE_STRUCK(
            label = "𝔻ouble",
            preview = "𝔸𝔹ℂ 𝕒𝕓𝕔",
            upperOffset = 0x1D538 - 'A'.code,
            lowerOffset = 0x1D552 - 'a'.code,
            digitOffset = 0x1D7D8 - '0'.code,
            exceptions = mapOf(
                'C' to "\u2102",  // ℂ
                'H' to "\u210D",  // ℍ
                'N' to "\u2115",  // ℕ
                'P' to "\u2119",  // ℙ
                'Q' to "\u211A",  // ℚ
                'R' to "\u211D",  // ℝ
                'Z' to "\u2124"   // ℤ
            )
        ),

        SANS(
            label = "𝖲ans",
            preview = "𝖠𝖡𝖢 𝖺𝖻𝖼",
            upperOffset = 0x1D5A0 - 'A'.code,
            lowerOffset = 0x1D5BA - 'a'.code,
            digitOffset = 0x1D7E2 - '0'.code
        ),

        SANS_BOLD(
            label = "𝗦ans 𝗕old",
            preview = "𝗔𝗕𝗖 𝗮𝗯𝗰",
            upperOffset = 0x1D5D4 - 'A'.code,
            lowerOffset = 0x1D5EE - 'a'.code,
            digitOffset = 0x1D7EC - '0'.code
        ),

        SANS_ITALIC(
            label = "𝘚ans 𝘐talic",
            preview = "𝘈𝘉𝘊 𝘢𝘣𝘤",
            upperOffset = 0x1D608 - 'A'.code,
            lowerOffset = 0x1D622 - 'a'.code,
            digitOffset = 0
        ),

        SANS_BOLD_ITALIC(
            label = "𝙎ans 𝘽old 𝙄talic",
            preview = "𝘼𝘽𝘾 𝙖𝙗𝙘",
            upperOffset = 0x1D63C - 'A'.code,
            lowerOffset = 0x1D656 - 'a'.code,
            digitOffset = 0
        ),

        MONOSPACE(
            label = "𝙼ono",
            preview = "𝙰𝙱𝙲 𝚊𝚋𝚌",
            upperOffset = 0x1D670 - 'A'.code,
            lowerOffset = 0x1D68A - 'a'.code,
            digitOffset = 0x1D7F6 - '0'.code
        );

        /**
         * Transforma una cadena de texto al estilo Unicode correspondiente.
         * Los caracteres que no son letras ni dígitos se mantienen intactos.
         */
        fun transform(input: String): String {
            val sb = StringBuilder()
            for (ch in input) {
                sb.append(transformChar(ch))
            }
            return sb.toString()
        }

        private fun transformChar(ch: Char): String {
            // Verificar mapa de excepciones primero
            exceptions[ch]?.let { return it }

            return when {
                ch.isUpperCase() && upperOffset != 0 -> {
                    codePointToString(ch.code + upperOffset)
                }
                ch.isLowerCase() && lowerOffset != 0 -> {
                    codePointToString(ch.code + lowerOffset)
                }
                ch.isDigit() && digitOffset != 0 -> {
                    codePointToString(ch.code + digitOffset)
                }
                else -> ch.toString()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Convierte un code point Unicode (potencialmente > 0xFFFF) a String.
     * Los caracteres matemáticos están en el plano suplementario (BMP+1)
     * y requieren un par sustituto (surrogate pair) en UTF-16.
     */
    private fun codePointToString(codePoint: Int): String {
        return String(Character.toChars(codePoint))
    }

    /**
     * Aplica un estilo al texto. Punto de entrada principal.
     */
    fun apply(text: String, style: Style): String {
        return style.transform(text)
    }

    /**
     * Devuelve todos los estilos disponibles para mostrar en la UI.
     */
    fun allStyles(): List<Style> = Style.values().toList()
}
