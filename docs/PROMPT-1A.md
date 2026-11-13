# PROMPT-1A: base de Name Mangling y resolucion por ambitos

## Objetivo

Preparar la tabla de simbolos para el name mangling y para la resolucion por cadena de
ambitos, sin cambiar todavia la forma en que `ControlAmbito` abre y cierra funciones,
clases y metodos. La semantica de cierre de ambitos se completa en el Prompt 1B.

**Estado:** ejecutado y superado por `docs/PROMPT-1B.md`. `MonitorAmbito` ya no existe y
las clases y metodos abren su propio ambito.

## Cambios realizados

### `EntradaTabla`

- Se agrego el enum `Uso` con: `INDEFINIDO`, `VARIABLE`, `PARAMETRO`, `FUNCION`,
  `METODO`, `CLASE`, `ATRIBUTO`, `TIPO`, `CONSTANTE`, `CADENA` y `RESERVADA`.
- Se agrego el campo `uso`, que clasifica semanticamente la entrada.
- Se agrego `declarado`, que distingue una declaracion real de un placeholder creado por
  un uso previo del mismo identificador.

### `TablaSimbolos`

- `resolver(nombre, ambito)` busca la clave exacta y sube por los ambitos padres
  (`main:f:g` -> `main:f` -> `main`). Solo devuelve entradas con `declarado = true`.
- `estaDeclarado(nombre, ambito)` consulta la clave exacta, sin subir de ambito.
- `declarar(entrada, nombre, ambito, uso)` confirma una declaracion, reutiliza el
  placeholder de la misma clave si existe y marca `ambito`, `uso` y `declarado`.
  Devuelve `null` si la clave ya pertenece a otra declaracion.
- Las palabras reservadas se cargan con `uso = RESERVADA`.
- Las constantes y cadenas se marcan con su uso correspondiente.
- La impresion de la tabla agrega `Uso` y `Declarado`.

### `Globals` y `ControlAmbito`

- `Globals.AMBITO_ACTUAL` arranca en `main`.
- Al entrar a una funcion, el ambito se arma con el nombre tal como se leyo, sin
  convertir a mayuscula: `main:a`, `main:a:b`. Esto implementa D1.

### `Entorno`

- Todos los puntos de declaracion pasan por `TablaSimbolos.declarar` y registran su uso:
  variable, parametro, funcion, clase, atributo o tipo.
- `declararVariable` solo trata un identificador como atributo si esta dentro de una
  clase **y no** dentro de un metodo. Los locales de un metodo dejan de registrarse como
  atributos.
- Se agrego `resolver(nombre, ambito)` como fachada de la tabla.

### `Verificaciones`

- `verificarAlcance` usa `Entorno.resolver`. Un placeholder de uso ya no satisface la
  comprobacion: se exige una declaracion real en el ambito actual o en un padre.

## Decisiones

- **D1 aplicada**: todos los ambitos se escriben en minuscula.
- **D3 revisada**: no se implementa todavia, pero el diseno de `Uso` y `declarado` deja
  preparada la separacion entre miembros propios y heredados. Ver `PROMPT-3.10.md`.
- **D5 parcial**: se agrega la resolucion por cadena de ambitos, pero `MonitorAmbito`
  sigue eliminando entradas al salir. El Prompt 1B elimina ese comportamiento.
- **Compatibilidad**: se mantiene la estructura actual de `ControlAmbito` para que este
  prompt no mezcle el cambio de modelo de datos con el cambio de apertura/cierre de
  ambitos.

## Verificacion

- Compila sin errores con:

  ```bash
  javac -encoding UTF-8 -d /tmp/opencode/tp3build *.java "acciones semanticas"/*.java otras_acciones/*.java
  ```

- Se ejecutaron los 65 casos del baseline y se guardaron en `/tmp/opencode/tp3-1a/`.
- **Diagnosticos**: no hubo diferencias con `/tmp/opencode/tp3-baseline/`; los mismos
  casos siguen informando los mismos errores. Esto confirma que 1A es un refactor de la
  tabla y no un cambio prematuro de comportamiento.
- **Tercetos**: 52 casos cambian solo por el pasaje de `MAIN` a `main` y de los nombres
  de funcion a minuscula. No cambio la estructura de ningun terceto.

## Pendiente para el Prompt 1B (completado en `docs/PROMPT-1B.md`)

- Eliminar `MonitorAmbito`.
- Abrir el ambito de funcion al leer `(` para que los parametros se lexen directamente
  en el ambito de la funcion.
- Hacer que las clases abran su propio ambito y que los metodos abran un ambito hijo.
- Conservar las entradas al cerrar un ambito (D2/D5) y resolver por padres.
- Actualizar `Entorno`, `Verificaciones` y las acciones para usar las claves con ambito.
