# Revisión de los prompts 3.3 y 3.4

Alcance: prompts 3.3 (`repeat while`, Tema 13) y 3.4 (asignación dentro de
expresiones, Tema 17) de `docs/Promt.md`. Se usaron `docs/PROMPT-13.md` y
`docs/PROMPT-17.md` como guía de los cambios ya implementados.

Se detectaron y corrigieron defectos en ambos prompts. Los cambios de código
están sin commitear al momento de escribir este documento.

---

## 1. Prompt 3.3 — `repeat while`

### 1.1 Requisitos y verificación

| Requisito | Verificación |
|---|---|
| `test/TP3_y_TP4/ITERACION/*` con los mismos tercetos (salvo nombres de ámbito) | ✅ 00..09 y 11 sin cambios; el caso 10 se corrigió (ver 1.2) |
| `repeat` con `pout` e invocación emite el código de ambos | ✅ el caso 10, corregido, emite `PAR`/`CALL`, `(:=, r, [..])` y `pout` |
| `repeat ... while (x > 1.0)` con `x ulongint` informa error de tipos | ✅ `Tipos incompatibles en la condición: se esperaba ulongint y se encontró doublef` |
| `repeat ... while (x > 1$ul)` no informa error | ✅ sin error |
| Condición de tipo `cadena` rechazada | ✅ `FACTOR_CADENA` no es parte de `factor`, la cadena se rechaza sintácticamente |

### 1.2 Defectos encontrados y corregidos

**a) El caso `10_repeat_con_pout_y_invocacion.txt` estaba mal escrito.**
El cuerpo del `repeat` con más de una sentencia debe ir entre `begin` y `end`
(`bloque_sentencias : sentencia | BEGIN lista_sentencias END`). El caso original
ponía tres sentencias sueltas y el parser reportaba `Falta while` y `Sentencia
mal formada`. El comportamiento del compilador es intencional: se reescribió el
test con `begin`/`end` y se actualizó su comentario.

**b) Una condición inválida dejaba tercetos huérfanos del ciclo.**
`generarRepeat` emitía la etiqueta de inicio al reducir `INICIO_REPEAT` (antes
del cuerpo) y, si la condición fallaba el chequeo de tipos, se retiraba sin
emitir `BF`/`BI`/etiqueta de salida. Quedaba la etiqueta de inicio y el cuerpo
del ciclo como código intermedio de un programa que no compila.

Solución:

- `Terceto.java`: nuevo `ListaTercetos.descartarDesde(int)`, que recorta la
  lista hasta la cantidad de tercetos a conservar.
- `GeneradorTercetos.java`: nueva pila `marcasRepeat` con la cantidad de
  tercetos previos a cada `repeat`. En `generarRepeat`, si la comparación es
  `null` (condición ya reportó error), se llama a `descartarDesde(marca)` y se
  retiran etiqueta de inicio y cuerpo.

Antes / después en `11_repeat_condicion_tipos_distintos.txt`:

```
antes:  (:=, x, 0$ul) (label_1) (+, x, 1$ul) (:=, x, [..]) (FIN_PROGRAMA)
ahora:  (:=, x, 0$ul) (FIN_PROGRAMA)
```

**c) Nota sobre condiciones de tipo `cadena`.**
El chequeo `Tipos.CADENA` de la acción `CONDICION` es defensivo: la gramática no
permite una `cadena` como `factor`, por lo que ese camino nunca se alcanza. Se
mantuvo sin cambios y se documenta como código muerto defensivo.

### 1.3 Riesgo residual

Los casos `test/tp2/generales/14_falta_cuerpo_iter.txt` y
`16_falta_while.txt` dejan etiquetas de inicio sin recortar, porque el error es
sintáctico y no llega a `generarRepeat`. Es un comportamiento previo a esta
revisión y no se modificó para no ampliar el alcance.

---

## 2. Prompt 3.4 — Asignación dentro de expresiones

### 2.1 Requisitos y verificación

| Requisito | Verificación |
|---|---|
| `estructura_respetada.txt` emite las asignaciones a `y` y a `x` | ✅ |
| `anidamiento.txt` y `anidamiento_indirecto.txt` reportan `Anidamiento de asignacion en expresion` | ✅ y ya no emiten tercetos de asignación |
| `en_invocacion.txt` emite la asignación a `y` antes del `PAR` | ✅ |
| Tipo incompatible en la asignación interna informa error | ✅ `Tipos incompatibles en la asignación a 'y'` |

### 2.2 Defectos encontrados y corregidos

**a) El anidamiento no invalidaba la expresión externa.**
La acción `ASIGNACION_EN_EXPRESION` reportaba el anidamiento pero seguía
construyendo el resultado válido, por lo que el generador emitía la asignación
externa `(:=, x, [..])` y la del destino. Ahora el resultado se marca inválido
cuando hay anidamiento y no se emite ningún terceto de la asignación.

**b) Destino no asignable o no declarado emitía tercetos.**
Con `x := f = (1$ul)` donde `f` es una función, o con un destino fuera de
alcance, el generador emitía igual `(:=, f, ...)` y `(:=, x, ...)`. Ahora:

- si `verificarAlcance` devuelve `null`, la expresión se marca inválida;
- si el destino no es asignable (`esAsignable` falso), el resultado se marca
  inválido y no se emite nada.

**c) La detección de anidamiento no debe mirar los argumentos de una invocación.**
En una primera pasada se hizo recursiva la búsqueda de asignaciones para que
llegara también a los parámetros reales de una invocación. Eso era un exceso: el
Tema 19 del enunciado admite asignaciones en expresiones como parámetros reales
(`c := f(a, b=4, a+b)[1,2,3]`, con la forma con paréntesis del Tema 17), y el PDF
oficial de errores del grupo **no** incluye un error de anidamiento. El único
error propio de los Temas 17/18 es "uso de `:=` donde debe usarse `=`".

Por eso la detección vuelve a basarse en la marca `contieneAsignacion()` de la
expresión, que viaja por el plegado aritmético (`absorber`) pero no por los
argumentos de una invocación. Así:

- `x := y = (z = (1$ul))` y `x := y = (w + z = (1$ul))` siguen siendo
  `Anidamiento de asignacion en expresion`;
- `x := y = (f(z = (1$ul))[1$ul])` es válido: el valor de `y` es el retorno de
  `f` y `z=(1$ul)` es un efecto de lado del argumento.

Cambios:

- `Verificaciones.verificarAsignacionAnidada` vuelve a usar
  `expresion.contieneAsignacion()`.
- `AccionesExpresiones.java` calcula el anidamiento antes de envolver el
  resultado, porque al envolver la marca propia taparía la del lado derecho.
- `ExpresionDiferida.contieneAsignacion(Object)` se retiró.

**d) Comentario desactualizado.**
`AsignacionEnExpresion.java` decía que el terceto propio de la asignación
quedaba para el Tema 17; ya se emite en `emitir()`. Se corrigió el comentario.

### 2.3 Alcance del anidamiento

La marca de asignación se propaga por el plegado aritmético y por las
conversiones que envuelven el valor, no por los argumentos de una invocación.
Queda fuera del error de anidamiento cualquier asignación que aparezca como
parámetro real, en línea con el Tema 19.

### 2.4 Nota sobre la forma del enunciado

El enunciado escribe los parámetros con asignación sin paréntesis
(`f(a, b=4, a+b)`). La gramática del proyecto exige paréntesis para la
asignación en expresión (`ID '=' '(' expresion ')'`, Tema 17) y usa
`ID '=' expresion` para el parámetro real nombrado; por eso en los tests la
forma equivalente es `f(a, b = (4$ul), a + b)`. El caso sin paréntesis se
interpreta como parámetro nombrado y no se modificó la gramática.

---

## 3. Tests agregados o corregidos

| Archivo | Cambio |
|---|---|
| `test/TP3_y_TP4/ITERACION/10_repeat_con_pout_y_invocacion.txt` | reescrito con `begin`/`end` y comentario corregido |
| `test/tp2/verificacion_r17_asig_expresiones/tipo_incompatible.txt` | nuevo: tipo incompatible en la asignación interna |
| `test/tp2/verificacion_r17_asig_expresiones/destino_no_asignable.txt` | nuevo: destino no asignable (`f` función) |
| `test/tp2/verificacion_r17_asig_expresiones/asignacion_en_parametro_valida.txt` | nuevo: asignación en parámetro real no es anidamiento |
| `test/tp2/verificacion_r19_param_reales/asignacion_en_parametros.txt` | nuevo: forma del ejemplo del Tema 19 con `b = (4$ul)` |

---

## 4. Verificación ejecutada

Suites ejecutadas:

- `test/TP3_y_TP4/ITERACION/*`: 00..10 sin errores; 11 sólo el error de tipos
  esperado.
- `test/tp2/verificacion_r17_asig_expresiones/*`: sólo reportan error los casos
  que deben hacerlo (`anidamiento`, `anidamiento_indirecto`,
  `destino_no_asignable`, `tipo_incompatible`, `igual_como_asignacion_sentencia`,
  `parentesis_anidados`, `sin_parentesis`, `usa_asig_sin_anidamiento`).
- `test/tp2/verificacion_r19_param_reales/asignacion_en_parametros.txt`: sin
  errores.

### Comparación contra la línea base (HEAD `a253367`)

Se compiló el árbol de `HEAD` en `/tmp/opencode/basebuild` y se corrieron los
228 `.txt` de `test/`. Sólo 5 salidas difieren, todas mejoras esperadas:

| Test | Diferencia |
|---|---|
| `test/tp2/generales/23_asig_anidada.txt` | ya no emite asignaciones anidadas |
| `test/tp2/generales/27_extensivo_200_varioserrores_unalinea.txt` | ya no emite asignaciones anidadas |
| `.../r17/anidamiento.txt` | ya no emite asignaciones |
| `.../r17/anidamiento_indirecto.txt` | ya no emite asignaciones |
| `.../r17/destino_no_asignable.txt` | nuevo; ya no emite asignaciones |

Los casos `asignacion_en_parametro_valida.txt` y
`r19/asignacion_en_parametros.txt` no figuran porque su salida coincide con la
línea base: la asignación en un parámetro real ya era válida.

---

## 5. Decisiones

- **No emitir tercetos de código inválido.** Tanto en el `repeat` con condición
  inválida como en la asignación anidada/no asignable se prefiere retirar o
  invalidar el código antes que dejar tercetos de un programa que no compila.
- **El cuerpo de `repeat` con varias sentencias exige `begin`/`end`.** Se
  documentó como comportamiento intencional en lugar de ampliar la gramática.
- **El anidamiento no incluye parámetros reales.** El Tema 19 admite
  asignaciones en expresiones como argumentos; la marca de anidamiento sólo
  viaja por el plegado aritmético y las conversiones.
- **No tocar los casos de error sintáctico** (`14_falta_cuerpo_iter`,
  `16_falta_while`) para no ampliar el alcance de la revisión.

## 6. Riesgos residuales

- Etiquetas de inicio huérfanas en errores sintácticos de `repeat` (ver 1.3).
- El enunciado escribe `b=4` sin paréntesis; la gramática del proyecto lo trata
  como parámetro nombrado y exige `b = (4$ul)` para la asignación en expresión
  (ver 2.4).
- El chequeo de `cadena` en `CONDICION` es código muerto defensivo.
