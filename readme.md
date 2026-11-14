# Compilador - Grupo 43

Compilador desarrollado para la materia Compiladores e Intérpretes, de la carrera Ingeniería en Sistemas de la Facultad de Ciencias Exactas de la UNICEN.

El programa lee un archivo fuente del lenguaje definido en los trabajos prácticos de la cursada 2026 y encadena el analizador léxico del TP1 con el analizador sintáctico del TP2. El léxico reconoce los tokens con una matriz de transiciones. El sintáctico es un parser LALR(1) generado con BYACC/J a partir de la gramática del grupo. La salida por consola comienza con el código fuente con las líneas numeradas (`N: <línea>`) e informa los tokens detectados, las estructuras sintácticas, los errores y el contenido de la tabla de símbolos.

Los temas particulares del grupo son los enteros `ulongint`, los reales `doublef`, las cadenas de una línea, los comentarios multilínea, la sentencia `repeat`/`while`, la asignación de expresiones dentro de expresiones, el orden de evaluación de los parámetros, las enumeraciones, las clases, el acceso a atributos con punto, la herencia múltiple con prefijo y la conversión explícita `todf`.

---

## Estado actual del proyecto

| Campo | Valor |
|-------|-------|
| Rama | `SEMANTICAStp03` |
| Último commit | `746c60d` — generacion de codigo base |
| Prompts ejecutados | 0, 1A, 1B, 2, 2B, 6 y 8, 9 y 16, 13 |
| Prompt en curso / siguiente | **17 — Asignación dentro de expresiones** |
| Commits del plan completados | 8 de 15 |

### Reglas de granularidad para commits

Cada commit del plan debe cumplir, salvo excepciones justificadas:

- **Máximo 200 líneas modificadas** por commit (suma de inserciones y borrados).
- **Máximo 10 archivos modificados** por commit.
- Cada commit debe ser reviewable de forma independiente (tests y docs junto al código).

Cuando un prompt supere esos límites, se divide en commits más pequeños manteniendo el orden del plan.

---

## Plan de prompts — progreso

| # | Prompt | Estado | Archivo de decisiones | Tests |
|---|--------|--------|----------------------|-------|
| 0 | Baseline reproducible | ✅ Ejecutado | `docs/PROMPT-0.md` | 65 casos de regresión |
| 1A | Base de Name Mangling | ✅ Ejecutado | `docs/PROMPT-1A.md` | `prompt-1a-ambitos/` |
| 1B | Apertura y cierre real de ámbitos | ✅ Ejecutado | `docs/PROMPT-1B.md` | `prompt-1b-name-mangling/` |
| 2 | Igualdad estricta de tipos | ✅ Ejecutado | `docs/PROMPT-2.md` | `prompt-2-tipos-estrictos/` |
| 2B | Codegen base (tercetos, `pout`, `ret`, `todf`, `PAR`/`CALL`/`CALLMET`) | ✅ Ejecutado | `docs/PROMPT-2B.md` | `prompt-2b-codegen-base/` |
| **6 y 8** | **Constantes `ulongint` y `doublef` — tipado en el léxico** | ✅ Ejecutado | `docs/PROMPT-6-8.md` | `test/tp1/verificacion_constantes_*` |
| 9 y 16 | Cadenas y comentarios | ✅ **Ejecutado (validado por lexico)** | `docs/PROMPT-9-16.md` | `test/tp1/verificaciones_comentarios_cadenas/` |
| 13 | `repeat while` — condiciones booleanas y tipo | ✅ **Ejecutado** | `docs/PROMPT-13.md` | `test/TP3 _y_TP4/ITERACION/` |
| 17 | Asignación dentro de expresiones | ⏳ **Siguiente** | `docs/PROMPT-17.md` (por crear) | `test/tp2/verificacion_r17_asig_expresiones/` |
| 33 | Conversión `todf` — restricción de origen | ❌ Pendiente | `docs/PROMPT-33.md` (por crear) | `test/tp2/verificacion_r33_conversion/` |
| 19 | Parámetros reales y orden | ❌ Pendiente | `docs/PROMPT-19.md` (por crear) | `test/tp2/verificacion_r19_param_reales/` |
| 23 | Enumeraciones | ❌ Pendiente | `docs/PROMPT-23.md` (por crear) | `test/tp2/verificacion_r23_enums/` |
| 25 | Objetos y asignación estructural | ❌ Pendiente | `docs/PROMPT-25.md` (por crear) | `test/tp2/verificacion_r28_r31_atributos/` |
| 28 | Acceso tradicional a atributos | ❌ Pendiente | `docs/PROMPT-28.md` (por crear) | `test/tp2/verificacion_r28_r31_atributos/` |
| 31 | Herencia múltiple con prefijo (D3 revisada) | ❌ Pendiente | `docs/PROMPT-31.md` (por crear) | `test/tp2/verificacion_r28_r31_atributos/` |

### Próximos a implementar (orden del plan)

1. **Prompt 17 — Asignación dentro de expresiones** (siguiente inmediato): generar asignación interna y chequear tipos.
2. **Prompt 33 — Conversión `todf`**: restringir el origen a `ulongint` o enum con base `ulongint`.
3. **Prompt 19 — Parámetros reales y orden**: chequear tipos de parámetros reales contra formales.

---

## Issues abiertas

Las siguientes issues se desprenden del plan de prompts y de `docs/SEMANTICAS_FALTANTES.md`:

| Issue | Prompt | Descripción | Prioridad |
|-------|--------|-------------|-----------|
| Tipado de constantes en el léxico | 6 y 8 | `AccionConstanteEntera` y `AccionConstanteFloat` ahora insertan las constantes con `tipoDato` (`ulongint` o `doublef`) y `uso = CONSTANTE` durante el análisis léxico, usando `insertarConstante`. Resuelto. | Alta (completado) |
| Validación de cadenas y comentarios | 9 y 16 | `AccionCadena.java` ya inserta cadenas con tipo `cadena`, no permite saltos de línea y descarta comentarios `{{...}}`. Validado por el lexico. | Media (completado) |
| Condiciones booleanas en `repeat while` | 13 | Condicion con chequeo de mismo tipo y rechazo de cadena en `AccionesDeclaraciones.java`. Cuerpo de repeat con todas las sentencias ejecutables. Todos los 11 casos de ITERACION pasan. | Media (completado) |
| Asignación interna en expresiones | 17 | Se reconoce la sintaxis y se detecta el anidamiento, pero no se genera la asignación interna ni se chequean sus tipos. | Media |
| Restricción de origen en `todf` | 33 | `todf` ya emite terceto y devuelve `doublef`, pero falta restringir el origen a `ulongint` o enum con base `ulongint`, y rechazar conversiones vacías, de `doublef` o de objetos. | Media |
| Tipos de parámetros reales | 19 | Se verifica cantidad, orden y ubicación del formal, pero falta chequear que los tipos de los parámetros reales coincidan con los formales. | Media |
| Existen tipos al declarar variables | 23 | Falta verificar que el tipo exista al declarar variables y que la constante asignada pertenezca al enumerado. | Media |
| Asignación estructural de objetos | 25 | Falta verificar que la clase exista y que dos objetos sean estructuralmente compatibles; generar copia estructural, no asignación simple. | Media |
| Acceso a atributos con punto | 28 | Falta verificar existencia, visibilidad y tipo del atributo en la clase del objeto; generar terceto con la referencia. | Media |
| Herencia múltiple con prefijo | 31 | Falta validar clases base, que el prefijo corresponda a una clase heredada, y que el atributo o método exista y sea accesible. | Media |

---

### Nota: cadenas y comentarios (Prompt 9 y 16)

**Validado por el léxico.** Según `docs/SEMANTICAS_FALTANTES.md` y `docs/Promt.md` (Prompt 3.2), las cadenas de una línea se insertan con tipo `cadena`, no se permiten saltos de línea, y los comentarios multilínea `{{ ... }}` se descartan por completo. No se detectó un faltante léxico importante; hay casos TP1 para estos temas en `test/tp1/`.

> **RIESGO:** futuras modificaciones sobre el core del compilador (léxico, tabla de símbolos o acciones semánticas) llevarán a una revisión de estas reglas antes de la entrega final.

---

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

---

## Documentos de decisión

Cada prompt ejecutado deja su archivo de decisiones en `docs/`:

- `docs/PROMPT-PLAN.md` — plan reajustado de prompts con decisiones D1 a D5
- `docs/PROMPT-0.md` — baseline reproducible
- `docs/PROMPT-1A.md` — base de Name Mangling
- `docs/PROMPT-1B.md` — apertura y cierre real de ámbitos
- `docs/PROMPT-2.md` — igualdad estricta de tipos
- `docs/PROMPT-2B.md` — codegen base
- `docs/PROMPT-3.10.md` — herencia múltiple con prefijo (D3 revisada)
- `docs/SEMANTICAS_FALTANTES.md` — diagnóstico de faltantes por tema
 - `docs/PROMPT-6-8.md` — tipado de constantes ulongint y doublef en el lexico
 - `docs/PROMPT-9-16.md` — cadenas y comentarios validados por el lexico
 - `docs/PROMPT-13.md` — condicion booleana y tipo en repeat while
 - `docs/DEC-6.md` — registro de tipos de constantes en el analisis lexico
 - `docs/DEC-8.md` — condicion booleana y tipo en repeat while (prompt 3.3)
