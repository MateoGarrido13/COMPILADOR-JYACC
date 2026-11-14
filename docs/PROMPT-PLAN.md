# Plan de prompts reajustado

Este plan reemplaza la secuencia anterior. Los cambios respecto de la version previa son:

- **D1**: ambitos en minuscula (`main:f:g`).
- **D2**: clases y metodos abren ambito propio.
- **D3 revisada**: los miembros heredados exigen prefijo explicito. El uso sin prefijo de
  un miembro heredado es error. Ver `PROMPT-3.10.md`.
- **D4**: compatibilidad estructural por conjunto efectivo de atributos y firmas.
- **D5**: las entradas de ambitos cerrados se conservan; la visibilidad se resuelve por
  la cadena de ambitos.

Cada prompt ejecutado debe dejar su archivo de decisiones en `docs/`.

---

## Prompt 0 - Baseline reproducible (ejecutado)

- Compilar y ejecutar los 65 casos de regresion.
- Guardar salidas en `/tmp/opencode/tp3-baseline/`.
- Documentar errores, tercetos y validez esperada.
- Resultado: `docs/PROMPT-0.md`.

## Prompt 1A - Base de Name Mangling (ejecutado)

- `EntradaTabla.Uso` y `declarado`.
- `TablaSimbolos.resolver`, `estaDeclarado`, `declarar`.
- `Globals.AMBITO_ACTUAL = "main"`.
- Declaraciones del `Entorno` marcadas con su uso.
- `verificarAlcance` por resolucion de ambitos.
- Resultado: `docs/PROMPT-1A.md`.

## Prompt 1B - Apertura y cierre real de ambitos (ejecutado)

- Eliminar `MonitorAmbito`.
- Abrir el ambito de funcion al consumir `(`, para que los parametros se lexen dentro.
- Clase `ca` en `main`: clave `ca:main`.
- Atributo `x` de `ca`: clave `x:main:ca`.
- Metodo `m` de `ca`: clave `m:main:ca`; su cuerpo usa `main:ca:m`.
- No borrar entradas al cerrar; solo restaurar `AMBITO_ACTUAL`.
- Actualizar `Entorno`, `Verificaciones` y acciones para usar claves con ambito.
- Documentar D1, D2 y D5 en `docs/PROMPT-1B.md`.
- Resultado: `docs/PROMPT-1B.md`. Casos en
  `test/TP3_SEMANTICAS/prompt-1a-ambitos/` y `prompt-1b-name-mangling/`.

## Prompt 2 - Igualdad estricta de tipos (ejecutado)

- No conversiones implicitas.
- `ulongint` solo con `ulongint`; `doublef` solo con `doublef`; objetos solo con la misma
  clase o estructura compatible (D4).
- Chequear asignacion, operadores aritmeticos, comparaciones, condiciones, `ret`, `pout`
  y parametros reales.
- `FACTOR_ID` restringido a variables, parametros y atributos.
- Resolucion de atributos heredados con prefijo de clase.
- Documentar en `docs/PROMPT-2.md`.
- Resultado: `docs/PROMPT-2.md`. Casos en
  `test/TP3_SEMANTICAS/prompt-2-tipos-estrictos/`.

## Prompt 2B - Codegen base (ejecutado)

- Delimitadores `PROGRAMA`, `FUNCION`, `METODO`, `CLASE` y sus cierres.
- `pout`, `ret`, invocaciones de funcion y de metodo.
- Convencion de llamadas: `(PAR, formal, real)` y `(CALL, funcion, cantidad)`; para
  metodos, `(CALLMET, objeto, metodo)`.
- `todf` emite `(todf, valor, --)` y devuelve tipo `doublef`; la restriccion de tipo de
  origen queda para el Prompt 3.5.
- La asignacion en expresion emite su propio `(:=, destino, valor)` y vale el numero de
  ese terceto.
- Documentar en `docs/PROMPT-2B.md`.
- Resultado: `docs/PROMPT-2B.md`. Casos en
  `test/TP3_SEMANTICAS/prompt-2b-codegen-base/`.

## Prompt 6 y 8 - Constantes `ulongint` y `doublef` (ejecutado)

- Tipar cada constante en el momento del analisis lexico.
- Mantener rango y formato.
- Documentar en `docs/PROMPT-6-8.md`.
- Resultado: `docs/PROMPT-6-8.md`. Cambios en `AccionConstanteEntera.java` y `AccionConstanteFloat.java`: usar `insertarConstante(lexema, tipo, linea)` para registrar `tipoDato` y `uso = CONSTANTE` en el lexico.

## Prompt 9 y 16 - Cadenas y comentarios (ejecutado)

- Verificar el estado actual contra TP1/TP3.
- Completar solo si falta validacion o tipo.
- Documentar en `docs/PROMPT-9-16.md`.
- Resultado: `docs/PROMPT-9-16.md`. Validado por el lexico en `AccionCadena.java`: cadenas con tipo `cadena`, comentarios `{{...}}` descartados, sin saltos de línea en cadenas. No requiere cambios de código.

## Prompt 13 - `repeat while` (ejecutado)

- Condicion obligatoria y booleana por comparacion de tipos.
- Integrar todas las sentencias ejecutables.
- Documentar en `docs/PROMPT-13.md`.
- Resultado: `docs/PROMPT-13.md`. Condicion con chequeo de mismo tipo y rechazo de cadena en `AccionesDeclaraciones.java`. Cuerpo de repeat con asignaciones, pout, if/else, repeat anidado, invocaciones y ret. Todos los 11 casos de ITERACION pasan.

## Prompt 17 - Asignacion dentro de expresiones

- `ID = (expr)` genera asignacion interna y devuelve el valor asignado.
- Prohibir anidamiento.
- Verificar tipos del destino y de la expresion.
- Documentar en `docs/PROMPT-17.md`.

## Prompt 33 - Conversion `todf`

- Solo `ulongint` a `doublef`.
- Emitir terceto `todf`.
- Rechazar conversion vacia, de `doublef` o de objetos.
- Documentar en `docs/PROMPT-33.md`.

## Prompt 19 - Parametros reales y orden

- Cantidad, tipos y orden `[ ... ]`.
- Copias `(PAR, formal, real)` y `(CALL, funcion, --)`.
- Documentar en `docs/PROMPT-19.md`.

## Prompt 23 - Enumeraciones

- Verificar que el tipo exista al declarar variables.
- Verificar que la constante asignada pertenezca al enumerado.
- Documentar en `docs/PROMPT-23.md`.

## Prompt 25 - Objetos y asignacion estructural

- Verificar que la clase exista.
- Compatibilidad estructural por atributos y firmas efectivas (D4).
- Generar copia estructural, no asignacion simple.
- Documentar en `docs/PROMPT-25.md`.

## Prompt 28 - Acceso tradicional a atributos

- `objeto.atributo` solo para atributos propios de la clase del objeto.
- Verificar existencia, visibilidad y tipo.
- Generar terceto con la referencia al atributo.
- Documentar en `docs/PROMPT-28.md`.

## Prompt 31 - Herencia multiple con prefijo (D3 revisada)

- Validar clases base y jerarquia.
- `objeto.clase.miembro` solo si `clase` es base y el miembro esta declarado en ella.
- `objeto.miembro` solo busca miembros propios; si el miembro es heredado, error por
  falta de prefijo.
- Aplicar a atributos y metodos, en lectura y asignacion.
- Actualizar o mantener como error esperado los casos con `a1.m(...)`.
- Documentar en `docs/PROMPT-31.md`.
