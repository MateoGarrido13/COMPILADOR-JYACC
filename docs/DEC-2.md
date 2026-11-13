## DEC-2: Se supera el ambito de una variable

Al salir del ambito donde la variable fue declarada, la entrada **no se elimina** de la
tabla de simbolos. Queda guardada con su clave `nombre:ambito` para conservar la
identidad, el puntero y el historial de declaraciones.

La visibilidad no se decide por presencia o ausencia en el mapa, sino por la cadena de
ambitos: desde el ambito actual se busca en `nombre:ambito`, luego en el ambito padre,
hasta `main`. Una variable de un ambito hermano o ya cerrado no es alcanzable desde el
ambito actual.

Desde el Prompt 1B ya no existe `MonitorAmbito`: al cerrar un ambito solo se restaura
`Globals.AMBITO_ACTUAL`. Ninguna entrada se renombra ni se elimina al salir. Por eso una
funcion anidada conserva su clave (`fun_c:main:a:b`) y la funcion hermana con el mismo
nombre tiene otra clave (`fun_c:main:a`).

Riesgo: si en el futuro hubiera modificadores de acceso o constantes con visibilidad
global, la entrada seguira existiendo y la regla de visibilidad debera ampliarse. La
decision actual prioriza no perder informacion y poder reconstruir el ambito.
