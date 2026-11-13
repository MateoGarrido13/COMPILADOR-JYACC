## DEC-4: TS y su impresion

La tabla es un solo mapa. Almacena:

- Palabras reservadas, cargadas al crearla, con la clave pelada en minuscula (`if`,
  `function`, `ulongint`). No llevan ambito.
- Identificadores con la clave `nombre:AMBITO_ACTUAL`: variables, parametros, funciones,
  clases, metodos, atributos, typedefs y tambien un uso que no se declaro.
- Constantes numericas, con el texto del numero (`1$ul`, `2.2d+5`).
- Cadenas, con su contenido.

Cada entrada distingue:

- `uso`: variable, parametro, funcion, metodo, clase, atributo, tipo, constante, cadena
  o reservada.
- `declarado`: verdadero solo si la entrada corresponde a una declaracion. Un uso previo
  crea un placeholder con `declarado = false`; la declaracion posterior lo marca.

**Importante:** el beneficio del `Map` es que la clave es la identidad de la entrada.
`get` la encuentra o la descarta en un acceso, sin recorrer la tabla. `function` y
`var_2:main` son claves distintas, asi evitamos que buscar un identificador compare ese
nombre contra TODAS las palabras reservadas.

La busqueda de un identificador no compara contra las reservadas: pide `nombre:ambito` y
sube por la cadena de ambitos (DEC-3). La comparacion con las reservadas ocurre antes, en
el lexico, con la palabra en minuscula. Las constantes y las cadenas buscan su propio
texto.

Se imprime el estado final del mapa, despues del analisis. Las entradas cuyo ambito se
cerro **siguen estando** (DEC-2); la impresion no implica que sean visibles desde
cualquier punto del programa. Las reservadas y las constantes siguen, porque no se borran
al salir de un ambito.

Un uso no declarado tambien se imprime: aparece con `Uso: indefinido` y
`Declarado: no`. Por ejemplo, despues de usar `var_1` en una funcion hermana sin
declararla, la tabla puede mostrar `var_1:main:a:fun_c` con `Declarado: no`. Esa entrada
es un placeholder y no satisface la resolucion (DEC-3).
