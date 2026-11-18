### Estado base
- Rama/HEAD: tp4alt, commit b9250e0 — Plan 6 (al cierre de la auditoría Prompt 5; más los cambios de corrección descriptos en `ESTADO_RUNTIME_P5.md`). TP3 completado en rama SEMANTICAStp03 (temas 13,17,19,23,25,28,31,33 ejecutados).
- Pipeline actual (Compilador.java): AnalizadorLexico -> Parser+AccionesSemanticas -> Verificaciones/Entorno -> GeneradorTercetos -> salida stdout (fuente numerado, tokens, estructuras, errores, TS, tercetos). Genera archivo .asm esqueleto (encabezado, .DATA, .CODE mínimo) mediante GeneradorAssembler. Main toma [ruta] o input.txt.

### IR / Tercetos (Terceto.java, GeneradorTercetos.java)
- Terceto(op, op1, op2) 1-based en ListaTercetos. Operandos: EntradaTabla (se imprime lexema), Integer = ref [n], String = etiqueta/literal textual (p.ej. cant params en CALL), ReferenciaAtributo (objeto.atributo/objeto.clase.atributo), null -> --.
- Etiquetas: label_N como terceto (label_N,--,--); BF usa (BF, cond, label) o completa pos 2; BI usa (BI, label, --).
- Ops emitidos hoy: PROGRAMA/FIN_PROGRAMA, FUNCION/FIN_FUNCION, METODO/FIN_METODO, CLASE/FIN_CLASE, :=, COPY (asig estructural objeto), todf, + - * /, comparadores (en condicion), pout, ret, PAR(formal,real), CALL(func,cant), CALLMET(obj,metodo), BF, BI.
- Expresiones: ExpresionDiferida pliega izq, propaga tipo/validez; Conversion(todf); AsignacionEnExpresion; resultado invocacion = nro terceto CALL/CALLMET.

### Tabla de Simbolos / Tipos (TablaSimbolos.java, EntradaTabla.java, Tipos.java, Entorno.java)
- TS LinkedHashMap + acceso por puntero. Claves: ids nombre:ambito, ctes $const:lexema, cadenas $cadena:..., reservadas. EntradaTabla: lexema, tokenID, tipoDato, uso, ambito, declarado, puntero, numeroLinea, .... Usos: VARIABLE, PARAMETRO, FUNCION, METODO, CLASE, ATRIBUTO, TIPO, TIPO_ENUMERADO, CONSTANTE, CADENA, RESERVADA.
- Tipos base: ulongint (32b sin signo, 0..4294967295, sufijo $ul), doublef (64b), cadena. Tipos.tipoDeConstante, valorEntero(BigInteger), valorDoublef. Sin promociones implicitas; todf explicito (tema 33) implementado en semantica/codegen IR.
- Entorno (33k): resolucion de tipos/ambitos, enums (TipoEnumerado base+valores, tipoEfectivo), clases/funciones/metodos/params, atributos materializados por objeto (obj.atr:ambito, heredados obj.clase.atr:ambito), mangling. ReferenciaAtributo lleva tipo y texto para IR.

### Grupo 43 (TP4)
- Chequeos runtime asignados: b, e, f, h = overflow suma enteros, overflow producto doublef, resultado negativo en resta de enteros sin signo, asignacion invalida a variable de tipo enumeracion. (confirmado tabla ASSETS/TPs/Tp04-2026-1.pdf, grupo 43 -> befh)
- Temas particulares ya en lenguaje/IR a soportar en ASM: 13 repeat-while (BF/BI), 17 asignacion en expresion, 19 orden explicito params+PAR/CALL, 23 enums/typedef, 25 asignacion estructural objetos (COPY), 28 acceso atributos, 31 herencia multiple con prefijo/CALLMET, 33 todf.
- Enunciado TP4: salida .asm MASM32 32-bit (.386/.model flat,stdcall, masm32rt.inc), mecanismo variables auxiliares, enteros en registros EAX-DX, float con FPU 80x87, conversiones explicitas/implicitas traducidas, chequeos runtime con mensaje + terminar ejecucion, salida compilador = errores compilacion, representacion intermedia, TS y archivo ASM (sin tokens/estructuras salvo reentrega).

### Brechas para TP4
- No existe GeneradorAssembler ni escritura de archivo .asm.
- TS no exporta aun vista/metadata lista para .DATA (vars, ctes, cadenas, temporales @auxN, tipos ASM DD/DQ/DB, nombres ensamblables/unicos).
- Falta mapeo estable terceto->temporal/resultado y convencion de uso de auxiliares para EAX/FPU.
- Faltan rutinas runtime befh + datos de mensajes/constantes de rango (2^32, max doublef, etc.) y puntos de inyeccion en + ulongint, * doublef, - ulongint, := a enum.
- Faltan traducciones ASM de control/llamadas/atributos/COPY segun IR actual (algunas pueden requerir normalizacion de IR o lowering previo).
- Compilador/Main deben producir .asm y ajustar formato de salida a TP4 sin romper lo util.


## Plan incremental de prompts (ejecutar en orden)
Nota: cada prompt asume trabajar sobre estado actual y dejar cambios chicos, compilables, con pruebas focalizadas. Ajustar nombres/rutas reales encontrados en auditoria.

### Prompt 1 — Auditoría fina y adaptación de TS/Tercetos para ASM
Estado: ✅ Ejecutado (commit `944e3aa`). Ver `docs/TP4/PROMPT-1.md`, `docs/TP4/IR_CONTRATO.md`, `docs/TP4/DECISIONES_P1.md`.
Contexto: TP4 Grupo 43 (MASM32 32-bit, vars auxiliares). Revisa GeneradorTercetos.java, Terceto.java, TablaSimbolos.java, EntradaTabla.java, Entorno.java, Tipos.java, ReferenciaAtributo.java, LlamadaFuncion.java.

Tareas:
1) Audita y documenta (en comentario/docs breve) el contrato actual de IR: operadores, forma de operandos (EntradaTabla/Integer ref/String label/ReferenciaAtributo), significado de CALL/CALLMET/PAR/COPY/todf/BF/BI, y como se representan ctes, cadenas, enums, atributos y temporales implicitos.
2) Extiende TS/EntradaTabla para soportar generacion .DATA sin romper TP3:
   - clasificacion util para ASM de simbolos emitibles (var ulongint->DD, var/const doublef->DQ, cadena->DB/0, enum subyacente ulongint->DD, temporales @auxN con tipo, ctes literales/materializadas).
   - metodo(s) para recolectar/exportar: variables usuario por ambito, parametros/atributos materializados relevantes, constantes usadas, cadenas usadas, temporales requeridos.
   - asegurar nombres/labels ASM unicos y validos (sanitizar lexemas con ':' '.' etc., evitar colision con reservadas/instrucciones/MASM), manteniendo trazabilidad al simbolo original.
3) Adapta/expone lo necesario en ListaTercetos/GeneradorTercetos para que el backend ASM pueda:
   - iterar tercetos en orden con indice,
   - conocer tipo/resultado de expresiones y temporales creados por emitir (p.ej. registrar tipo de resultado de cada terceto que produce valor),
   - identificar etiquetas reales y destinos de BF/BI.
4) Si hace falta, agrega estructura auxiliar (p.ej. descriptor de temporal/resultado) sin cambiar semantica observable del IR.
5) Compila todo y agrega/ejecuta pruebas minimas que verifiquen que la salida de tercetos/TS de casos existentes no se rompe (o solo cambia en lo estrictamente necesario y justificado).

Entrega: diff, resumen de decisiones de representacion ASM-level (nombres, temporales, tipos .DATA), y riesgos abiertos para siguientes prompts.

### Prompt 2 — Esqueleto GeneradorAssembler y sección .DATA
Estado: ✅ Ejecutado. Ver `docs/TP4/DECISIONES_P2.md`. Archivos: `GeneradorAssembler.java` (nuevo), `Compilador.java` (modificado).
Contexto: Partiendo del Prompt 1 (TS/IR adaptados). Grupo 43, MASM32 32-bit, variables auxiliares.

Tareas:
1) Crea GeneradorAssembler.java (o paquete adecuado existente) que reciba (ListaTercetos/estructura de tercetos, TablaSimbolos, Entorno y metadata de tipos/temporales) y escriba un archivo .asm.
2) Emite esqueleto MASM32:
   - .386
   - .model flat, stdcall
   - option casemap:none (si aplica)
   - include \masm32\include\masm32rt.inc (o forma compatible decidida)
   - .data / .code / end start (estructura base)
3) Construye .DATA dinamicamente:
   - variables ulongint (usuario, params/atributos segun convencion definida) como DD ?
   - variables/constantes doublef y temporales float como DQ ?/valor
   - cadenas para pout/mensajes runtime como DB ...,0
   - temporales enteros @auxN como DD ?
   - constantes numericas/materializaciones necesarias (incl. limites si conviene) con nombres unicos.
   - no emitir duplicados; orden estable.
4) Genera seccion .CODE con `start:` y `invoke ExitProcess,0` minima y placeholders claros para traduccion real.
5) Integra en Compilador (y/o Main) una API para producir el .asm (ruta derivada del fuente o parametro) sin alterar aun el formato global salvo lo imprescindible; deja TODOs acotados.
6) Prueba con 1-2 casos simples (programa minimo, vars ulongint/doublef, cadena): debe compilar Java y generar .asm con encabezados y .DATA coherente.

Entrega: archivos nuevos/modificados, ejemplo de .asm generado, convencion de nombres ASM elegida.

### Prompt 3 — Traducción básica a .CODE: aritmética, asignación y todf
Estado: ✅ Ejecutado. Ver `docs/TP4/DECISIONES_P3.md`. Archivo modificado: `GeneradorAssembler.java`.
Contexto: Esqueleto ASM listo (Prompt 2). Implementa traduccion de operaciones basicas usando variables auxiliares.

Tareas realizadas:
1) Recorrido de tercetos PROGRAMA..FIN_PROGRAMA y emision .CODE para:
   - asignacion escalar `:=` (ulongint via EAX/mov; doublef via FPU fld/fstp),
   - aritmetica ulongint `+ - * /` con registros EAX y auxiliares DD,
   - aritmetica doublef `+ - * /` con FPU (fld, fadd/fsub/fmul/fdiv, fstp) y auxiliares DQ,
   - comparaciones no traducidas (pendiente Prompt 4).
2) Conversion explicita `todf` (Tema 33): fild entero + fstp doublef.
3) Convencion de resultado de terceto: @auxN tipado (DD/DQ) materializado en .DATA y referenciado desde [n].
4) Operandos soportados: constantes, variables (via nombreAsm de TS), refs a terceto (via @auxN), ReferenciaAtributo (texto). Cadenas y literal String soportados.
5) Prueba ASM-level: caso con ulongint, doublef, +, *, todf y asignaciones. .asm generado con instrucciones MASM32 correctas.

Entrega: mapeo operador->instrucciones en DECISIONES_P3.md, caso probado, limitaciones pendientes (control flujo, objetos/attrs, runtime checks).

### Prompt 4 — Control de flujo y temas particulares (13,19,25,28,31) en ASM
Contexto: Traduccion basica lista (Prompt 3). Completa saltos y construcciones del Grupo 43.

#### Sub-prompts atomizados (ejecutar en orden 4a -> 4b -> 4c -> 4d -> 4e)

---

### Prompt 4a — Control de flujo: etiquetas, BF, BI (Tema 13)
Estado: ✅ Ejecutado. Ver `docs/TP4/DECISIONES_P4a.md`, `docs/TP4/TEMPLATES_ASM.md`. Archivo modificado: `GeneradorAssembler.java`; script: `test_tp_4/probar_prompt4a.sh`.
Contexto: GeneradorAssembler emite .CODE basico. Falta traducir saltos condicionales/incondicionales.

Tareas:
1) Implementar `emitirEtiqueta(String label)` para tercetos `label_N` -> `label_N:` en .CODE.
2) Implementar `emitirBF(Object condicion, String etiquetaSalida)`:
   - condicion viene de terceto comparador (`<`, `>`, `<=`, `>=`, `==`, `!=`) que produce ulongint 0/1.
   - usar `cmp`/`test` + `jz`/`jnz` (BF salta si falso = 0) para enteros.
   - para doublef: `fcom`/`fcompp` + `fstsw`/`sahf` + `jcc` apropiado.
   - respetar tipo de la comparacion (ver `ResultadoTerceto.tipo`).
3) Implementar `emitirBI(String etiquetaDestino)` -> `jmp etiquetaDestino`.
4) En `emitirCodigo()`, despachar operadores `label_*`, `BF`, `BI` a sus emisores.
5) Pruebas: `if/else`, `repeat-while` anidados, condiciones con ulongint y doublef.

Entrega: mapeo comparador->instrucciones MASM en DECISIONES_P4a.md, casos .asm generados.

---

### Prompt 4b — Salida por consola: pout
Estado: ✅ Ejecutado. Ver `docs/TP4/DECISIONES_P4b.md`, `docs/TP4/TEMPLATES_ASM.md` (T-14..T-17). Archivo modificado: `GeneradorAssembler.java`; casos: `test_tp_4/Salida por consola/`; script: `test_tp_4/probar_prompt4b.sh`.
Contexto: Terceto `pout` ya se genera. Falta emision MASM32rt para ulongint/doublef/cadena.

Tareas:
1) En `.DATA`: agregar buffers/format strings necesarios (`fmt_ulongint db "%u",0`, `fmt_doublef db "%f",0`, `fmt_cadena db "%s",0`, buffer temporal si hace falta).
2) Implementar `emitirPout(Object operando)`:
   - `EntradaTabla`/`@auxN`/`ReferenciaAtributo` -> resolver tipo (ulongint/doublef/cadena).
   - ulongint: `invoke crt_printf, offset fmt_ulongint, valor` (valor en EAX o memoria).
   - doublef: `invoke crt_printf, offset fmt_doublef, valor` (valor en memoria DQ, FPU no necesario para printf).
   - cadena: `invoke crt_printf, offset fmt_cadena, offset nombreAsmCadena`.
   - usar `invoke StdOut`/`printf` compatible con masm32rt.inc.
3) En `emitirCodigo()`, despachar `pout`.
4) Pruebas: pout de variable, constante, expresion, cadena literal, resultado de funcion.

Entrega: convencion de formatos/buffers en DECISIONES_P4b.md, casos .asm.

---

### Prompt 4c — Funciones/metodos: PAR, CALL, CALLMET, ret, FUNCION/FIN_FUNCION, METODO/FIN_METODO (Tema 19)
Estado: ✅ Ejecutado. Ver `docs/TP4/DECISIONES_P4c.md`, `docs/TP4/TEMPLATES_ASM.md` (T-18..T-23). Archivo modificado: `GeneradorAssembler.java`; casos: `test_tp_4/Funciones y metodos/`; script: `test_tp_4/probar_prompt4c.sh`.
Contexto: Tercetos de invocacion ya existen. Falta convencion de llamada y traduccion.

Tareas:
1) Definir convencion de llamada interna (stack, stdcall-like, 32-bit):
   - `FUNCION nombre` / `METODO nombre`: prologue (`push ebp; mov ebp,esp; sub esp, locals`), label `fn_nombre:` / `mt_nombre:`.
   - Parametros en pila (caller limpia o callee limpia? documentar). `PAR` empuja valor real en pila en orden de evaluacion.
   - `CALL func, cant`: `call fn_func` + `add esp, cant*4` (si caller limpia). Resultado en EAX (ulongint) o ST(0)/memoria (doublef) -> mover a `@auxN`.
   - `CALLMET obj, metodo`: resolver direccion metodo (vtable o llamada directa segun herencia), `this` en ECX o pila. Documentar decision.
   - `ret`: `mov eax, valor` (ulongint) o `fld valor` + dejar en ST(0) (doublef) + `leave; ret n` (callee limpia) o solo `ret` (caller limpia). Elegir y documentar.
   - `FIN_FUNCION` / `FIN_METODO`: epilogue + label fin.
2) Implementar emisores en GeneradorAssembler.
3) Acceso a parametros dentro de funcion: `ebp+8`, `ebp+12`... o via nombre ASM del parametro en TS.
4) Variables locales: espacio en stack frame, no en .DATA.
5) Pruebas: funcion con params/retorno ulongint/doublef, llamada anidada, metodo con `this`, orden de evaluacion PAR.

Entrega: convencion completa en DECISIONES_P4c.md, diagrama stack frame, casos .asm.

---

### Prompt 4d — Objetos/atributos: COPY, ReferenciaAtributo, layout (Temas 25,28,31)
Estado: ✅ Ejecutado y verificado en 4e. Ver `docs/TP4/DECISIONES_P4d.md`, `docs/TP4/TEMPLATES_ASM.md` (T-24..T-27), `docs/TP4/DECISIONES_P4.md`. Archivo modificado: `GeneradorAssembler.java`; casos: `test_tp_4/Objetos y atributos/`; script: `test_tp_4/probar_prompt4d.sh`. Corrección al cerrar 4e: `COPY` pasó a ser posicional para clases compatibles con nombres de atributos distintos.
Contexto: Tercetos de invocacion ya existen. Falta convencion de llamada y traduccion.

Tareas (completadas):
1) Layout de objeto en memoria: atributos como simbolos planos en .DATA, bases primero (heredaDe) luego propios, DD para ulongint/enum y DQ para doublef.
2) TablaSimbolos ya emite atributos de objeto via `declararAtributoDeObjeto` (nombre calificado `objeto.atributo`); no se modifica la TS de TP3.
3) `COPY`: copia campo-a-campo entre objetos compatibles (un mov por ulongint, dos por doublef).
4) `ReferenciaAtributo`: se resuelve a nombre ASM del atributo en `operandoAsm()`; lectura y escritura emiten `mov`/`fld`/`fstp` sobre ese nombre.
5) `CLASE` / `FIN_CLASE`: no-op en .CODE (solo marcadores).
6) Pruebas: 4 casos manuales en `test_tp_4/Objetos y atributos/` + script `probar_prompt4d.sh`.

---

### Prompt 4e — Integracion y pruebas Prompt 4
Estado: ✅ Ejecutado. Ver `docs/TP4/DECISIONES_P4.md`, `docs/TP4/TEMPLATES_ASM.md` (T-28..T-29), `docs/TP4/PlanTP4.md`. Archivos modificados: `GeneradorAssembler.java`, `Entorno.java`; casos: `test_tp_4/Integracion/` (6 `.txt` + 6 `.asm` + 5 snapshots `consola_tp3/`); script: `test_tp_4/probar_prompt4e.sh`.
Contexto: Sub-prompts 4a-4d implementados. Unificar y probar.

Tareas (completadas):
1) `emitirCodigo()`: despacho explícito de todos los operadores del IR. `PROGRAMA`/`FIN_PROGRAMA` pasaron a no-op; el `default` queda solo para etiquetas `label_N` y operadores desconocidos (defensivo), sin comentarios de operadores ya implementados.
2) `.DATA` revisado: ya incluía formatos `pout`, `@auxN` tipados, `@fpu_status`, constantes/cadenas y atributos; no hicieron falta símbolos nuevos. Se regeneraron los `.asm` por la desaparición de los comentarios `PROGRAMA`/`FIN_PROGRAMA`.
3) Casos integrales dirigidos en `test_tp_4/Integracion/`:
   - `if/else` anidado con `ulongint` y `doublef` (`01_if_doublef_anidado`).
   - `repeat-while` anidado con `if` interno (`02_repeat_while_anidado`). El lenguaje no tiene `repeat-until` (ver `gramatica.y`).
   - Funcion recursiva (factorial) + llamada a metodo (`03_recursion_factorial_metodo`). Requirió fallback a funciones en curso en `Entorno.getFuncion()`.
   - Objetos con herencia multiple, COPY posicional y acceso prefijado (`04_objetos_multiherencia_copy`).
   - `pout` combinado con cadena, expresion, llamada y `todf` (`05_pout_expresiones_llamadas`).
   - Regresion 4d: COPY entre layouts compatibles con nombres distintos (`06_copy_layout_compat_distinto_nombre`).
4) Salida de consola de TP3 verificada: 5 snapshots comparados contra build limpio de `HEAD` y build actual → 5/5 `IGUAL`.
5) Limitaciones documentadas en `DECISIONES_P4.md`: sin vtable, layout plano/`this` simbolico, `@auxN` globales con recursion, MASM no ensamblado aqui, checks b/e/f/h para Prompt 5 y contrato final para Prompt 6.

Entrega: diff final Prompt 4, matriz de pruebas, `DECISIONES_P4.md` consolidado. `bash test_tp_4/probar_prompt4e.sh` → OK (incluye regresiones 4d, 4c, 4b, 4a, 3 y 2).

### Prompt 5 — Errores en tiempo de ejecución Grupo 43 (b, e, f, h)
Contexto: Backend ASM funcional (Prompt 4). Grupo 43 requiere chequeos runtime: b,e,f,h.

El Prompt 5 se ejecuta como **cuatro paquetes semánticos independientes** más una infraestructura común, cada uno compitable y verificable por separado, ejecutados en este orden:

---

#### Paquete 5.0 — Infraestructura común de runtime (base para b/e/f/h)
Estado: ✅ Ejecutado. Ver `docs/TP4/DECISIONES_5.0.md`. Archivo modificado: `GeneradorAssembler.java`.
Contexto: Proveer los mensajes de error y las rutinas de fallo que todos los chequeos reutilizan, sin instrumentar aún ningún punto concreto.
Tareas (completadas):
1) En `.DATA`: definir las cadenas de mensaje de error (una por chequeo: b, e, f, h) y sus `fmt_*` si fuera necesario.
2) En `.CODE`: definir labels de fallo (`error_overflow_suma`, `error_overflow_float`, `error_negativo_resta`, `error_enum_invalido`) que impriman el mensaje claro e invoquen terminación (`invoke ExitProcess, <codigo>` o equivalente).
3) Crear un emisor helper reutilizable `emitirGuardia(saltoCondicional, labelError)` que emita el salto condicional al label de error correspondiente.

---

#### Paquete 5.b — Overflow en sumas de enteros `ulongint`
Estado: ✅ Ejecutado. Ver `docs/TP4/DECISIONES_5.b.md`. Archivo modificado: `GeneradorAssembler.java`. Pruebas: la carpeta `test_tp_4/resta_enteros_negativos/` contiene los casos de suma (`01_suma_valida`, `02_suma_desborda`) pese a su nombre, junto con `test_tp_4/probar_prompt5_b.sh`. Ver `ESTADO_RUNTIME_P5.md`.
Contexto: En cada suma de enteros sin signo (`ADD`) de `ulongint`, detectar si el resultado excede el rango de 32 bits sin signo (0..4294967295). Si excede, saltar a error de overflow.
Tareas (completadas):
1) En `GeneradorAssembler.emitirAritmetica()` para operador `+` con tipo `ulongint`, tras `add eax, <op2>`, insertar comprobación de acarreo (`jc`) con salto a `error_overflow_suma`.
2) El acarreo CF=1 indica que el resultado no cabe en 32 bits sin signo.

---

#### Paquete 5.e — Overflow en productos de punto flotante `doublef`
Estado: ✅ Ejecutado. Ver `docs/TP4/DECISIONES_5.e.md` y `docs/TP4/DECISIONES_5_e.md` (duplicados, ver `ESTADO_RUNTIME_P5.md`). Archivo modificado: `GeneradorAssembler.java`. Pruebas: `test_tp_4/producto_doublef/` y `test_tp_4/producto_doublef_overflow/` (esta última recién agregada al script), con `test_tp_4/probar_prompt5_e.sh`.
Contexto: Tras cada producto FPU (`fmul`) de `doublef`, detectar overflow/infinito o resultado fuera del rango representable y reportar.
Tareas (completadas):
1) En `GeneradorAssembler.emitirAritmetica()` para operador `*` con tipo `doublef`, tras `fmul <op2>` e `fstp @auxN`, insertar inspección de estado FPU (`FXAM` + `FSTSW`/`SAHF`) con salto condicional a `error_overflow_float`.

---

#### Paquete 5.f — Resultados negativos en restas de enteros sin signo `ulongint`
Estado: ✅ Ejecutado. Ver `docs/TP4/DECISIONES_5.f.md`. Archivo modificado: `GeneradorAssembler.java`. Pruebas: `test_tp_4/resta_enteros_negativa/` y `test_tp_4/probar_prompt5_f.sh`.
Contexto: En cada resta de enteros sin signo (`SUB`) de `ulongint`, detectar si el resultado sería negativo (es decir, si el minuendo es menor que el sustraendo). Si es así, saltar a error.
Tareas (completadas):
1) En `GeneradorAssembler.emitirAritmetica()` para operador `-` con tipo `ulongint`, tras `sub eax, <op2>`, insertar comprobación de préstamo/salto por debajo (`jb`) con salto a `error_negativo_resta`.
2) Como `ulongint` es sin signo, el resultado nunca debe ser negativo; si `op1 < op2`, la resta produce un valor fuera de rango.

---

#### Paquete 5.h — Asignación inválida a variable de tipo enumeración
Estado: ✅ Ejecutado. Ver `docs/TP4/DECISIONES_5_h.md` y `docs/TP4/DECISIONES_P5h.md` (duplicados, ver `ESTADO_RUNTIME_P5.md`).
Contexto: Al asignar a una variable/enumerado destino, validar que el valor (constante o resultado de expresión) pertenezca al conjunto permitido del enum; si no, error runtime y terminar.
Tareas completadas:
1) En `GeneradorAssembler.emitirAsignacionSentencia()` se detecta cuando el destino es una variable de tipo enumerado y se emite el chequeo runtime: `mov eax, <valor>` seguido de `cmp eax, <valorN>` / `je enum_valido_<n>` para cada valor del enum, y `jmp error_enum_invalido` si ninguno coincide.
2) `calcularChequeosRuntime()` activa `usaMsgEnumInvalido` cuando hay alguna asignación `:=` cuyo destino es variable de tipo enum (usando `Entorno.getTipoEnumerado()`).
3) Se reutilizan `msg_enum_invalido` en `.DATA` y el label `error_enum_invalido:` en `.CODE` (infraestructura 5.0 ya existente).
4) Casos de prueba en `test_tp_4/enumerados/`: `01_asignacion_valida.txt` (valor en el enum) y `02_asignacion_invalida.txt` (valor fuera del enum vía variable ulongint), más script `probar_prompt5_h.sh` con regresiones 5.0/5.b/5.e/5.f.

---

#### Reglas transversales del Prompt 5

- **No romper casos válidos:** ningún chequeo puede disparar sobre programas que actualmente compilan y generan `.asm` correcto.
- **Inserta chequeos en todos los puntos relevantes del IR/expresiones generadas** (incluye temporales y resultados intermedios cuando aplique).
- Cada paquete deja su propio script o casos en `test_tp_4/` y no avanza al siguiente hasta que el anterior esté verde.

**Entrega consolidada del Prompt 5:** lista exacta de puntos instrumentados, forma de cada chequeo, ejemplos de mensajes/casos, y scripts de prueba por paquete (5.0, 5.b, 5.e, 5.f, 5.h).



### Prompt 6 — Integración final, contrato de salida TP4 y suite de pruebas
Contexto: Generador ASM + runtime checks completos (Prompt 5).

El Prompt 6 se ejecuta como **paquetes secuenciales**, cada uno compitable y verificable por separado, dejando la eliminación de comentarios para el último:

---

#### Paquete 6.0 — Contrato de salida TP4 y generación de `.asm`
Estado: ⏳ Pendiente.
Contexto: Backend ASM completo con checks runtime (Prompt 5). Falta ajustar `Compilador.java`/`Main.java` al formato de salida que exige la cátedra para TP4.
Tareas:
1) En `Compilador.java`/`Main.java`: ante compilación sin errores, generar archivo `.asm` (mismo nombre base que el fuente, extensión `.asm`, en el mismo directorio o salida definida) y mostrar ruta/éxito por stdout.
2) Salida por stdout **según cátedra para TP4**:
   - errores de compilación (léxico, sintáctico, semántico) juntos con descripción y línea;
   - representación intermedia (tercetos);
   - contenidos de Tabla de Simbolos.
   - **NO mostrar** tokens ni estructuras salvo que sea estrictamente necesario por compatibilidad interna (documentar decisión).
3) Ante errores que impidan ASM correcto: no generar `.asm` inválido; documentar comportamiento.
4) Prueba: compilar un caso válido y verificar que stdout contiene solo lo pedido y que el `.asm` aparece en la ruta esperada.

---

#### Paquete 6.1 — Validación MASM32: includes, directivas y ensamblable
Estado: ⏳ Pendiente.
Contexto: El `.asm` se genera pero no se ha validado con `ml.exe`/`ml64.exe` + `link` usando `masm32rt.inc` en Windows.
Tareas:
1) Revisar includes: `.386`, `.model flat, stdcall`, `option casemap:none`, `include \masm32\include\masm32rt.inc` — confirmar rutas y sintaxis exacta para el entorno de la cátedra.
2) Revisar directivas de sección (`.data`/`.code`), alineación de `DQ` (¿requiere `ALIGN 8`?), `end start` vs `end`, CRLF vs LF.
3) Verificar que `invoke crt_printf` con `DQ` (doublef) y `%f` funciona en el runtime MASM32 real (pasar qword por pila vs registros).
4) Ajustar `GeneradorAssembler.java` si hace falta (p.ej. `ALIGN 8` antes de `DQ`, formato de cadenas, terminadores).
5) Prueba: ensamblar y enlazar al menos 3 casos representativos (ulongint, doublef, objetos) en Windows con la toolchain indicada; registrar comandos y salida.

---

#### Paquete 6.2 — Batería de regresión TP3 y suite TP4/Grupo 43
Estado: ⏳ Pendiente.
Contexto: Contrato de salida y MASM validados. Ejecutar suite completa.
Tareas:
1) Regresión TP3: temas 13, 17, 19, 23, 25, 28, 31, 33 — fuentes de `test/tp2` y `test/TP3_SEMANTICAS` (si aplica) contra el contrato TP4.
2) Runtime checks (Prompt 5): al menos un caso que **dispare** cada error b, e, f, h y casos **válidos** límite que no disparen.
3) Programa integral representativo: combinar control de flujo, funciones/métodos recursivos, objetos con herencia múltiple + COPY + acceso prefijado, `pout` mixto.
4) Registrar en `docs/TP4/MATRIZ_PRUEBAS_6.2.md`: caso → comando → esperado → obtenido → OK/FALLA.
5) Script consolidado: `test_tp_4/probar_prompt6_2.sh` que ejecute todo y reporte resumen.

---

#### Paquete 6.3 — Limpieza final, docs de uso y resumen de estado
Estado: ⏳ Pendiente.
Contexto: Suite verde. Cierre de documentación y código.
Tareas:
1) **Remover `TODO` resueltos** del código (`GeneradorAssembler.java`, `Compilador.java`, `Entorno.java`, etc.). Solo borrar los que ya están implementados y no dejan acción pendiente.
2) **Actualizar y desarrollar comentarios** (headers de archivo, Javadoc de clases/métodos públicos, y comentarios internos no triviales) para que expliquen en **mayor profundidad** el comportamiento:
   - Qué hace la clase/método, sus precondiciones y efectos colaterales.
   - Convenciones clave (p.ej. caller limpia pila, `this` en `[ebp+8]`, `@auxN` globales, layout plano de objetos, copia posicional, fallback recursivo en `getFuncion`).
   - Decisiones no obvias: por qué se elige X sobre Y (cdecl-like vs stdcall, sin vtable, símbolos planos vs STRUCT, FPU vs SSE, etc.).
   - Puntos de extensión/risk conocidos (alineación `DQ`, recursión profunda, checks runtime b/e/f/h).
   - Trazabilidad: relación IR → ASM (p.ej. `; (PAR, a:suma, x:main)` que el generador emite intencionadamente).
3) Docs cortas de uso en `README.md` o `docs/USO.md`: `javac ...`, `java Main archivo.txt`, cómo ensamblar/ejecutar el `.asm` generado (comandos `ml`/`link` o `masm32rt`).
4) Resumen final en `docs/TP4/DECISIONES_P6.md` / `docs/TP4/ESTADO_FINAL.md`: qué cumple (temas, runtime), limitaciones conocidas, pasos manuales de ensamblado/ejecución, desviaciones si las hay.

---

#### Paquete 6.4 — Eliminación de comentarios en el proyecto (último) PEDIR PERMISO  CON "**BORRAR COMENTARIOS**"
Estado: ⏳ Pendiente.
Contexto: Todo funcional, documentado y probado. Limpieza estética final.
Tareas:
1) Eliminar comentarios de depuración, `// TODO` ya resueltos, bloques de código comentado, trazas verbosas que no aportan.
2) Mantener solo: headers de archivo/clase, Javadoc público útil, comentarios de decisión no obvia (por qué se elige X sobre Y), y comentarios de traceabilidad ASM (p.ej. `; (PAR, a:suma, x:main)` que el generador emite intencionadamente).
3) Verificar que `git diff` muestra solo eliminación de ruido, sin cambio funcional.
4) Ejecutar `bash test_tp_4/probar_prompt6_2.sh` una vez más para confirmar que nada se rompió.

---

**Entrega consolidada del Prompt 6:** diff final por paquete, `MATRIZ_PRUEBAS_6.2.md`, `ESTADO_FINAL.md`, instrucciones de ejecución y ensamblado, y confirmación de que la eliminación de comentarios (6.4) no introdujo regresiones.

Nota de auditoría: se eliminó un bloque duplicado del Prompt 6 (texto original agregado por el commit `b9250e0` junto a la versión atomizada 6.0–6.4). La versión vigente es la de los paquetes 6.0 a 6.4; 6.4 (eliminación de comentarios) queda como último paquete y requiere confirmación explícita.