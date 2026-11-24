# P4-01 — `jmp main_inicio` + detección local del main

Fecha: 24/11/2026  
Prompt del plan: `P4-01`, “Layout de `.code`” (`docs/reentrega/plan_prompts_reentrega.md:372-396`).  
Estado inicial: commit `bf352c3` (`2-07 documentacion sobre pruebas`), árbol limpio.  
Dependencia: P0-02.  
Resultado: cambio aplicado y verificado; pendiente de commit por decisión del usuario.

---

## 1. Alcance del prompt

```text
ID: P4-01
Tema: Layout de .code.
Raíz confirmada: GeneradorAssembler.emitirCodigo() (:459-553) escribe "start:\n" y luego traduce el IR lineal. El primer `$ul` no es FUNCION: es el prólogo de la primera subrutina. No hay CALL previo, así que [ebp+8]/[ebp+12] son basura y `ret` no tiene dirección válida.
Archivos: GeneradorAssembler.java, método emitirCodigo().
Cambio permitido:
  - Tras "start:\n", emitir "    jmp main_inicio\n".
  - Agregar boolean dentroSubrutina = false; boolean mainInicioEmitido = false;
  - En el switch: FUNCION/METODO -> dentroSubrutina = true; FIN_FUNCION/FIN_METODO -> dentroSubrutina = false. Mantener las llamadas actuales a emitirPrologo/emitirFinFuncion.
  - Antes de despachar cada terceto: si !dentroSubrutina && !mainInicioEmitido && operador no es PROGRAMA/FIN_PROGRAMA/CLASE/FIN_CLASE/FUNCION/METODO/FIN_FUNCION/FIN_METODO, emitir "main_inicio:\n" y marcar mainInicioEmitido = true.
Prohibido:
  - Reordenar la lista de tercetos.
  - Agregar Reglas.INICIO_MAIN ni regenerar Parser.java.
  - Cambiar la convención de llamadas.
Verificación:
  - 01_funcion_ulongint.asm y 03_recursion_factorial_metodo.asm.
Aceptación:
  - `start:` seguido de `jmp main_inicio`.
  - `fn_*`/`mt_*` quedan después del jmp y antes de `main_inicio:`.
  - `main_inicio:` precede a la primera instrucción ejecutiva del main.
```

Límites respetados:

- **Un solo archivo de código modificado:** `GeneradorAssembler.java`, método `emitirCodigo()`.
- No se reordenó la lista de tercetos; se recorre el IR en el orden existente.
- No se regeneró `Parser.java` ni se tocó `gramatica.y`.
- No se cambiaron `emitirPrologo`, `emitirFinFuncion`, `prepararDeclaraciones`, `declaracionPorTerceto` ni la convención de llamadas.
- No se modificaron los snapshots `.asm` versionados en `test_tp_4/`.
- No se tocó TP03 ni los tests de TP01/TP02.

---

## 2. Raíz del defecto

En el baseline, `emitirCodigo()` escribía:

```java
salida.append(".code\n");
salida.append("start:\n");

for (int i = 0; i < tercetos.size(); i++) {
    ...
    switch (op) {
        case "PROGRAMA":
        case "FIN_PROGRAMA":
            break;
        ...
        case "FUNCION":
        case "METODO":
            emitirPrologo(numero, op, op1);
            break;
        case "FIN_FUNCION":
        case "FIN_METODO":
            emitirFinFuncion(numero);
            break;
```

El IR conserva las declaraciones de funciones y métodos antes del código ejecutivo del `main`. Por eso, en `test_tp_4/Funciones_y_metodos/01_funcion_ulongint.asm` y `test_tp_4/Integracion/03_recursion_factorial_metodo.asm`, `start:` era seguido directamente por `fn_suma_main:` / `fn_fact_main:`. La CPU entraba en el prólogo de la subrutina sin un `CALL` previo: `[ebp+8]` y `[ebp+12]` contenían valores arbitrarios y el `ret` no tenía dirección de retorno válida. El primer `mov eax, c_3_ul_ul` del main quedaba después de ese `ret`, inalcanzable.

---

## 3. Cambio aplicado

`GeneradorAssembler.java`, `emitirCodigo()`:

1. Tras `start:` se emite `    jmp main_inicio`.
2. Se agregan los flags locales `dentroSubrutina` e `mainInicioEmitido`, ambos en `false`.
3. Antes de despachar cada terceto, si no estamos dentro de subrutina, `main_inicio` aún no se emitió y el operador no es marcador estructural (`PROGRAMA`, `FIN_PROGRAMA`, `CLASE`, `FIN_CLASE`, `FUNCION`, `METODO`, `FIN_FUNCION`, `FIN_METODO`), se emite `main_inicio:` y se marca `mainInicioEmitido = true`.
4. En el `switch`, `FUNCION`/`METODO` ponen `dentroSubrutina = true` antes de `emitirPrologo`; `FIN_FUNCION`/`FIN_METODO` lo ponen en `false` antes de `emitirFinFuncion`. Las llamadas originales a esos métodos se conservan.
5. Fallback al final del recorrido: si después de todos los tercetos `mainInicioEmitido` sigue en `false`, se emite `main_inicio:` igualmente. Esto cubre seis fuentes válidas cuyo IR solo tiene clases/métodos y ninguna instrucción ejecutiva en el main; sin el fallback el `jmp` apuntaría a una etiqueta inexistente.

Las seis fuentes cubiertas por el fallback son:

- `test/TP3_SEMANTICAS/prompt-1b-name-mangling/metodo_abre_ambito_propio.txt`
- `test/TP3_SEMANTICAS/prompt-1b-name-mangling/clases_hermanas_no_comparten_atributos.txt`
- `test/TP3_y_TP4/AMBITO/prueba_local_eliminada.txt`
- `test/tp2/verificacion_r28_r31_atributos/receptor_atributo.txt`
- `test/tp2/verificacion_r28_r31_atributos/atributos_objeto_ambito_funcion.txt`
- `test/tp2/verificacion_r28_r31_atributos/receptor_parametro.txt`

En esos casos `main_inicio:` queda inmediatamente antes de `invoke ExitProcess, 0`.

---

## 4. Preparación y ejecución

Baseline tomado desde `HEAD` (`bf352c3`), sin cambios:

```bash
rm -rf /tmp/opencode/p4_01_refs && mkdir -p /tmp/opencode/p4_01_refs/{base_src,base_build,run_base,out_base}
git archive HEAD | tar -x -C /tmp/opencode/p4_01_refs/base_src
javac -encoding UTF-8 -d /tmp/opencode/p4_01_refs/base_build \
      /tmp/opencode/p4_01_refs/base_src/*.java \
      /tmp/opencode/p4_01_refs/base_src/"acciones semanticas"/*.java \
      /tmp/opencode/p4_01_refs/base_src/otras_acciones/*.java
cp -a test test_tp_4 /tmp/opencode/p4_01_refs/run_base/
find /tmp/opencode/p4_01_refs/run_base -name '*.txt' -print0 | while IFS= read -r -d '' f; do
  rel=${f#/tmp/opencode/p4_01_refs/run_base/}
  d=/tmp/opencode/p4_01_refs/out_base/$(dirname "$rel")
  mkdir -p "$d"
  java -cp /tmp/opencode/p4_01_refs/base_build Main "$f" > "$d/$(basename "$rel").out" 2>&1
done
```

El cambio se aplicó primero en `/tmp/opencode/p4_01_refs/mod_src`, se compiló y se ejecutó toda la suite; después se replicó idéntico en el repo (`cmp -s GeneradorAssembler.java /tmp/opencode/p4_01_refs/mod_src/GeneradorAssembler.java` → `IDENTICOS`) y se volvió a compilar y ejecutar desde `/tmp/opencode/tp3build_p4_01`. No se corrió `java Main` desde la raíz: hay `.class` viejos no versionados que pueden contaminar la comparación.

Resultados de compilación y corrida:

```text
BASE COMPILA OK
MOD COMPILA OK
javac repo OK
GeneradorAssembler repo vs mod: IDENTICOS
Casos: 299          (baseline / mod / repo)
ASM generados: 161  (baseline / mod / repo)
```

---

## 5. Verificación

### 5.1 Regresión de stdout TP3

Comparación de los 299 `.out` entre baseline, copia modificada y repo:

```text
--- diffs stdout (baseline vs mod) ---
Total stdout diffs: 0
--- repo vs mod stdout diffs ---
Total: 0
```

El cambio solo afecta al `.asm`; el listado de tercetos y los mensajes de error son idénticos a `bf352c3`.

### 5.2 Diffs de ASM

Los 161 ASM generados difieren respecto del baseline. El diff es únicamente la inserción del salto y de la etiqueta; se mantiene el orden de todas las instrucciones y subrutinas. Casos de aceptación del prompt:

```diff
--- test_tp_4/Funciones_y_metodos/01_funcion_ulongint.asm
+++ (generado con el cambio)
 .code
 start:
+    jmp main_inicio
 fn_suma_main:
     push ebp
     ...
 fin_suma_main:
     leave
     ret
+main_inicio:
     mov eax, c_3_ul_ul
     mov x_main, eax
```

```diff
--- test_tp_4/Integracion/03_recursion_factorial_metodo.asm
+++ (generado con el cambio)
 .code
 start:
+    jmp main_inicio
 fn_fact_main:
     push ebp
     ...
 fin_escala_main_calculadora:
     leave
     ret
+main_inicio:
     ; (PAR, n:main:fact, 5$ul)
     sub esp, 4
```

Ejemplo del fallback:

```diff
--- test/TP3_y_TP4/AMBITO/prueba_local_eliminada.asm
+++ (generado con el cambio)
 .code
 start:
+    jmp main_inicio
 fn_a_main:
     push ebp
     ...
 fin_a_main:
     leave
     ret
+main_inicio:
     invoke ExitProcess, 0
 end start
```

### 5.3 Checker estático sobre los 161 ASM

Se verificó para cada ASM generado:

- `start:` existe y la línea siguiente es exactamente `jmp main_inicio`.
- `main_inicio:` aparece una sola vez.
- Ninguna etiqueta `fn_*` o `mt_*` aparece después de `main_inicio:`.
- Ningún `invoke crt_printf` del main aparece antes de `main_inicio:` (los `invoke` dentro de subrutinas se aceptan; se rastrea el estado `dentro/fuera de subrutina` con `fn_`/`mt_` y `fin_*`).
- Después de `main_inicio:` existe contenido ejecutivo; la primera línea no es `leave`, `ret`, `fin_*`, `fn_*`, `mt_*` ni un label de error.

Resultado:

```text
ASM revisados: 161
Fallos: 0
```

Un primer checker más estricto marcó dos falsos positivos (`test_tp_4/Funciones_y_metodos/07_pout_dentro_de_funcion.asm` y `test_tp_4/Salida_consola/11_pout_dentro_de_funcion.asm`) porque contaban los `crt_printf` dentro de `fn_eco_main` como si fueran del main. Al rastrear el ámbito de la subrutina, esos dos casos pasan: los `invoke` están dentro de `fn_eco_main`, antes de `main_inicio:`, y son correctos.

### 5.4 Límite de verificación

No se ensambló ni ejecutó ningún `.asm`: el entorno es Linux y no dispone de MASM32 (`ml.exe`) ni Windows. La verificación es estática (estructura del `.code` y regresión de salidas) y funcional solo hasta el generador Java. Ensamblar y correr los dos casos de aceptación queda para P4-02 en entorno MASM32/Windows.

---

## 6. Estado de git

```text
## Reentrega
 M GeneradorAssembler.java
?? docs/reentrega/P4/
```

`HEAD` sigue en `bf352c3`. El cambio de código y esta documentación quedan sin commitear; el usuario realiza el commit manualmente. No se modificaron snapshots `.asm`, tests ni documentación ya commiteada.
