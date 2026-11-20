# Guía de pruebas — Reglas asignadas y chequeos semánticos

Este documento establece, para cada regla asignada y cada chequeo semántico de ejecución, al menos tres reglas significativas que deben cumplirse al momento de testing. Sirve como referencia para la elaboración, revisión y ejecución de los casos de prueba del compilador.

---

## 1. Reglas semánticas asignadas

### 1.1 R13 — Estructura de iteración `repeat`/`while`

**Descripción:** Verifica que las sentencias de repetición se escriban correctamente, con su condición booleana, su cuerpo delimitado por `begin`/`end` cuando sea necesario, y que la condición se evalúe al final del cuerpo.

**Reglas de testing:**

1. **Condición booleana válida:** El programa debe aceptar una condición de tipo booleano en `while` y `repeat`, y rechazar condiciones de tipo `ulongint` o `doublef` con un error semántico claro.
2. **Cuerpo con `begin`/`end`:** Cuando el cuerpo contiene más de una sentencia, debe envolverse en `begin ... end`; de lo contrario, el parser debe reportar falta de delimitador.
3. **Sentencia vacía o mal formada:** Un `repeat` sin cuerpo o un `while` sin condición debe generar error sintáctico o semántico, sin producir código intermedio incompleto.

---

### 1.2 R17 — Asignación de expresiones dentro de expresiones

**Descripción:** Permite usar una asignación (`:=`) como parte de una expresión más amplia, retornando el valor asignado como operando.

**Reglas de testing:**

1. **Anidamiento válido:** Una expresión como `x := (y := 5 + 3)` debe compilar y asignar `8` tanto a `y` como a `x`, mostrando el valor correcto en `pout`.
2. **Asignación como operando:** El resultado de una asignación embebida debe poder usarse inmediatamente en operaciones aritméticas (`z := (x := 2) * 3` debe dar `6`).
3. **Tipos incompatibles en asignación anidada:** Asignar un `doublef` a una variable `ulongint` dentro de una expresión anidada debe generar error semántico de tipo incompatible.

---

### 1.3 R19 — Orden de evaluación de parámetros

**Descripción:** Define el orden en que se evalúan los argumentos reales antes de pasar a una función, especificando si se evalúan de izquierda a derecha, derecha a izquierda, o en un orden personalizado entre corchetes `[...]`.

**Reglas de testing:**

1. **Orden por defecto (1..n):** Sin especificar corchetes, los parámetros deben evaluarse en orden fuente (primero el primero, luego el segundo, etc.).
2. **Orden personalizado con corchetes:** `[2,1]` debe evaluar primero el segundo argumento y luego el primero, afectando el resultado cuando los argumentos tengan efectos secundarios.
3. **Cantidad incorrecta de argumentos:** Pasar más o menos argumentos de los declarados en la función debe producir error semántico de aridad.

---

### 1.4 R23 — Enumeraciones (`typedef` con lista de valores)

**Descripción:** Permite definir tipos enumerados mediante `typedef nombre = [v1, v2, ...]` y declarar variables de ese tipo.

**Reglas de testing:**

1. **Valor dentro del conjunto:** Asignar uno de los valores declarados en el `typedef` a una variable del tipo enumerado debe ser aceptado.
2. **Valor fuera del conjunto:** Asignar un valor numérico que no aparezca en la lista del `typedef` debe generar error semántico en tiempo de ejecución (guardia `error_enum_invalido`).
3. **Enum vacío:** Un `typedef nombre = []` debe ser válido sintácticamente pero no permitir asignar ningún valor a variables de ese tipo.

---

### 1.5 R25 — Objetos y compatibilidad por estructura

**Descripción:** Define clases con atributos y permite la copia estructural entre objetos de clases distintas pero con misma estructura (mismos atributos en mismo orden y tipos).

**Reglas de testing:**

1. **Copia estructural válida:** Asignar un objeto de clase `A` a un objeto de clase `B` con mismos atributos en mismo orden y tipos debe copiar todos los atributos correctamente.
2. **Distinta cantidad de atributos:** Intentar copiar entre clases con diferente número de atributos debe generar error semántico.
3. **Distinto tipo en misma posición:** Dos clases con mismos nombres de atributos pero diferente tipo en alguna posición deben ser rechazadas como incompatibles.

---

### 1.6 R28 — Atributos de clase y acceso con punto

**Descripción:** Permite definir atributos dentro de clases y acceder a ellos mediante `objeto.atributo`, incluyendo herencia y desambiguación con prefijo de clase.

**Reglas de testing:**

1. **Acceso a atributo propio:** `objeto.atributo` debe leer y escribir el atributo declarado en la clase del objeto.
2. **Acceso a atributo heredado con prefijo:** Cuando una clase hija y su padre tienen un atributo con mismo nombre, `objeto.padre.atributo` y `objeto.hija.atributo` deben resolver correctamente a cada uno.
3. **Atributo inexistente:** Acceder a un atributo no declarado en la clase ni en sus ancestros debe generar error semántico.

---

### 1.7 R31 — Herencia múltiple con prefijo

**Descripción:** Permite que una clase herede de múltiples clases base (`extends A, B`) y desambigue miembros usando el prefijo `clase.miembro`.

**Reglas de testing:**

1. **Herencia de múltiples bases:** Una clase que extiende dos clases debe tener acceso a los atributos y métodos de ambas bases.
2. **Desambiguación con prefijo:** Cuando dos bases definen un miembro con mismo nombre, `objeto.base1.miembro` y `objeto.base2.miembro` deben resolver sin ambigüedad.
3. **Herencia circular:** Una clase que hereda directa o indirectamente de sí misma debe ser detectada y rechazada con error semántico.

---

### 1.8 R33 — Conversión explícita `todf`

**Descripción:** Proporciona la función `todf(expresion)` para convertir un valor `ulongint` a `doublef` de forma explícita.

**Reglas de testing:**

1. **Conversión válida:** `todf(42$ul)` debe producir el valor `doublef` `42.0` y ser asignable a una variable `doublef`.
2. **Uso en aritmética:** El resultado de `todf()` debe poder usarse en operaciones con otros `doublef` (`todf(x) + 1.5`).
3. **Conversión inválida:** Aplicar `todf` sobre un operando que no sea `ulongint` (por ejemplo, otro `doublef` o una cadena) debe generar error semántico.

---

## 2. Chequeos semánticos de ejecución (runtime)

### 2.1 Chequeo b — Overflow de suma de enteros (`ulongint`)

**Descripción:** Detecta cuando la suma de dos valores `ulongint` excede el rango de 32 bits sin signo (0 a 4294967295).

**Reglas de testing:**

1. **Suma sin overflow:** `100 + 200` debe completar normalmente e imprimir `300`.
2. **Suma en límite:** `4294967294 + 1` debe completar con resultado `4294967295` sin activar la guardia de error.
3. **Suma que desborda:** `4294967295 + 1` debe activar `error_overflow_suma` y terminar el programa con código de salida distinto de cero.

---

### 2.2 Chequeo e — Overflow por producto de `doublef`

**Descripción:** Detecta cuando el producto de dos valores `doublef` excede el rango representable (aprox. 1.7976931348623157e+308).

**Reglas de testing:**

1. **Producto finito:** `2.0 * 3.0` debe completar e imprimir `6.0`.
2. **Producto grande pero finito:** `1000000.0 * 2000000.0` debe completar e imprimir `2000000000000.0`.
3. **Producto que desborda:** `1.0d308 * 1.0d308` debe activar `error_overflow_float` y terminar el programa.

---

### 2.3 Chequeo f — Resta de enteros con resultado negativo

**Descripción:** Detecta cuando una resta entre valores `ulongint` produciría un resultado negativo (que no es representable en unsigned).

**Reglas de testing:**

1. **Resta sin resultado negativo:** `100 - 50` debe completar e imprimir `50`.
2. **Resta con resultado cero:** `100 - 100` debe completar e imprimir `0` sin activar la guardia.
3. **Resta que da negativo:** `5 - 10` debe activar `error_negativo_resta` y terminar el programa.

---

### 2.4 Chequeo h — Asignación de valor inválido a variable enum

**Descripción:** Detecta cuando se asigna a una variable enumerada un valor que no pertenece al conjunto declarado en su `typedef`.

**Reglas de testing:**

1. **Asignación válida:** Asignar un valor dentro del conjunto del `typedef` debe completar normalmente.
2. **Asignación inválida (ulongint):** Asignar un valor numérico fuera del conjunto debe activar `error_enum_invalido` y terminar el programa.
3. **Asignación inválida (doublef):** Asignar un valor `doublef` fuera del conjunto de un enum basado en `doublef` debe activar la misma guardia de error.

---

## 3. Reglas generales para todos los tests

**Aplicables a toda regla y chequeo:**

1. **Un efecto observable por prueba:** Cada archivo de prueba debe verificar exactamente una regla o chequeo, con una entrada clara y una salida esperada definida.
2. **Salida por consola (`pout`):** Cada prueba debe usar `pout` para imprimir el resultado calculado, permitiendo verificar el valor por comparación de stdout.
3. **Código de salida:** Las pruebas que esperan un error de ejecución (overflow, negativo, enum inválido) deben terminar con código de salida distinto de cero; las pruebas válidas deben terminar con código cero.

---

## 4. Estructura recomendada de archivos de prueba

Cada prueba debe seguir el formato:

```
{{ Descripción de la regla o chequeo que se verifica }}
prog
  ... declaraciones y sentencias ...
begin
  ... cuerpo del programa ...
end
```

Donde:
- El comentario inicial entre `{{` y `}}` describe qué regla se prueba y qué se espera.
- El bloque `prog ... begin ... end` es la estructura obligatoria del lenguaje.
- `pout({mensaje})` imprime etiquetas identificadoras; `pout(variable)` imprime valores numéricos.

---

**Fin de la guía**
