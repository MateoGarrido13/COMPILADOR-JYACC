# Compilador

## Proyecto

Compilador desarrollado para la materia Compiladores e Intérpretes, de la carrera Ingeniería en Sistemas de la Facultad de Ciencias Exactas de la UNICEN.

- **Grupo:** 43
- **Integrantes:** Ciro Gervasio, Rafael Fredes, Mateo Garrido
- **Cursada:** 2026

El programa lee un archivo fuente del lenguaje definido en los trabajos prácticos de la cursada y encadena el analizador léxico del TP1 con el analizador sintáctico del TP2, seguido del análisis semántico, la generación de código intermedio y la generación de código assembler. La salida por consola comienza con el código fuente con las líneas numeradas (`N: <línea>`) e informa los tokens detectados, las estructuras sintácticas, los errores y el contenido de la tabla de símbolos.

Los temas particulares del grupo son los enteros `ulongint`, los reales `doublef`, las cadenas de una línea, los comentarios multilínea, la sentencia `repeat`/`while`, la asignación de expresiones dentro de expresiones, el orden de evaluación de los parámetros, las enumeraciones, las clases, el acceso a atributos con punto, la herencia múltiple con prefijo y la conversión explícita `todf`.

## Fases del compilador

El compilador se organiza en las fases clásicas de un compilador, cada una implementada en sus propias clases:

### 1. Análisis léxico

`AnalizadorLexico` reconoce los tokens del archivo fuente utilizando un autómata de **matriz de transiciones** (`MatrizTransiciones`). Lee el archivo carácter a carácter mediante `LectorFuente`, registra los tokens en `RegistroTokens` y los guarda en la `TablaSimbolos`. Reconoce constantes `ulongint` y `doublef` (dentro y fuera de rango), identificadores, palabras reservadas, cadenas de una línea y comentarios multilínea.

### 2. Análisis sintáctico

El sintáctico es un **parser LALR(1)** generado con **BYACC/J** a partir de la gramática del grupo (`gramatica.y`), ya pregenerado en `Parser.java`. No hace falta instalar BYACC/J para ejecutar el proyecto. El parser consume los tokens del léxico, construye el árbol sintáctico implícito mediante reducciones y detecta errores sintácticos (falta de `begin`/`end`, `;`, paréntesis, nombre de programa, etc.).

### 3. Análisis semántico

`Verificaciones` y `AccionesSemanticas` realizan los chequeos que necesitan contexto: alcance de identificadores (`ControlAmbito`), declaración previa de variables y funciones, tipos de asignación, enumeraciones válidas, conversión explícita `todf`, orden de evaluación de parámetros y atributos/herencia de clases. Los errores se reportan a través de `Reporte` como canal único de las producciones `ERR_*`.

### 4. Generación de código intermedio

`GeneradorTercetos` recorre las reducciones del parser y emite **tercetos** (instrucciones de tres direcciones) almacenados en `ListaTercetos` (clase interna de `Terceto.java`). Gestiona saltos pendientes (`BF` para condicionales falsos y `BI` para saltos incondicionales), etiquetas `label_N` para `if`/`else` y `repeat`/`while`, y el plegado de expresiones a izquierda por nivel de precedencia.


## Requisitos para correrlo

Hace falta un JDK, con `javac` y `java`. El proyecto se probó con OpenJDK 21. No usa librerías externas: el parser ya está generado en `Parser.java`, así que no hace falta instalar BYACC/J para ejecutarlo.

Los archivos `.class` no están en el repositorio. Quien clone el proyecto los genera al compilar.

## Cómo correrlo

Desde la raíz del proyecto:

```bash
javac -encoding UTF-8 -d . *.java "acciones semanticas"/*.java otras_acciones/*.java
java Main test/tp2/generales/00_programa_valido.txt
```

El argumento de `java Main` es la ruta del archivo fuente a compilar. Si no se pasa ninguna, el programa busca `input.txt` en la raíz.

El repositorio incluye la carpeta `test`, con casos de prueba en archivos de texto:

- `test/tp1/` cubre el analizador léxico: constantes `ulongint` y `doublef` (dentro y fuera de rango, y los formatos del real), identificadores, palabras reservadas, cadenas y comentarios, tanto válidos como inválidos.
- `test/tp2/generales/` cubre el analizador sintáctico: un programa válido y los errores sintácticos previstos, como la falta de nombre de programa, de `begin`, de `end`, de `;` o de paréntesis.
- `test/semantica/` agrupa las verificaciones semánticas por área: `estructuras/` (`if`/`begin`/`end`, `repeat`/`while`), `asignaciones/` (asignación en expresiones, unarios, bordes de rango), `atributos_herencia/` (atributos, herencia múltiple, ámbito), `enums/` (enumeraciones), `conversion/` (conversión explícita `todf`) y `parametros/` (orden de evaluación de parámetros).
- `test/e2e/` contiene pruebas end-to-end con programas más extensos: `operaciones_aritmeticas.txt` (varias operaciones aritméticas con `ulongint` y `doublef`), `formatos_doublef.txt` (asignaciones y operaciones con todos los formatos de constante `doublef`, incluidos exponentes `d+` y `d-`), `herencia_atributos_doublef.txt` (jerarquía de herencia, atributos, conversión `todf` y suma de tipos distintos), `programa_extenso_300.txt` (~300 líneas con operaciones aritméticas, conversiones, impresiones y clases con herencia) y `comentario_extenso.txt` (mismo programa dentro de un comentario multilínea `{{ ... }}`).
- `test/chequeos_ejecucion/` contiene pruebas de chequeos en tiempo de ejecución con múltiples casos por categoría: `overflow_suma_enteros_*` (overflow de suma de enteros: sin overflow, en límite, literal desborda, variables desbordan, encadenada), `resta_enteros_*` (resta de enteros: sin resultado negativo, resultado cero, resultado negativo, desde variables, encadenada), `overflow_producto_doublef_*` (overflow por producto de doublef: finito, grande finito, desborda, máximo por dos) y `asignacion_enum_*` (asignación de valor fuera de rango a variable enum: válido, inválido, doublef válido, doublef inválido).

Cualquier archivo de esas carpetas se puede pasar como argumento. Por ejemplo:

```bash
java Main test/tp1/verificacion_constantes_ulongint/limites_validos.txt
java Main test/semantica/estructuras/una_sentencia.txt
```


## Guía de pruebas

El archivo `guia.md` en la raíz del repositorio detalla, para cada regla semántica asignada (R13, R17, R19, R23, R25, R28, R31, R33) y cada chequeo de ejecución (b, e, f, h), las reglas de testing específicas y las rutas exactas de los archivos de prueba que las implementan.

## Pendiente

**Errores no abarcados en pruebas de ejecución (MASM/Windows):**

Los casos de prueba que involucran declaraciones de funciones y métodos (ubicados en `test_tp_4/Funciones_y_metodos/` y, en particular, `test_tp_4/Integracion/03_recursion_factorial_metodo.txt`) presentan fallos en tiempo de ejecución o salidas inesperadas. La causa es una limitación en la ordenación de la salida generada dentro de la sección `.code` del archivo Assembler: las funciones y métodos se declaran antes del bloque principal, por lo que sus tercetos quedan al inicio de la estructura intermedia. Al traducirlos de forma secuencial sin reordenar los bloques delimitados por los operadores `FUNCION` o `METODO` ni insertar una bifurcación inicial hacia el cuerpo principal, el punto de entrada del ejecutable ingresa directamente al prólogo de la primera subrutina definida. En consecuencia, el marco de pila `call stack` se inicializa con los registros `EBP` y `ESP` del programa principal sin que haya mediado una instrucción `CALL` que apile una dirección de retorno válida, provocando el desequilibrio de la pila al alcanzar el retorno de la función.

**Solución arquitectónica propuesta:** emitir el bloque de sentencias ejecutables de `main` en primer término (o anteponer un salto incondicional al punto de entrada principal) y relegar las definiciones de subrutinas al final de la sección de código, garantizando que cada invocación construya y destruya su propio stack frame mediante `CALL` y `RET`.

**Documentación incompleta:**

Debido a la priorización en la resolución de chequeos semánticos, manejo de clases, generación de código intermedio y controles en tiempo de ejecución, no se completó con el nivel de profundidad deseado la documentación de ciertas secciones transversales de la segunda entrega: el detalle exhaustivo de la traducción a Assembler, el esquema de name mangling, la sistematización de los casos de prueba y las conclusiones finales. Estos quedan identificados como los principales puntos de mejora para el cierre integral del compilador.
