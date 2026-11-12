## DEC-4: TS y su impresion

La tabla es un solo mapa. Almacena:

- Palabras reservadas, cargadas al crearla, con la clave pelada en minuscula (`if`, `function`, `ulongint`). No llevan ambito.
- Identificadores con la clave `nombre:AMBITO_ACTUAL`: variables, parametros, funciones, clases, typedefs y tambien un uso que no se declaro.
- Constantes numericas, con el texto del numero (`1$ul`, `2.2d+5`).
- Cadenas, con su contenido.

**Importante:** el beneficio del `Map` es que la clave es la identidad de la entrada. `get` la encuentra o la descarta en un acceso, sin recorrer la tabla. `function` y `var_2:MAIN` son claves distintas, así  evitamos que buscar un identificador no compara ese nombre contra TODAS las palabras reservadas.

La busqueda de un identificador no compara contra esas reservadas: pide solo `nombre:AMBITO_ACTUAL`. La comparacion con las reservadas ocurre antes, en el lexico, con la palabra en minuscula. Las constantes y las cadenas buscan su propio texto.

Se imprime el estado final del mapa, despues del analisis. No es el historial de todo lo declarado. Las entradas cuyo ambito se cerro ya no estan. Las reservadas y las constantes siguen, porque no se borran al salir de un ambito.

Riesgo/Arreglo a verificar: TS debe almacenar constantes ??? 
