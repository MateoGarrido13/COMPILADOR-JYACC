# Prompt 1 (TP4) — Auditoría fina y adaptación de TS/Tercetos para ASM

## Objetivo
Preparar IR y TS para backend MASM32 32-bit (Grupo 43, variables auxiliares) sin romper semántica/salida observable de TP3.

## Entregables
- Código: `EntradaTabla.java`, `TablaSimbolos.java`, `Terceto.java`, `GeneradorTercetos.java`, `ResultadoTerceto.java` (nuevo).
- Documentación: `docs/TP4/IR_CONTRATO.md`, `docs/TP4/DECISIONES_P1.md`, `docs/TP4/PROMPT-1.md`, `docs/TP4/PlanTP4.md` (preexistente).

## 1) Auditoría y documentación del contrato IR
Relevado y documentado el contrato actual en `docs/TP4/IR_CONTRATO.md`:
- Estructura: `Terceto(op,op1,op2)` en `ListaTercetos` **1-based**; `crear` devuelve índice; refs de resultado como `Integer` `[n]`; `visualizar/contenido` sin cambios.
- Operandos: `EntradaTabla` (lexema TS), `Integer` (ref terceto), `String` (label/nombre/cantidad literal), `ReferenciaAtributo` (`obj.atr`/`obj.clase.atr`), `null`.
- Operadores/convenciones: `PROGRAMA/FIN_PROGRAMA`, `FUNCION/FIN_FUNCION`, `METODO/FIN_METODO`, `CLASE/FIN_CLASE`, etiquetas `label_N`, `:=`, `COPY`, `+ - * /`, comparadores, `todf`, `BF/BI`, `pout`, `ret`, `PAR`, `CALL`, `CALLMET`.
- Representación: ctes `$const:*`, cadenas `$cadena:*`, enums (tipo base efectivo), atributos materializados por objeto, temporales implícitos `[n]`.

## 2) Extensión TS/EntradaTabla para `.DATA`
### `EntradaTabla`
- Nuevo campo privado `nombreAsm` + `getNombreAsm()` / `setNombreAsm(String)` (acceso paquete para TS). No altera `lexema`, clave, `puntero`, `contenido()` ni semántica TP3.

### `TablaSimbolos`
- Nuevo descriptor `public static class SimboloDataAsm { entrada, categoria, tipoDato, tipoAsm, nombreAsm, valorInicial, origen }` para exportación `.DATA`.
- Cache/registro de nombres ASM: `nombresAsmUsados` (case-insensitive), `nombreAsmPorClave`.
- Reservadas/instrucciones/directivas/registros MASM32 considerados para evitar colisión (`RESERVADAS_ASM`).
- API:
  - `Collection<EntradaTabla> entradas()`: vista de entradas.
  - `String nombreAsmDe(EntradaTabla e)`: nombre MASM único y estable por clave/entrada; sanitiza `: . $ + - { } ' " espacio` y otros; normaliza prefijo si inicia en dígito o choca con reservada; desambigua con `_N`; trunca bases muy largas preservando hash; mantiene trazabilidad vía `origen` en descriptor.
  - `boolean esConstante/esCadena/esSimboloDatoUsuario(EntradaTabla e)`.
  - `List<SimboloDataAsm> colectarSimbolosData(Entorno entorno)`: recolecta en orden estable de TS constantes, cadenas y datos usuario declarados (`VARIABLE, PARAMETRO, ATRIBUTO`) cuando el tipo efectivo mapea a dato escalar ASM (`ulongint/enum efectivo -> DD`, `doublef -> DQ`, `cadena -> DB`).
  - `void limpiarCacheNombresAsm()`.
- `eliminar(EntradaTabla)`: libera nombre ASM asociado.
- Criterios de mapeo:
  - `categoria`: `constante`, `cadena`, `dato`.
  - `tipoAsm`: `DD` (ulongint/enum base), `DQ` (doublef), `DB` (cadena).
  - `valorInicial`: constante numérica -> literal/valor normalizado (`?` si no parseable), dato usuario -> `?`, cadena -> `null` en descriptor (contenido/escape lo define backend).
  - Objetos/clases/tipos no escalares: no se emiten como fila `.DATA` escalar en Prompt 1 (layout diferido); entradas materializadas de atributos escalares sí se incluyen.

## 3) Exposición/adaptación de tercetos para backend ASM
### `Terceto`
- Getters: `getOperador()`, `getOp1()`, `getOp2()`.
- Utilidades: `esEtiqueta()`, `esSaltoCondicional()`, `esSaltoIncondicional()`, `destinoSalto()` (BF usa op2, BI usa op1 cuando es `String`).

### `ListaTercetos`
- `Terceto obtener(int numero)` (1-based), `List<Terceto> elementos()` (vista inmodificable), `Map<String,Integer> indicesEtiquetas()`.
- Se preserva API/comportamiento existente (`crear`, `cantidad`, `completar`, `descartarDesde`, `reservarEtiqueta`, `crearEtiqueta`, `contenido`).

### `GeneradorTercetos`
- Nuevo `ResultadoTerceto` (`ResultadoTerceto.java`): `indice, operador, tipo, produceValor`, `nombreAuxiliar()` -> `@auxN`, `tipoAsmDato()` (`DD|DQ|null`).
- Registro de metadata por terceto creado: `Map<Integer,ResultadoTerceto> resultados` (limpiado en cada `generar`).
- API backend: `getLista()`, `getTercetos()`, `getResultados()`, `getResultado(int)`.
- Inferencia de tipo de resultado:
  - `todf` -> `DOUBLEF`, produce valor.
  - Aritmética `+ - * /` -> tipo de operando izq o der (efectivo/propagado).
  - Comparadores en condición -> `ULONGINT` (flag), produce valor para `BF`.
  - Asignación en expresión `(:=)` -> tipo del destino, produce valor.
  - `CALL/CALLMET` -> tipo de retorno de la llamada, produce valor.
  - `PROGRAMA/FIN_*`, `CLASE/FUNCION/METODO` delimitadores, labels, `COPY`, `:=` sentencia, `BF/BI`, `pout`, `ret`, `PAR` -> no productores.
- Todos los `lista.crear(...)` de emisión pasan por helpers internos para registrar metadata sin cambiar IR textual.

## 4) Temporales implícitos
- No se crean entradas `@auxN` en TS (el IR sigue usando `[n]`).
- Cada resultado con `produceValor=true` es candidato a auxiliar tipado `@auxN` materializable en `.DATA`/slots según convención final. La selección de "requeridos" (usados) puede hacerse en backend a partir de referencias `Integer` en operandos.

## 5) Verificación
- Compilación: `javac -encoding UTF-8 -d /tmp/tp4p1 *.java "acciones semanticas"/*.java otras_acciones/*.java` (OK).
- Smoke tests (sin regresión visible en tercetos/TS reportada):
  - `test/tp2/generales/00_programa_valido.txt`
  - `test/TP3_y_TP4/ITERACION/01_repeat_simple.txt` (tercetos esperados de repeat/BF/BI)
  - `test/tp2/verificacion_r17_asig_expresiones/estructura_respetada.txt`
  - `test/tp2/verificacion_r33_conversion/todf_a_entero.txt`
  - `test/tp2/verificacion_r25_objetos/misma_estructura.txt` (COPY)
  - `test/tp2/verificacion_r28_r31_atributos/metodo_heredado_prefijado_con_orden.txt` (PAR/CALLMET/refs)
- No se modificó `contenido()` de tercetos ni `contenido()` de TS.

## 6) Limitaciones / riesgos abiertos
- Tipo de comparación fijado a `ULONGINT` como flag lógico: suficiente para `BF`, confirmar si backend requiere otra convención.
- `tipoDato` de destino en asignación de expresión se toma de entrada/atributo; si hay casos con tipo no normalizado puede requerir `Entorno.tipoEfectivo`.
- Símbolos de clase/objeto (no escalares) no reciben `tipoAsm` en colecta `.DATA`; su layout/memoria se define en Prompt 4.
- Nombres ASM: conjunto de reservadas es preventivo, no exhaustivo para todas las variantes MASM32; validar con ensamblador real.
- `ListaTercetos` sigue package-private (archivo de `Terceto`); backend accede vía `GeneradorTercetos.getLista()/getTercetos()/indicesEtiquetas()`.
