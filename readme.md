# Compilador

## Descripción general

Compilador desarrollado para la materia Compiladores e Intérpretes, de la carrera Ingeniería en Sistemas de la Facultad de Ciencias Exactas de la UNICEN.

El programa lee un archivo fuente del lenguaje definido en los trabajos prácticos de la cursada 2026 y encadena el analizador léxico del TP1 con el analizador sintáctico del TP2. El léxico reconoce los tokens con una matriz de transiciones. El sintáctico es un parser LALR(1) generado con BYACC/J a partir de la gramática del grupo. La salida por consola informa los tokens detectados, las estructuras sintácticas, los errores y el contenido de la tabla de símbolos.

Los temas particulares del grupo son los enteros `ulongint`, los reales `doublef`, las cadenas de una línea, los comentarios multilínea, la sentencia `repeat`/`while`, la asignación de expresiones dentro de expresiones, el orden de evaluación de los parámetros, las enumeraciones, las clases, el acceso a atributos con punto, la herencia múltiple con prefijo y la conversión explícita `todf`.

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
- `test/tp2/` cubre el analizador sintáctico. En `generales/` hay un programa válido y los errores sintácticos previstos, como la falta de nombre de programa, de `begin`, de `end`, de `;` o de paréntesis. El resto agrupa la estructura de `if`/`begin`/`end` y las verificaciones de los temas del grupo: `repeat`/`while`, asignación en expresiones, orden de evaluación, enumeraciones, atributos y herencia, y la conversión `todf`.

Cualquier archivo de esa carpeta se puede pasar como argumento. Por ejemplo:

```bash
java Main test/tp1/verificacion_constantes_ulongint/limites_validos.txt
java Main test/tp2/verificacion_r13_estructura_while/una_sentencia.txt
```
