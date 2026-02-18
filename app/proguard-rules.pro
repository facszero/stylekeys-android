# StyleKeys — ProGuard rules

# Mantener el servicio IME (Android lo referencia por nombre en el manifest)
-keep class com.stylekeys.StyleKeyboardService { *; }
-keep class com.stylekeys.MainActivity { *; }
-keep class com.stylekeys.TextStyler { *; }
-keep enum com.stylekeys.TextStyler$Style { *; }
