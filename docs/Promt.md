## Prompts listos para usar

Estado actual:

- Prompt 0: ejecutado. Resultado en `docs/PROMPT-0.md`.
- Prompt 1A: ejecutado. Resultado en `docs/PROMPT-1A.md`.
- Prompt 1B: ejecutado. Resultado en `docs/PROMPT-1B.md`.
- Siguiente: Prompt 2 (motor de tipos).

### Prompt 0 — Baseline (ejecutado)
Estás trabajando en /home/rafael/Documents/Compiladores/COMPILADOR/TP3.
No modifiques código de producción en esta tarea. Solo relevá el estado actual.

1. Compilá con:
   javac -encoding UTF-8 -d /tmp/opencode/tp3build *.java "acciones semanticas"/*.java otras_acciones/*.java
2. Ejecutá el compilador sobre todos los archivos de:
   - test/TP3 _y_TP4/AMBITO
   - test/TP3 _y_TP4/ASIGNACIONES
   - test/TP3 _y_TP4/ITERACION
   - test/TP3 _y_TP4/SELECCION
   - test/tp2/verificacion_r17_asig_expresiones
   - test/tp2/verificacion_r19_param_reales
   - test/tp2/verificacion_r23_enums
   - test/tp2/verificacion_r28_r31_atributos
   - test/tp2/verificacion_r33_conversion
3. Guardá cada salida en /tmp/opencode/tp3-baseline/ conservando el nombre del caso.
4. Armá una tabla con: caso, errores actuales, tercetos actuales y si el caso debería ser válido o inválido.
5. No avances a otra fase hasta dejar este baseline reproducible.

### Prompt 1A — Uso, tabla de símbolos y resolución por cadena (ejecutado)
Implementá únicamente la base de Name Mangling en la tabla de símbolos.
No implementes todavía chequeos de tipos ni generación de código.

Archivos: EntradaTabla.java, TablaSimbolos.java, Globals.java, Verificaciones.java.

1. En EntradaTabla agregá:
   - String uso;        // variable, parametro, funcion, metodo, clase, atributo, tipo_enumerado, constante, cadena, programa, referencia
   - boolean declarado; // false para el placeholder que crea el léxico al ver un identificador
   Podés usar un enum Uso en un archivo nuevo Uso.java, o constantes String. Elegí una sola forma y usala en todo el proyecto.

2. En Globals, si se confirma la decisión D1, cambiá AMBITO_ACTUAL a "main".

3. En TablaSimbolos agregá:
   - EntradaTabla buscarExacto(String nombre, String ambito)
       -> tabla.get(nombre + ":" + ambito)
   - EntradaTabla resolver(String nombre, String ambito)
       -> probar nombre:ambito; si no está, quitar el último segmento de ambito y repetir.
          Ejemplo desde main:f:g: x:main:f:g, x:main:f, x:main.
   - boolean estaDeclarado(String nombre, String ambito)
       -> buscarExacto(...) != null && entrada.declarado
   - void declarar(EntradaTabla entrada, String nombre, String ambito, String uso, String tipo)
       -> si ya hay una entrada declarada en ese ámbito, es redeclaración: informar error.
          Si hay un placeholder no declarado, reutilizarlo: renombrarlo a nombre:ambito,
          marcarlo declarado, asignarle uso y tipo.
          Si no hay entrada, crearla con la clave nombre:ambito.
   - Eliminá buscarIdentificador(nombre, ambito) o dejalo como alias de buscarExacto.

4. El léxico sigue creando placeholders con buscarOInsertarIdentificador(texto).
   No cambies ese comportamiento todavía.

5. En Verificaciones agregá:
   - EntradaTabla resolverIdentificador(EntradaTabla id, String usoEsperado)
       * resolver con TablaSimbolos.resolver(nombre, Globals.AMBITO_ACTUAL)
       * si no existe: error "identificador 'x' no declarado o no visible"
       * si existe pero entrada.uso no es compatible con usoEsperado:
         error "uso incorrecto de 'x'"
       * devolver la entrada declarada, no el placeholder.
   - Sobrecargas para variable/parámetro/atributo, función/método, clase y tipo.

6. No borres entradas al cerrar ámbitos. Dejá TablaSimbolos.eliminar sin uso.

Criterio de aceptación:
- Un input con main -> f -> g y una variable x declarada en g debe resolver x:main:f:g.
- Desde main no debe resolver x:main:f:g.
- Dos funciones hermanas con variables del mismo nombre no deben colisionar.
- Una redeclaración en el mismo ámbito debe informar error.
- Un uso de un nombre no declarado en ningún ámbito debe informar error.

Nota de ejecución (ver `docs/PROMPT-1A.md`): se usó el enum anidado
`EntradaTabla.Uso`, `TablaSimbolos.declarar` devuelve la entrada reutilizada y
`buscarIdentificador` quedó como búsqueda exacta. La detección de redeclaración
todavía no emite error; se completará junto con los chequeos de tipos.

### Prompt 1B — ControlAmbito, Entorno y acciones para Name Mangling (ejecutado)
Continuá la Fase 1. Asumí que ya existe Uso, declarado y resolver().

Archivos: ControlAmbito.java, MonitorAmbito.java, Entorno.java,
Verificaciones.java, otras_acciones/AccionesExpresiones.java,
otras_acciones/AccionesDeclaraciones.java, docs/DEC-1.md,
docs/DEC-2.md, docs/DEC-3.md, docs/DEC-4.md, docs/DEC-5.md.

1. Eliminá el uso de MonitorAmbito. Podés borrar MonitorAmbito.java o dejarlo sin llamadas.
   No debe renombrar declaraciones externas ni borrar ámbitos.

2. Reescribí ControlAmbito manteniendo solo Globals.AMBITO_ACTUAL:
   - función: al ver "function" y luego el ID, guardar el nombre.
     Al ver el '(' que abre los parámetros, entrar al ámbito de la función:
       AMBITO_ACTUAL = AMBITO_ACTUAL + ":" + nombre
     Así los parámetros ya se lexan en el ámbito correcto.
   - método: dentro del cuerpo de una clase, detectar la forma tipo_declarado ID '('
     y entrar al ámbito del método en el '('.
       clase main:ca -> método m -> main:ca:m
   - clase: al ver CLASS ID guardar el nombre; al ver el BEGIN del cuerpo de la clase
     entrar al ámbito main:ca. Al cerrar ese END, salir.
   - al cerrar el END de una función o método, restaurar el ámbito anterior.
   - no mantengas declarados ni alcanzable; la visibilidad ahora la resuelve la TS.
   Importante: una clase puede declararse dentro de una función; el ámbito de la clase
   debe ser el ámbito actual más :NOMBRE_CLASE.

3. En Entorno:
   - Reemplazá funcionActual y claseActual por pilas:
       Deque<FuncionDeclarada> pilaFunciones
       Deque<ClaseDeclarada> pilaClases
   - FuncionDeclarada debe guardar:
       nombre, tipoRetorno, linea, EntradaTabla entrada, boolean esMetodo
       List<Parametro>, con Parametro { nombre, tipo, EntradaTabla entrada }
   - FuncionDeclarada debe conocer su ámbito de declaración y su propio ámbito:
       ambitoDeclaracion = entrada.ambito
       ambitoPropio = ambitoDeclaracion + ":" + nombre
   - ClaseDeclarada debe guardar su entrada, su ámbito propio y sus métodos.
   - abrirFuncion debe marcar la entrada como FUNCION o METODO, con tipoRetorno.
     Para métodos, el nombre se declara en el ámbito de la clase.
   - agregarParametroFormal debe marcar la entrada como PARAMETRO en el ámbito actual
     y guardar la entrada en Parametro.
   - declararVariable debe distinguir:
       * si hay función actual: variable local del ámbito actual;
       * si no hay función actual pero hay clase actual: atributo de la clase;
       * si no: variable del ámbito actual.
     No conviertas variables locales de métodos en atributos.
   - declararAtributo debe declarar en la TS con Uso=ATRIBUTO en el ámbito de la clase.
   - declararObjetos debe resolver la clase con resolver(), verificar que sea Uso=CLASE,
     y guardar en la variable el tipo de la clase resuelta.
   - registrarTipoEnumerado debe declarar con Uso=TIPO_ENUMERADO.
   - abrirClase debe declarar el nombre de la clase en el ámbito actual y luego
     crear la clase; el cuerpo de la clase usará ámbito actual + ":" + nombre.
   - getFuncion y getClase deben buscar por clave completa (nombre:ámbito), no por nombre pelado.
   - Los mapas variables/funciones/clases/enumerados deben usar claves con ámbito.
     Si preferís, eliminá el mapa de variables y leé tipoDato desde la TS.

4. En Verificaciones reemplazá verificarAlcance por la resolución nueva.
   FACTOR_ID y ASIGNACION deben usar la entrada resuelta, no el placeholder del léxico.
   En ASIGNACION, devolvé una estructura con el destino ya resuelto para que el
   generador no use el placeholder.

5. Actualizá la documentación:
   - DEC-1: ámbito en minúsculas, ejemplo main:f:g.
   - DEC-2/DEC-4: no se borran entradas al cerrar ámbito.
   - DEC-3: búsqueda por nombre:ámbito y prefijos, no solo ámbito actual.
   - DEC-5: ámbitos de clase y regla de herencia/prefijo.

Criterio de aceptación:
- test/TP3 _y_TP4/AMBITO/prueba_funcion_anidada.txt:
  a:main, n:main:a, b:main:a, m:main:a:b.
- Un x local de g se ve como x:main:f:g.
- test/TP3 _y_TP4/AMBITO/prueba_funciones_iguales.txt:
  var_1:main:a:b:fun_c existe declarada; var_1:main:a:fun_c queda como
  placeholder no declarado y el uso sigue siendo error.
- test/TP3 _y_TP4/AMBITO/prueba_variable_en_main.txt:
  la función ve global:main.
- Las clases ya no comparten atributos en main.

Nota de ejecución (ver `docs/PROMPT-1B.md`): el parser puede reducir el
encabezado cuando ya leyó el `(`, por eso `ControlAmbito` expone
`ambitoDeclaracionDe(nombre, ambitoActual)` para que `Entorno` declare el
nombre en el ámbito padre correcto. La lista original de aceptación escribía
`n:main` y `m:main:a`; los parámetros se lexan dentro de la función, así que
las claves correctas son `n:main:a` y `m:main:a:b`.

### Prompt 2 — Motor de tipos y prohibición de conversiones implícitas
Implementá el chequeo de tipos de TP3. Regla central: no hay conversiones implícitas.
Dos operandos de una operación deben tener el mismo tipo. La única conversión permitida
es la explícita todf del Tema 33, que se implementa después.

Archivos: Tipos.java, ExpresionDiferida.java, AccionesSemanticas.java,
otras_acciones/AccionesExpresiones.java, otras_acciones/AccionesDeclaraciones.java,
Verificaciones.java, Entorno.java.

1. En Tipos agregá:
   - boolean esNumerico(String tipo)
   - boolean esEntero(String tipo)
   - boolean esFlotante(String tipo)
   - boolean mismoTipo(String a, String b)  // igualdad exacta
   - String tipoEfectivo(String tipo)       // enum -> su tipo base; clase -> la clase
   No agregues promociones ni conversiones implícitas.

2. En ExpresionDiferida:
   - agregá String tipo y getTipo()/conTipo().
   - deOperando(Object, String tipo).
   - anteponer/absorber deben conservar el tipo.
   - Si un operando es una ExpresionDiferida anidada, su tipo es el de esa expresión.
   - Si un operando es EntradaTabla, su tipo es entrada.tipoDato.
   - Si un operando es LlamadaFuncion, su tipo es el tipo de retorno de la función.
   - Si un operando es Conversion, su tipo es el tipo destino.
   - Si un operando es AsignacionEnExpresion, su tipo es el de la asignación.

3. En AccionesSemanticas.combinar:
   - obtené tipoIzquierdo y tipoDerecho.
   - si alguno es null/indefinido, no sigas chequeando para no cascadear errores.
   - si no son iguales, informá:
       "Tipos incompatibles en el operador '+': se esperaba ulongint y se encontró doublef"
     y devolvé una expresión de tipo indefinido.
   - si son iguales, el resultado tiene ese tipo.
   - cadena solo es válida en pout y asignaciones de cadena; no en +, -, *, /.

4. En AccionesExpresiones:
   - FACTOR_ID: resolverIdentificador(id, VARIABLE/PARAMETRO/ATRIBUTO) y devolver
     la entrada resuelta con su tipo.
   - FACTOR_CONSTANTE: tipo desde la entrada (o tiparConstante como fallback).
   - FACTOR_CADENA: tipo cadena.
   - ASIGNACION_EN_EXPRESION: resolver destino, chequear tipo destino vs tipo RHS,
     chequear anidamiento, y devolver una ExpresionDiferida que represente la asignación.
   - CONVERSION_TODF: no la implementes acá todavía; dejala para el Tema 33.
   - INVOCACION_FUNCION: resolver la función, guardar su firma en LlamadaFuncion
     y devolver tipo de retorno.
   - ACCESO_ATRIBUTO: devolver una ReferenciaAtributo con el tipo del atributo.

5. En AccionesDeclaraciones:
   - ASIGNACION: resolver el destino; verificar que sea asignable;
     chequear tipo destino == tipo RHS. Si no, error de incompatibilidad.
   - CONDICION: chequear que ambos lados tengan el mismo tipo.
   - SENTENCIA_RET: chequear que el tipo de la expresión coincida con el tipo de retorno
     de la función actual. Si no hay función actual, error.
   - DECL_VARIABLES/DECL_OBJETOS: si el tipo es un ID de usuario, resolverlo;
     si no existe, error "tipo no declarado".

6. En Verificaciones agregá un helper único de error de tipos, por ejemplo:
   errorTipos(int linea, String contexto, String esperado, String obtenido)

Criterio de aceptación:
- ulongint x; doublef z; x := z; -> error.
- z := x; -> error, salvo que x esté convertido con todf (Tema 33).
- x := x + 1$ul; -> válido.
- z := z + 1.0; -> válido.
- if (x > 1.0) -> error.
- No debe haber promociones automáticas en ningún operador.

### Prompt 2B — Codegen base: pout, ret, delimitadores, llamadas y conversiones
Implementá la generación de código intermedio base que falta. No implementes todavía
enumeraciones, objetos, atributos ni herencia; eso va en sus filas.

Archivos: GeneradorTercetos.java, Terceto.java (contiene ListaTercetos),
LlamadaFuncion.java, ParametroReal.java, AccionesDeclaraciones.java,
otras_acciones/AccionesExpresiones.java, Entorno.java, Verificaciones.java.

1. Delimitadores:
   - Al inicio de generar(), emitir (PROGRAMA, --, --).
   - Al final, emitir (FIN_PROGRAMA, --, --).
   - En INICIO_FUNCION, emitir (FUNCION, nombre, --) o (METODO, nombre, --)
     según FuncionDeclarada.esMetodo.
   - En DECL_FUNCION/DECL_METODO, emitir (FIN_FUNCION, nombre, --) o
     (FIN_METODO, nombre, --).
   - En INICIO_CLASE/DECL_CLASE, si querés delimitar, emitir (CLASE, nombre, --)
     y (FIN_CLASE, nombre, --). No es obligatorio, pero ayuda a leer la salida.

2. Sentencias:
   - SENTENCIA_POUT: emitir (pout, valor, --). Aceptar cadena, variable o expresión.
   - SENTENCIA_RET: emitir (ret, valor, --).
   - SENTENCIA_REPEAT_WHILE e if: mantener la lógica actual, pero usando los
     tipos ya chequeados.
   - Toda sentencia ejecutable del lenguaje debe generar algo o estar explícitamente
     justificada como no generable.

3. Invocaciones:
   - LlamadaFuncion debe guardar la FuncionDeclarada resuelta y su tipo de retorno.
   - ParametroReal debe guardar la EntradaTabla del parámetro formal cuando se conozca.
   - Antes del CALL, emitir la copia de cada real al formal en el orden de evaluación:
       (PAR, formal, real)
     Si el parámetro es nombrado, usar el formal indicado; si es posicional, el formal
     que corresponde a esa posición.
   - Luego emitir (CALL, funcion, cantidadDeParametros).
   - El resultado de una invocación es el número del terceto CALL y su tipo es el
     tipo de retorno de la función.
   - Para métodos, definí una convención y usala en todo el TP:
       (CALLMET, objeto, metodo) precedido por los PAR de los parámetros.
     Documentala en un comentario del generador.

4. Conversión todf:
   - En emitir(), si el valor es Conversion, emitir (todf, valor, --) y devolver
     el número de ese terceto.
   - El tipo del terceto es el tipo destino de la conversión.

5. Asignación en expresión:
   - En emitir(), si el valor es AsignacionEnExpresion:
       * emitir el RHS;
       * emitir (:=, destino, rhs);
       * devolver el número del terceto de asignación como valor de la expresión.
   - Para x := y = (2$ul) se espera:
       1. (:=, y, 2$ul)
       2. (:=, x, [1])

6. Terceto.texto debe soportar:
   - EntradaTabla
   - String
   - Integer
   - ReferenciaAtributo: mostrarla como objeto.atributo

Criterio de aceptación:
- pout({hola}); genera (pout, hola, --).
- ret(x); dentro de una función genera (ret, x, --).
- Una función con cuerpo genera FUNCION ... FIN_FUNCION.
- foo(x, z)[1,2] genera PAR formal/real y CALL.
- z := todf(x) genera (todf, x, --) y (:=, z, [1]).
- x := y = (2$ul) genera la asignación a y y luego la asignación a x.

### Prompt 3.1 — Tema 6 y 8: ulongint y doublef
Implementá únicamente la fila "6 y 8: ulongint, doublef".
Requisito de TP3: el tipo de cada constante debe quedar registrado en la Tabla de
Símbolos durante el Análisis Léxico, no recién al usarla como factor.

Archivos: acciones semanticas/AccionConstanteEntera.java,
acciones semanticas/AccionConstanteFloat.java, TablaSimbolos.java,
Entorno.java, Tipos.java.

1. En AccionConstanteEntera, después de validar formato y rango:
   - insertar la constante con tipo Tipos.ULONGINT.
   - Podés agregar una sobrecarga:
       buscarOInsertarConstante(String lexema, int tokenID, String tipo)
     que cree o actualice la entrada y le asigne tipoDato.
   - Marcar uso CONSTANTE.

2. En AccionConstanteFloat, después de validar formato y rango:
   - insertar la constante con tipo Tipos.DOUBLEF.
   - Mantener el caso especial lexema "." que devuelve el token '.' para el acceso
     a atributos; en ese caso no insertar nada.

3. Revisar constantes negativas:
   - El léxico ve el '-' como símbolo y la constante sin signo.
   - Entorno.aplicarSignoNegativo debe crear la entrada "-lexema" con el tipo correcto
     y marcarla CONSTANTE.
   - Verificar rango después del signo.

4. Dejar Tipos.tipoDeConstante como fallback, pero la fuente principal debe ser
   el tipo asignado en el léxico.

5. No cambiar los rangos ni los formatos ya implementados.

Criterio de aceptación:
- Al compilar 1$ul, la TS muestra Tipo de dato: ulongint.
- Al compilar 2.5d+3, la TS muestra Tipo de dato: doublef.
- Al compilar -1$ul, la TS muestra la constante negativa con tipo ulongint.
- Los tests test/tp1/verificacion_constantes_ulongint/* y
  test/tp1/verificacion_constantes_doublef/* siguen pasando.
- Un uso posterior de la constante conserva el tipo sin depender de tiparConstante.

### Prompt 3.2 — Temas 9 y 16: cadenas y comentarios
Verificá y, solo si encontrás un defecto, corregí la fila "9 y 16: cadenas y comentarios".
No agregues funcionalidad nueva que no corresponda.

Casos a verificar:
1. Cadena de una línea entre llaves: {texto}.
   - Se inserta en la TS con Tipo de dato: cadena.
   - No se permite salto de línea.
   - pout({hola}) debe generar (pout, hola, --).
2. Comentario multilínea: {{ ... }}.
   - Se descarta por completo.
   - No debe aparecer en la TS ni en la tira de tokens.
   - Puede contener llaves, guiones bajos y varias líneas.
3. Cadena con guion bajo y con llaves internas válidas.
4. Cadena sin cerrar: error léxico.
5. Comentario sin cerrar: error léxico.

Archivos a revisar: acciones semanticas/AccionCadena.java,
AnalizadorLexico.java, MatrizTransiciones.java, TablaSimbolos.java.

Si todo funciona, no modifiques código: agregá casos de regresión en
test/TP3 _y_TP4/CADENAS_COMENTARIOS/ y documentá el resultado.

Criterio de aceptación:
- Los casos válidos no generan errores.
- Los inválidos generan el error léxico correspondiente.
- Las cadenas llegan al codegen con su tipo.

### Prompt 3.3 — Tema 13: repeat ... while
Implementá únicamente la fila "13: repeat ... while".
La estructura básica ya existe; completá los chequeos de tipos y asegurate de que
el cuerpo admita todas las sentencias ejecutables.

Archivos: GeneradorTercetos.java, Verificaciones.java,
otras_acciones/AccionesDeclaraciones.java, otras_acciones/AccionesExpresiones.java.

1. En la acción CONDICION, chequear que ambos operandos tengan el mismo tipo.
   Si la condición no es comparable, informar error en la línea.
2. En generarRepeat, usar la comparación ya emitida y mantener:
   - etiqueta de inicio antes del cuerpo;
   - BF a la etiqueta de salida;
   - BI a la etiqueta de inicio;
   - etiqueta de salida al final.
3. Verificar que dentro del cuerpo se generen correctamente:
   - asignaciones;
   - pout;
   - if/else;
   - repeat anidado;
   - invocaciones;
   - ret si corresponde.
4. No permitir una condición de tipo cadena.
5. Mantener el comportamiento actual para repeat anidados y secuenciales.

Criterio de aceptación:
- test/TP3 _y_TP4/ITERACION/* sigue generando los mismos tercetos, salvo los
  cambios de nombre de ámbito.
- Un repeat con pout y una invocación dentro del cuerpo genera el código de ambos.
- repeat ... while (x > 1.0) con x ulongint informa error de tipos.
- repeat ... while (x > 1$ul) no informa error.

### Prompt 3.4 — Tema 17: asignación dentro de expresiones
Implementá únicamente la fila "17: asignación de expresiones en expresiones".
La gramática ya reconoce ID '=' '(' expresion ')'. Falta generar la asignación
interna y chequear sus tipos.

Archivos: ExpresionDiferida.java, otras_acciones/AccionesExpresiones.java,
Verificaciones.java, GeneradorTercetos.java.

1. Modelá la asignación interna. Podés crear:
   class AsignacionEnExpresion {
       EntradaTabla destino;
       ExpresionDiferida valor;
       String tipo;
   }
   o agregar los campos equivalentes a ExpresionDiferida.

2. En ASIGNACION_EN_EXPRESION:
   - resolver el ID destino con uso variable/parámetro/atributo;
   - chequear tipo destino == tipo del RHS;
   - si el RHS contiene otra asignación, informar ERR_ASIGNACION_ANIDADA;
   - devolver una ExpresionDiferida que contenga la AsignacionEnExpresion y
     cuyo tipo sea el tipo del destino.
   - Mantener marcarAsignacion() para que el anidamiento se detecte hacia afuera.

3. En GeneradorTercetos.emitir, para AsignacionEnExpresion:
   - emitir el valor del RHS;
   - emitir (:=, destino, rhs);
   - devolver el número del terceto de asignación.
   Así x := y = (2$ul) genera:
     1. (:=, y, 2$ul)
     2. (:=, x, [1])

4. Chequear que el plegado de ExpresionDiferida no pierda la asignación:
   - probar y = (2$ul) + 1$ul;
   - probar f(y = (2$ul))[1$ul];
   - probar x := y = (z = (1$ul)); debe dar error de anidamiento.

Criterio de aceptación:
- test/tp2/verificacion_r17_asig_expresiones/estructura_respetada.txt genera
  la asignación a y y la asignación a x.
- anidamiento.txt y anidamiento_indirecto.txt informan ERR_ASIGNACION_ANIDADA.
- en_invocacion.txt genera la asignación a y antes del PAR.
- Un tipo incompatible en la asignación interna informa error.

### Prompt 3.5 — Tema 33: conversión explícita todf
Implementá únicamente la fila "33: conversión explícita todf".
No agregues conversiones implícitas: la única conversión permitida es todf.

Archivos: Tipos.java, ExpresionDiferida.java,
otras_acciones/AccionesExpresiones.java, AccionesSemanticas.java,
Verificaciones.java, GeneradorTercetos.java.

1. Creá una clase Conversion:
   class Conversion {
       String tipoDestino;     // Tipos.DOUBLEF
       Object expresion;       // ExpresionDiferida o EntradaTabla
   }

2. En CONVERSION_TODF:
   - obtener el tipo del operando;
   - si no es ulongint (o un enum con base ulongint), informar error
     "todf solo convierte enteros a doublef";
   - si el operando ya es doublef, informar error;
   - devolver una ExpresionDiferida con tipo doublef que contenga la Conversion.

3. En AccionesSemanticas.combinar y en las asignaciones/comparaciones:
   - el tipo de una Conversion es doublef;
   - z := todf(x) es válido si z es doublef;
   - z := todf(x) + y es válido si y es doublef;
   - x := todf(x) es inválido si x es ulongint.

4. En GeneradorTercetos.emitir:
   - para Conversion, emitir (todf, valor, --) y devolver el número del terceto.
   - z := todf(x) debe generar:
       1. (todf, x, --)
       2. (:=, z, [1])
   - z := todf(x + 1$ul) debe generar:
       1. (+, x, 1$ul)
       2. (todf, [1], --)
       3. (:=, z, [2])

5. No convertir automáticamente en asignaciones ni en operadores.

Criterio de aceptación:
- test/tp2/verificacion_r33_conversion/* deja de mostrar (:=, z, x) y muestra todf.
- z := x + 1$ul con z doublef y x ulongint informa error.
- x := z con x ulongint y z doublef informa error.
- if (todf(x) > z) es válido.
- todf(z) con z doublef informa error.

### Prompt 3.6 — Tema 19: orden obligatorio de parámetros
Implementá únicamente la fila "19: orden de evaluación de parámetros reales obligatorio".
La gramática ya exige los corchetes. Falta validar completamente y generar la copia
de reales a formales.

Archivos: Verificaciones.java, otras_acciones/AccionesExpresiones.java,
LlamadaFuncion.java, ParametroReal.java, Entorno.java, GeneradorTercetos.java.

1. Resolver la función o método invocado con Name Mangling.
   - Si no existe o no es visible: error.
   - Si es una variable: error de uso incorrecto.
   - Guardar la FuncionDeclarada resuelta en LlamadaFuncion.

2. Verificaciones.verificarInvocacion debe chequear:
   - cantidad de reales == cantidad de formales;
   - la lista de orden no está vacía;
   - cada posición está en [1, cantidad];
   - no hay posiciones repetidas;
   - hay exactamente cantidad posiciones;
   - para cada real:
       * si es nombrado, el formal existe y no está repetido;
       * si es posicional, le corresponde el formal de esa posición;
       * tipo real == tipo formal, sin conversiones implícitas.
   - Informar todos los errores con la línea de la invocación.

3. En ParametroReal guardar:
   - nombreFormal;
   - EntradaTabla formal;
   - Object valor;
   - String tipoReal;
   - int linea.

4. En GeneradorTercetos.generarInvocacionFuncion:
   - recorrer el orden de evaluación;
   - para cada posición, emitir (PAR, formal, real);
   - emitir (CALL, funcion, cantidadDeParametros);
   - para métodos, usar la convención definida en la Fase 2B.
   - El resultado del CALL es el valor de la invocación y su tipo es el tipo de retorno.

5. Chequear también el tipo de retorno cuando la invocación se usa en una asignación
   o en una operación.

Criterio de aceptación:
- test/tp2/verificacion_r19_param_reales/estructura_respetada.txt no tiene errores
  y genera PAR formal/real + CALL.
- orden_sin_corchetes.txt y sin_orden.txt informan falta de orden.
- futuros/orden_fuera_de_rango.txt informa posición fuera de rango.
- Una invocación con cantidad incorrecta informa error.
- Una invocación con tipo incompatible informa error.
- x := foo(x, z)[1,2] usa el tipo de retorno de foo en la asignación.

### Prompt 3.7 — Tema 23: enumeraciones
Implementá únicamente la fila "23: enumeraciones".

Archivos: Entorno.java, Verificaciones.java,
otras_acciones/AccionesDeclaraciones.java, Tipos.java, TablaSimbolos.java.

1. En DECL_TYPEDEF:
   - declarar el nombre del typedef en la TS con Uso=TIPO_ENUMERADO;
   - detectar redeclaración en el mismo ámbito;
   - guardar en TipoEnumerado: nombre, tipoBase, valores y entrada;
   - todos los valores deben ser constantes del mismo tipo base; si hay mezcla,
     informar error;
   - rechazar enumeración vacía (ya existe ERR_FALTA_VALORES_ENUMERADO).

2. En DECL_VARIABLES y DECL_OBJETOS:
   - si el tipo es un ID de usuario, resolverlo con Name Mangling;
   - si no existe, error "tipo no declarado";
   - si es un typedef, la variable queda con ese tipo enumerado.

3. En asignaciones:
   - si el destino es de tipo enumerado y el RHS es una constante, chequear que
     la constante pertenezca a los valores de esa enumeración;
   - si el RHS es una variable o expresión, no chequear el valor: se posterga;
   - si el RHS es del mismo tipo enumerado, es válido;
   - informar error si la constante no pertenece.

4. Para operaciones aritméticas/comparaciones, usar el tipo base del enumerado
   como tipo efectivo. No inventes conversiones.

5. En la salida de la TS, el typedef debe figurar con su tipo y ámbito.

Criterio de aceptación:
- typedef pares = [2$ul, 4$ul, 6$ul]; pares p1; p1 := 2$ul; -> válido.
- p1 := 3$ul; -> error por constante fuera de la enumeración.
- p1 := p2; -> válido.
- p1 := p2 + 1$ul; -> válido, sin chequear valor.
- typedef roto = []; -> error.
- pares p1; sin typedef previo -> error de tipo no declarado.
- Dos typedefs con el mismo nombre en ámbitos distintos conviven.

### Prompt 3.8 — Tema 25: asignación de objetos
Implementá únicamente la fila "25: asignación por estructura en objetos sin código".
No implementes todavía herencia ni acceso a atributos; eso va en 28 y 31.

Archivos: Entorno.java, Verificaciones.java,
otras_acciones/AccionesDeclaraciones.java, GeneradorTercetos.java,
ExpresionDiferida.java, Terceto.java.

1. En DECL_OBJETOS:
   - resolver el nombre de clase con Name Mangling;
   - si no existe o no es Uso=CLASE, error;
   - la variable queda con tipo = clase resuelta.

2. Definir Entorno.clasesCompatibles(ClaseDeclarada a, ClaseDeclarada b):
   - comparar atributos efectivos: nombre y tipo, incluyendo heredados;
   - comparar métodos: nombre, tipo de retorno y tipos de parámetros;
   - dos variables del mismo tipo de clase son siempre compatibles;
   - documentar la regla en DEC-6.
   Para esta fila podés comparar solo atributos; si ya existe la lista de métodos,
   incluila.

3. En ASIGNACION:
   - si destino y origen son de tipo clase, verificar compatibilidad estructural;
   - si no son compatibles, error;
   - devolver una estructura Asignacion con ambos tipos.

4. En GeneradorTercetos.generarAsignacion:
   - si es una asignación entre objetos, no emitir una sola asignación;
   - emitir la copia estructural. Usá una de estas dos formas y documentala:
       a) (COPY, destino, origen);
       b) un (:=, atributoDestino, atributoOrigen) por cada atributo.
   Recomendación: (COPY, destino, origen) y, si querés, comentar los atributos
   copiados. Si elegís b), necesitás ReferenciaAtributo, que se implementa en 28;
   en ese caso podés hacer 28 antes que 25.

5. Chequear que no se asigne un objeto a una variable de tipo básico ni al revés.

Criterio de aceptación:
- class ca begin ulongint x; end; ca a1, a2; a1 := a2; -> válido y genera COPY.
- class cb begin ulongint y; end; ca a1; cb b1; a1 := b1; -> error estructural.
- ca a1; ulongint x; a1 := x; -> error.
- ca a1; a1 := 1$ul; -> error.
- Dos clases con el mismo nombre en ámbitos distintos se resuelven por Name Mangling.

### Prompt 3.9 — Tema 28: acceso tradicional a atributos
Implementá únicamente la fila "28: acceso tradicional a atributos con punto".

Archivos: Entorno.java, Verificaciones.java,
otras_acciones/AccionesExpresiones.java, ExpresionDiferida.java,
GeneradorTercetos.java, Terceto.java.

1. Creá ReferenciaAtributo:
   class ReferenciaAtributo {
       EntradaTabla objeto;      // variable de tipo clase
       EntradaTabla atributo;    // entrada del atributo en la TS
       String tipo;              // tipo del atributo
   }

2. En ACCESO_ATRIBUTO (objeto.atributo):
   - resolver el ID del objeto; debe ser variable/parámetro/atributo de tipo clase;
   - resolver la clase del objeto;
   - buscar el atributo en la clase, según la regla D3/DEC-5;
   - si no existe, error "atributo 'x' no existe en la clase 'ca'";
   - devolver una ExpresionDiferida de tipo atributo que contenga ReferenciaAtributo.

3. En ASIGNACION con LHS referencia_atributo:
   - chequear tipo del atributo vs tipo del RHS;
   - devolver una Asignacion con destino ReferenciaAtributo.

4. En GeneradorTercetos:
   - emitir (:=, referencia, valor) para asignaciones a atributos;
   - Terceto.texto debe mostrar objeto.atributo;
   - usar el tipo del atributo en operaciones y en pout.

5. No implementes todavía la forma objeto.clase.atributo; eso es la fila 31.
   Si la gramática ya la reduce a ACCESO_ATRIBUTO_PREFIJADO, dejá esa acción
   para la fila 31.

Criterio de aceptación:
- test/tp2/verificacion_r28_r31_atributos/clase_con_atributos.txt:
  a1.x := 3$ul; y pout(a1.x); son válidos.
- a1.noexiste := 1$ul; -> error.
- ulongint x; x.atributo := 1$ul; -> error, x no es objeto.
- class ca begin ulongint x; end; ca a1; a1.x := 1.0; -> error de tipos.
- La TS muestra el atributo con ámbito main:ca.

### Prompt 3.10 — Tema 31: herencia múltiple con prefijo
Implementá únicamente la fila "31: herencia múltiple con desambiguado por prefijado".
Usá la decisión D3/DEC-5 sobre prefijo obligatorio o búsqueda sin prefijo.

Archivos: Entorno.java, Verificaciones.java,
otras_acciones/AccionesDeclaraciones.java,
otras_acciones/AccionesExpresiones.java, GeneradorTercetos.java,
LlamadaFuncion.java, ParametroReal.java.

1. En SENTENCIA_EXTENDS:
   - resolver cada clase base con Name Mangling;
   - verificar que exista y sea Uso=CLASE;
   - verificar que no sea la misma clase ni genere un ciclo;
   - guardar la lista de ancestros en orden;
   - detectar bases repetidas.

2. Búsqueda de atributos:
   - ACCESO_ATRIBUTO sin prefijo: buscar en la clase propia y luego en ancestros
     según DEC-5; si hay más de un candidato, error de ambigüedad;
   - ACCESO_ATRIBUTO_PREFIJADO (objeto.clase.atributo):
       * verificar que "clase" esté en la cadena de herencia del objeto;
       * verificar que el atributo exista en esa clase;
       * devolver el tipo correspondiente.

3. Búsqueda de métodos:
   - INVOCACION_METODO sin prefijo: misma regla que atributos;
   - INVOCACION_METODO_PREFIJADO (objeto.clase.metodo(...)[...]):
       * verificar que "clase" sea ancestro;
       * verificar que el método exista en esa clase;
       * chequear cantidad, orden y tipos de parámetros como en el Tema 19;
       * devolver el tipo de retorno.
   - Para las llamadas, generar la copia de parámetros y el CALLMET definido
     en la Fase 2B.

4. Detectar:
   - prefijo que no es una clase heredada;
   - atributo/método que no existe en la clase prefijada;
   - herencia circular;
   - clase base no declarada;
   - ambigüedad sin prefijo.

Criterio de aceptación:
- test/tp2/verificacion_r28_r31_atributos/desambiguado_tradicional_y.txt:
  a1.padre.y y a1.hija.y son válidos.
- metodo_heredado_prefijado_con_orden.txt y
  metodo_heredado_prefijado_un_parametro.txt generan llamadas y no tienen errores.
- a1.nohereda.x -> error.
- a1.padre.noexiste -> error.
- Si dos bases definen el mismo atributo y se usa sin prefijo, error de ambigüedad.
- Una herencia circular informa error.
- D3 revisada: un miembro heredado usado sin prefijo (`a1.m(...)`) es error por
  falta de prefijo. El prefijo explícito `objeto.clase.miembro` es obligatorio
  para miembros heredados; los propios pueden usarse sin prefijo.
- Los casos existentes con `a1.m(...)` deben quedar como error esperado o
  actualizarse a la forma prefijada.