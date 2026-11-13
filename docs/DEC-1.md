## DEC-1: Ambito en minuscula

El ambito se lleva en `Globals.AMBITO_ACTUAL`, una variable global que se concatena
sobre `main` en minuscula: `main`, `main:f`, `main:f:g`.

Se usa minuscula para que las claves sean uniformes con los identificadores y con el
nombre del programa. La exigencia lexica de los identificadores es:

**l \*{l|d|'_'}**

No se distingue entre `funcion` y `Funcion`; el lexico ya rechaza identificadores con
mayusculas.

El nombre de la funcion o clase se agrega tal como se leyo, y el ambito completo queda
en minuscula. Ejemplo: la funcion `g` dentro de `f` dentro de `main` es `main:f:g`; la
variable `x` declarada en `g` es `x:main:f:g`.

Con el Prompt 1B la apertura efectiva es:

- funcion: el ambito se abre al consumir el `(` de los parametros;
- metodo: `main:ca:m`, con el nombre declarado en `main:ca`;
- clase: `main:ca`, abierto en el `BEGIN` de su cuerpo;
- clase dentro de una funcion: si la funcion es `main:a`, la clase es `main:a:ca`.

Los parametros y las variables locales se lexan y declaran en el ambito que ya
incluye el nombre de la funcion o del metodo.
