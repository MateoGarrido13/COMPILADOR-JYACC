## DEC-2: Se supera el ambito de una variable

Al salir del ambito de donde la variable fue dclarada, ya no es accesible y sera elimindada de la TS.

Riesgo: si hubieran modificadores de acceso o se declararia de tipo 'CONSTANTE', podria ser accesible desde fuera y al eliminarla con esta decision ya no existe.