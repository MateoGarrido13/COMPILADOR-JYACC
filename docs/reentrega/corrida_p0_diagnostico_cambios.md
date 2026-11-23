# Corrida P0 — diagnóstico y cambios

Fecha: 23/11/2026  
Rama: `reentrega`  
Plan ejecutado: `docs/reentrega/plan_prompts_reentrega.md`, prompts `P0-01`, `P0-02`, `P0-03`.  
Alcance: **solo Ola 0**. No se ejecutaron P1–P6. No se generó ni modificó código Java.

---

## 1. Resumen

| Prompt | Estado | Resultado |
|---|---|---|
| P0-01 | Ejecutado | Baseline compilado y guardado fuera del repo. 6 salidas de consola + 2 `.asm`. |
| P0-02 | Verificado / ya absorbido | Política de IR escrita en el plan v2. Revisión de `docs/PROMPT-0.md` registrada abajo. |
| P0-03 | Verificado / ya absorbido | Decisión A (recursión aceptada) vigente en el plan. Pendiente de código en P5A-01. |

Cambios en el repo durante esta corrida: **ninguno sobre fuentes, tests ni docs existentes**.  
Único archivo nuevo: este documento.  
Nota de estado: al iniciar la sesión global `test/tp2/generales/21_enum_vacio.txt` aparecía modificado por el usuario; al cierre de esta corrida `git status --short` solo muestra el documento nuevo. No fui autor de ese cambio. El contenido usado como baseline es el que estaba en disco al ejecutar P0-01.

---

## 2. P0-01 — baseline

### 2.1 Comandos ejecutados

```bash
mkdir -p /tmp/opencode/tp3build_baseline /tmp/opencode/baseline
javac -encoding UTF-8 -d /tmp/opencode/tp3build_baseline \
      *.java "acciones semanticas"/*.java otras_acciones/*.java

cp test/tp1/verificacion_identificadores/caracter_invalido.txt \
   test/tp2/generales/17_asig_donde_igual.txt \
   test/tp2/generales/18_falta_orden.txt \
   test/tp2/generales/21_enum_vacio.txt \
   test_tp_4/Funciones_y_metodos/01_funcion_ulongint.txt \
   test_tp_4/Integracion/03_recursion_factorial_metodo.txt \
   /tmp/opencode/baseline/

for f in caracter_invalido.txt 17_asig_donde_igual.txt 18_falta_orden.txt \
         21_enum_vacio.txt 01_funcion_ulongint.txt 03_recursion_factorial_metodo.txt; do
  java -cp /tmp/opencode/tp3build_baseline Main "/tmp/opencode/baseline/$f" \
      > "/tmp/opencode/baseline/$f.out" 2>&1
done
```

Compilación: **sin errores**.

### 2.2 Artefactos

```text
/tmp/opencode/tp3build_baseline/     clases .class
/tmp/opencode/baseline/*.txt          fuentes copiadas
/tmp/opencode/baseline/*.txt.out      stdout TP3 completo
/tmp/opencode/baseline/01_funcion_ulongint.asm
/tmp/opencode/baseline/03_recursion_factorial_metodo.asm
```

Solo se escribieron `.asm` para los dos casos TP04: los otros cuatro tienen errores y `Compilador.java:141-144` bloquea la escritura. Esto confirma la compuerta actual.

### 2.3 Salidas relevantes

| Caso | Errores | IR observado |
|---|---|---|
| `caracter_invalido.txt` | `Lexema no reconocido ''` ×4, `Falta de ','` ×2, `Sentencia mal formada` ×2 | `(PROGRAMA)`, `(pout, mal, --)`, `(FIN_PROGRAMA)` |
| `17_asig_donde_igual.txt` | `Uso de ':=' donde debe usarse '='` | `(:=, y:main, 1$ul)`, `(:=, x:main, [2])` |
| `18_falta_orden.txt` | `Falta del orden de evaluacion y asignacion de parametros` | `(:=, x:main, foo:main)` |
| `21_enum_vacio.txt` | `Ausencia de valores para la enumeracion`, `tipo no declarado 'color'` | `(pout, x, --)`, `(pout, ok, --)` |
| `01_funcion_ulongint.txt` | ninguno | `FUNCION suma` → `(CALL, suma:main, 2)` |
| `03_recursion_factorial_metodo.txt` | ninguno | `FUNCION fact` sin `PAR`/`CALL` internos; `CALL` externo al main |

### 2.4 ASM baseline

`01_funcion_ulongint.asm:20-23`:

```asm
start:
fn_suma_main:
    push ebp
    mov ebp, esp
```

`03_recursion_factorial_metodo.asm:19-22`:

```asm
start:
fn_fact_main:
    push ebp
    mov ebp, esp
```

`fin_normal:` y `error_overflow_suma:` aparecen solo en `01_funcion_ulongint.asm` (hay chequeo runtime activo). Esto coincide con `GeneradorAssembler.java:562-566,588-591`.

### 2.5 Hashes de referencia

```text
1617e356a0ef0d2f4b69471314f409077c01673ad1672545c93f899febf3410a  01_funcion_ulongint.txt
830c3f623f9f706faa3f78a44c6f2079e472b2469225c82c63437a4b4caeff37  03_recursion_factorial_metodo.txt
120c0280956a31375ced2b35f5f271ab1dc9b56338370001b7aeb3a26cca4db7  17_asig_donde_igual.txt
38561952207f84a5c32e900f2b4ffc931a1e0b2ee78cc6733bc62d8f46caec22  18_falta_orden.txt
a514db04193d82e4f6ec698e24955e465bddaeb9f4562aa1cfb7737c8cb096ba  21_enum_vacio.txt
8ffe0f00d0641f940cfd44c64154b87bbc7e0269902ad84366a40da495a47c20  caracter_invalido.txt
f8bdc552ab3eb68882d09561cada48989862ce939212c4d498fc66955b1d03f9  01_funcion_ulongint.asm
aac7910fcdfe8bccf5b1675545296b849b815a9a0249b99c1f018937df2dd442  03_recursion_factorial_metodo.asm
```

---

## 3. P0-02 — política de IR ante errores

### 3.1 Estado

La política ya quedó escrita en `docs/reentrega/plan_prompts_reentrega.md:76-100`:

> Los errores se informan todos juntos; el análisis continúa y el programa se entrega completo. Una reducción inválida no aporta tercetos; las reducciones válidas posteriores sí.

No se agregó `if (reporte.hayErrores())` en ninguna etapa. No se usó `descartarDesde()`. No se tocó `21_enum_vacio.txt`.

Confirmación estática:

- `GeneradorTercetos.generar()` (`:147-207`) no consulta `Reporte`.
- `Compilador.java:93,107` imprime `contenido()` siempre.
- `Compilador.java:141-144` solo bloquea el `.asm`.

### 3.2 Revisión de `docs/PROMPT-0.md`

| Línea | Contenido | Evaluación bajo P0-02 |
|---|---|---|
| 119 | `r17/usa_asig_sin_anidamiento.txt`: 1 terceto, `Invalido` | El baseline real emite 2. Tras P2-03 debe ser 0 ejecutables. Requiere P2-01. |
| 121 | `r19/orden_sin_corchetes.txt`: 2; asignaciones desde `foo` | Describe exactamente el IR que P2-05 debe eliminar. Requiere P2-02. |
| 122 | `r19/sin_orden.txt`: 2; asignaciones desde `foo` | Igual que la fila 121. Requiere P2-02. |
| 126 | `r23/enum_vacio.txt`: Ninguno, `Invalido` | Conflicto conocido y **aceptado por decisión del usuario**. No se modifica. |

`docs/PROMPT-17.md:45` valida solo la presencia del mensaje; es compatible con la política.  
`docs/PROMPT-19.md:103` y `docs/PROMPT-23.md:101` solo validan mensajes; compatibles.

### 3.3 Conclusión P0-02

La política es consistente con el comportamiento deseado: **el programa se entrega completo** y solo se anula el constructor inválido. Los ajustes de expected results de TP02 quedan delegados a P2-01/P2-02. TP03 queda cerrado por P3-00.

---

## 4. P0-03 — recursión aceptada

### 4.1 Estado

Decisión A vigente en `docs/reentrega/plan_prompts_reentrega.md:101-119`. Rama B eliminada. Orden actualizado: `P5A-01 → P5A-04`.

### 4.2 Verificación estática

```bash
grep -rni "Recursion no soportada" --include=*.java --include=*.md .
```

Resultado:

- `0` coincidencias en `*.java`.
- Coincidencias solo en documentos: `ASSETS/DIAGNOSTICO_FUNCIONES_METODOS.md:393,699`, `ASSETS/recursion.md` y los propios documentos de `docs/reentrega/`.

`Entorno.java:174-179` sigue sin fallback:

```java
public FuncionDeclarada getFuncion(String clave) {
    if (clave == null) {
        return null;
    }
    return funciones.get(clave);
}
```

El comentario de `:165-172` sí describe el fallback a `pilaFunciones`. Cadena real de silencio:

1. `AccionesExpresiones.java:240` → `firma = null`.
2. `AccionesExpresiones.java:249-250` → llamada marcada inválida.
3. `Verificaciones.java:352-354,432-435` → `false` sin mensaje.
4. `GeneradorTercetos.java:342-344` → no emite `PAR`/`CALL`.

El IR baseline muestra `label_1:` y `label_2:` vacíos dentro de `fact`, confirmando el efecto.

### 4.3 Conclusión P0-03

Decisión registrada. Implementación pendiente en `P5A-01`. Documentación obsoleta pendiente en `P5A-02`. No se hizo nada de esto en esta corrida.

---

## 5. Cambios realizados en esta corrida

| Tipo | Cambio |
|---|---|
| Archivo nuevo | `docs/reentrega/corrida_p0_diagnostico_cambios.md` |
| Fuera del repo | `/tmp/opencode/tp3build_baseline/` |
| Fuera del repo | `/tmp/opencode/baseline/` (6 `.txt`, 6 `.out`, 2 `.asm`) |
| Repo | Sin cambios en `.java`, `.y`, tests ni docs existentes |

Estado final de git al cerrar esta corrida se deja registrado en la sección siguiente.

---

## 6. Estado y próximos pasos

Próximo prompt según el orden del plan:

```text
P1-01 → P1-02 → P1-03
```

Pendientes inmediatos después de P0:

1. Estado léxico 19 para `id#malo` (P1-01).
2. Anulación de `x := y := (...)` (P2-03, requiere regenerar `Parser.java`).
3. Anulación de `x := foo(1$ul)` (P2-05, sin regenerar Parser).
4. `start: → jmp main_inicio` en `GeneradorAssembler` (P4-01).
5. `getFuncion()` con fallback (P5A-01).

No se avanzó sobre ninguno de ellos en esta corrida.

### Estado final de git

```text
?? docs/reentrega/corrida_p0_diagnostico_cambios.md
```

Nota: la carpeta `docs/reentrega/` completa sigue sin seguimiento en git; el resto del árbol está limpio.
