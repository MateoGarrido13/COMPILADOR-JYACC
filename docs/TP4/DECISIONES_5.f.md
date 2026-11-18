# Decisiones y cambios — Paquete 5.f (TP4, Grupo 43, MASM32 32-bit)

## Resumen

El Paquete 5.f implementa el chequeo de **resultado negativo en restas de enteros sin signo `ulongint`** (condición **f** del Grupo 43).

Semántica: En cada resta de enteros sin signo (`SUB`) de `ulongint`, detectar si el minuendo es menor que el sustraendo (lo que produciría un resultado negativo, fuera del rango de `ulongint`). Si es así, saltar a error de resultado negativo.

Se activa solo cuando el programa fuente contiene al menos una resta `-` con tipo `ulongint`. La detección se hace en `calcularChequeosRuntime()` escaneando los tercetos del IR.

## Archivos .java modificados

| Archivo | Cambio | Utilidad |
|---------|--------|----------|
| `GeneradorAssembler.java` | En `calcularChequeosRuntime()`: se agregó detección de tercetos de resta `-` con tipo `ulongint` para activar el booleano `usaMsgNegativoResta`. | Determina si el programa fuente requiere el chequeo de resultado negativo en restas, evitando emitir mensajes y labels innecesarios en programas que no usan restas de enteros sin signo. |
| `GeneradorAssembler.java` | En `emitirAritmetica()`, caso `"-"` con tipo `ulongint`: tras emitir `sub eax, op2`, se inserta `jb error_negativo_resta` cuando `usaMsgNegativoResta` está activo. | Inserta la guardia de resultado negativo en tiempo de ejecución: si el minuendo es menor que el sustraendo (préstamo CF=1), salta al label de error. El uso de `jb` es correcto para aritmética sin signo, ya que detecta la condición de préstamo en la resta. |

No se modificaron `TablaSimbolos.java`, `EntradaTabla.java`, `Entorno.java` ni ningún otro archivo `.java`. El Paquete 5.0 ya provee el mensaje `msg_negativo_resta` y el label `error_negativo_resta:` que este paquete consume.

## Descripción y utilidad de los cambios

### ¿Por qué se efectuaron estos cambios?

El Grupo 43 requiere chequeos de errores en tiempo de ejecución. La condición **f** especifica que para restas de datos de tipo entero sin signo se debe controlar si el resultado sería negativo. Sin este chequeo, una operación como `10 - 20` produciría un valor envuelto (wrap-around) a un número grande sin advertencia.

### Utilidad de cada cambio

1. **Detección automática en `calcularChequeosRuntime()`**: Evita emitir el mensaje de error y el label de fallo en programas que no contienen restas de `ulongint`, manteniendo el `.asm` limpio y minimizando el tamaño del código generado.

2. **Guardia `jb error_negativo_resta` tras `sub`**: La instrucción `sub` de x86 establece el flag de acarreo/préstamo (CF) cuando el minuendo es menor que el sustraendo en aritmética sin signo. El salto condicional `jb` (jump if below) detecta esta condición y transfiere el control al label de fallo, que imprime el mensaje y termina con `ExitProcess, 3`.

3. **Uso de registros del procesador (EAX)**: Conforme a la restricción del TP4, las operaciones enteras usan registros del procesador (EAX para el acumulador), y el chequeo de resultado negativo usa la bandera de estado del procesador (CF), no el coprocesador 80x87.

## Archivos .java creados

Ninguno. Todos los cambios de código están en `GeneradorAssembler.java` (ya existente).

## Archivos no-.java creados

| Archivo | Descripción |
|---------|-------------|
| `docs/TP4/DECISIONES_5.f.md` | Este documento. |
| `test_tp_4/resta_enteros_negativa/01_resta_valida.txt` | Caso de prueba: resta `ulongint` que NO produce negativo (`20 - 10`). Verifica que se genera el código de chequeo `jb error_negativo_resta` tras el `sub`. |
| `test_tp_4/resta_enteros_negativa/02_resta_negativa.txt` | Caso de prueba: resta `ulongint` QUE produce negativo (`10 - 20`). Verifica que se genera el código de chequeo `jb error_negativo_resta`. |
| `test_tp_4/resta_enteros_negativa/01_resta_valida.asm` | Snapshot generado del caso válido. |
| `test_tp_4/resta_enteros_negativa/02_resta_negativa.asm` | Snapshot generado del caso que produce negativo. |
| `test_tp_4/probar_prompt5_f.sh` | Script de verificación del Paquete 5.f. |

## ## TEST PRUEBA

Las pruebas se encuentran en `test_tp_4/resta_enteros_negativa/` y se ejecutan con:

```bash
bash test_tp_4/probar_prompt5_f.sh
```

### Descripción de cada prueba y resultado esperado

| Archivo | Utilidad | Resultado esperado |
|---------|----------|-------------------|
| `test_tp_4/resta_enteros_negativa/01_resta_valida.txt` + `.asm` | Valida que una resta `ulongint` sin desbordamiento (`20 - 10`) genere el código de chequeo `jb error_negativo_resta` tras el `sub eax, ...`, pero como el resultado no es negativo, el salto no se tomaría en tiempo de ejecución. El .asm debe contener `sub eax, c_10_ul_ul`, `jb error_negativo_resta`, `msg_negativo_resta` en `.DATA` y el label `error_negativo_resta:` en `.CODE`. | Código de salida 0 del script; todas las verificaciones `verificar` reportan `OK`. |
| `test_tp_4/resta_enteros_negativa/02_resta_negativa.txt` + `.asm` | Valida que una resta `ulongint` que produciría negativo (`10 - 20`) genere el mismo código de chequeo `jb error_negativo_resta` tras el `sub eax, ...`. En este caso, el salto sí se tomaría en tiempo de ejecución porque el resultado es negativo (fuera de rango para `ulongint`). | Código de salida 0 del script; todas las verificaciones `verificar` reportan `OK`. |
| `test_tp_4/probar_prompt5_f.sh` | Script de verificación que compila el proyecto, ejecuta los 2 casos, verifica los patrones en los `.asm` y ejecuta las regresiones de todos los prompts anteriores (5.0, 5.b, 5.e, 4e, 4d, 4c, 4b, 4a, 3, 2). | Código de salida 0; todas las verificaciones `OK`; 0 `FALLA`. |

## Riesgos abiertos

1. **Códigos de salida de `ExitProcess`**: el valor 3 es provisional; la cátedra/Grupo 43 puede exigir otro. Ajustar si hay un contrato definido.
2. **Solo resta `-`**: el chequeo se aplica únicamente al operador `-` con tipo `ulongint`. Las sumas (`+`, paquete 5.b) y productos (`*`, paquete 5.e) se tratan en sus respectivos paquetes.
3. **Idioma de los mensajes**: los mensajes están en español; revisar si la cátedra requiere inglés u otro idioma.
