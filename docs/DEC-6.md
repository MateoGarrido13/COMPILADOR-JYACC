## DEC-6: Registro de tipos de constantes en el analisis lexico

El enunciado de TP3 exige que **el tipo de cada constante numerica (`ulongint` o `doublef`) quede registrado en la Tabla de Símbolos durante el Analisis Léxico**, no recién al usarla como factor en una expresion.

### Cambios realizados

**`acciones semanticas/AccionConstanteEntera.java`**

- Antes: `buscarOInsertarConstante(lexema, Globals.CONSTANTE_NUMERICA)` insertaba la constante sin asignar `tipoDato`.
- Después: `insertarConstante(lexema, Tipos.ULONGINT, Globals.numeroLinea)` asigna `tipoDato = ulongint`, `uso = CONSTANTE` y `declarado = true` durante el lexico.

**`acciones semanticas/AccionConstanteFloat.java`**

- Antes: `buscarOInsertarConstante(lexema, Globals.CONSTANTE_NUMERICA)` insertaba la constante sin asignar `tipoDato`.
- Después: `insertarConstante(lexema, Tipos.DOUBLEF, Globals.numeroLinea)` asigna `tipoDato = doublef`, `uso = CONSTANTE` y `declarado = true` durante el lexico.

**No se modificó:**

- `Tipos.rangoValido()`: rangos y formatos se mantienen sin cambios.
- `Tipos.tipoDeConstante()`: queda como fallback, pero la fuente principal es el tipo asignado en el lexico.
- `Entorno.aplicarSignoNegativo()`: ya usaba `insertarConstante` con el tipo deducido del lexema.
- `TablaSimbolos.insertarConstante()`: ya existía con la firma `(String lexema, String tipoDato, int numeroLinea)`.

### Verificacion con tests

Se utilizaron los tests existentes de **tp1** y **tp2** para verificar que el tipo de dato se asigne correctamente a cada constante:

**tp1 (analizador lexico):**

- `test/tp1/verificacion_constantes_ulongint/limites_validos.txt`: `0$ul` y `4294967295$ul` registran `Tipo de dato: ulongint`, `Uso: constante`.
- `test/tp1/verificacion_constantes_ulongint/fuera_de_rango.txt`: `4294967296$ul` y `-1$ul` reportan error de rango (comportamiento esperado).
- `test/tp1/verificacion_constantes_doublef/limites_validos.txt`: `0.0`, `2.2250738585072014d-308`, `1.7976931348623157d+308` y sus negativos registran `Tipo de dato: doublef`, `Uso: constante`.
- `test/tp1/verificacion_constantes_doublef/fuera_de_rango.txt`: valores fuera de rango IEEE-754 reportan error (comportamiento esperado).
- `test/tp1/verificacion_constantes_doublef/chequeo_formatos.txt`: formatos invalidos como `12.` y `+2` reportan error (comportamiento esperado).

**tp2 (analizador semantico y generador de tercetos):**

- `test/tp2/verificacion_r17_asig_expresiones/estructura_respetada.txt`: constantes `ulongint` mantienen tipo correcto en asignaciones.
- `test/tp2/verificacion_r17_asig_expresiones/en_invocacion.txt`: constantes `ulongint` como parametros reales conservan tipo.
- `test/tp2/verificacion_r33_conversion/en_asignacion.txt`: constantes `ulongint` y `doublef` en asignaciones con `todf`.
- `test/tp2/verificacion_r33_conversion/en_condicion_if.txt`: constantes `ulongint` y `doublef` en condiciones.
- `test/tp2/generales/00_programa_valido.txt`: programa valido con constantes de ambos tipos.

No se detectaron regresiones: los tipos se muestran correctamente en la Tabla de Símbolos y los tercetos siguen usando el lexema de la constante como operando directo (sin generar tercetos intermedios por cada constante).
