# Prompt 3.6: orden obligatorio de parámetros reales

## Estado

Implementado. Las invocaciones de funciones y métodos validan sus argumentos y
el orden de evaluación; las llamadas válidas generan sus copias a formales y
conservan el tipo de retorno.

## Cambios

- Las funciones se resuelven desde el ámbito visible mediante su nombre
  mangled. Invocar una variable o un nombre no declarado informa un error.
- Las llamadas a métodos resuelven la firma en la clase del objeto (incluida
  la búsqueda heredada existente); un método no declarado o no visible ahora
  informa un error.
- La validación semántica comprueba cantidad de reales, tipo exacto de cada
  real, existencia de formales nombrados y ausencia de formales asignados más
  de una vez. La comprobación de repetidos también cubre la mezcla de argumentos
  nombrados y posicionales.
- El orden debe contener una permutación completa de posiciones entre `1` y la
  cantidad de formales. Se valida con `BigInteger` para evitar desbordamientos
  al procesar constantes grandes.
- El generador emite un `(PAR, formal, real)` por posición en orden de
  evaluación y después `(CALL, funcion, cantidad)`. Para métodos conserva la
  convención de Fase 2B: `(PAR, formal, real)` y `(CALLMET, objeto, metodo)`.
- El tipo de retorno de la llamada participa en las verificaciones de
  asignación y de operadores, sin conversiones implícitas.

Los errores semánticos de argumentos y orden se reportan con la línea de la
invocación. Una llamada inválida no genera su `CALL`/`CALLMET`.

## Clases editadas para completar R19

### `otras_acciones/AccionesExpresiones.java`

- Reporta si no se encuentra la firma del método para el objeto y, si
  corresponde, la clase prefijada. La búsqueda de firmas existente se reutiliza.

### `Verificaciones.java`

- Restringe la resolución de una llamada funcional a símbolos de uso `FUNCION`
  y distingue los nombres no declarados de los símbolos que no son funciones.
- Añade el diagnóstico para métodos sin firma resuelta.
- Valida posiciones del orden directamente como `BigInteger`, comprobando
  rango, duplicados y que haya una permutación completa sin desbordamiento.
- Verifica que no haya dos reales asociados al mismo formal, incluso si se
  mezclan argumentos nombrados y posicionales.

## Clases relevantes reutilizadas

Estas clases participan en el flujo de invocación, pero no se editaron como
parte de los cambios descritos arriba.

### `ParametroReal.java`

Transporta nombre formal, valor, línea y entrada formal. El campo `tipoReal`
permite comparar el tipo de la expresión con el tipo del formal. Este campo y
los constructores que lo preservan ya estaban en los cambios locales presentes
al iniciar esta tarea; no fueron editados en la modificación final descrita
aquí.

### `LlamadaFuncion.java`

Ya transporta la firma y tipo de retorno, parámetros, orden, validez, receptor
del método y número del terceto de llamada. No se modificó: las acciones y el
generador reutilizan ese estado.

### `ExpresionDiferida.java`

Ya obtiene el tipo de retorno de `LlamadaFuncion`, de modo que las verificaciones
existentes de asignación y operadores también se aplican al resultado de una
invocación. No se modificó.

### `GeneradorTercetos.java`

Ya recorre el orden de evaluación para emitir `(PAR, formal, real)`, seguido de
`(CALL, funcion, cantidad)` o `(CALLMET, objeto, metodo)`. Guarda el número del
terceto de llamada para que la invocación pueda usarse como operando. No se
modificó.

### `Entorno.java`

Provee las firmas `FuncionDeclarada`, la resolución por nombre mangled y la
búsqueda heredada de métodos. Este soporte preexistente se reutiliza; no se
modificó para R19.

## Verificación

Compilación:

```bash
javac -encoding UTF-8 -d /tmp/tp3build *.java "acciones semanticas"/*.java otras_acciones/*.java
```

Se ejecutaron los archivos de `test/tp2/verificacion_r19_param_reales/` y los
casos existentes de métodos heredados de
`test/tp2/verificacion_r28_r31_atributos/`.

| Archivo/caso | Resultado verificado |
|---|---|
| `estructura_respetada.txt` | Sin errores; produce `PAR` en el orden indicado y `CALL`. |
| `asignacion_en_parametros.txt` | Sin errores; conserva las asignaciones internas y cambia el orden de los `PAR`. |
| `orden_sin_corchetes.txt`, `sin_orden.txt` | Informan falta del orden obligatorio. |
| `futuros/orden_fuera_de_rango.txt`, `futuros/orden_repetido.txt` | Informan posiciones inválidas o repetidas y no generan llamadas. |
| `tipos_argumentos.txt` | Acepta tipos iguales y rechaza los incompatibles, también para parámetros nombrados. |
| `aridad_y_formales.txt` | Detecta cantidad incorrecta, nombre inexistente y formal repetido, incluso en mezcla nombrado/posicional. |
| `resolucion_funcion.txt` | Detecta función no declarada e intento de invocar una variable. |
| `tipo_retorno.txt` | Rechaza retornos incompatibles en asignaciones y operaciones; acepta usos compatibles. |
| `orden_limites.txt` | Rechaza orden incompleto y `4294967295` fuera de rango sin desbordamiento. |
| `metodos.txt` | Valida tipos y orden de métodos, y reporta un método inexistente. |
| `metodo_heredado_prefijado_con_orden.txt` | Conserva la convención `CALLMET` para métodos heredados válidos. |
