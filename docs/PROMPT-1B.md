# PROMPT-1B: ControlAmbito, Entorno y acciones para Name Mangling

## Objetivo

Completar la Fase 1 abriendo y cerrando ambitos reales para funciones, metodos y clases.
La tabla de simbolos resuelve visibilidad por la cadena de ambitos; `ControlAmbito` solo
mantiene `Globals.AMBITO_ACTUAL`.

## Cambios realizados

### `ControlAmbito`

- Se elimino `MonitorAmbito` y todas sus llamadas. `MonitorAmbito.java` fue borrado.
- Ya no se guardan `declarados` ni `alcanzable`; la visibilidad la decide la TS.
- Funcion: al ver `function` se guarda el nombre; al leer el `(` que abre los parametros
  se entra al ambito `actual + ":" + nombre`. Los parametros se lexan dentro.
- Metodo: dentro del cuerpo de una clase se detecta el patron
  `tipo_declarado ID '('` y se entra al ambito del metodo en el `(`.
- Clase: al ver `CLASS ID` se guarda el nombre; al leer el `BEGIN` del cuerpo se entra al
  ambito `actual + ":" + clase`; el `END` correspondiente lo cierra.
- Los `BEGIN/END` de sentencias no abren ambito; se distinguen por una pila de marcos.
- Una clase declarada dentro de una funcion usa `ambitoActual + ":" + nombreClase`.
- `reiniciar` solo vuelve a `main` y limpia las pilas.

#### Detalle de timing del parser

El parser ascendente puede reducir el encabezado cuando ya leyo el `(` como lookahead.
En ese caso `Globals.AMBITO_ACTUAL` ya contiene el nombre de la funcion o del metodo, y
`Entorno` no puede usarlo como ambito de declaracion. Para resolverlo sin romper la regla
de que `ControlAmbito` solo mantiene el ambito actual se agrego:

```java
ControlAmbito.ambitoDeclaracionDe(nombre, ambitoActual)
```

Devuelve el ambito padre de la declaracion que acaba de abrir el ambito actual. Si el
parser redujo antes de leer el `(`, devuelve el mismo `ambitoActual`. Es una consulta de
solo lectura; no duplica el estado del ambito.

### `Entorno`

- `funcionActual` y `claseActual` se reemplazaron por:
  `Deque<FuncionDeclarada> pilaFunciones` y `Deque<ClaseDeclarada> pilaClases`.
- `FuncionDeclarada` guarda nombre, tipo de retorno, linea, `EntradaTabla`,
  `esMetodo`, `ambitoDeclaracion`, `ambitoPropio` y la lista de `Parametro`.
- `Parametro` guarda nombre, tipo y `EntradaTabla`.
- `ClaseDeclarada` guarda entrada, ambito de declaracion, ambito propio, atributos,
  metodos y herencia.
- `abrirFuncion` declara el nombre en el ambito de declaracion con `Uso.FUNCION` o
  `Uso.METODO`, y marca `tipoRetorno`.
- `agregarParametroFormal` declara en el ambito actual con `Uso.PARAMETRO` y guarda la
  entrada en `Parametro`.
- `declararVariable` declara en el ambito actual; solo pasa a atributo si no hay funcion
  abierta y hay clase abierta. Los locales de metodos no se convierten en atributos.
- `declararAtributo` declara con `Uso.ATRIBUTO` en `clase.ambitoPropio`.
- `declararObjetos` resuelve la clase, verifica `Uso.CLASE` y guarda la clave resuelta
  (`ca:main`) como tipo del objeto.
- `registrarTipoEnumerado` declara con `Uso.TIPO` en el ambito actual.
- `abrirClase` declara el nombre en el ambito actual y crea la clase con
  `ambitoPropio = ambitoDeclaracion + ":" + nombre`.
- `getFuncion` y `getClase` buscan por clave completa; los mapas de variables, funciones,
  clases y enumerados usan `nombre:ambito`.

### `Verificaciones` y acciones

- `verificarAlcance` ahora devuelve la `EntradaTabla` resuelta o `null` despues de
  reportar el error.
- `FACTOR_ID` usa la entrada resuelta, no el placeholder.
- `ASIGNACION` resuelve el destino y lo devuelve como resultado de la reduccion.
- `INVOCACION_FUNCION` resuelve la funcion visible y pasa esa entrada a `LlamadaFuncion`.
- `verificarCantidadParametros` resuelve por nombre y ambito y busca la firma por clave
  completa.
- `GeneradorTercetos.generarAsignacion` usa `reduccion.resultado()` para el destino. Esto
  evita emitir el placeholder del lexico y corrige las asignaciones a atributos que antes
  terminaban en "No se puede asignar un valor a una cte o variable sin referencia".

### Documentacion

- `DEC-1`: apertura de funcion/metodo/clase y ejemplos de claves.
- `DEC-2`: `MonitorAmbito` eliminado; no se borran ni renombran entradas al cerrar.
- `DEC-3`: busqueda por clave completa y cadenas de prefijos; placeholders no cuentan.
- `DEC-4`: los placeholders no declarados se imprimen con `Declarado: no`.
- `DEC-5`: ambitos de clase, metodos y regla estricta de prefijo para heredados.
- `docs/Promt.md`: prompts 0, 1A y 1B marcados como ejecutados y criterios corregidos.

## Pruebas de aceptacion

### `prueba_funcion_anidada.txt`

Claves declaradas:

```
a:main
n:main:a
b:main:a
m:main:a:b
```

La lista original del prompt decia `n:main` y `m:main:a`. Como los parametros se lexan
al entrar en el `(` de la funcion, la clave correcta es la que incluye el ambito de la
funcion: `n:main:a` y `m:main:a:b`.

### `prueba_funciones_iguales.txt`

- `var_1:main:a:b:fun_c` existe con `Declarado: si`.
- `var_1:main:a:fun_c` existe solo como placeholder con `Declarado: no`; el uso sigue
  reportando `Error de ambito 'var_1' no es alcanzable`.
- Las dos `fun_c` conviven: `fun_c:main:a:b` y `fun_c:main:a`.

### `prueba_variable_en_main.txt`

- La funcion `a` resuelve `global:main`.
- El terceto generado es `(:=, global:main, n:main:a)`.

### Clases

- `x` de `ca` se declara como `x:main:ca`; `x:main` queda como placeholder de uso.
- Dos clases hermanas no comparten atributos: `x:main:ca` y `x:main:cb`.

## Pruebas agregadas

Se creo `test/TP3_SEMANTICAS/` con un ejecutor propio y dos subdirectorios:

- `prompt-1a/`: 6 casos de resolucion por cadena (hermanos, anidados, globales, clases).
- `prompt-1b/`: 7 casos de name mangling y apertura/cierre de ambitos.

Resultado:

```bash
bash test/TP3_SEMANTICAS/ejecutar.sh
# 13/13 casos OK
```

Cada caso tiene un `.esperado` con reglas `SIN ERRORES`, `ERROR`, `SIMBOLO`,
`NO_SIMBOLO`, `DECLARADO_SI`, `DECLARADO_NO` y `TERCETO`.

## Regresion

- Compila con:

  ```bash
  javac -encoding UTF-8 -d /tmp/opencode/tp3build *.java "acciones semanticas"/*.java otras_acciones/*.java
  ```

- Se ejecutaron los 121 archivos de `test/` y se guardaron en `/tmp/opencode/tp3-1b/`.
- Contra los 65 casos del baseline de 1A, los unicos cambios de diagnostico son dos
  errores que desaparecen en los casos de atributos:

  - `test/tp2/verificacion_r28_r31_atributos/clase_con_atributos.txt`
  - `test/tp2/verificacion_r28_r31_atributos/desambiguado_tradicional_y.txt`

  Ambos reportaban "No se puede asignar un valor a una cte o variable sin referencia"
  porque el generador usaba el placeholder del destino. Ahora la asignacion usa la
  entrada resuelta y el error desaparece. No hay nuevos errores en el resto.

## Pendiente

- Prompt 2: tipos y prohibicion de conversiones implicitas.
- Prompt 3.10: resolucion de herencia con prefijo obligatorio (DEC-5).
- La redeclaracion en el mismo ambito todavia no emite error.
