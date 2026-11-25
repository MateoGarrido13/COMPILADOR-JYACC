# Prompt 3.6: orden obligatorio de parámetros reales

## Estado

Implementado para las invocaciones que incluyen el orden entre corchetes. Estas
validan sus argumentos y el orden de evaluación; las llamadas válidas generan sus
copias a formales y conservan el tipo de retorno. La anulación del IR cuando falta
el orden queda registrada en P2-02 y su implementación en P2-05.

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
invocación. Una llamada inválida no genera su `CALL`/`CALLMET`. La omisión del
orden ya no es un caso pendiente: P2-05 hace que la acción devuelva una
expresión inválida y P2-07 verificó que esos casos no emiten `PAR`, `CALL` ni
`(:=, x:main, foo:main)`.

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
| `18_falta_orden.txt`, `orden_sin_corchetes.txt`, `sin_orden.txt` | Informan falta del orden obligatorio; tras P2-05 no generan `PAR`, `CALL` ni `(:=, x:main, foo:main)`. |
| `futuros/orden_fuera_de_rango.txt`, `futuros/orden_repetido.txt` | Informan posiciones inválidas o repetidas y no generan llamadas. |
| `tipos_argumentos.txt` | Acepta tipos iguales y rechaza los incompatibles, también para parámetros nombrados. |
| `aridad_y_formales.txt` | Detecta cantidad incorrecta, nombre inexistente y formal repetido, incluso en mezcla nombrado/posicional. |
| `resolucion_funcion.txt` | Detecta función no declarada e intento de invocar una variable. |
| `tipo_retorno.txt` | Rechaza retornos incompatibles en asignaciones y operaciones; acepta usos compatibles. |
| `orden_limites.txt` | Rechaza orden incompleto y `4294967295` fuera de rango sin desbordamiento. |
| `metodos.txt` | Valida tipos y orden de métodos, y reporta un método inexistente. |
| `metodo_heredado_prefijado_con_orden.txt` | Conserva la convención `CALLMET` para métodos heredados válidos. |

### Caso general `18_falta_orden.txt`

El enunciado del tema 19 (TP02, líneas 224-236) exige la lista de orden entre
corchetes: `ID ( <lista_parametros_reales> ) [ <lista_orden_evaluacion> ]`.
Omitir los corchetes —o escribir `[]`— es inválido. El tema 20, que no corresponde
al grupo, es el que vuelve opcional esa lista; acá no se adopta esa relajación.

En `test/tp2/generales/18_falta_orden.txt` el error
`Falta del orden de evaluacion y asignacion de parametros` se informa en la
línea 8. El baseline emitía `(:=, x:main, foo:main)`, es decir, una asignación
desde el nombre de la función. Ese IR es inválido: no hay `PAR`, no hay `CALL` y
el resultado de la invocación no puede usarse como valor. P2-05 eliminó esa
asignación; P2-07 confirmó el estado final.

El expected result es: error presente, cero `PAR`, cero `CALL` y cero
`(:=, x:main, foo:main)`. Se conservan los tercetos de la declaración de `foo`,
el `ret(n)` interno y los delimitadores `(PROGRAMA)` / `(FIN_PROGRAMA)`, según la
política P0-02. P2-05 registró la acción que devuelve una expresión inválida
para `ERR_FALTA_ORDEN_EVALUACION`; P2-07 verificó este caso junto con
`orden_sin_corchetes.txt` y `sin_orden.txt`: los tres emiten cero `PAR`, cero `CALL` y
cero asignaciones desde `foo`.
