# Contrato IR actual (TP3) — base para backend ASM (TP4)

Documento de auditoría (Prompt 1 TP4). Describe el código intermedio tal como lo emite hoy `GeneradorTercetos`/`ListaTercetos`/`Terceto`, sin cambios semánticos de TP3.

## 1. Estructura

- `Terceto`: `(operador: String, op1: Object, op2: Object)`.
- `ListaTercetos`: lista **1-based** (`crear` agrega y devuelve índice `size` = número de terceto, usado como referencia de resultado). `cantidad()` = total. `completar(n,pos,valor)` rellena operandos diferidos (saltos). `descartarDesde(k)` trunca cola (condiciones inválidas en `repeat`).
- Impresión (`Terceto.visualizar` / `texto`):
  - `null` -> `--`
  - `EntradaTabla` -> `entrada.lexema` (clave/lexema visible en TS, incluye mangling/ámbito cuando aplica)
  - `String` -> literal tal cual (etiquetas, nombres, cantidad params en `CALL`)
  - `Integer` -> `[n]` (referencia al terceto `n`)
  - `ReferenciaAtributo` -> `toString()` (texto `obj.atr` o `obj.clase.atr`)
  - otro -> `?`

## 2. Operandos

| Forma Java | Significado | Ejemplos |
|---|---|---|
| `EntradaTabla` | Símbolo/constante/cadena declarado o materializado en TS | variables, params, funciones/métodos, ctes `$const:*`, cadenas `$cadena:*` |
| `Integer` | Resultado del terceto `n` (valor intermedio) | `(:=, x, [3])`, `(+, [1], [2])` |
| `String` | Nombre/etiqueta/cantidad textual | `label_1`, nombre de función/clase, `"2"` en `CALL` |
| `ReferenciaAtributo` | Acceso a atributo tipado, con texto completo | `(+, a1.x:main, 1$ul)`, `(:=, a1.padre.y:main, ...)` |
| `null` | Operando ausente | `(--, --, --)` en labels/marcadores |

## 3. Operadores y convenciones

### 3.1 Delimitadores / estructura
- `(PROGRAMA, --, --)` / `(FIN_PROGRAMA, --, --)`: envuelven todo el programa.
- `(FUNCION, nombre, --)` / `(FIN_FUNCION, nombre, --)`: cuerpo de función.
- `(METODO, nombre, --)` / `(FIN_METODO, nombre, --)`: cuerpo de método.
- `(CLASE, nombre, --)` / `(FIN_CLASE, nombre, --)`: bloque de clase (puede descartarse si clase inválida; no ejecutable en ASM pero delimita contexto).
- `(label_N, --, --)`: etiqueta de salto (terceto-marcador; no produce valor).

### 3.2 Asignación y copia
- `(:=, destino, origen)`: asignación escalar. `destino` = `EntradaTabla` o `ReferenciaAtributo`. `origen` = `EntradaTabla|Integer|ReferenciaAtributo|String` según caso. Usada también para asignación en expresión (devuelve valor = nro de terceto `:=`).
- `(COPY, destino, origen)`: copia/asignación estructural entre objetos compatibles (Tema 25). `destino` y `origen` = `EntradaTabla` (objetos).

### 3.3 Aritmética, conversión y comparación
- `(+, izq, der)`, `(-, izq, der)`, `(*, izq, der)`, `(/, izq, der)`: aritmética. Operandos `EntradaTabla|Integer|ReferenciaAtributo` (y conversiones ya emitidas como refs). Producen valor intermedio.
- `(todf, valor, --)`: conversión explícita a `doublef` (Tema 33). Produce valor `doublef`.
- Comparadores emitidos en condición: `(<, izq, der)`, `(>, izq, der)`, `(<=, izq, der)`, `(>=, izq, der)`, `(==, izq, der)`, `(!=, izq, der)` (según gramática/acciones). Producen resultado usado por `BF`.

### 3.4 Control de flujo
- `(BF, cond, destino)`: salto condicional si falso. `cond` = `Integer` ref a comparación. `destino` = `String` label (p.ej. `label_2`) o `null` diferido y luego completado en op2.
- `(BI, destino, --)`: salto incondicional. `destino` = `String` label (o completado en op1 en cierre de salto según caso).

### 3.5 I/O y retorno
- `(pout, operando, --)`: salida. `operando` puede ser `EntradaTabla` (cadena/var/cte) o ref/atributo según emisión.
- `(ret, operando, --)`: retorno de función/método.

### 3.6 Invocaciones (Tema 19 y métodos)
- `(PAR, formal, real)`: copia de parámetro real a formal, en **orden de evaluación** ya resuelto. `formal` = `EntradaTabla` del parámetro; `real` = valor emitido.
- `(CALL, funcion, cant)`: invocación a función. `funcion` = `EntradaTabla`; `cant` = `String` con cantidad de parámetros. Produce valor = nro de terceto (retorno), referenciable.
- `(CALLMET, objeto, metodo)`: invocación a método. `objeto` = `EntradaTabla` receptor; `metodo` = `EntradaTabla`/firma del método. Produce valor = nro de terceto.

## 4. Representación de entidades

- **Constantes**: entradas TS con prefijo de clave `$const:lexema`, `uso=CONSTANTE`, `tipoDato` `ulongint`/`doublef`, lexema visible original.
- **Cadenas**: `$cadena:contenido`, `uso=CADENA`, `tipoDato=cadena`.
- **Enums (Tema 23)**: tipo declarado `TIPO_ENUMERADO` con base (efectiva `ulongint`); variables de enum guardan tipo enumerado y operan por tipo efectivo/base según semántica.
- **Atributos (Temas 28,31)**: materializados por objeto en TS como `obj.atr:ambito` (propio) u `obj.clase.atr:ambito` (heredado). En IR aparecen como `ReferenciaAtributo` o, en algunos contextos, como entradas materializadas.
- **Temporales implícitos**: no hay `@auxN` explícito en TS; el resultado de cada terceto que produce valor se referencia por `[n]`. Para ASM se materializarán como auxiliares tipados según el tipo de resultado del terceto.

## 5. Tipos efectivos (referencia rápida)

- `Tipos.ULONGINT` = `ulongint` (32 bits sin signo, DD). `Tipos.DOUBLEF` = `doublef` (64 bits, DQ). `Tipos.CADENA`.
- `Entorno.tipoEfectivo(tipo)`: enum -> tipo base; clase -> clave clase; básicos -> normalizados. Relevante para operaciones y almacenamiento.

## 6. Notas para ASM

- Los nombres de etiqueta IR (`label_N`) son válidos; los nombres derivados de lexemas TS (`:`, `.`, `$`, signos) deben sanitizarse a identificadores MASM únicos.
- `BF/BI` completados: destino final es label string; detectar saltos por operador y operandos no nulos.
- Clases/atributos definen layout lógico; su materialización física en `.DATA`/memoria se define en prompts posteriores.
