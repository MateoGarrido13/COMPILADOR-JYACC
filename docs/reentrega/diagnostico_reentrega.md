# Diagnóstico profundo de la reentrega — Grupo 43

Fecha del análisis: 21/11/2026  
Rama: `reentrega`  
HEAD al iniciar: `e925098 elimino asm`  
Estado inicial: `git status --short` limpio.

Este documento **no implementa correcciones**. Registra diagnóstico, evidencia reproducible, flujo de datos, clases involucradas y consecuencias de las cuatro observaciones del profesor.

> Nota de cierre P6-01 (24/11/2026): este análisis es histórico. La posibilidad
> de aceptar recursión quedó descartada por decisión del usuario: los enunciados
> de TP01–TP04 no la exigen y Ola 5 no se ejecutó. Estado final vigente:
> `Entorno.getFuncion()` solo consulta funciones cerradas; una llamada a función
> abierta se descarta silenciosamente, sin `PAR`/`CALL` interno y sin mensaje de
> error; el programa y sus llamadas externas sí se emiten. Las frases que hablan
> de “corregir la recursión”, “P5A” o “rechazo explícito” describen el análisis
> original, no el estado aceptado ni trabajo pendiente.

---

## 0. Alcance y método

### 0.1 Fuentes usadas

- Enunciados:
  - `ASSETS/TPs/Tp01-2026.pdf`
  - `ASSETS/TPs/Tp02-2026.pdf`
  - `ASSETS/TPs/Tp03-2026-1.pdf`
  - `ASSETS/TPs/Tp04-2026-1.pdf`
- Código fuente Java, gramática YACC y documentación del repositorio.
- Casos de prueba de `test/tp1/`, `test/tp2/` y `test_tp_4/`.
- Informes previos:
  - `ASSETS/DIAGNOSTICO_FUNCIONES_METODOS.md`
  - `ASSETS/informe_generador_tercetos.md`
  - `ASSETS/recursion.md`
  - `docs/PROMPT-0.md`
  - `docs/PROMPT-17.md`
  - `docs/PROMPT-19.md`
  - `docs/PROMPT-23.md`

### 0.2 Cómo se extrajeron los PDF

Los PDF no se pueden analizar como texto plano con las herramientas normales de lectura. Se usó:

```bash
pdftotext -layout ASSETS/TPs/Tp01-2026.pdf -
pdftotext -layout ASSETS/TPs/Tp02-2026.pdf -
pdftotext -layout ASSETS/TPs/Tp03-2026-1.pdf -
pdftotext -layout ASSETS/TPs/Tp04-2026-1.pdf -
```

### 0.3 Compilación y ejecución aislada

Para no ensuciar el repositorio con `.class` ni `.asm`, se compiló fuera del árbol:

```bash
mkdir -p /tmp/opencode/tp3build
javac -encoding UTF-8 -d /tmp/opencode/tp3build \
      *.java "acciones semanticas"/*.java otras_acciones/*.java
```

Ejecución:

```bash
java -cp /tmp/opencode/tp3build Main /tmp/opencode/tp3run/<caso>.txt
```

Las fuentes de prueba se copiaron a `/tmp/opencode/tp3run/` y `/tmp/opencode/lexprobe/`.

### 0.4 Experimento temporal

Para confirmar la causa de la recursión se modificó **solo una copia** en `/tmp/opencode/tp3patched/`. El repositorio no fue tocado. El cambio experimental restauró el fallback de `Entorno.getFuncion()` hacia `pilaFunciones`.

### 0.5 Limitación

No hay MASM32/`ml.exe`/Windows en el entorno. Por eso TP04 se confirmó por:

1. inspección del texto `.asm` generado;
2. trazabilidad del IR;
3. lectura del generador assembler.

No se ensambló ni ejecutó el ejecutable. El profesor ya reportó fallos reales en MASM32; este análisis explica those failures desde el código generado.

---

## 1. Resumen ejecutivo

| TP | Observación | Veredicto | Causa raíz principal |
|---|---|---|---|
| TP01 | ¿Se reconoce asignación si carácter inválido divide identificador? | **No se reconoce `id#malo`. Se reconocen fragmentos y queda IR truncado.** | Matriz léxica: desde estado ID, columna “otro” va a `ESTADO_F`, no a `ERROR`. El `#` se devuelve como lookahead. |
| TP02 | Guía da por válidos dos comportamientos contrarios al enunciado | **Confirmado como contradicción de expected results.** El parser sí reporta error, pero emite IR parcial. | Producciones de error continúan construyendo resultados semánticos; expected results no coinciden con enunciado ni con IR real. |
| TP03 | Lista dinámica de tercetos vs. typedef vacío | **Contradicción verificable.** Error reportado, pero IR posterior se conserva. | `descartarDesde()` existe, pero no se usa para typedefs inválidos; IR se genera aunque `Reporte` tenga errores. |
| TP04 | Ejecutable entra en subrutina antes del main | **Confirmado con ASM generado.** | `emitirCodigo()` emite `start:` y luego traduce IR lineal; prólogos de funciones quedan inmediatamente después de `start:`, sin `jmp` que los separe. |
| TP04 | Caso factorial | **Doble fallo.** Entrada anticipada a subrutina + llamada recursiva suprimida silenciosamente. | `Entorno.getFuncion()` solo consulta `funciones`, aunque su comentario documenta fallback a `pilaFunciones`. |

---

## 2. Marco del grupo

Según `readme.md:7`, temas particulares del grupo 43:

- `ulongint`
- `doublef`
- cadenas de una línea
- comentarios multilínea
- `repeat`/`while`
- asignación de expresiones dentro de expresiones — tema 17
- orden de evaluación de parámetros — tema 19
- enumeraciones — tema 23
- clases
- acceso a atributos con punto
- herencia múltiple con prefijo
- conversión explícita `todf`

Los chequeos runtime asignados al grupo, según enunciado TP04, son `b, e, f, h`. La recursión es el chequeo `i` y **no corresponde al grupo 43**. En el estado final P6-01 la recursión quedó fuera de alcance: el test `03_recursion_factorial_metodo.txt` se conserva solo como regresión de `jmp main_inicio` y llamadas externas, no como prueba positiva de recursión.

---

# 3. TP01 — carácter inválido que divide un identificador

## 3.1 Requisito del enunciado

TP01 define identificadores así:

- primer carácter: letra;
- resto: letras, dígitos o `_`;
- letras solo minúsculas;
- máximo 22 caracteres, con warning por truncamiento.

TP01, sección de casos de prueba, pide explícitamente:

> Intento de incluir en el nombre de un identificador un carácter que no sea letra, dígito o `_`.

Por lo tanto `id#malo`, `otro@nombre`, etc. deben reconocerse como error léxico. No deben entregar `id`, `#`, `malo` como si fueran tokens válidos de un programa aceptable.

## 3.2 Flujo real del léxico

### 3.2.1 Clase involucrada

- `AnalizadorLexico.java`
- `ContextoLexico.java`
- `MatrizTransiciones.java`
- `acciones semanticas/AccionIdentificador.java`
- `Reporte.java`

### 3.2.2 Matriz de transiciones

`MatrizTransiciones.java:21-42` define la matriz. Columna 20 = “otro”.

Estado 16 = identificador:

```java
{ 16, 16, 16, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 16, 99, 99, 99, 99, 99, 99}, // 16 id
```

Fila correspondiente a estado 16 en `MATRIZ_ESTADOS` (`MatrizTransiciones.java:39`):

- columna 1 (minúscula) → 16;
- columna 0 (dígito) → 16;
- columna 16 (`_`) → 16;
- columna 20 (`otro`) → **99 = ESTADO_F**.

No va a `-1 = ERROR`.

Esto significa que `#`, `@`, etc. se tratan como **delimitadores del ID**, igual que `;`, `(`, espacio, etc.

### 3.2.3 `esLookahead()`

`AnalizadorLexico.java:60-66`:

```java
boolean lookahead = esLookahead(contexto.estado, contexto.caracter, siguiente);
if (!lookahead && !(contexto.estado == 0 && Character.isWhitespace(contexto.caracter))) {
    contexto.lexema.append(contexto.caracter);
}
if (lookahead) {
    fuente.retroceder(contexto.caracter);
}
```

`AnalizadorLexico.java:81-101`:

```java
private static boolean esLookahead(int estado, char caracter, int siguiente) {
    if (siguiente != MatrizTransiciones.ESTADO_F) return false;
    if (Character.isWhitespace(caracter)) return false;
    if (caracter == '=' && (estado == 1 || estado == 2 || estado == 3 || estado == 4)) return false;
    if (estado == 0) return false;
    if ((estado == 6 || estado == 8) && caracter == '}') return false;
    if ((estado == 5 || estado == 6) && caracter == '\n') return false;
    return true;
}
```

Con `estado = 16`, `caracter = '#'`, `siguiente = 99`, todas las condiciones de excepción fallan. Resultado: `lookahead = true`.

Entonces:

1. `#` **no** se agrega al lexema;
2. `#` se devuelve a la fuente;
3. se ejecuta la acción semántica del estado 16;
4. `AccionIdentificador` recibe `id`, no `id#malo`.

`AccionIdentificador.java:10-15` valida el regex sobre `texto = "id"`:

```java
String texto = contexto.lexema.toString();
if (!texto.matches("^[a-z][a-zA-Z0-9_]*$")) { ... }
```

El regex nunca ve `#`. Por eso no puede detectar el identificador inválido completo.

### 3.2.4 Segundo token: error con lexema vacío

En el siguiente `yylex()`:

1. estado = 0;
2. se lee `#`;
3. `ContextoLexico.columna()` devuelve 20;
4. `MATRIZ_ESTADOS[0][20] = -1`;
5. `AnalizadorLexico.java:56-57` llama a `errorLexico()`;
6. `contexto.lexema` está vacío, porque el camino de `ERROR` no agrega el carácter.

`AnalizadorLexico.java:122-125`:

```java
private int errorLexico(ContextoLexico contexto) {
    reporte.error(fuente.linea(), "Lexema no reconocido '" + contexto.lexema + "'");
    return -1;
}
```

Mensaje observado:

```text
Línea 4: Error: Lexema no reconocido ''
```

El mensaje no identifica `#` ni `id#malo`.

## 3.3 Reproducción

 Caso del repositorio: `test/tp1/verificacion_identificadores/caracter_invalido.txt`.

```text
prog
ulongint id#malo, otro@nombre;
begin
    id#malo := 1$ul;
    otro@nombre := 2$ul;
    pout({mal});
end
```

Ejecución:

```bash
cp test/tp1/verificacion_identificadores/caracter_invalido.txt /tmp/opencode/tp3run/
java -cp /tmp/opencode/tp3build Main /tmp/opencode/tp3run/caracter_invalido.txt
```

Salida relevante:

```text
Línea 3: Error: Lexema no reconocido ''
Línea 4: Error: Lexema no reconocido ''
Línea 4: Error: Sentencia mal formada
Línea 4: Error: Falta ';' al final de la sentencia
Línea 3: Error: Falta de ',' en declaracion de variables
```

## 3.4 Pruebas adicionales en `/tmp/opencode/lexprobe/`

### 3.4.1 Lado izquierdo de asignación

Fuente:

```text
prog
ulongint id, malo;
begin
    id#malo := 1$ul;
end
```

Resultado:

```text
Línea 4: Error: Lexema no reconocido ''
Línea 4: Error: Sentencia mal formada
```

IR:

```text
1. (PROGRAMA, --, --)
2. (FIN_PROGRAMA, --, --)
```

No hay asignación.

### 3.4.2 Lado derecho de asignación

Fuente:

```text
prog
ulongint x, id, malo;
begin
    x := id#malo + 1$ul;
end
```

Resultado:

```text
Línea 4: Error: Lexema no reconocido ''
Línea 4: Error: Falta ';' al final de la sentencia
Línea 4: Error: Sentencia mal formada
```

IR:

```text
1. (PROGRAMA, --, --)
2. (:=, x:main, id:main)
3. (FIN_PROGRAMA, --, --)
```

La asignación **sí se emite**, pero truncada: `x := id`, sin `+ 1$ul` y sin `malo`.

### 3.4.3 Declaración seguida de asignación

Fuente:

```text
prog
ulongint id#malo;
begin
    id := 1$ul;
end
```

Resultado:

```text
Línea 2: Error: Lexema no reconocido ''
Línea 2: Error: Falta de ',' en declaracion de variables
```

IR:

```text
1. (PROGRAMA, --, --)
2. (:=, id:main, 1$ul)
3. (FIN_PROGRAMA, --, --)
```

Otra vez: IR parcial sobreviviente al error.

### 3.4.4 Cadena de una línea

Fuente:

```text
prog
begin
    pout({id#malo});
end
```

Resultado: sin error léxico.

IR:

```text
1. (PROGRAMA, --, --)
2. (pout, id#malo, --)
3. (FIN_PROGRAMA, --, --)
```

Esto es coherente con el tema 9: cadenas entre `{}` de una línea admiten cualquier carácter. El problema TP01 aparece **fuera de cadenas**.

## 3.5 Conclusión TP01

La pregunta del profesor queda respondida:

- `id#malo` **no** se reconoce como identificador inválido completo.
- Se entrega `id` al parser.
- `#` se reporta como lexema vacío.
- El parser puede recuperar y emitir asignaciones sobre fragmentos.
- Por eso aparecen IR truncados como `(:=, x:main, id:main)`.

Causa raíz: `MATRIZ_ESTADOS[16][20] = 99` y la política de lookahead de `esLookahead()`. El error léxico se informa tarde y con información incompleta.

## 3.6 Consecuencias

- Salida léxica engañosa: ``Lexema no reconocido ''``.
- Error no describe el carácter ofensor.
- Programas inválidos dejan IR parcial.
- La tabla de símbolos puede quedar con `id` y `malo` como identificadores separados.
- El compilador no escribe `.asm` si hay errores (`Compilador.java:141-144`), pero igual imprime tercetos (`Compilador.java:93,107`).

---

# 4. TP02 — expected results contrarios al enunciado

El profesor observa dos comportamientos que la guía da por válidos y que contradicen los temas 17 y 19.

No se encontró en el repositorio una “guía” con esos dos casos marcados como válidos. La búsqueda cubrió `*.md`, `*.txt`, `*.java`, `*.y` y `*.sh` con patrones como `17_asig_donde_igual`, `18_falta_orden`, `x := y :=`, `foo(1$ul)`, `Válido`, `Valido`. Lo que sí existe en el repo son documentos que los tratan como **inválidos**. Si la guía mencionada es externa o está en otra carpeta, debe incorporarse y corregirse antes de usarla para calificar.

## 4.1 Tema 17 — `:=` donde el enunciado exige `=`

### 4.1.1 Enunciado

TP02, “Temas 17 y 18: Asignaciones en expresiones”:

> Tema 17: Asignaciones de expresiones en expresiones  
> Se debe permitir asignar expresiones a los términos de expresiones aritméticas. Estas nuevas asignaciones utilizarán el símbolo `=`, y la expresión a asignar debe ir delimitada por paréntesis.

Ejemplo válido:

```text
a:=b=(2$l);
```

Ejemplo inválido:

```text
a:=b:=(2$l);
```

### 4.1.2 Código

`gramatica.y:503-509`:

```yacc
asignacion_en_expresion
    : ID '=' '(' expresion ')'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, $1.obj, $4.obj)); }
    | ID ASIG '(' expresion ')'
        { acciones.ejecutar(Reglas.ERR_ASIG_DONDE_IGUAL);
          $$ = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, $1.obj, $4.obj)); }
    ;
```

Producción incorrecta:

- reporta `ERR_ASIG_DONDE_IGUAL`;
- **pero igual devuelve una `AsignacionEnExpresion` válida desde el punto de vista del parser**.

`otras_acciones/AccionesExpresiones.java:164-202`:

- verifica destino, anidamiento, tipos y enumeración;
- si todo lo demás está bien, devuelve `AsignacionEnExpresion`;
- el error de `:=` ya fue reportado por la regla, pero no cambia el valor semántico.

`GeneradorTercetos.generar()` consume la reducción `ASIGNACION_EN_EXPRESION` y emite IR.

### 4.1.3 Reproducción

Caso: `test/tp2/generales/17_asig_donde_igual.txt`.

```text
prog
ulongint x, y;
begin
    x := y := (1$ul);
end
```

Resultado:

```text
Línea 4: Error: Uso de ':=' donde debe usarse '='
```

IR:

```text
1. (PROGRAMA, --, --)
2. (:=, y:main, 1$ul)
3. (:=, x:main, [2])
4. (FIN_PROGRAMA, --, --)
```

### 4.1.4 Documentación contradictoria

| Documento | Contenido |
|---|---|
| `docs/PROMPT-17.md:45` | `usa_asig_sin_anidamiento.txt`: “✅ Pasa (`Uso de ':=' donde debe usarse '='`)” |
| `docs/PROMPT-0.md:119` | `r17/usa_asig_sin_anidamiento.txt`: error `Uso de ':=' donde va '='`, 1 terceto, `Invalido` |

Ambos coinciden en que es inválido. Pero `PROMPT-0` espera 1 terceto y el compilador emite 2. Hay una segunda contradicción: expected result de IR vs. IR real.

### 4.1.5 Conclusión parcial

El parser **sí detecta** el error exigido por el tema 17. El problema está en:

1. la guía/expected results que lo trataría como válido;
2. el IR posterior, que convierte una construcción inválida en dos asignaciones reales.

## 4.2 Tema 19 — omisión del orden de evaluación

### 4.2.1 Enunciado

TP02, líneas 224-236:

> Tema 19: Orden de evaluación de parámetros reales obligatorio  
> El programador debe, para cada invocación, indicar el orden en el cual quiere que se evalúe y se realice la asignación de los parámetros reales a los formales.  
> Estructura: `ID ( <lista_parámetros_reales> ) [<lista_constantes>]`

Ejemplo:

```text
a := f(x,y,z, …) [1,2,3,...]
```

Omitir `[...]` es inválido para el tema 19. El tema 20 es el que lo hace opcional; no es el tema del grupo.

### 4.2.2 Código

`gramatica.y:516-523`:

```yacc
invocacion_funcion
    : ID '(' lista_parametros_reales ')' '[' lista_orden_evaluacion ']'
        { ... }
    | ID '(' lista_parametros_reales ')' '[' ']'
        { acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, $1.obj, $3.obj); }
    | ID '(' lista_parametros_reales ')'
        { acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, $1.obj, $3.obj); }
    ;
```

Igual patrón:

- error reportado;
- resultado semántico construido de todas formas.

`otras_acciones/AccionesExpresiones.java:231-253`:

```java
EntradaTabla resuelta = v.resolverFuncion(funcion, ctx.getLinea());
Entorno.FuncionDeclarada firma = resuelta == null ? null : entorno.getFuncion(resuelta.lexema);

List<ParametroReal> conFormales = asignarFormales(v, firma, parametros, ctx.getLinea());
boolean invocacionOk = v.verificarInvocacion(firma, conFormales, ordenRaw, ctx.getLinea());

LlamadaFuncion llamada = new LlamadaFuncion(
        resuelta != null ? resuelta : funcion,
        conFormales, ordenEvaluacion, ctx.getLinea(), firma, null);
if (firma == null || !invocacionOk) {
    llamada.marcarInvalida();
}
return llamada;
```

`Verificaciones.java:397-407` reporta falta de orden correctamente.

### 4.2.3 Reproducción

Caso: `test/tp2/generales/18_falta_orden.txt`.

```text
prog
ulongint x;
ulongint function foo(ulongint n)
begin
    ret(n);
end;
begin
    x := foo(1$ul);
end
```

Resultado:

```text
Línea 8: Error: Falta del orden de evaluacion y asignacion de parametros
```

IR:

```text
1. (PROGRAMA, --, --)
2. (FUNCION, foo, --)
3. (ret, n:main:foo, --)
4. (FIN_FUNCION, foo, --)
5. (:=, x:main, foo:main)
6. (FIN_PROGRAMA, --, --)
```

No hay `PAR` ni `CALL`. La asignación usa directamente el símbolo `foo:main` como valor. IR inválido desde el punto de vista del lenguaje.

### 4.2.4 Documentación relacionada

- `docs/PROMPT-19.md:20-22`: orden entre corchetes; permutación completa.
- `docs/PROMPT-19.md:103`: `orden_sin_corchetes.txt`, `sin_orden.txt` informan falta de orden obligatorio.
- `docs/PROMPT-0.md:121-122`: ambos casos marcados como `Invalido`.
- Casos relacionados:
  - `test/tp2/verificacion_r19_param_reales/orden_sin_corchetes.txt`
  - `test/tp2/verificacion_r19_param_reales/sin_orden.txt`

### 4.2.5 Conclusión parcial

El error sintáctico/semántico se reporta. Lo que no puede usarse para calificar es el resultado posterior:

- expected results que dan el programa por válido;
- IR que simula una asignación desde el nombre de la función;
- Guaná ausente en el repo; documentación correcta dispersa.

## 4.3 Conclusión TP02

Confirmar con el profesor o localizar fuera del repo la guía que marca estos dos casos como válidos. Antes de usar tests para calificar parser:

1. alinear expected results con TP02 temas 17 y 19;
2. separar “error detectado” de “IR emitido”;
3. decidir si casos inválidos deben tener IR nulo o IR parcial documentado;
4. corregir el conteo de tercetos de `PROMPT-0.md:119`.

---

# 5. TP03 — lista dinámica de tercetos vs. typedef con lista vacía

## 5.1 Requisito del enunciado

TP03, líneas 7-9:

> Generación de código intermedio para las sentencias ejecutables. Es requisito para la aprobación del trabajo, que el código intermedio sea almacenado en una estructura dinámica.

TP03, líneas 521-523:

> Los errores generados en cada una de las etapas […] se deberán mostrar todos juntos, indicando la descripción de cada error y la línea en la que fue detectado.

Tema 23, líneas 360-368:

- registrar tipo definido por usuario;
- chequear existencia del tipo;
- chequear pertenencia de constantes.

El enunciado no define explícitamente qué IR debe quedar tras un typedef vacío. Pero la documentación interna sí lo hace, y ahí aparece la contradicción.

## 5.2 Estructura dinámica real

`Terceto.java:104-112`:

```java
class ListaTercetos {
    private final List<Terceto> tercetos = new ArrayList<>();
    private int siguienteEtiqueta = 1;

    public int crear(String operador, Object op1, Object op2) {
        tercetos.add(new Terceto(operador, op1, op2));
        return tercetos.size();
    }
}
```

`Terceto.java:141-145`:

```java
public void descartarDesde(int cantidadConservada) {
    while (tercetos.size() > cantidadConservada && !tercetos.isEmpty()) {
        tercetos.remove(tercetos.size() - 1);
    }
}
```

La lista es dinámica. Existe mecanismo de descarte. El problema es **dónde se usa**.

## 5.3 Lo que dice el informe

`ASSETS/informe_generador_tercetos.md:98-143`:

- describe `ListaTercetos`;
- índices 1-based;
- metadata `ResultadoTerceto`;
- API `getLista()`, `getTercetos()`, `getResultados()`.

`ASSETS/informe_generador_tercetos.md:518-525`:

> `ListaTercetos.descartarDesde()` elimina tercetos, pero `GeneradorTercetos` no elimina de `resultados` las entradas correspondientes. Si se descarta una clase o el cuerpo de un `repeat`, pueden quedar metadatos de tercetos que ya no están en la lista.

`ASSETS/informe_generador_tercetos.md:228-239`:

- para clase inválida, `cerrarClase()` descarta tercetos desde marca;
- “evita que una clase que ya falló semánticamente deje código intermedio parcial”.

El informe prevé descarte para clases y repeat. **No prevé typedef vacío**. Ahí queda el diagnóstico pendiente que marca el profesor.

## 5.4 Flujo real para `typedef color = [];`

### 5.4.1 Gramática

`gramatica.y:284-289`:

```yacc
declaracion_typedef
    : TYPEDEF ID '=' '[' lista_valores_enumerado ']' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_TYPEDEF, $2.obj, $5.obj)); }
    | TYPEDEF ID '=' '[' ']' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_VALORES_ENUMERADO, $2.obj)); }
    ;
```

### 5.4.2 Reporte del error

No hay acción registrada para `ERR_FALTA_VALORES_ENUMERADO`. `TablaAcciones.java:35-41`:

```java
AccionRegla accion = acciones.get(regla);
if (accion == null) {
    if (regla.esError()) {
        reporte.error(linea, regla.getDescripcion());
    }
    resultado = valores != null && valores.length > 0 ? valores[0] : null;
}
```

`Reglas.java:115-118`:

```java
public boolean esError() {
    return name().startsWith("ERR_");
}
```

Mensaje:

```text
Ausencia de valores para la enumeracion
```

También existe validación semántica en `Verificaciones.java:486-490`:

```java
public String verificarValoresEnumerado(List<Object> valores, int linea) {
    if (valores == null || valores.isEmpty()) {
        reportar(Reglas.ERR_FALTA_VALORES_ENUMERADO, linea);
        return null;
    }
    ...
}
```

### 5.4.3 Generación de IR

`GeneradorTercetos.generar()` líneas 147-207:

- limpia `resultados`;
- emite `PROGRAMA`;
- recorre **todas** las reducciones;
- despacha por regla;
- emite `FIN_PROGRAMA`.

No consulta `reporte.hayErrores()`. No hay caso `ERR_FALTA_VALORES_ENUMERADO` en el `switch`.

`Compilador.java:82-107` genera IR y lo imprime siempre:

```java
GeneradorTercetos generador = new GeneradorTercetos(reporte);
generador.generar(tablaAcciones.getReducciones());
...
String intermedio = generador.contenido();
...
System.out.print(intermedio);
```

La compuerta de errores recién aparece en `generarArchivoAsm()` (`Compilador.java:141-144`).

## 5.5 Reproducción

Caso: `test/tp2/generales/21_enum_vacio.txt`.

```text
prog
typedef color = [];
begin
    pout({ok});
end
```

Resultado:

```text
Línea 2: Error: Ausencia de valores para la enumeracion
```

IR:

```text
1. (PROGRAMA, --, --)
2. (pout, ok, --)
3. (FIN_PROGRAMA, --, --)
```

## 5.6 Contradicción verificable

| Fuente | Expectativa |
|---|---|
| `docs/PROMPT-0.md:126` | `r23/enum_vacio.txt`: error + “Ninguno” tercetos, `Invalido` |
| `docs/PROMPT-23.md:101` | informa `Ausencia de valores para la enumeracion` |
| `ASSETS/informe_generador_tercetos.md:518-525` | lista dinámica con descarte disponible |
| Ejecución real | error + `(pout, ok, --)` conservado |

La lista dinámica existe, pero no se usa para eliminar el IR posterior a un typedef inválido. El informe describe descarte para clases/repeat; este caso queda fuera y produce IR contradictorio con el expected result.

## 5.7 Consecuencias

- ERROres reportados y código intermedio emitido conviven sin política clara.
- La salida TP3 mostrada al profesor parece contradecir el informe de lista dinámica.
- Si se llegara a escribir `.asm`, la compuerta actual lo evita; pero el IR impreso igual queda como evidencia del programa inválido.
- `ResultadoTerceto` y `ListaTercetos` pueden quedar desincronizados si se empiezan a agregar descartes sin limpiar metadata, tal como el informe ya advierte.

---

# 6. TP04 — funciones, métodos y factorial

## 6.1 Requisito del enunciado

TP04, líneas 7-9:

> Generar código Assembler, a partir del código intermedio generado en el Trabajo Práctico Nº 3.  
> El mecanismo a utilizar para la generación de la salida será el de variables auxiliares.  
> El código Assembler generado debe poder ser ensamblado y ejecutado sin errores.

El profesor reportó fallos de ejecución en MASM32 en programas con funciones y métodos, incluido factorial, y pidió confirmar con el ASM generado la hipótesis de entrada anticipada a subrutina.

**Confirmación: la hipótesis es correcta.**

## 6.2 Cómo se arma hoy el `.code`

`GeneradorAssembler.emitirCodigo()` líneas 459-553:

```java
salida.append(".code\n");
salida.append("start:\n");

for (int i = 0; i < tercetos.size(); i++) {
    Terceto t = tercetos.get(i);
    ...
    switch (op) {
        case "FUNCION":
        case "METODO":
            emitirPrologo(numero, op, op1);
            break;
        case "FIN_FUNCION":
        case "FIN_METODO":
            emitirFinFuncion(numero);
            break;
        ...
    }
}
```

No existe:

- una partición `main` / funciones;
- un `jmp` inicial que salte los cuerpos de funciones;
- un bloque de funciones ubicado después del `ExitProcess` del main;
- una tabla de etiquetas con salto de entrada.

Los marcadores `FUNCION`, `METODO`, `FIN_FUNCION`, `FIN_METODO` son hitos de layout que el assembler traduce **en el punto donde aparecen en el IR**.

## 6.3 Evidencia ASM — función simple

Fuente: `test_tp_4/Funciones_y_metodos/01_funcion_ulongint.txt`.

```text
prog
ulongint function suma(ulongint a, ulongint b)
begin
    ret(a + b);
end;
ulongint x, y, r;
begin
    x := 3$ul;
    y := 4$ul;
    r := suma(x, y)[1$ul, 2$ul];
    pout(r);
end
```

ASM generado:

```asm
.code
start:
fn_suma_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    add eax, dword ptr [ebp+12]
    jc error_overflow_suma
    mov @aux3, eax
    mov eax, @aux3
    leave
    ret
fin_suma_main:
    leave
    ret
    mov eax, c_3_ul_ul
    mov x_main, eax
    ...
    call fn_suma_main
    add esp, 8
    ...
    invoke ExitProcess, 0
end start
```

### 6.3.1 Flujo de ejecución real

1. Windows/MASM entra por `start:`.
2. `start:` no emite instrucción; la etiqueta cae directamente en `fn_suma_main:`.
3. Se ejecuta `push ebp`, `mov ebp, esp`.
4. No hubo `CALL`, por lo tanto `[ebp+8]` y `[ebp+12]` no son parámetros: son datos del stack de entrada del proceso.
5. `add` opera sobre basura.
6. `jc error_overflow_suma` puede disparar un falso overflow.
7. `leave`, `ret` devuelve a una dirección que no fue empujada por un `call` del programa.
8. El main puede no ejecutarse nunca.

Esto explica fallos de ejecución aunque el texto ASM sea sintácticamente ensamblable.

## 6.4 Evidencia ASM — factorial

Fuente: `test_tp_4/Integracion/03_recursion_factorial_metodo.txt`.

ASM generado:

```asm
.code
start:
fn_fact_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    cmp eax, c_1_ul_ul
    setbe al
    movzx eax, al
    mov @aux3, eax
    mov eax, dword ptr [ebp+8]
    cmp eax, c_1_ul_ul
    ja label_1
    mov eax, c_1_ul_ul
    leave
    ret
    jmp label_2
label_1:
label_2:
fin_fact_main:
    leave
    ret
mt_escala_main_calculadora:
    push ebp
    mov ebp, esp
    ...
```

Dos problemas simultáneos:

1. `start:` cae en `fn_fact_main:` sin frame válido.
2. `label_1:` y `label_2:` quedan vacíos: falta la rama recursiva completa.

## 6.5 Origen del doble `leave`/`ret`

`emitirRet()` líneas 1507-1521 emite valor de retorno y cierra frame:

```java
salida.append("    mov eax, ").append(valor).append("\n");
...
salida.append("    leave\n");
salida.append("    ret\n");
```

`emitirFinFuncion()` líneas 1496-1505 emite otra etiqueta y otro cierre:

```java
salida.append("fin_").append(sufijo).append(":\n");
salida.append("    leave\n");
salida.append("    ret\n");
```

Resultado:

```asm
    mov eax, c_1_ul_ul
    leave
    ret
    jmp label_2
label_1:
label_2:
fin_fact_main:
    leave
    ret
```

El segundo `leave/ret` es código muerto en el flujo normal, pero confirma que los marcadores de cierre no separan bloques: solo insertan una etiqueta y repiten epílogo.

## 6.6 Salto `fin_normal`

`GeneradorAssembler.java:562-566`:

```java
boolean hayChequeosRuntime = usaMsgOverflowSuma || usaMsgOverflowFloat
        || usaMsgNegativoResta || usaMsgEnumInvalido;
if (hayChequeosRuntime) {
    salida.append("    jmp fin_normal\n");
}
```

`GeneradorAssembler.java:588-591`:

```java
if (hayChequeosRuntime) {
    salida.append("fin_normal:\n");
}
```

Consecuencias:

- en `01_funcion_ulongint.asm` aparece `jmp fin_normal` y `fin_normal:` porque hay `overflow_suma`;
- en `03_recursion_factorial_metodo.asm` no aparece ese par porque no se emite ningún mensaje de error runtime;
- aun cuando aparece, el `jmp` está al final de todo el código, no después del main. No soluciona que `start:` haya entrado primero en una subrutina.

## 6.7 Caso `doublef`

Fuente: `test_tp_4/Funciones_y_metodos/02_funcion_doublef.asm`.

ASM relevante:

```asm
start:
fn_escala_main:
    push ebp
    mov ebp, esp
    fld qword ptr [ebp+8]
    fmul qword ptr [ebp+16]
    fstp @aux3
    fld @aux3
    fxam
    fstsw ax
    fstp st(0)
    sahf
    jc error_overflow_float
    fld @aux3
    leave
    ret
```

Para una función con dos `doublef`, `[ebp+8]` y `[ebp+16]` son correctos **si hubo CALL**. Sin CALL, el coprocesador clasifica bytes arbitrarios. `FXAM` puede marcar condición de no normal/NaN e inducir falso `error_overflow_float`.

Otra vez: el fallo no está en la instrucción FPU aislada, sino en la ubicación del prólogo respecto de `start:`.

## 6.8 Recursión: IR actual y causa raíz

### 6.8.1 IR observado

Para `03_recursion_factorial_metodo.txt`:

```text
1. (PROGRAMA, --, --)
2. (FUNCION, fact, --)
3. (<=, n:main:fact, 1$ul)
4. (BF, [3], label_1)
5. (ret, 1$ul, --)
6. (BI, label_2, --)
7. (label_1, --, --)
8. (label_2, --, --)
9. (FIN_FUNCION, fact, --)
...
16. (PAR, n:main:fact, 5$ul)
17. (CALL, fact:main, 1)
```

Dentro de `fact` no hay:

```text
(PAR, n:main:fact, ...)
(CALL, fact:main, 1)
```

La llamada recursiva desaparece.

### 6.8.2 Código responsable

`Entorno.java:165-179`:

```java
// Busca por clave completa nombre:ambito, no por nombre pelado.
// Durante el cuerpo de una funcion todavia no fue cerrada (cerrarFuncion
// la inserta en 'funciones'), asi que se consulta tambien la pila: eso
// permite la recursion directa y mantiene la firma visible al resolver
// la invocacion.
...
public FuncionDeclarada getFuncion(String clave) {
    if (clave == null) {
        return null;
    }
    return funciones.get(clave);
}
```

El comentario documenta fallback a `pilaFunciones`. El código no lo implementa.

Cadena de consecuencias:

1. `AccionesExpresiones.java:240`:
   ```java
   Entorno.FuncionDeclarada firma = resuelta == null ? null : entorno.getFuncion(resuelta.lexema);
   ```
2. Como `fact` está abierta, no está en `funciones`; `firma = null`.
3. `AccionesExpresiones.java:249-251` marca la llamada inválida.
4. `Verificaciones.java:352-354` y `432-435` devuelven `false` sin reportar cuando `firma == null`.
5. `generarInvocacion()` no emite `PAR`/`CALL`.
6. ASM queda con labels vacíos.

### 6.8.3 Falta de diagnóstico

`ASSETS/recursion.md:53-54` afirma:

> La invocación se marca como inválida y se informa el diagnóstico de recursión no soportada.

`ASSETS/DIAGNOSTICO_FUNCIONES_METODOS.md:390-394` afirma que existe:

```text
Recursion no soportada: la funcion no puede invocarse a si misma
```

Búsqueda real:

```bash
grep -rn "Recursion no soportada" --include=*.java .
```

Sin resultados.

Salida real del factorial tampoco muestra error. Hay silencio semántico. Documentación y ejecución contradicen.

### 6.8.4 Confirmación experimental en `/tmp`

Se copió el proyecto a `/tmp/opencode/tp3patched/` y se restauró solo el fallback documentado en `getFuncion()`:

```java
FuncionDeclarada cerrada = funciones.get(clave);
if (cerrada != null) return cerrada;
for (FuncionDeclarada abierta : pilaFunciones) {
    if (clave.equals(abierta.clave())) return abierta;
}
return null;
```

IR resultante:

```text
8. (-, n:main:fact, 1$ul)
9. (PAR, n:main:fact, [8])
10. (CALL, fact:main, 1)
11. (*, n:main:fact, [10])
12. (ret, [11], --)
13. (label_2, --, --)
14. (FIN_FUNCION, fact, --)
```

ASM resultante dentro de `fn_fact_main`:

```asm
label_1:
    mov eax, dword ptr [ebp+8]
    sub eax, c_1_ul_ul
    jb error_negativo_resta
    mov @aux8, eax
    ; (PAR, n:main:fact, [8])
    sub esp, 4
    mov eax, @aux8
    mov dword ptr [esp+0], eax
    call fn_fact_main
    add esp, 4
    mov @aux10, eax
    mov eax, dword ptr [ebp+8]
    imul eax, @aux10
    mov @aux11, eax
    mov eax, @aux11
    leave
    ret
```

Esto confirma que la causa de la desaparición de la recursión es `getFuncion()`, no `GeneradorTercetos` ni `GeneradorAssembler`.

No se aplicó este cambio al repositorio.

### 6.8.5 Contexto de chequeos

La recursión es el chequeo runtime `i` del TP04. El grupo 43 tiene `b, e, f, h`; por lo tanto no corresponde instrumentar “control de recursión” como chequeo asignado. Pero el test de factorial sigue incluido y hoy:

- no ejecuta recursión;
- no reporta error;
- entra igual en subrutina antes del main.

Es un caso inválido según el diseño vigente, presentado como prueba positiva.

## 6.9 Llamadas normales sí tienen convención coherente

Cuando la función ya está cerrada, la convención es correcta:

- `emitirPrologo()` (`GeneradorAssembler.java:1474-1494`): `push ebp`, `mov ebp, esp`, `sub esp, bytes` si hay locales.
- `emitirPar()` (1523-1528): solo registra el `PAR` pendiente.
- `emitirLlamada()` (1530-1628):
  - calcula slots por formal;
  - reserva bytes con `sub esp`;
  - copia argumentos;
  - emite `call etiqueta`;
  - limpia con `add esp`;
  - guarda retorno en `@auxN`.
- `emitirRet()` devuelve `EAX` para enteros o `ST(0)` para flotantes.

El problema no es la convención de llamada en sí. Es que el flujo inicial llega a los prólogos sin pasar por `emitirLlamada()`.

## 6.10 Clases y archivos involucrados (análisis original)

| Archivo | Responsabilidad en el fallo observado entonces |
|---|---|
| `GeneradorAssembler.java` | Emitía `start:` y todo el IR lineal; prólogos quedaban antes del main. Corregido por P4-01. |
| `Entorno.java` | `getFuncion()` no consulta `pilaFunciones`; descarta la llamada abierta silenciosamente. Estado final, fuera de alcance desde P6-01. |
| `AccionesExpresiones.java` | Marca invocación inválida cuando `firma == null`. |
| `Verificaciones.java` | `firma == null` → `false` sin mensaje. Sigue siendo el comportamiento final. |
| `GeneradorTercetos.java` | No emite `CALL` para llamadas inválidas; conserva labels vacíos. |
| `ASSETS/recursion.md` | Antes de P6-01 documentaba un diagnóstico inexistente; corregido. |
| `ASSETS/DIAGNOSTICO_FUNCIONES_METODOS.md` | Repetía el diagnóstico inexistente; corregido en P6-01. |

---

# 7. Puntos débiles transversales

## 7.1 Errores vs. IR

El pipeline separa mal “hubo error” de “no generar código”:

- `Reporte` acumula errores;
- `TablaAcciones` reporta y sigue;
- `GeneradorTercetos` consume todas las reducciones;
- `Compilador` imprime IR;
- solo `generarArchivoAsm()` aplica compuerta.

Esto explica TP01, TP02 y TP03.

## 7.2 Documentación desactualizada (estado al momento del diagnóstico)

- `ASSETS/recursion.md`: antes prometía un diagnóstico de recursión no soportada; P6-01 lo corrigió al descarte silencioso.
- `ASSETS/DIAGNOSTICO_FUNCIONES_METODOS.md`: mismo problema, corregido en P6-01.
- `ASSETS/informe_generador_tercetos.md`: describía descarte dinámico pero no cubría `typedef` vacío; TP03 quedó sin cambios por decisión P0-02.
- `docs/PROMPT-0.md:119`: la cuenta vieja quedó corregida en P2-01/P2-07; la fila de `enum_vacio` permanece intacta.
- Posible guía externa TP02: no localizada en repo.

## 7.3 Tests positivos que ya no son positivos

`test_tp_4/Integracion/03_recursion_factorial_metodo.txt` sigue afirmando en su comentario:

```text
Verifica call recursivo con @auxN, frame con EBP y limpieza caller.
```

Hoy no verifica eso. No hay `call` recursivo.

## 7.4 Estado interno del generador

`ASSETS/informe_generador_tercetos.md:510-525` ya advierte:

- `generar()` limpia `resultados`, pero no reinicia lista, pilas ni contador de etiquetas;
- `descartarDesde()` no limpia metadata de `resultados`.

No es la causa directa de las cuatro observaciones, pero agrava cualquier política futura de descarte por error.

---

# 8. Apéndice — comandos reproducibles

## 8.1 Compilar

```bash
mkdir -p /tmp/opencode/tp3build
javac -encoding UTF-8 -d /tmp/opencode/tp3build \
      *.java "acciones semanticas"/*.java otras_acciones/*.java
```

## 8.2 TP01

```bash
mkdir -p /tmp/opencode/lexprobe
cat > /tmp/opencode/lexprobe/b_rhs.txt <<'EOF'
prog
ulongint x, id, malo;
begin
    x := id#malo + 1$ul;
end
EOF
java -cp /tmp/opencode/tp3build Main /tmp/opencode/lexprobe/b_rhs.txt
```

Salida clave:

```text
Línea 4: Error: Lexema no reconocido ''
2. (:=, x:main, id:main)
```

## 8.3 TP02

```bash
cp test/tp2/generales/17_asig_donde_igual.txt /tmp/opencode/tp3run/
cp test/tp2/generales/18_falta_orden.txt /tmp/opencode/tp3run/
java -cp /tmp/opencode/tp3build Main /tmp/opencode/tp3run/17_asig_donde_igual.txt
java -cp /tmp/opencode/tp3build Main /tmp/opencode/tp3run/18_falta_orden.txt
```

Salidas clave:

```text
Uso de ':=' donde debe usarse '='
(:=, y:main, 1$ul)
(:=, x:main, [2])

Falta del orden de evaluacion y asignacion de parametros
(:=, x:main, foo:main)
```

## 8.4 TP03

```bash
cp test/tp2/generales/21_enum_vacio.txt /tmp/opencode/tp3run/
java -cp /tmp/opencode/tp3build Main /tmp/opencode/tp3run/21_enum_vacio.txt
```

Salida clave:

```text
Línea 2: Error: Ausencia de valores para la enumeracion
2. (pout, ok, --)
```

## 8.5 TP04

```bash
mkdir -p /tmp/opencode/tp3run
cp test_tp_4/Funciones_y_metodos/01_funcion_ulongint.txt /tmp/opencode/tp3run/
cp test_tp_4/Integracion/03_recursion_factorial_metodo.txt /tmp/opencode/tp3run/
java -cp /tmp/opencode/tp3build Main /tmp/opencode/tp3run/01_funcion_ulongint.txt
java -cp /tmp/opencode/tp3build Main /tmp/opencode/tp3run/03_recursion_factorial_metodo.txt
sed -n '18,60p' /tmp/opencode/tp3run/01_funcion_ulongint.asm
sed -n '18,60p' /tmp/opencode/tp3run/03_recursion_factorial_metodo.asm
```

Salida clave:

```asm
start:
fn_suma_main:
    push ebp
    mov ebp, esp
```

```asm
start:
fn_fact_main:
    push ebp
    mov ebp, esp
```

## 8.6 Búsqueda de la guía TP02

```bash
grep -rniE 'asig_donde_igual|18_falta_orden|x := y :=|foo\(1\$ul\)' \
  --include='*.md' --include='*.txt' --include='*.y' --include='*.java' --include='*.sh' .
```

No aparece guía que marque ambos casos como válidos.

---

# 9. Conclusiones

1. **TP04 tenía una falla grave de layout confirmada.** El ASM entraba en la subrutina apenas arrancaba `start:`; P4-01 la corrigió con `jmp main_inicio`.
2. **Factorial mostró dos conductas distintas.** La entrada anticipada quedó corregida en P4-01; la llamada recursiva se descarta silenciosamente y la recursión quedó fuera de alcance en P6-01.
3. **TP01 no reconocía `id#malo`.** P1-01 fijó el estado léxico `19` y P1-02 conservó el lexema completo.
4. **TP02 no era un bug de detección.** El error se reportaba; P2-01/P2-05 anularon los constructos inválidos y P2-07 verificó el IR resultante.
5. **TP03 muestra una contradicción documental aceptada.** La lista dinámica existe, pero `21_enum_vacio.txt` conserva IR posterior; P0-02 decidió no cambiar ese caso.
6. **La documentación quedó alineada en P6-01:** sin diagnóstico de recursión inexistente; el conteo de tercetos de `PROMPT-0.md:119` ya era correcto; `enum_vacio` intacto.
7. **Este archivo no implementa correcciones.** Las corridas P0–P4 y la auditoría P6-01 registran por separado los cambios y decisiones finales.
