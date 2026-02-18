# StyleKeys — Teclado Android con estilos UTF-8

> Texto con **negrita**, *itálica*, 𝔉raktur y más, en cualquier campo de texto de Android.  
> Sin salir de la app. Sin copiar/pegar de webs externas.

---

## Qué es

StyleKeys es un **teclado personalizado para Android** (Input Method Editor / IME)  
que permite escribir texto con variantes tipográficas Unicode directamente en  
LinkedIn, Facebook, Instagram, Twitter, o cualquier otra app.

Funciona porque Unicode incluye un bloque de caracteres matemáticos  
(U+1D400–U+1D7FF) que son visualmente distintos pero se comportan como  
texto plano: se copian, pegan y muestran en cualquier plataforma UTF-8.

---

## Estilos disponibles (11)

| Estilo | Muestra |
|---|---|
| Bold | 𝐒𝐭𝐲𝐥𝐞𝐊𝐞𝐲𝐬 |
| Italic | 𝑆𝑡𝑦𝑙𝑒𝐾𝑒𝑦𝑠 |
| Bold Italic | 𝑺𝒕𝒚𝒍𝒆𝑲𝒆𝒚𝒔 |
| Script | 𝒮𝓉𝓎𝓁𝑒𝒦𝑒𝓎𝓈 |
| Fraktur | 𝔖𝔱𝔶𝔩𝔢𝔎𝔢𝔶𝔰 |
| Double Struck | 𝕊𝕥𝕪𝕝𝕖𝕂𝕖𝕪𝕤 |
| Sans | 𝖲𝗍𝗒𝗅𝖾𝖪𝖾𝗒𝗌 |
| Sans Bold | 𝗦𝘁𝘆𝗹𝗲𝗞𝗲𝘆𝘀 |
| Sans Italic | 𝘚𝘵𝘺𝘭𝘦𝘒𝘦𝘺𝘴 |
| Sans Bold Italic | 𝙎𝙩𝙮𝙡𝙚𝙆𝙚𝙮𝙨 |
| Monospace | 𝚂𝚝𝚢𝚕𝚎𝙺𝚎𝚢𝚜 |

---

## Arquitectura del proyecto

```
StyleKeys/
├── app/
│   ├── build.gradle                          # Dependencias y configuración de build
│   ├── proguard-rules.pro                    # Reglas de ofuscación
│   └── src/main/
│       ├── AndroidManifest.xml               # Registro del IME en el sistema
│       ├── java/com/stylekeys/
│       │   ├── TextStyler.kt                 # Motor de transformación UTF-8
│       │   ├── StyleKeyboardService.kt       # Servicio IME (el teclado en sí)
│       │   └── MainActivity.kt              # Pantalla de onboarding/configuración
│       └── res/
│           ├── layout/
│           │   ├── keyboard_view.xml         # Layout del teclado
│           │   └── activity_main.xml         # Layout de la pantalla de configuración
│           ├── xml/
│           │   └── method.xml               # Metadata del IME para Android
│           ├── drawable/                     # Backgrounds y shapes
│           └── values/
│               ├── strings.xml
│               ├── colors.xml
│               └── themes.xml
└── build.gradle                              # Plugin declarations
```

---

## Decisiones técnicas

### ¿Por qué un IME y no una extensión o overlay?

Las apps nativas de Android (LinkedIn, Facebook) son cajas cerradas.  
No hay DOM accesible, no hay hooks de accessibility que permitan modificar  
texto in-place sin permiso explícito de la app.  

El **único mecanismo que Android provee** para insertar texto en cualquier  
campo de cualquier app sin permisos especiales es el IME (Input Method Editor).  
Es el mismo canal que usa el teclado Gboard de Google.

### ¿Por qué no usar la librería Chip de Material?

Para un IME, el proceso de inflado de vistas debe ser ultraliviano.  
Las chips de Material tienen dependencias en temas de Activity que pueden  
conflictuar con el contexto de un servicio. Los chips son `TextView` simples  
con backgrounds intercambiables — exactamente la misma funcionalidad, cero overhead.

### TextStyler: por qué offsets de codepoints y no tablas de lookup

Los bloques matemáticos Unicode son secuencias contiguas de 26 letras  
(mayúsculas y minúsculas) más 10 dígitos, con un offset fijo respecto al ASCII.  
Usar una fórmula `char.code + offset` es O(1), sin allocations innecesarias.  
Las excepciones (caracteres que históricamente ya existían en Unicode antes  
de que se formalizara el bloque matemático) se manejan con un Map pequeño.

### ¿Por qué `String(Character.toChars(codePoint))` y no un cast directo?

Los caracteres matemáticos están en el **Plano Suplementario Multilingüe** (SMP),  
con codepoints > U+FFFF. En Java/Kotlin, un `Char` es de 16 bits (UTF-16)  
y no puede representar estos codepoints directamente. Se necesita un  
**surrogate pair** (dos `Char` de 16 bits). `Character.toChars()` maneja  
eso automáticamente y devuelve un `CharArray` de 1 o 2 elementos.

---

## Cómo compilar

### Requisitos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 8+
- Android SDK con API 26+ instalado

### Pasos
```bash
git clone <repo>
cd StyleKeys
./gradlew assembleDebug
```

El APK quedará en `app/build/outputs/apk/debug/app-debug.apk`.

---

## Cómo instalar y usar

### Instalación (primera vez)
1. Instalar el APK en el dispositivo
2. Abrir la app **StyleKeys** — verás la guía de 2 pasos
3. **Paso 1**: tocá "Abrir configuración" → activá StyleKeys en la lista de teclados
4. **Paso 2**: tocá "Seleccionar teclado" → elegí StyleKeys

### Uso diario
1. Abrí LinkedIn (o cualquier app) y tocá un campo de texto
2. Tocá el ícono de teclado en la barra de navegación
3. Elegí **StyleKeys** de la lista
4. Seleccioná un estilo en la barra superior
5. Escribí tu texto — la vista previa se actualiza en tiempo real
6. Tocá **Insertar** → el texto estilizado aparece en LinkedIn
7. Tocá **⌨ Cambiar teclado** para volver a tu teclado habitual

---

## Roadmap de features futuras

- **Historial**: guardar los últimos N textos insertados
- **Favoritos**: marcar estilos favoritos para que aparezcan primero
- **Combinación de estilos**: texto mixto (parte bold, parte italic)
- **Emojis semánticos**: atajos para emojis de uso frecuente en posts
- **Themes**: dark/light/AMOLED
- **Teclado QWERTY integrado**: modo full-keyboard sin necesidad de cambiar
- **Widget de acceso rápido**
- **Soporte RTL**: árabe, hebreo

---

## Compatibilidad

| Android | Estado |
|---|---|
| 8.0 (API 26) | ✅ Mínimo soportado |
| 10 (API 29) | ✅ Probado |
| 12 (API 31) | ✅ Probado |
| 14 (API 34) | ✅ Target |

---

## Licencia

MIT — Libre para usar, modificar y distribuir.
