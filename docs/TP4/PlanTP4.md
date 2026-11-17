### Estado base
- Rama/HEAD: tp4, commit db80077 — P2: produccion de los '.asm' (TP4 Prompt 2). TP3 completado en rama SEMANTICAStp03 (temas 13,17,19,23,25,28,31,33 ejecutados).
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
Contexto: Esqueleto ASM listo (Prompt 2). Implementa traduccion de operaciones basicas usando variables auxiliares.

Tareas:
1) Implementa recorrido de tercetos PROGRAMA..FIN_PROGRAMA y emision .CODE para:
   - asignacion escalar `:=` (ulongint via EAX/mov; doublef via FPU fld/fstp),
   - aritmetica ulongint `+ - * /` con auxiliares/registros EAX,EBX,ECX,EDX segun convenga (sin chequeos runtime aun, pero deja puntos de extension),
   - aritmetica doublef `+ - * /` con FPU (FLD, FADD/FSUB/FMUL/FDIV, FSTP) usando temporales/vars QWORD,
   - comparaciones necesarias para BF (deja interfaz reutilizable; si completo, documenta).
2) Implementa conversion explicita `todf` (Tema 33): cargar entero (ulongint/enum base ulongint) y convertir con FILD/FSTP a doublef.
3) Define y aplica convencion de "resultado de terceto": donde se almacena (temporal @auxN tipado) y como se referencia desde otros tercetos/operandos.
4) Soporta operandos: constantes, variables, refs a terceto, y (si ya viable) ReferenciaAtributo simple en expresiones/asignaciones escalares; si no, documenta limitacion y stub controlado.
5) Agrega pruebas ASM-level (generacion): expresiones ulongint, doublef, mixtas con `todf`, asignaciones encadenadas de Tema 17 segun IR actual. Verifica que el Java compila y el .asm emitido es estructuralmente correcto.

Entrega: mapeo operador->instrucciones, casos probados, limitaciones pendientes (control flujo, objetos/attrs, runtime checks).

### Prompt 4 — Control de flujo y temas particulares (13,19,25,28,31) en ASM
Contexto: Traduccion basica lista (Prompt 3). Completa saltos y construcciones del Grupo 43.

Tareas:
1) Control de flujo (Tema 13): traduce etiquetas, `BF` y `BI` a MASM (`cmp/test/jcc/jmp` segun tipo de condicion/comparador). Soporta `IF/ELSE/END_IF` y `REPEAT-WHILE` tal como los emite GeneradorTercetos hoy.
2) `pout`: emite salida por consola para ulongint/doublef/cadena usando rutinas MASM32rt apropiadas (p.ej. printf/StdOut) con formatos y buffers/temporales necesarios en .DATA.
3) Funciones/metodos e invocaciones (Tema 19 + convencion actual):
   - traduce `PAR`, `CALL`, `CALLMET`, `ret`, `FUNCION/FIN_FUNCION`, `METODO/FIN_METODO` segun el modelo elegido (stack/stdcall-like interno o convencion propia documentada) respetando orden de evaluacion ya codificado en IR.
   - resuelve acceso a parametros/retorno.
   - si hay limitacion de recursion/profundidad fuera de alcance, declarala (runtime recursion es chequeo i, no asignado al grupo 43).
4) Objetos/atributos (Temas 25,28,31):
   - `COPY` para asignacion estructural entre objetos compatibles (copia de campos/slots segun layout definido).
   - lectura/escritura de `ReferenciaAtributo` (`obj.atr`, `obj.clase.atr`) en asignaciones/expresiones donde aplique.
   - define layout de objeto/atributos en .DATA o convencion de direccionamiento y documentala.
5) Ignora/no-op controlado para marcadores estructurales no ejecutables si corresponde (`CLASE/FIN_CLASE`) siempre que no pierdas semantica necesaria.
6) Pruebas dirigidas: if/else, repeat while, invocacion con orden, atributo propio/heredado, asignacion de objetos.

Entrega: convencion de llamada y layout objetos, ejemplos .asm, casos cubiertos/no cubiertos.

### Prompt 5 — Errores en tiempo de ejecución Grupo 43 (b, e, f, h)
Contexto: Backend ASM funcional (Prompt 4). Grupo 43 requiere chequeos runtime: b,e,f,h.

Tareas:
1) Infraestructura comun: en .DATA define mensajes de error y en .CODE rutinas/labels de fallo que impriman mensaje claro e invoquen terminacion (`invoke ExitProcess, codigo` o equivalente). Reutilizable por todos los chequeos.
2) `b` Division por cero en `doublef`:
   - antes de `FDIV/FDIVP/FDIVR` relevante, comprobar divisor == 0.0 (FTST/FXAM o cargar y comparar via entero/FPU+SAHF segun estrategia) y saltar a error si cero.
3) `e` Overflow en producto de enteros `ulongint`:
   - en multiplicacion 32-bit sin signo (`MUL`), detectar overflow (CF/EDX segun forma) cuando resultado excede 32 bits / rango ulongint y reportar.
4) `f` Overflow en producto de punto flotante `doublef`:
   - tras producto FPU, detectar overflow/infinito/resultado fuera de rango representable (examinar estado FPU, `FXAM/FSTSW`, o comparar magnitud con max doublef) y reportar.
5) `h` Asignacion invalida a variables de tipo enumeracion (Tema 23):
   - al asignar a variable/enumerado destino, validar que el valor (constante o resultado de expresion) pertenezca al conjunto permitido del enum; si no, error runtime y terminar.
   - usa metadata de Entorno/TS (valores del enum, tipo base ulongint) para generar comparaciones/rangos/tabla de valores segun implementacion.
6) Inserta chequeos en todos los puntos relevantes del IR/expresiones generadas (incluye temporales/resultados intermedios cuando aplique). No rompas casos validos.
7) Pruebas: un caso que dispare cada error b,e,f,h y casos limite validos que no disparen. Compila Java y verifica que el .asm contiene las guardas.

Entrega: lista exacta de puntos instrumentados, forma de cada chequeo, ejemplos de mensajes/casos.

### Prompt 6 — Integración final, contrato de salida TP4 y suite de pruebas
Contexto: Generador ASM + runtime checks completos (Prompt 5).

Tareas:
1) Ajusta Compilador.java/Main.java al contrato TP4:
   - ante compilacion sin errores, generar archivo .asm (nombre/ruta clara, p.ej. junto al fuente o salida definida) ademas de mostrar ruta/exito.
   - salida por stdout segun catedra para esta etapa: errores de compilacion (lex/sint/sem) juntos con descripcion y linea; representacion intermedia (tercetos); contenidos de Tabla de Simbolos. Evita mostrar tokens/estructuras salvo que sea necesario por compatibilidad interna (documenta decision).
   - ante errores que impidan ASM correcto, no generar .asm invalido o generar solo si esta explicitamente permitido; documenta comportamiento.
2) Revisa includes/rutinas MASM32 usadas para que el .asm sea ensamblable y ejecutable con la herramienta indicada (masm32rt). Ajusta detalles de formato/directivas si hace falta.
3) Ejecuta bateria de regresion TP3 relevante y casos TP4/Grupo 43:
   - temas 13,17,19,23,25,28,31,33
   - runtime b,e,f,h (al menos un disparo por chequeo + validos)
   - programa integral representativo.
   Registra comandos, resultados y cualquier desviacion.
4) Limpieza final: remover TODOs resueltos, comentarios coherentes, docs cortas de uso (`javac ...`, `java Main ...`, como ensamblar/ejecutar .asm) si corresponde en README o doc existente.
5) Resume estado final: que cumple, limitaciones conocidas, y pasos manuales de ensamblado/ejecucion.

Entrega: diff final, instrucciones de ejecucion, matriz breve de pruebas (caso -> esperado -> obtenido), y ubicacion del .asm generado.