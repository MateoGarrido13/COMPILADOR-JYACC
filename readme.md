# Compilador

## Proyecto

Compilador desarrollado para la materia Compiladores e Intérpretes, de la carrera Ingeniería en Sistemas de la Facultad de Ciencias Exactas de la UNICEN.

- **Grupo:** 43
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

`GeneradorTercetos` recorre las reducciones del parser y emite **tercetos** (instrucciones de tres direcciones) almacenados en `ListaTercetos`. Gestiona saltos pendientes (`BF` para condicionales falsos y `BI` para saltos incondicionales), etiquetas `label_N` para `if`/`else` y `repeat`/`while`, y el plegado de expresiones a izquierda por nivel de precedencia.

### 5. Generación de código assembler

`GeneradorAssembler` genera código **assembler MASM/x86 para Windows** a partir de los tercetos. Emite instrucciones FPU para aritmética de punto flotante, instrucciones enteras para el resto, labels de error en tiempo de ejecución (`error_overflow_suma`, `error_overflow_float`, `error_enum_invalido`) y llamadas a `crt_printf` / `ExitProcess` para la salida por consola y la finalización del programa.

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
- `test/chequeos_ejecucion/` contiene pruebas de chequeos en tiempo de ejecución: `overflow_suma_enteros.txt` (overflow de suma de enteros), `resta_enteros_negativo.txt` (resta con resultado negativo), `overflow_producto_doublef.txt` (overflow por producto de doublef) y `asignacion_enum_invalida.txt` (asignación de valor fuera de rango a variable enum).

Cualquier archivo de esas carpetas se puede pasar como argumento. Por ejemplo:

```bash
java Main test/tp1/verificacion_constantes_ulongint/limites_validos.txt
java Main test/semantica/estructuras/una_sentencia.txt
```
