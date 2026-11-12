## DEC-3: Busqueda de identificadores solo con ambito

Un identificador se busca y se da de alta unicamente con la clave `nombre:AMBITO_ACTUAL`. Si esa clave no esta, no se acepta una coincidencia por el nombre pelado.

La regla vale para variables y tambien para funciones: a sus identificadores se les concatena y actualiza el ambito al entrar y al salir. Clases y typedefs usan el mismo identificador, asi que tambien llevan el atributo `ambito`. Si no lo llevaran, un uso dentro de una funcion no los encontraria y habria que volver a la clave pelada.

No hay un identificador del lenguaje que haya que buscar sin ambito. Las palabras reservadas, las constantes y las cadenas no pasan por esta busqueda: su clave es el texto del lexema.

Riesgo: QUe las clases y los tipos definiidos sean alcanzados desde cualquier ambito
