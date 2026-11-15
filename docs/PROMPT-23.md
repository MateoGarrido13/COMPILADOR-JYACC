# Prompt 3.7: enumeraciones

## Alcance

Se implementó el Tema 23: declaración y resolución de tipos enumerados,
validación de sus valores y asignaciones, y uso de su tipo base en operaciones.

## Cambios por clase

### `EntradaTabla.java`

- Añade `Uso.TIPO_ENUMERADO` para distinguir los typedefs de clases y de otros
  tipos genéricos.

### `Entorno.java`

- Registra cada tipo enumerado por clave mangled `nombre:ambito`, con su nombre,
  tipo base, valores y entrada de tabla.
- Rechaza registrar un nombre ya declarado en el mismo ámbito.
- Resuelve tipos de usuario visibles desde el ámbito actual hacia sus padres.
  Las declaraciones guardan la clave mangled del tipo resuelto, permitiendo
  que nombres iguales en ámbitos distintos convivan y que el más cercano tenga
  precedencia.
- Reconoce un enum al declarar identificadores: sus variables reciben el tipo
  enumerado resuelto, mientras que los objetos siguen usando tipos de clase.
- Devuelve el tipo base de un enum desde `tipoEfectivo`, que habilita las
  operaciones aritméticas y comparaciones existentes sin agregar conversiones.
- Comprueba la pertenencia de constantes numéricas a la lista del enum. Los
  enteros se comparan como `BigInteger` y los reales por valor numérico, no por
  formato léxico.
- Incluye el ámbito en las claves que muestra su resumen de tipos enumerados.

### `Verificaciones.java`

- Verifica que la lista del typedef no esté vacía y que todos sus valores sean
  constantes numéricas del mismo tipo base.
- Detecta una redeclaración del nombre del typedef en el ámbito actual.
- Para asignaciones a variables enumeradas, comprueba los literales constantes
  contra los valores declarados. Si el literal tiene otro tipo base, el chequeo
  general de tipos reporta la incompatibilidad.
- Acepta una variable del mismo enum y expresiones con el tipo base sin validar
  su valor resultante.
- Rechaza la asignación directa desde una variable de otro enum, aunque ambos
  tengan el mismo tipo base.

### `otras_acciones/AccionesDeclaraciones.java`

- Integra la validación del typedef antes de registrar su nombre y contenido.
- Resuelve los tipos de usuario en declaraciones de variables, objetos y
  atributos; un tipo no declarado produce error y no deja declaradas variables
  con un tipo inválido.
- Aplica la validación de pertenencia a las asignaciones ordinarias.

### `otras_acciones/AccionesExpresiones.java`

- Aplica la misma validación de pertenencia en asignaciones dentro de
  expresiones; si falla, marca la expresión inválida para que no se genere
  código intermedio.

### `TablaSimbolos.java`

- Muestra el tipo base, `Uso: tipo enumerado` y el ámbito del typedef en la
  salida de la tabla de símbolos.

### `Tipos.java`

No requirió cambios. La deducción del tipo de constantes, comparación de tipos
y conversión de literales numéricos ya estaban disponibles. La resolución
contextual enum → tipo base corresponde a `Entorno.tipoEfectivo`.

## Decisiones semánticas

- El typedef solo se registra si tiene valores y todos son constantes válidas  
  de un mismo tipo base. Una declaración vacía o con bases mezcladas informa
  error y no crea un tipo enumerado utilizable. ---- (SE DEBERÍA PERMTIR LA MEZCLA DE TIPOS ULONGINT Y DOUBLEF?)
- Los tipos enumerados comparten el espacio de nombres de declaraciones del
  ámbito; una variable, clase u otro símbolo ya declarado con el mismo nombre
  también impide registrar el typedef.
- El nombre del tipo en la tabla de símbolos es mangled; su campo `tipoDato`
  contiene el tipo base y su campo `ambito` conserva explícitamente el alcance.
- La membresía se chequea solo cuando el RHS es un literal constante directo.
  Variables y expresiones se aceptan según las reglas de tipo existentes, sin
  evaluar su valor en compilación.
- Dos variables de diferentes enums no se asignan entre sí aunque compartan
  tipo base; las operaciones y comparaciones sí trabajan con el tipo base, tal
  como pide el Tema 23.

## Pruebas

Se compilaron las fuentes Java y se ejecutaron los archivos de
`test/tp2/verificacion_r23_enums/`.

| Caso | Resultado |
|---|---|
| `estructura_respetada.txt`, `dos_tipos_validos.txt` | Aceptan enums enteros, variables y asignaciones de valores listados. |
| `constante_fuera_enumeracion.txt`, `enumerado_doublef.txt` | Aceptan constantes listadas y rechazan las que no pertenecen. |
| `variables_y_expresiones.txt` | Acepta asignación entre variables del mismo enum, una expresión del tipo base y una comparación con ese tipo. |
| `asignacion_en_expresion.txt` | Rechaza una constante no listada también en una asignación dentro de una expresión. |
| `enums_distintos.txt` | Rechaza asignación directa entre enums distintos con igual tipo base. |
| `tipos_base_mezclados.txt` | Informa valores de distinto tipo base y no registra el typedef. |
| `enum_vacio.txt` | Informa `Ausencia de valores para la enumeracion`. |
| `tipo_no_declarado.txt` | Informa tipo no declarado y no declara la variable. |
| `typedef_redeclarado.txt` | Rechaza el segundo typedef en el mismo ámbito. |
| `ambitos_tipedef.txt` | Acepta el mismo nombre de typedef en `main` y en una función; cada variable conserva la clave de su enum. |
| `nombre_tipo_primitivo.txt`, `tipos_difieren_mayusculas.txt` | Conservan los diagnósticos existentes del parser y del léxico. |
