# Decisiones del Prompt 6 (paquetes 6.0 a 6.3)

**Estado**: 6.0, 6.1, 6.2 y 6.3 ✅ ejecutados. 6.4 ⏳ no ejecutado (requiere
«**BORRAR COMENTARIOS**»). Ensamblado/enlace real ⚠️ pendiente de Windows.

Este archivo resume las decisiones transversales del cierre. El detalle por
paquete sigue en `DECISIONES_6.0.md`, `DECISIONES_6.1.md` y
`MATRIZ_PRUEBAS_6.2.md`.

---

## 1. Resumen por paquete

| Paquete | Objetivo | Resultado | Evidencia |
|---------|----------|-----------|-----------|
| 6.0 | Compuerta de errores + ruta robusta del `.asm` | ✅ | `probar_prompt6_0.sh`, exit 0 |
| 6.1 | Validación estática MASM32 y CRLF al escribir | ✅ (estático) | `probar_prompt6_1.sh`, exit 0 |
| 6.2 | Regresión TP3 + b/e/f/h + programa integral | ✅ | `probar_prompt6_2.sh`, 137 fuentes, 0 `FALLA:` |
| 6.3 | TODOs, comentarios profundos, docs de uso y estado final | ✅ | este archivo + `USO.md` + `ESTADO_FINAL.md` |
| 6.4 | Eliminación de comentarios | ⏳ | Sin ejecutar; permiso explícito pendiente |

---

## 2. Contrato de salida (desviación declarada)

El enunciado TP4 pide como salida los errores, la representación intermedia, la
TS y el archivo ASM, **sin tokens ni estructuras**. El usuario pidió conservar
la salida del TP3 y eliminarla manualmente antes de entregar. Por eso:

- `Compilador.compilar()` mantiene el bloque marcado que imprime fuente
  numerado, tokens, estructuras, errores, TS y tercetos.
- No se agregó ningún mensaje nuevo de estado del `.asm` (`ASM generado:`,
  `ASM no generado:` ni similares). Los errores de escritura van a `stderr`.
- `probar_prompt4e.sh` y sus 5 snapshots canónicos se restauraron a `HEAD`;
  6.2 comprueba orden TP3 y determinismo sin inventar golden files nuevos.

Esta es la **única desviación intencional** respecto del enunciado y queda
registrada para que el usuario la recorte en el momento de la entrega.

---

## 3. Decisiones de genericación ASM (6.1, ratificadas en 6.3)

1. **CRLF solo al escribir.** `GeneradorAssembler.generar()` sigue devolviendo
   texto con `\n`; `escribirArchivo()` normaliza a `\r\n`. Así las comparaciones
   de texto y los snapshots no cambian, y MASM recibe terminadores estándar.
2. **Sin `ALIGN 8`.** Con `.386`, la alineación de segmento es DWORD (4). Los
   `doublef` se declaran `DQ` y se copian con dos `mov dword ptr` (`[s+4]` y
   `[s]`). Agregar alineación cambiaría offsets ya probados sin aportar
   corrección en x86.
3. **`crt_printf` con `DQ` por valor.** Es `PROTO C :VARARG`; recibe los 8 bytes
   del double en la pila. No se carga la FPU ni se pasa por EAX. Queda pendiente
   comprobar el `%f` con `ml.exe` real.
4. **Llamadas internas cdecl-like.** El caller reserva y limpia argumentos para
   que lo mismo sirva a funciones y a `crt_printf` varargs. El `.model flat,
   stdcall` del encabezado rige las APIs de `masm32rt.inc`, no esas llamadas.
5. **Sin vtable.** El IR trae el método resuelto tras el análisis semántico;
   el despacho directo a `mt_<clave>` evita duplicar la resolución.
6. **Símbolos planos.** Atributos `objeto.base.atributo` se aplanan en `.DATA`
   y `COPY` copia campo por campo en orden de declaración. No hay `STRUCT`.
7. **x87 FPU.** `.386` + `masm32rt.inc`; comparaciones con FLD/FCOMP/FSTSW/SAHF
   y overflow float con FXAM + JC sobre C0. No se usó SSE.
8. **Trazabilidad conservada.** Los comentarios `; (PAR, a:suma, x:main)` se
   emiten a propósito; 6.3 los documentó como decisión y 6.4 deberá conservarlos.

---

## 4. Decisiones de pruebas (6.2)

1. **Sin snapshots por tema.** No existían salidas golden para los ~90 fuentes
   de los temas 13–33. Se prefirió determinismo entre dos corridas + presencia
   del orden TP3 + los 5 snapshots de 4e, antes que inventar salidas esperadas.
2. **"Disparar" = guardia presente + valor de fuente que la activa.** Sin
   `ml.exe` no se puede observar el salto real. La matriz lo declara así y deja
   la confirmación runtime para Windows.
3. **Programa integral en `/tmp`.** El script no depende del `.asm` versionado
   junto al `.txt`; ese artefacto lo regenera `probar_prompt4e.sh`.
4. **Suite reutilizable.** `probar_prompt6_2.sh` es el punto de entrada único
   que 6.4 debe volver a correr para comprobar que borrar comentarios no rompió
   nada.

---

## 5. Decisiones de cierre (6.3)

1. **No había TODOs reales en Java.** La búsqueda `\bTODO\b|FIXME|XXX` dio 0
   coincidencias en `*.java`. Las apariciones en docs son menciones históricas;
   la única desactualizada era `test_tp_4/guia.md:32` y se corrigió. No se borró
   código por este concepto.
2. **Profundizar, no podar.** Se ampliaron cabeceras y comentarios de
   `Compilador.java`, `Main.java`, `GeneradorAssembler.java`, `Entorno.java`,
   `TablaSimbolos.java` y `GeneradorTercetos.java` con precondiciones, efectos,
   convenciones, decisiones no obvias, trazabilidad IR→ASM y riesgos. El borrado
   de ruido queda para 6.4, con permiso explícito.
3. **Documentación de uso separada del plan.** `docs/USO.md` contiene los
   comandos concretos; `PlanTP4.md` sigue siendo el historial de ejecución.
4. **Comentario stale corregido.** `TablaSimbolos.tipoAsmDe()` ya no dice que
   el layout de objetos está "pendiente de Prompt 4": los objetos se reservan
   como `DD` en `emitirData()` y sus atributos se materializan con tipo efectivo.

---

## 6. Limitaciones conocidas

- No hay `ml.exe`, `ml64.exe` ni SDK MASM32 en este entorno (`/masm32` no
  existe). `link` y `wine` están, pero no bastan.
- No se verificó ensamblado, enlace ni ejecución real de ningún `.asm`.
- No se comprobó en ejecución el salto de las guardias b/e/f/h ni la impresión
  de `%f`/`%u`.
- Recursión profunda consume stack frames sin chequeo de desbordamiento; el
  runtime check (i) no pertenece al grupo 43.
- `TablaSimbolos` depende de orden de inserción estable para `.DATA`; es
  determinista, pero no necesariamente el orden textual del fuente.
- Las brechas 1–12 de `ESTADO_RUNTIME_P5.md` §7 siguen documentadas como
  heredadas, no resueltas en 6.2 ni 6.3.

---

## 7. Pasos manuales pendientes en Windows

```bat
ml.exe /c /coff /Fo caso.obj caso.asm
link.exe /SUBSYSTEM:CONSOLE /OUT:caso.exe caso.obj
caso.exe
```

Registrar salida de `ml`/`link`, códigos de salida, impresión de `%f` y el
disparo real de b/e/f/h. Hasta entonces los paquetes 6.1 y 6.2 están
**validados estáticamente, no ensamblados**.

---

## 8. Archivos tocados en 6.3

- Modificados: `Compilador.java`, `Main.java`, `GeneradorAssembler.java`,
  `Entorno.java`, `TablaSimbolos.java`, `GeneradorTercetos.java`,
  `docs/TP4/PlanTP4.md`, `docs/TP4/ESTADO_RUNTIME_P5.md`, `test_tp_4/guia.md`.
- Nuevos: `docs/USO.md`, `docs/TP4/DECISIONES_P6.md`,
  `docs/TP4/ESTADO_FINAL.md`.
- Sin cambios funcionales: solo comentarios y documentación.
