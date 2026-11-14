# PROMPT-9-16: Cadenas y comentarios

## Estado

**Validado por el léxico. No requiere cambios de código.**

El analizador léxico (`AccionCadena.java`) ya implementa correctamente los requisitos de TP3 para los temas 9 y 16.

## Qué implementa `AccionCadena.java`

- **Cadenas de una línea** `{texto}`:
  - Se insertan en la Tabla de Símbolos con `tipoDato = cadena`, `uso = cadena`, `declarado = true`.
  - No se permiten saltos de línea dentro de la cadena: error léxico `"Cadena de una linea con salto de linea"`.
  - Cadena sin cerrar: error léxico `"Cadena no cerrada '<lexema>'"`.
  - El contenido se inserta sin las llaves delimitadoras.
- **Comentarios multilínea** `{{ ... }}`:
  - Se descartan por completo (retorna -1).
  - No aparecen en la Tabla de Símbolos ni en la tira de tokens.
  - Pueden contener llaves, guiones bajos y varias líneas.

## Verificación con tests existentes

Los siguientes tests de `test/tp1/verificaciones_comentarios_cadenas/` se ejecutaron y pasaron sin defectos:

- `cadenas_validas.txt`: cadenas `{hola}`, `{texto con espacios 123}`, `{simbolos = + - * /}` se insertan con tipo `cadena` y generan `(pout, contenido, --)`.
- `cadenas_invalidas.txt`: `{cadena` y `{sin cierre` reportan error léxico de cadena no cerrada.
- `comentarios_validos.txt`: comentarios `{{ ... }}` se descartan sin errores.
- `comentarios_invalidos.txt`: comentario sin cerrar `{{ este comentario nunca se cierra` reporta error léxico.
- `cadena_con_guion_bajo.txt`: cadena con guion bajo se inserta correctamente.
- `guion_bajo_en_comentario_y_cadena.txt`: guion bajo en comentario y cadena se maneja correctamente.

No se detectaron defectos ni faltantes léxicos importantes.

## Criterios de aceptación cumplidos

- Los casos válidos no generan errores. ✅
- Los inválidos generan el error léxico correspondiente. ✅
- Las cadenas llegan al codegen con su tipo (`pout, contenido, --`). ✅

## Riesgo

Futuras modificaciones sobre el core del compilador (léxico, tabla de símbolos o acciones semánticas) llevarán a una revisión de estas reglas antes de la entrega final.
