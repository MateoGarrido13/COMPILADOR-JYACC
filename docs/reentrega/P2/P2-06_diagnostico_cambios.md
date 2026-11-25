# P2-06 — Propagar invalidez de `LlamadaFuncion` al envolverla

Fecha: 24/11/2026  
Prompt del plan: `P2-06`, “Consistencia de expresiones” (`docs/reentrega/plan_prompts_reentrega.md:297-315`).  
Estado inicial: commit `646d872` (`2-05 cambio en accion para tema 19`), árbol limpio. P2-01 (`736089b`), P2-02 (`5afeb88`), P2-03 (`744ca55`) y P2-05 (`646d872`) ya están commiteados.  
Dependencia: P2-05.

---

## 1. Alcance del prompt

```text
ID: P2-06
Tema: Consistencia de expresiones.
Raíz confirmada: ExpresionDiferida.java:29-36 propaga invalidez solo cuando el operando ya es ExpresionDiferida; una LlamadaFuncion marcada inválida queda envuelta como válida.
Archivos: ExpresionDiferida.java.
Cambio permitido:
  - Si operando instanceof LlamadaFuncion y !esValida(), marcar la envoltura inválida.
Prohibido:
  - Marcar inválidas llamadas válidas.
  - Cambiar LlamadaFuncion.java.
Verificación:
  - Casos con invocación con corchetes pero firma inexistente; factorial después de P5A-01.
Aceptación:
  - ASIGNACION ve origen inválido desde la envoltura.
 dependencias: P2-05.
Confirmación estática: No es necesario para 17/18: GeneradorTercetos.java:541-543 ya descarta LlamadaFuncion inválida al emitir operandos (:564). Se conserva para que el estado semántico y el IR coincidan.
```

Restricciones respetadas:

- Se editó solo `ExpresionDiferida.java`.
- No se tocó `LlamadaFuncion.java`.
- No se marcaron inválidas llamadas válidas.
- No se regeneró `Parser.java`.
- No se modificaron tests ni expected results.

**Nota sobre la verificación:** el caso “factorial después de P5A-01” pertenece a la Ola 5, que todavía no se ejecutó. Se dejó registrado para P2-07 / Ola 5; en esta corrida se usaron los casos ya existentes con firma inexistente y una sonda semántica directa.

---

## 2. Raíz del defecto

### 2.1 La invocación inválida se construye bien

`AccionesExpresiones.java:231-253` resuelve la firma y marca la llamada:

```java
EntradaTabla resuelta = v.resolverFuncion(funcion, ctx.getLinea());
Entorno.FuncionDeclarada firma = resuelta == null ? null : entorno.getFuncion(resuelta.lexema);
...
LlamadaFuncion llamada = new LlamadaFuncion(
        resuelta != null ? resuelta : funcion,
        conFormales, ordenEvaluacion, ctx.getLinea(), firma, null);
if (firma == null || !invocacionOk) {
    llamada.marcarInvalida();
}
return llamada;
```

Lo mismo hace `llamadaMetodo()` en `AccionesExpresiones.java:64-84` para métodos. `LlamadaFuncion.esValida()` (`LlamadaFuncion.java:64-66`) refleja correctamente ese estado.

### 2.2 El envoltorio pierde la marca

`ContextoRegla.comoExpresion()` (`ContextoRegla.java:74-82`) convierte cualquier valor que no sea `ExpresionDiferida` en una:

```java
public static ExpresionDiferida comoExpresion(Object valor) {
    if (valor instanceof ExpresionDiferida) {
        return (ExpresionDiferida) valor;
    }
    if (valor == null) {
        return null;
    }
    return ExpresionDiferida.deOperando(valor);
}
```

La rama de asignación (`AccionesDeclaraciones.java:103-105`) llama justo a ese converter:

```java
Object destinoValor = ctx.valor(0);
ExpresionDiferida origen = ctx.expresion(1);
```

Y `deOperando()` antes del cambio solo propagaba invalidez entre `ExpresionDiferida`:

```java
if (operando instanceof ExpresionDiferida && !((ExpresionDiferida) operando).esValida()) {
    expresion.valida = false;
}
```

Una `LlamadaFuncion` inválida entraba por el `else` implícito y quedaba envuelta en una expresión con `valida = true` (`ExpresionDiferida.java:22`).

### 2.3 Consecuencia

- `AccionesDeclaraciones.java:148` evaluaba `!origen.esValida()` como `false` y seguía adelante, construyendo una `Asignacion`.
- `GeneradorTercetos.java:539-543` sí descartaba la `LlamadaFuncion` inválida al emitir el operando, así que el IR ya salía limpio.
- Resultado: IR correcto, estado semántico incorrecto. El mensaje de error se emitía, pero la envoltura decía que el valor era utilizable.

Este prompt no cambia el IR; alinea el estado con lo que el generador ya hacía.

---

## 3. Casos usados para verificar

### 3.1 `test/tp2/verificacion_r19_param_reales/resolucion_funcion.txt`

```text
ulongint function f(ulongint p)
begin
    ret(p);
end;
begin
    x := ausente(1$ul)[1$ul];   <- firma inexistente
    x := f(1$ul)[1$ul];         <- firma válida
    x := x(1$ul)[1$ul];         <- variable usada como función
end
```

### 3.2 `test/tp2/verificacion_r19_param_reales/metodos.txt`

```text
x := objeto.m(1$ul)[1$ul];          <- método válido
x := objeto.m(1.0)[1$ul];           <- tipo de real incompatible
x := objeto.inexistente(1$ul)[1$ul]; <- método inexistente
```

### 3.3 Baseline de salida

Antes y después del cambio, con el código en `646d872`:

```text
== resolucion_funcion.txt
Línea 11: Error: Funcion no declarada 'ausente'
Línea 13: Error: Uso incorrecto de 'x': no es una funcion
1. (PROGRAMA, --, --)
2. (FUNCION, f, --)
3. (ret, p:main:f, --)
4. (FIN_FUNCION, f, --)
5. (PAR, p:main:f, 1$ul)
6. (CALL, f:main, 1)
7. (:=, x:main, [6])
8. (FIN_PROGRAMA, --, --)
```

```text
== metodos.txt
Línea 16: Error: Tipos incompatibles en el parametro real 'p' de 'm': se esperaba ulongint y se encontró doublef
Línea 17: Error: Metodo no declarado o no visible 'inexistente'
1. (PROGRAMA, --, --)
2. (CLASE, ca, --)
3. (METODO, m, --)
4. (ret, p:main:ca:m, --)
5. (FIN_METODO, m, --)
6. (FIN_CLASE, ca, --)
7. (PAR, p:main:ca:m, 1$ul)
8. (CALLMET, objeto:main, m:main:ca)
9. (:=, x:main, [8])
10. (FIN_PROGRAMA, --, --)
```

No hay `PAR`/`CALL` para `ausente`, ni `CALLMET` para `inexistente` ni para el `m(1.0)` inválido. Sí los hay para las invocaciones válidas. Esa igualdad de salida es esperada: el cambio actúa en la capa semántica, antes del generador.

---

## 4. Cambio aplicado

Único archivo modificado por P2-06: `ExpresionDiferida.java`, método `deOperando(Object, String)`.

```diff
@@ -33,6 +33,12 @@ public class ExpresionDiferida {
         if (operando instanceof ExpresionDiferida && !((ExpresionDiferida) operando).esValida()) {
             expresion.valida = false;
         }
+        // Una invocacion puede estar marcada invalida por resolucion de firma,
+        // aridad, orden o tipos. Al envolverla, la envoltura debe heredar esa
+        // marca para que ASIGNACION no trate el resultado como un valor valido.
+        if (operando instanceof LlamadaFuncion && !((LlamadaFuncion) operando).esValida()) {
+            expresion.valida = false;
+        }
         return expresion;
     }
```

### Por qué cada parte

1. **Se conserva la propagación desde `ExpresionDiferida`**  
   Los casos anidados y las sumas ya dependían de ella. No se reemplazó, se complementó.

2. **`instanceof LlamadaFuncion`**  
   Cubre tanto invocaciones de función como de método: `llamadaMetodo()` también devuelve `LlamadaFuncion` (`AccionesExpresiones.java:77-83`).

3. **`!((LlamadaFuncion) operando).esValida()`**  
   Solo se propaga la marca cuando el objeto ya la tiene. Una llamada válida (`esValida() == true`) deja la envoltura válida. La sonda lo confirma.

4. **No se tocó `LlamadaFuncion.java`**  
   La clase ya expone `esValida()` / `marcarInvalida()` y el plan lo prohíbe. El arreglo pertenece al envoltorio, no al envuelto.

5. **No se modificaron `anteponer()` ni `absorber()`**  
   `absorber()` (`ExpresionDiferida.java:159-173`) ya propaga `valida = false` desde otra `ExpresionDiferida`; al nacer inválida, la envoltura se propaga sola por las reducciones posteriores. `anteponer()` recibe operandos sueltos en operaciones binarias, pero el acceptance criterion apunta a `ASIGNACION` vía `comoExpresion()`, que es exactamente `deOperando()`.

Hashes:

```text
HEAD  ExpresionDiferida.java  85e39a40d6aa3c0414a398c3632d7eaa39fc7a3cd9b5aa0e8a0e42b4b91d7f6e
work  ExpresionDiferida.java  25229400acec5e08d5214d98db32538f13ac1be5ec479e4083edd5d732cbffb5
HEAD  LlamadaFuncion.java     fd98827eab78f6db899072e8026b08ed52bfc43e054a86ef92c666b7f8994327
work  LlamadaFuncion.java     fd98827eab78f6db899072e8026b08ed52bfc43e054a86ef92c666b7f8994327
HEAD  Parser.java             047b057607ab534751d909306a838514562dc12c7b46d7c67bae9e9b97b93257
work  Parser.java             047b057607ab534751d909306a838514562dc12c7b46d7c67bae9e9b97b93257
HEAD  gramatica.y             f32a95c5bc0b54a939b4a8574d06c4e78fbca37faace342b740260d83645c6b0
work  gramatica.y             f32a95c5bc0b54a939b4a8574d06c4e78fbca37faace342b740260d83645c6b0
```

---

## 5. Verificación

### 5.1 Compilación

```bash
javac -encoding UTF-8 -d /tmp/opencode/tp3build_p2_06 \
      *.java "acciones semanticas"/*.java otras_acciones/*.java
```

Resultado: **sin errores**.

### 5.2 Regresión completa de salida

Se comparó el árbol en `646d872` contra el mismo árbol con el cambio, ejecutando todos los `.txt` de `test/` y `test_tp_4/` en árboles separados:

```text
Casos ejecutados: 299
Archivos con diff: 0
ASM en baseline: 161
ASM con diff: 0
```

**Por qué cero diffs:** ya existía una segunda barrera en `GeneradorTercetos.java:539-543`, que descarta la `LlamadaFuncion` inválida al emitir operandos. El cambio no modifica esa barrera ni los mensajes; corrige el estado que ve `ASIGNACION` antes de llegar al generador. Una salida distinta habría sido una regresión, no el objetivo.

### 5.3 Sonda semántica directa

Como el acceptance criterion es interno, se compiló una sonda temporal (`/tmp/opencode/p2_06_refs/P2_06Probe.java`) contra ambos builds:

```java
public class P2_06Probe {
    public static void main(String[] args) {
        LlamadaFuncion invalida = new LlamadaFuncion(null, null, null, 1);
        invalida.marcarInvalida();
        LlamadaFuncion valida = new LlamadaFuncion(null, null, null, 1);

        System.out.println("llamadaInvalida=" + invalida.esValida());
        System.out.println("envolturaDeInvalida=" + ExpresionDiferida.deOperando(invalida).esValida());
        System.out.println("llamadaValida=" + valida.esValida());
        System.out.println("envolturaDeValida=" + ExpresionDiferida.deOperando(valida).esValida());
    }
}
```

Antes del cambio:

```text
llamadaInvalida=false
envolturaDeInvalida=true
llamadaValida=true
envolturaDeValida=true
```

Después del cambio:

```text
llamadaInvalida=false
envolturaDeInvalida=false
llamadaValida=true
envolturaDeValida=true
```

Con el código final del repo se repitió la sonda y dio la misma salida “después”.

Lectura:

- `envolturaDeInvalida` pasa de `true` a `false`: `ASIGNACION` ahora ve `origen.esValida() == false` y devuelve `null` en `AccionesDeclaraciones.java:148`.
- `envolturaDeValida` sigue `true`: ninguna llamada válida se marca inválida.

### 5.4 Criterios de aceptación

| Criterio | Cumplido | Evidencia |
|---|---|---|
| `LlamadaFuncion` inválida propaga al envolverla | sí | `envolturaDeInvalida=false` en la sonda. |
| Llamadas válidas no se marcan inválidas | sí | `envolturaDeValida=true` antes y después. |
| `ASIGNACION` ve origen inválido | sí | `AccionesDeclaraciones.java:148` consulta `!origen.esValida()`; la sonda fija ese valor en `false`. |
| `LlamadaFuncion.java` sin cambios | sí | hash idéntico a `HEAD`. |
| Parser no regenerado | sí | hash de `Parser.java` y `gramatica.y` idénticos a `HEAD`. |
| Sin regresión de IR ni ASM | sí | 299 casos / 0 diffs; 161 ASM / 0 diffs. |
| Mensajes de error intactos | sí | `resolucion_funcion.txt` y `metodos.txt` sin diff en `Error:`. |

---

## 6. Efectos colaterales analizados

- **P2-07**: es el próximo prompt. Debe repetir la regresión TP02 con este cambio integrado y revisar la fila `PROMPT-0.md:135` de métodos heredados sin orden (baseline viejo, IR real ya sin asignaciones).
- **P2-05**: sin cambios. Sus tres casos (`18_falta_orden.txt`, `sin_orden.txt`, `orden_sin_corchetes.txt`) siguen sin diff.
- **P2-03**: sin cambios; los dos casos con `:=` siguen con IR de solo delimitadores.
- **Ola 5 / P5A-01**: quedó fuera de la reentrega por decisión P6-01. El caso `03_recursion_factorial_metodo.txt` sigue en el árbol solo como regresión de layout; no se implementa recursión y no se evalúa `envolturaDeValida` sobre una llamada recursiva. P2-07 no asumió ninguna expectativa de Ola 5.
- **TP03 (`21_enum_vacio.txt`)**: intacto.
- **ASM y tests**: ningún `.asm`, `.txt` o `.sh` modificado.

---

## 7. Estado de git al cierre de P2-06

```text
 M ExpresionDiferida.java
 M docs/reentrega/P2/P2_diagnostico_cambios.md
?? docs/reentrega/P2/P2-06_diagnostico_cambios.md
```

P2-06 queda sin commitear. El commit anterior es `646d872` (P2-05).

---

## 8. Estado de la Ola 2

| Prompt | Estado | Observación |
|---|---|---|
| P2-01 | Ejecutado y commiteado (`736089b`) | Expected results tema 17. |
| P2-02 | Ejecutado y commiteado (`5afeb88`) | Expected results tema 19. |
| P2-03 | Ejecutado y commiteado (`744ca55`) | Gramática + Parser regenerado; dos casos sin `:=`. |
| P2-04 | Eliminado | Según el plan. |
| P2-05 | Ejecutado y commiteado (`646d872`) | Tres casos sin `(:=, x:main, foo:main)`. |
| P2-06 | **Ejecutado** | Invalidez de `LlamadaFuncion` propagada; IR sin cambios, sonda semántica OK. |
| P2-07 | Pendiente | Regresión TP02. |
