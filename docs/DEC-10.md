## DEC-10: Entradas de atributos por objeto en la Tabla de Símbolos

### Problema

La declaración de una clase registraba cada atributo una sola vez, con una
clave de clase (`x:main:ca`). El acceso `a1.x` lo usaba para conocer el tipo,
pero el léxico creaba además un placeholder `x:main` por el identificador
`x`. Ese placeholder quedaba con `Tipo de dato: indefinido`, `Uso:
indefinido` y `Declarado: no`. Además, todos los objetos de la misma clase
compartían esa misma clave: `a1.x` y `a2.x` no tenían entradas propias.

### Decisión

Al declarar un símbolo de tipo clase se materializan en la Tabla de Símbolos
las entradas de sus atributos:

- atributo propio: `<objeto>.<atributo>:<ambito>`;
- atributo heredado: `<objeto>.<claseQueLoDeclara>.<atributo>:<ambito>`.

Ejemplos:

| Declaración | Entradas de atributos |
|---|---|
| `class ca begin ulongint x; ulongint z; end; ca a1, a2;` | `a1.x:main`, `a1.z:main`, `a2.x:main`, `a2.z:main` |
| `class padre begin ulongint y; end; class hija begin ulongint x; extends padre; end; hija a1;` | `a1.x:main`, `a1.padre.y:main` |
| `class b1 begin ulongint y; end; class b2 begin ulongint y; end; class ca begin extends b1, b2; end; ca a1;` | `a1.b1.y:main`, `a1.b2.y:main` |

Cada entrada lleva el tipo declarado del atributo, uso `atributo`,
`Declarado: si` y la línea de la declaración del objeto. El ámbito es el del
objeto, así que dos objetos con el mismo nombre en ámbitos distintos no
colisionan.

La entrada de clase (`x:main:ca`) se conserva: sigue siendo la declaración
del miembro. Las entradas por objeto son las que representan el estado de
cada instancia y permiten que `a1.x` y `a2.x` sean independientes.

### Implementación

**`TablaSimbolos`**

- `declararAtributoDeObjeto(String nombreCalificado, String ambito,
  String tipoDato, int numeroLinea)`: crea o actualiza la entrada con la clave
  `nombreCalificado:ambito`, uso `ATRIBUTO` y el tipo recibido.

**`Entorno`**

- `declararVariable` y `declararAtributo` ahora devuelven la `EntradaTabla`
  declarada.
- `materializarAtributosDeObjeto(EntradaTabla objeto, String tipo, int linea)`
  recorre la jerarquía de la clase en orden base primero y crea una entrada
  por cada atributo de cada clase declarante. Para la clase del objeto usa el
  nombre del objeto; para las bases agrega el nombre simple de la base.
- `recolectarDeclarantes(...)` reutiliza el control de ciclos y de bases
  repetidas de la herencia.
- La materialización se dispara al declarar una variable de tipo clase, un
  parámetro formal de tipo clase o un atributo de tipo clase.
- `descartarPlaceholder(EntradaTabla)` elimina el placeholder que creó el
  léxico para el nombre del atributo cuando todavía no estaba declarado.

**`AccionesExpresiones`**

- `ACCESO_ATRIBUTO` y `ACCESO_ATRIBUTO_PREFIJADO` descartan el placeholder del
  token de atributo antes de resolver la referencia. Si el identificador era
  una declaración real, no se elimina.

### Por que se genera ese placeholder?

El lexico genera placeholders porque buscarOInsertarIdentificador se llama para todo identificador que no sea palabra reservada (ver AccionIdentificador.ejecutar()). Al reducir las asignaciones a1.x := 3$ul, el parser ve el token x como FACTOR_ID antes de saber que es un acceso a atributo; el lexico inserta x:main con declarado=false, uso=INDEFINIDO, tipo=indefinido.
La acción semántica ACCESO_ATRIBUTO luego:
1. Resuelve el objeto y el atributo real (entrada declarada de la clase).
2. Llama entorno.descartarPlaceholder(atributo) que elimina el placeholder x:main si no está declarado.
3. Emite el terceto con el texto a1.x.
Así el placeholder es transitorio: sirve para reconocer el lexema, pero se limpia al resolver el acceso semántico.

### Resultado

Para `clase_con_atributos.txt` la tabla queda, entre otras entradas:

```
ca:main -> 300          uso clase
x:main:ca -> 300        ulongint, atributo
z:main:ca -> 300        ulongint, atributo
a1:main -> 300          ca:main, variable
a1.x:main -> 300        ulongint, atributo
a1.z:main -> 300        ulongint, atributo
a2:main -> 300          ca:main, variable
a2.x:main -> 300        ulongint, atributo
a2.z:main -> 300        ulongint, atributo
```

Ya no aparecen `x:main` ni `z:main` como entradas indefinidas.

### Pruebas

Casos en `test/tp2/verificacion_r28_r31_atributos/`:

- `clase_con_atributos.txt`: dos objetos de `ca`, entradas propias separadas.
- `atributos_heredados_en_tabla.txt`: atributos propios y heredados de dos
  objetos, con prefijo de la clase que declara.
- `atributos_objeto_ambito_funcion.txt`: objeto local, claves con el ámbito
  de la función.
- `desambiguado_tradicional_y.txt`, `dos_bases_mismo_miembro.txt`,
  `herencia_transitiva.txt`, `miembro_propio_con_herencia.txt`,
  `receptor_parametro.txt` y `receptor_atributo.txt`: conservan sus tercetos y
  ahora muestran las entradas por objeto.
