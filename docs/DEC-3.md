## DEC-3: Busqueda de identificadores por cadena de ambitos

Un identificador se da de alta con la clave `nombre:AMBITO_ACTUAL`. Para usarlo:

1. Se busca la clave exacta `nombre:ambitoActual`.
2. Si no existe o es solo un uso sin declaracion, se sube al ambito padre quitando el
   ultimo segmento separado por `:`.
3. Se repite hasta `main`.
4. Solo se acepta una entrada marcada como `declarado`. Un placeholder creado por un uso
   anterior no cuenta como declaracion.

La regla vale para variables, parametros, funciones, clases, atributos, metodos y
typedefs. Las palabras reservadas, las constantes y las cadenas no pasan por esta
busqueda: su clave es el texto del lexema.

Las funciones y metodos se buscan por su clave completa. Si `a` esta en `main` y `b`
dentro de `a`, las claves son `a:main` y `b:main:a`; el cuerpo de `b` usa `main:a:b`. Una llamada desde `main:a:b` resuelve primero `b:main:a:b`, luego
`b:main:a`, luego `b:main` y por ultimo `b:main`.

Un uso no declarado deja un placeholder con la clave del ambito donde se uso. Por
ejemplo, `var_1:main:a:fun_c` puede existir con `declarado = false`; no cuenta para la
resolucion y el uso sigue siendo error de ambito.

Un identificador usado sin declaracion en ningun ambito de la cadena es un error de
ambito. Ya no se compara contra una clave pelada.

Riesgo: las entradas de ambitos cerrados siguen en la tabla. Como la busqueda solo sube
por los padres del ambito actual, un ambito hermano no las encuentra. Si se agregaran
reglas de visibilidad global, habria que extender la resolucion.
