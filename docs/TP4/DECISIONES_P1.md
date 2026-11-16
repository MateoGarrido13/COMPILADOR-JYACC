# Decisiones y cambios — Prompt 1 (TP4, ASM MASM32 32-bit)

## Resumen
Se adaptó TS/IR para soportar generación `.DATA` y recorrido/metadata de tercetos sin cambiar la salida observable de TP3 (`contenido()` de tercetos y TS). Se agregó `ResultadoTerceto` y utilidades de nombres ASM, clasificación de símbolos y consulta de saltos/etiquetas.

## Cambios por archivo

### `ResultadoTerceto.java` (nuevo)
- Clase pública de metadata por terceto: índice, operador, tipo lógico, `produceValor`.
- `nombreAuxiliar()` = `@aux{indice}`.
- `tipoAsmDato()`: `DQ` si `doublef`, `DD` si `ulongint`, `null` en otros casos.
- Motivo: desacoplar inferencia de tipo/temporal del backend ASM.

### `Terceto.java`
- Imports: `Collections`, `LinkedHashMap`, `Map`.
- `Terceto`: getters `getOperador/getOp1/getOp2`; `esEtiqueta()` (`label_*` sin operandos), `esSaltoCondicional()` (`BF`), `esSaltoIncondicional()` (`BI`), `destinoSalto()` (`BF->op2`, `BI->op1` si `String`).
- `ListaTercetos`: `obtener(int)` 1-based, `elementos()` inmodificable, `indicesEtiquetas()` (`label -> índice`).
- Sin cambios en formato de `visualizar()`/`contenido()`.

### `GeneradorTercetos.java`
- Imports y estado: `resultados: LinkedHashMap<Integer,ResultadoTerceto>`.
- API nueva: `getLista()`, `getTercetos()`, `getResultados()`, `getResultado(int)`.
- Helpers internos: `crearTerceto(...,tipo,produceValor)`, `crearNoProductor(...)`, `crearEtiquetaConResultado(...)`, `tipoDeOperando(Object)`, `tipoAritmetico(izq,der)`.
- `generar(...)`: limpia `resultados` y registra metadata en cada terceto creado.
- Tipado registrado:
  - `+,-,*,/`: `tipoAritmetico(izq,der)`, produce valor.
  - `todf`: `doublef`, produce valor.
  - comparadores: `ulongint`, produce valor.
  - `:=` en `AsignacionEnExpresion`: tipo de destino (`EntradaTabla.tipoDato` o `ReferenciaAtributo.getTipo()`), produce valor.
  - `CALL/CALLMET`: `LlamadaFuncion.getTipoRetorno()`, produce valor.
  - resto estructural/control/IO/PAR/COPY/asignación sentencia/labels: no produce valor.
- `FIN_PROGRAMA` y demás emisiones convertidas a helpers (mismo IR).

### `EntradaTabla.java`
- Campo `private String nombreAsm` + getter/setter (paquete). Sin impacto en clave/lexema/puntero/impresión.

### `TablaSimbolos.java`
- Imports/utilidades: `Arrays`, `Collection`, `HashSet`, `Locale`, `Set`.
- `SimboloDataAsm` (descriptor `.DATA`): entrada, categoría, tipoDato efectivo, tipoAsm, nombreAsm, valorInicial, origen.
- Estado para ASM: `nombresAsmUsados`, `nombreAsmPorClave`, `RESERVADAS_ASM`.
- API: `entradas()`, `nombreAsmDe(EntradaTabla)`, `esConstante/esCadena/esSimboloDatoUsuario`, `colectarSimbolosData(Entorno)`, `limpiarCacheNombresAsm()`.
- Sanitización/nombre único: base por categoría/uso, remueve prefijos internos `$const:`/`$cadena:`, reemplaza separadores/caracteres no ASM por `_`, colapsa `_`, quita bordes, maneja vacío/dígito inicial/reservada, sufijo incremental `_N`, límite de longitud con hash.
- Colecta `.DATA`: constantes (`DD/DQ`, valor inicial desde lexema/`Tipos.valorEntero`), cadenas (`DB`, valor inicial diferido al backend), datos usuario escalares declarados (`DD/DQ`, `?`). Objetos/clases no escalares excluidos de mapeo escalar (layout pendiente).
- `eliminar`: libera cache de nombre ASM.

## Decisiones de representación ASM-level

| Elemento | Decisión Prompt 1 | Nota |
|---|---|---|
| `ulongint` / enum efectivo | `DD` | 32-bit |
| `doublef` | `DQ` | 64-bit IEEE |
| `cadena` | `DB` + terminador 0 (a emitir por backend) | descriptor marca `DB` |
| Constante numérica | símbolo `.DATA` con valor inicial | nombre único derivado de lexema |
| Variable/parámetro/atributo escalar | símbolo `.DATA` `?` | materializados TS existentes |
| Temporal `[n]` productor | `@auxN` sugerido, tipo por `ResultadoTerceto` | no se inserta en TS |
| Nombres ASM | sanitizados, únicos, case-insensitive, trazables | colisión resuelta con sufijo |
| Etiquetas IR `label_N` | ya válidas, expuestas por índice/utilidades | BF op2 / BI op1 |
| Objetos/atributos compuestos | layout diferido (Prompt 4) | no forzar emisión escalar incorrecta |

## Verificación realizada
- Compilación Java OK: `javac -encoding UTF-8 -d /tmp/tp4p1 *.java "acciones semanticas"/*.java otras_acciones/*.java`.
- Ejecuciones representativas (`java -cp /tmp/tp4p1 Main ...`), IR/TS coherentes y sin errores inesperados observados:
  - `test/tp2/generales/00_programa_valido.txt`
  - `test/TP3_y_TP4/ITERACION/01_repeat_simple.txt`
  - `test/tp2/verificacion_r17_asig_expresiones/estructura_respetada.txt`
  - `test/tp2/verificacion_r33_conversion/todf_a_entero.txt`
  - `test/tp2/verificacion_r25_objetos/misma_estructura.txt`
  - `test/tp2/verificacion_r28_r31_atributos/metodo_heredado_prefijado_con_orden.txt`

## Riesgos y puntos abiertos para prompts siguientes
1. **Tipo de comparadores**: marcado `ULONGINT`; backend debe definir materialización booleana/flags real.
2. **Temporales requeridos**: se exponen todos los productores; backend puede filtrar por uso real de `[n]`.
3. **Objetos/COPY/atributos**: faltan layout, tamaño, offsets y direccionamiento ASM.
4. **Convención de llamada**: `PAR/CALL/CALLMET/ret` y scopes de función/método requieren lowering a pila/registros.
5. **Nombres reservados MASM**: lista preventiva; validar contra `masm32rt`/ml real.
6. **Constantes doublef**: `valorInicial` conserva lexema fuente (puede incluir forma `d`/sufijo según caso); backend debe normalizar a formato MASM aceptado.
7. **Cadenas**: escape/comillas/terminador 0 y posibles duplicados semánticos a cargo del emisor `.DATA`.
8. **Visibilidad Java**: `ListaTercetos` no es clase pública top-level; usar APIs expuestas por `GeneradorTercetos`/`Terceto`.
