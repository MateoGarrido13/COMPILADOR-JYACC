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
- Si no son la misma clase, se compara el conjunto efectivo:
  - atributos por nombre y tipo, incluidos los heredados que ya estén en `heredaDe`;
  - métodos por nombre, tipo de retorno y tipos de los parámetros.
- El nombre del parámetro no entra en la firma. El orden de los parámetros sí.
- El orden de declaración de atributos y métodos no importa: es un conjunto.
- Un miembro propio con el mismo nombre pisa al heredado.
- El tipo de un miembro que es clase o enumerado se compara por su clave mangled,
  no por el tipo base del enumerado.
- Misma estructura y distinto nombre de clase es compatible. Estructura distinta
  es error y no genera terceto.

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
| `misma_estructura.txt` | Válido entre clases de distinto nombre con los mismos atributos y la misma firma. |
| `orden_atributos.txt` | Válido aunque `x` e `y` estén declarados en distinto orden. |
| `estructuras_distintas.txt` | Error estructural. Sin terceto de asignación. |
| `firmas_distintas.txt` | Mismos atributos y distinto tipo de retorno del método: error estructural. |
| `objeto_desde_ulongint.txt`, `objeto_desde_constante.txt`, `basico_desde_objeto.txt` | Error de tipos. Sin `COPY` ni `:=`. |
| `mismo_nombre_ambitos.txt` | `ca:main` y `ca:main:probar` conviven. Cada variable usa la clave de su ámbito y cada asignación genera `COPY`. |

`todf_asignacion.txt` sigue emitiendo `(:=, x:main, 1$ul)`. Los enumerados de
`verificacion_r23_enums` siguen asignando con `(:=)`.
