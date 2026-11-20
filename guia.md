# Guía de prueba — Compilador Grupo 43

Este documento explica cómo verificar el compilador y enumera los tests específicos que implementan cada regla asignada y cada chequeo semántico de ejecución.

---

## Reglas semánticas asignadas y sus tests

### R13 — Estructura de iteración `repeat`/`while`

**Tests:**
- `test/semantica/estructuras/01_repeat_simple.txt` — ciclo repeat-while con prueba al final, cuerpo simple
- `test/semantica/estructuras/02_repeat_bloque.txt` — cuerpo es un begin-end de dos asignaciones
- `test/semantica/estructuras/03_repeat_condicion_expresion.txt` — condición con operaciones en ambos lados

**Reglas de testing:**
1. Condición booleana válida: `while` y `repeat` aceptan condición booleana; rechazan `ulongint`/`doublef` con error semántico.
2. Cuerpo con `begin`/`end`: cuando el cuerpo tiene más de una sentencia, debe envolverse en `begin ... end`.
3. Sentencia vacía o mal formada: `repeat` sin cuerpo o `while` sin condición genera error sintáctico/semántico.

---

### R17 — Asignación de expresiones dentro de expresiones

**Tests:**
- `test/semantica/asignaciones/03_calculos_combinados.txt` — plegado de expresiones con operadores combinados
- `test/semantica/asignaciones/anidamiento.txt` — asignación anidada en expresión
- `test/semantica/asignaciones/en_invocacion.txt` — asignación dentro de invocación de función

**Reglas de testing:**
1. Anidamiento válido: `x := (y := 5 + 3)` asigna 8 a ambas variables y muestra el resultado correcto.
2. Asignación como operando: el resultado de una asignación embebida se usa inmediatamente en operaciones aritméticas.
3. Tipos incompatibles en asignación anidada: asignar `doublef` a `ulongint` dentro de expresión anidada genera error semántico.

---

### R19 — Orden de evaluación de parámetros

**Tests:**
- `test/semantica/parametros/orden_sin_corchetes.txt` — orden por defecto izquierda a derecha
- `test/semantica/parametros/sin_orden.txt` — lista de orden vacía o inválida
- `test/semantica/parametros/estructura_respetada.txt` — estructura de parámetros respetada

**Reglas de testing:**
1. Orden por defecto (1..n): sin corchetes, los parámetros se evalúan en orden fuente.
2. Orden personalizado con corchetes: `[2,1]` evalúa primero el segundo argumento y luego el primero.
3. Cantidad incorrecta de argumentos: pasar más o menos argumentos que los declarados genera error de aridad.

---

### R23 — Enumeraciones (`typedef` con lista de valores)

**Tests:**
- `test/semantica/enums/enum_vacio.txt` — enumeración sin valores
- `test/semantica/enums/tipos_difieren_mayusculas.txt` — dos typedef que difieren solo en mayúsculas/minúsculas
- `test/semantica/enums/estructura_respetada.txt` — estructura de typedef respetada

**Reglas de testing:**
1. Valor dentro del conjunto: asignar uno de los valores declarados es aceptado.
2. Valor fuera del conjunto: asignar un valor no declarado genera error semántico en tiempo de ejecución (`error_enum_invalido`).
3. Enum vacío: `typedef nombre = []` es válido sintácticamente pero no permite asignar ningún valor.

---

### R25 — Objetos y compatibilidad por estructura

**Tests:**
- `test_tp_4/Objetos_atributos/01_copia_estructural.txt` — asignación estructural entre objetos compatibles (COPY)
- `test_tp_4/Objetos_atributos/04_copia_con_herencia.txt` — copia estructural con herencia
- `test_tp_4/Objetos_atributos/02_atributo_doublef.txt` — atributo doublef en objeto

**Reglas de testing:**
1. Copia estructural válida: asignar objeto de clase A a clase B con mismos atributos en mismo orden y tipos copia todos los atributos.
2. Distinta cantidad de atributos: copiar entre clases con diferente número de atributos genera error semántico.
3. Distinto tipo en misma posición: clases con mismos nombres de atributos pero diferente tipo en alguna posición son rechazadas como incompatibles.

---

### R28 — Atributos de clase y acceso con punto

**Tests:**
- `test/semantica/atributos_herencia/clase_con_atributos.txt` — clase con atributos y acceso tradicional objeto.atributo
- `test/semantica/atributos_herencia/desambiguado_tradicional_y.txt` — acceso desambiguado tradicional objeto.clase.atributo
- `test/semantica/atributos_herencia/prueba_funcion_anidada.txt` — función anidada con ámbito

**Reglas de testing:**
1. Acceso a atributo propio: `objeto.atributo` lee y escribe el atributo declarado en la clase del objeto.
2. Acceso a atributo heredado con prefijo: `objeto.padre.atributo` y `objeto.hija.atributo` resuelven correctamente a cada uno cuando hay mismo nombre.
3. Atributo inexistente: acceder a un atributo no declarado en la clase ni en ancestros genera error semántico.

---

### R31 — Herencia múltiple con prefijo

**Tests:**
- `test/semantica/atributos_herencia/desambiguado_tradicional_y.txt` — desambiguado tradicional objeto.clase.atributo
- `test/semantica/atributos_herencia/metodo_heredado_prefijado_con_orden.txt` — invocación a método heredado prefijado con orden de parámetros
- `test/semantica/atributos_herencia/metodo_heredado_prefijado_sin_orden.txt` — método heredado prefijado sin orden de parámetros
- `test_tp_4/herencia_circular/01_herencia_circular_directa.txt` — clase hereda de sí misma (herencia circular directa)
- `test_tp_4/herencia_circular/02_herencia_circular_indirecta.txt` — a hereda de b, b hereda de a (ciclo indirecto)
- `test_tp_4/herencia_circular/03_herencia_circular_tres_niveles.txt` — a hereda de b, b hereda de c, c hereda de a (ciclo de tres niveles)

**Reglas de testing:**
1. Herencia de múltiples bases: una clase que extiende dos clases tiene acceso a atributos y métodos de ambas.
2. Desambiguación con prefijo: `objeto.base1.miembro` y `objeto.base2.miembro` resuelven sin ambigüedad cuando hay mismo nombre.
3. Herencia circular: una clase que hereda directa o indirectamente de sí misma es rechazada con error semántico.

---

### R33 — Conversión explícita `todf`

**Tests:**
- `test/semantica/conversion/en_asignacion.txt` — todf del lado derecho de una asignación
- `test/semantica/conversion/en_condicion_if.txt` — todf en la condición de un if
- `test/semantica/conversion/conversion_vacia.txt` — todf() sin argumentos (error esperado)
- `test/semantica/conversion/lado_izquierdo.txt` — todf en lado izquierdo de asignación (error esperado)

**Reglas de testing:**
1. Conversión válida: `todf(42$ul)` produce `42.0` y es asignable a `doublef`.
2. Uso en aritmética: el resultado de `todf()` se usa en operaciones con otros `doublef`.
3. Conversión inválida: aplicar `todf` sobre un operando que no sea `ulongint` genera error semántico.

---

## Chequeos semánticos de ejecución (runtime)

### Chequeo b — Overflow de suma de enteros (`ulongint`)

**Tests:**
- `test/chequeos_ejecucion/b_suma_ulongint/01_suma_sin_overflow.txt` — suma simple sin desbordamiento
- `test/chequeos_ejecucion/b_suma_ulongint/02_suma_en_limite.txt` — suma en límite del rango unsigned
- `test/chequeos_ejecucion/b_suma_ulongint/03_suma_literal_desborda.txt` — literal que desborda ulongint
- `test/chequeos_ejecucion/b_suma_ulongint/04_suma_variables_desborda.txt` — desbordamiento desde variables

**Reglas de testing:**
1. Suma sin overflow: `100 + 200` completa normalmente e imprime `300`.
2. Suma en límite: `4294967294 + 1` completa con resultado `4294967295` sin activar guardia.
3. Suma que desborda: `4294967295 + 1` activa `error_overflow_suma` y termina el programa.

---

### Chequeo e — Overflow por producto de `doublef`

**Tests:**
- `test/chequeos_ejecucion/e_producto_doublef/01_producto_finito.txt` — producto doble finito sin overflow
- `test/chequeos_ejecucion/e_producto_doublef/02_producto_grande_finito.txt` — producto grande pero finito
- `test/chequeos_ejecucion/e_producto_doublef/03_producto_desborda.txt` — producto que excede rango doublef
- `test/chequeos_ejecucion/e_producto_doublef/04_producto_maximo_por_dos.txt` — overflow al multiplicar máximo por 2.0

**Reglas de testing:**
1. Producto finito: `2.0 * 3.0` completa e imprime `6.0`.
2. Producto grande finito: `1000000.0 * 2000000.0` completa e imprime `2000000000000.0`.
3. Producto que desborda: `1.0d308 * 1.0d308` activa `error_overflow_float` y termina el programa.

---

### Chequeo f — Resta de enteros con resultado negativo

**Tests:**
- `test/chequeos_ejecucion/f_resta_ulongint/01_resta_sin_resultado_negativo.txt` — resta sin resultado negativo
- `test/chequeos_ejecucion/f_resta_ulongint/02_resta_resultado_cero.txt` — resta igual a cero
- `test/chequeos_ejecucion/f_resta_ulongint/03_resta_resultado_negativo.txt` — 10 - 20 da resultado negativo
- `test/chequeos_ejecucion/f_resta_ulongint/04_resta_negativa_desde_variables.txt` — negativo calculado entre variables
- `test/chequeos_ejecucion/f_resta_ulongint/05_resta_encadenada_negativa.txt` — resta encadenada negativa

**Reglas de testing:**
1. Resta sin resultado negativo: `100 - 50` completa e imprime `50`.
2. Resta con resultado cero: `100 - 100` completa e imprime `0` sin activar guardia.
3. Resta que da negativo: `5 - 10` activa `error_negativo_resta` y termina el programa.

---

### Chequeo h — Asignación de valor inválido a variable enum

**Tests:**
- `test/chequeos_ejecucion/h_enum_asignacion/01_enum_ulongint_valido.txt` — asignación válida a enum ulongint
- `test/chequeos_ejecucion/h_enum_asignacion/02_enum_ulongint_invalido.txt` — asignación inválida a enum ulongint
- `test/chequeos_ejecucion/h_enum_asignacion/03_enum_doublef_valido.txt` — asignación válida a enum doublef
- `test/chequeos_ejecucion/h_enum_asignacion/04_enum_doublef_invalido.txt` — asignación inválida a enum doublef

**Reglas de testing:**
1. Asignación válida: asignar un valor dentro del conjunto del `typedef` completa normalmente.
2. Asignación inválida (ulongint): asignar valor numérico fuera del conjunto activa `error_enum_invalido`.
3. Asignación inválida (doublef): asignar valor `doublef` fuera del conjunto activa `error_enum_invalido`.

---

## Cómo ejecutar los tests

Desde la raíz del proyecto:

```bash
# Compilar
javac -encoding UTF-8 -d . *.java "acciones semanticas"/*.java otras_acciones/*.java

# Ejecutar un test específico
java Main test/semantica/estructuras/01_repeat_simple.txt

# Ejecutar un chequeo de ejecución
java Main test/chequeos_ejecucion/b_suma_ulongint/03_suma_literal_desborda.txt
```

---

## Archivos involucrados

- `AnalizadorLexico.java` — análisis léxico
- `Parser.java` — parser LALR(1) generado con BYACC/J
- `Verificaciones.java` — chequeos semánticos con contexto
- `AccionesSemanticas.java` — acciones semánticas durante el parseo
- `ControlAmbito.java` — manejo de ámbitos y declaraciones
- `GeneradorTercetos.java` — generación de código intermedio (tercetos)
- `Reporte.java` — canal único de errores `ERR_*`
- `TablaSimbolos.java` — tabla de símbolos
