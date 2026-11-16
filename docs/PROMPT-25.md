# Prompt 3.8: asignación de objetos

## Alcance

Se implementó el Tema 25: asignación por estructura entre objetos. Este prompt
no cubrió el acceso a atributos ni la herencia. El acceso `objeto.atributo`
quedó resuelto en el Prompt 3.9 (`docs/PROMPT-28.md`). La herencia sigue en el 31.

La regla de compatibilidad es la D4 del plan. El enunciado del prompt la llama
DEC-6. En este repositorio `docs/DEC-6.md` ya documenta el tipado de constantes
en el léxico, así que la regla queda en este archivo y no se reescribe DEC-6.
El inventario de métodos agregados está en `docs/DEC-9.md`.

## Regla D4

`Entorno.clasesCompatibles(ClaseDeclarada a, ClaseDeclarada b)`:

- Dos variables de la misma clase (misma clave `nombre:ambito`) son compatibles.
- Si no son la misma clase, se compara la **lista efectiva de tipos de atributos**:
  - misma cantidad de atributos;
  - mismo tipo en cada posición, según el orden de definición.
- Los nombres de los atributos no participan. Tampoco las firmas de los métodos:
  dos clases con métodos distintos siguen siendo compatibles si sus atributos
  coinciden en cantidad y orden de tipos.
- Los atributos heredados se incluyen en orden base primero. Un atributo propio
  con el mismo nombre reemplaza al heredado sin moverlo de posición.
- El tipo de un miembro que es clase o enumerado se compara por su clave mangled,
  no por el tipo base del enumerado.
- Misma cantidad y orden de tipos con distinto nombre de clase es compatible.
  Distinta cantidad u orden de tipos es error y no genera terceto.

Ejemplo: si el primer atributo de `ca` es `ulongint`, el primer atributo de `cb`
también debe ser `ulongint`; lo mismo para cada posición siguiente.

### Revisión de la regla

La primera implementación del Prompt 3.8 comparaba el conjunto efectivo de
atributos por nombre y tipo, y además exigía que coincidieran las firmas de los
métodos. Se ajustó para que la asignación entre objetos dependa solo de la
cantidad de atributos y del orden de sus tipos, que es la regla pedida para el
Tema 25. El cambio quedó en `Entorno.clasesCompatibles`: ahora construye la
lista de tipos efectivos y la compara; se retiraron `firmasEfectivas`,
`recolectarFirmas` y `firmaDe`, que ya no tenían otro uso. El mensaje de error y
la emisión de `(COPY, destino, origen)` no cambiaron.

La copia no recorre los atributos. `GeneradorTercetos.generarAsignacion` emite
un solo terceto `(COPY, destino, origen)` cuando el destino de la `Asignacion`
es una variable de clase. Desde el Prompt 3.9, si ese destino es una
`ReferenciaAtributo`, el mismo método emite `(:=, objeto.atributo, valor)` y no
expande la copia en un `(:=)` por atributo. `Terceto` ya imprime cualquier
operador, y `ExpresionDiferida` no necesita marcar la copia: la acción devuelve
una `Asignacion` con el destino y los dos tipos.

## Declaración de objetos

`DECL_OBJETOS` ya resolvía el nombre con Name Mangling. Si el nombre no existe,
el error sigue siendo `tipo no declarado` y la variable no se declara. Si
existe y es `Uso=CLASE`, la variable guarda la clave de esa clase (`ca:main`).

La misma producción gramatical (`ID lista_variables`) declara los enumerados
del tema 23. Un typedef sigue siendo válido ahí: rechazar todo lo que no sea
`Uso=CLASE` rompería `pares p`. Un nombre que existe y no es clase ni
enumerado sigue informando `tipo no declarado`.

## Asignación

Si destino y origen son clases, se usa D4. Si no son compatibles:

```
Incompatibilidad estructural en la asignación a '<destino>': '<clase destino>' y '<clase origen>' no tienen la misma estructura
```

Si un lado es clase y el otro es `ulongint`, `doublef` o una constante, sigue
el error de tipos existente y no se emite `COPY` ni `:=`. Una asignación entre
`ulongint` o entre enumerados no pasa por esta rama y conserva el `(:=)`.

## Pruebas

Casos en `test/tp2/verificacion_r25_objetos/`.

| Caso | Resultado |
|---|---|
| `copia_misma_clase.txt` | Válido. `(COPY, a1:main, a2:main)`. |
| `misma_estructura.txt` | Válido entre clases de distinto nombre con los mismos atributos en el mismo orden de tipos. |
| `mismo_orden_tipos_distintos_nombres.txt` | Válido: cambian los nombres de los atributos, no la cantidad ni el orden de sus tipos. |
| `orden_atributos.txt` | Error estructural: `ulongint, doublef` contra `doublef, ulongint`. |
| `distinta_cantidad_atributos.txt` | Error estructural: dos atributos contra uno. |
| `distinto_tipo_en_posicion.txt` | Error estructural: el primer atributo cambia de tipo. |
| `atributos_heredados_orden.txt` | Válido: el atributo heredado cuenta primero y luego el propio. |
| `estructuras_distintas.txt` | Error estructural. Sin terceto de asignación. |
| `firmas_distintas.txt` | Válido: los atributos coinciden y las firmas de los métodos no participan. |
| `objeto_desde_ulongint.txt`, `objeto_desde_constante.txt`, `basico_desde_objeto.txt` | Error de tipos. Sin `COPY` ni `:=`. |
| `mismo_nombre_ambitos.txt` | `ca:main` y `ca:main:probar` conviven. Cada variable usa la clave de su ámbito y cada asignación genera `COPY`. |

`todf_asignacion.txt` sigue emitiendo `(:=, x:main, 1$ul)`. Los enumerados de
`verificacion_r23_enums` siguen asignando con `(:=)`.
