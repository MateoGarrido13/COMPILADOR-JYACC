## DEC-9: Métodos de la asignación estructural de objetos (Prompt 3.8 / Tema 25)

Inventario de lo agregado para comparar dos clases por estructura y emitir
`(COPY, destino, origen)`. La regla D4 y los casos están en `docs/PROMPT-25.md`.

`ExpresionDiferida` y `Terceto` no recibieron métodos: `COPY` entra en
`ListaTercetos.crear` como cualquier operador.

### `Entorno`

| Método | Visibilidad | Para qué |
|---|---|---|
| `claseDe(String tipo)` | público | Devuelve la `ClaseDeclarada` de una clave ya resuelta (`ca:main`). Si el tipo es básico o enumerado, devuelve `null`, y la asignación sigue por el camino de tipos existente. |
| `clasesCompatibles(ClaseDeclarada a, ClaseDeclarada b)` | público | D4. La misma clave de clase es compatible. Si no, exige la misma cantidad de atributos efectivos con el mismo orden de tipos. No compara nombres ni firmas de métodos. |
| `tiposDeAtributos(ClaseDeclarada)` | privado | Devuelve la lista de tipos de los atributos efectivos, en orden base primero. Es lo que compara `clasesCompatibles`. |
| `atributosEfectivos(ClaseDeclarada)` | privado | Arma el mapa ordenado nombre → tipo de los atributos, propios y heredados. |
| `recolectarAtributos(...)` | privado | Recorre `heredaDe` y después pisa con los atributos propios del mismo nombre, sin moverlos de posición. |
| `tipoDeMiembro(String)` | privado | Una clase o un enumerado se compara por su clave mangled. Un tipo básico queda como está. |

### `Verificaciones`

| Método | Visibilidad | Para qué |
|---|---|---|
| `errorEstructuraIncompatible(int, String, String, String)` | público | Informa que las dos clases no tienen la misma estructura y no se genera terceto de asignación. El mensaje es `Incompatibilidad estructural en la asignación a '<destino>'`. |

### `Asignacion` (clase nueva)

| Método | Visibilidad | Para qué |
|---|---|---|
| `Asignacion(Object destino, String tipoDestino, String tipoOrigen)` | público | Lleva al generador el destino ya resuelto y los dos tipos. Los campos son `destino`, `tipoDestino` y `tipoOrigen`. |

La acción de `ASIGNACION` en `AccionesDeclaraciones` no tiene un método nuevo.
Cuando `claseDe` resuelve los dos lados y `clasesCompatibles` acepta el par,
esa acción devuelve `new Asignacion(...)`.

### `GeneradorTercetos`

No se agregó un método. `generarAsignacion(Reduccion)` ya existía. Si el
resultado es una `Asignacion`, el operador del terceto pasa a ser `COPY`.
Una asignación entre tipos básicos o enumerados sigue emitiendo `:=`.
