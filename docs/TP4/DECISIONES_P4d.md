# Decisiones y cambios — Prompt 4d (TP4, objetos/atributos, layout y COPY, MASM32 32-bit)

## Resumen

Se implementó la traducción de objetos y atributos en `GeneradorAssembler.java`. El backend ya emite:

* **ReferenciaAtributo** (`obj.atr`, `obj.clase.atr`) resuelta al nombre ASM de la entrada **del objeto** en la Tabla de Simbolos (`p1_x_main`, `p1_padre_y_main`, etc.). Se busca por `ref.texto` en el ámbito de `ref.objeto`; solo como fallback se usa `ref.atributo` (declaración de la clase).
* **Lectura y escritura de atributos**: `:=` sobre `ReferenciaAtributo` genera `mov eax, <origen> / mov <dest>, eax` (ulongint) o `fld/fstp` (doublef).
* **COPY estructural**: copia **posicional** campo-a-campo entre objetos compatibles (mismo layout de tipos, bases primero y propios después), un `mov` por atributo ulongint/enum y dos `mov` por atributo doublef (high dword primero). Los nombres de los atributos no tienen que coincidir (corrección aplicada al cerrar 4e).
* **CLASE / FIN_CLASE**: no-op en `.CODE` (solo marcadores); los atributos de objetos se emiten en `.DATA` vía `emitirData()`.
* **Layout de objeto**: los atributos se calculan recorriendo primero las bases (`heredaDe`, en orden) y luego los atributos propios de cada clase. `ulongint`/enum = 4 bytes (`DD`), `doublef` = 8 bytes (`DQ`).

No se modificó la salida observable de TP3 (fuente numerado, tokens, estructuras, errores, TS, tercetos). El cambio en `.asm` respecto de 4c es que `COPY`, `CLASE`, `FIN_CLASE` y accesos `obj.atr` ya no quedan como comentarios. En 4e además se eliminaron los comentarios `PROGRAMA`/`FIN_PROGRAMA`, por lo que los `.asm` de 4d se regeneraron.

## Archivos modificados / creados

| Archivo | Cambio |
|---------|--------|
| `GeneradorAssembler.java` | Caso `COPY` en `emitirCodigo()`; nuevo `emitirCopiar()` (copia posicional campo-a-campo); `CLASE`/`FIN_CLASE` como no-op; resolucion de `ReferenciaAtributo` en `operandoAsm()` hacia la entrada TS del objeto. |
| `docs/TP4/DECISIONES_P4d.md` | Este documento (creado en este prompt). |
| `docs/TP4/PlanTP4.md` | Prompt 4d marcado como ejecutado. |
| `docs/TP4/TEMPLATES_ASM.md` | Templates T-24..T-27 (layout objeto, acceso atributo, COPY, CLASE/FIN_CLASE). |
| `test_tp_4/Objetos y atributos/*.txt` | 4 casos manuales nuevos (creados en este prompt). |
| `test_tp_4/probar_prompt4d.sh` | Script de verificacion del Prompt 4d (creado en este prompt). |

## Contrato IR consumido

| Terceto | Forma | Uso en ASM |
|---------|-------|------------|
| `CLASE` | `(CLASE, nombre, --)` | No-op en `.CODE` (marcador). |
| `FIN_CLASE` | `(FIN_CLASE, nombre, --)` | No-op en `.CODE` (marcador). |
| `:=` (destino `ReferenciaAtributo`) | `(:=, objeto.atributo, valor)` | `mov <dest>, eax` / `fstp <dest>` usando nombre ASM del atributo. |
| `COPY` | `(COPY, destinoObjeto, origenObjeto)` | Copia campo-a-campo entre objetos compatibles. |

## Layout de objeto en `.DATA`

Cada atributo de objeto se materializa como una entrada independiente en la TS (`declararAtributoDeObjeto`) con nombre calificado `objeto.atributo` o `objeto.clase.atributo`, ambito del objeto, y uso `ATRIBUTO`. En `.DATA` se emite una linea por atributo:

* `ulongint` / enum → `<nombreAsm> DD ?`
* `doublef` → `<nombreAsm> DQ ?`

El nombre ASM se deriva del lexema calificado (`p1.x:main` → `p1_x_main`) mediante `TablaSimbolos.nombreAsmDe()` / `sanitizarBaseAsm()`.

**Orden de atributos** (para COPY y layout mental): bases primero siguiendo `heredaDe` de forma recursiva, luego los atributos propios de la clase. Un atributo propio con mismo nombre que uno heredado reemplaza al heredado sin moverlo de posicion.

Ejemplo para `class hija extends padre` donde `padre { ulongint a; }` y `hija { ulongint b; }`:

```
; Layout de hija (base padre primero, luego propia)
[padre.a]   <- 4 bytes (heredado)
[hija.b]    <- 4 bytes (propio)
```

## ReferenciaAtributo -> ASM

En `operandoAsm()`, cuando el operando es una `ReferenciaAtributo`, se busca primero `tablaSimbolos.buscarIdentificador(ref.texto, ref.objeto.ambito)`, es decir la entrada materializada **`objeto.atributo`** o **`objeto.clase.atributo`** creada por `Entorno.materializarAtributosDeObjeto()`. A esa entrada se le aplica `nombreAsmDe()`. Solo si no aparece se usa `nombreAsmDe(ref.atributo)` (declaración de la clase) y, como último recurso, `ref.texto` (vista IR, no identificador MASM válido).

* Lectura: `mov eax, <nombreAsmAtributo>` o `fld qword ptr <nombreAsmAtributo>`
* Escritura: `mov <nombreAsmAtributo>, eax` o `fstp qword ptr <nombreAsmAtributo>`

## COPY estructural

Se ejecuta cuando el destino de una asignacion entre objetos es una `EntradaTabla` (variable de clase), no una `ReferenciaAtributo`. El generador de tercetos (`GeneradorTercetos.generarAsignacion`) emite `(COPY, destino, origen)` para objetos compatibles.

`emitirCopiar()`:
1. Resuelve las clases de destino y origen via `entorno.claseDe(tipo)`.
2. Calcula el layout efectivo de cada una con `atributosEfectivos()`: bases primero (recursivo por `heredaDe`), luego atributos propios; un propio con el mismo nombre reemplaza al heredado sin moverlo de posición.
3. Recorre ambos layouts **en paralelo por posición**. Para cada posición busca la entrada TS del objeto correspondiente (`claveAtributoDeObjeto()` produce `objeto.atributo` o `objeto.base.atributo`).
4. Emite `mov eax, <origen> / mov <destino>, eax` (ulongint/enum) o dos `mov` de dwords con `dword ptr [<sym>+4]` y `dword ptr [<sym>]` (doublef, high primero).

**Compatibilidad**: dos clases son compatibles si son la misma clase o tienen la misma cantidad de atributos efectivos en el mismo orden de tipos (ver `Entorno.clasesCompatibles()`). Los **nombres** de los atributos no participan; por eso la copia es posicional. Esta corrección se incorporó al cerrar 4e: antes, `b := a` entre clases con layouts compatibles pero nombres distintos emitía `; COPY (atributo no encontrado en TS: b.y / a.y)` y no copiaba.

## Correlacion con los prompts 4e y 5/6

| Prompt | Punto de contacto | Preparacion dejada |
|--------|-------------------|---------------------|
| **4e** (integracion) | `emitirCodigo()` despacha `COPY`, `CLASE`, `FIN_CLASE`. | Objetos con atributos y copia estructural ya funcionan en el backend ASM. |
| **5** (runtime) | Guardas `b/e/f/h` pueden insertarse en lecturas/escrituras de atributos. | Los nombres ASM de atributos ya son estables. |
| **6** (contrato final) | Revisar sintaxis MASM real en Windows y CRLF. | Ver riesgos abajo. |

## Verificacion realizada

Compilacion:

```bash
javac -encoding UTF-8 -d /tmp/tp4p4d_build *.java "acciones semanticas"/*.java otras_acciones/*.java
```

Script nuevo:

```bash
bash test_tp_4/probar_prompt4d.sh   # OK (incluye regresiones 4c, 4b, 4a, 3 y 2)
```

## Riesgos abiertos

1. **Atributos dispersos en `.DATA`**: los atributos de un objeto no forman un bloque contiguo en memoria, sino simbolos planos dispersos. El `rep movsd` (bloque) no se usa; la copia es campo-a-campo. Si en el futuro se requiere layout contiguo (`STRUCT` MASM), hay que reescribir `emitirCopiar()` y `emitirData()`.
2. **MASM real no verificado**: este entorno solo valida texto `.asm` y compilacion Java. Revisar en Windows las formas `mov eax, dword ptr [<atributo>+4]` / `mov dword ptr [<atributo>+4], eax` usadas para la copia de `doublef`.
3. **Nombres ASM de atributos**: dependen de `sanitizarBaseAsm()` que reemplaza `.` y `:` por `_`. Si dos objetos tienen atributos con nombres que colisionan tras sanitizacion, `TablaSimbolos.nombreAsmDe()` agrega sufijos numericos.
4. **COPY sobre objetos locales en stack**: `operandoAsm()` resuelve atributos via `nombreAsmDe` (siempre `.DATA`), incluso dentro de funciones. Si un objeto se declara local en una funcion, su atributo seguiria resolviendo a `.DATA`. Esto es consistente con la decision de 4c de que parametros/locales de tipo basico van al stack, pero objetos quedan en `.DATA`.

## TEST PRUEBA

### Script a ejecutar

```bash
bash test_tp_4/probar_prompt4d.sh
```

### Pruebas incluidas (4 casos en `test_tp_4/Objetos y atributos/`)

| Caso | Archivo | Qué valida |
|------|---------|------------|
| 01 | `01_copia_estructural.txt` / `.asm` | Asignacion estructural entre objetos compatibles (COPY campo-a-campo), pout de atributos destino |
| 02 | `02_atributo_doublef.txt` / `.asm` | Atributo `doublef` en objeto: escritura (`fstp`) y lectura (`pout` con `fmt_doublef`) |
| 03 | `03_herencia_atributos.txt` / `.asm` | Herencia: atributo heredado con prefijo clase (`h.padre.a`), atributo propio, escritura y lectura de ambos |
| 04 | `04_copia_con_herencia.txt` / `.asm` | COPY estructural con herencia (atributo heredado `base.x` + propio `y`) |

### Regresiones ejecutadas por el script

| Script | Qué valida |
|--------|------------|
| `probar_prompt4c.sh` | Funciones, metodos, stack frame, PARAM/CALL/ret, `this` |
| `probar_prompt4b.sh` | `pout` por consola (ulongint, doublef, cadena) |
| `probar_prompt4a.sh` | Control de flujo y comparadores |
| `probar_prompt3.sh` | Traducción básica a .CODE: aritmética, asignación |
| `probar_prompt2.sh` | Generación de esqueleto MASM32 |

### Resultados esperados

- **Código de salida**: `0` en todos los scripts.
- **Compilación Java**: exitosa sin errores.
- **Casos 4d**: 4 casos compilados sin errores (`Error:` ausente en la salida).
- **Verificaciones `.asm`**: todos los patrones `verificar` / `no_verificar` deben reportar `OK`.
- **Regresiones**: los 5 scripts anteriores deben reportar `OK` (sin regresiones introducidas por 4d).

### Resultado esperado del script completo

```
=== Prompt 4d de TP4 — Prueba de objetos y atributos ===
...
   OK: todas las verificaciones del Prompt 4d pasaron.
...
=== Prueba del Prompt 4d de TP4 completada ===
```
