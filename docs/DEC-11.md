## DEC-11: Herencia inválida sin espera de la clase base

### Problema

Al leer `extends`, la base se resolvía y se informaba el error si no existía.
Sin embargo, la clase que heredaba quedaba declarada igual. Además, el nombre
de la base faltante se guardaba en `basesPendientes`, que solo servía para
detectar más tarde un posible ciclo. No se agregaba la base cuando la clase
aparecía después, pero el estado intermedio dejaba una clase inválida
registrada junto con sus atributos y métodos.

### Decisión

La base se resuelve en el momento de leer `extends`. No se espera una
definición posterior de la clase base.

Si la herencia es inválida, la clase que hereda **no queda declarada**. Esto
aplica a:

- base inexistente: `clase base no declarada 'padre'`;
- nombre que no es clase: `'x' no es una clase`;
- herencia circular: `herencia circular entre 'a' y 'a'`;
- base repetida: `clase base repetida 'padre'`.

Se elimina el mecanismo de bases pendientes (`basesPendientes` y
`anotarBasePendiente`). Ya no se recuerda el nombre de una base inexistente
para una definición futura.

### Implementación

**`Entorno`**

- `ClaseDeclarada.valida` indica si la clase puede quedar declarada.
- `invalidarClase(ClaseDeclarada)` marca la clase abierta como inválida.
- `cerrarClase()` descarta la clase cuando `valida == false`.
- `descartarClase(...)`:
  - elimina la clase de `clases`, junto con clases anidadas en su ámbito;
  - elimina sus entradas de la Tabla de Símbolos;
  - elimina variables locales y funciones registradas en su ámbito y
    subámbitos;
  - elimina la entrada de la propia clase.
- Se quitó `anotarBasePendiente` y toda referencia a `basesPendientes`.

**`TablaSimbolos`**

- `eliminarAmbito(String ambito)` elimina las entradas declaradas o usadas en
  un ámbito y sus subámbitos. No toca constantes, cadenas ni palabras
  reservadas. Así desaparecen atributos, métodos, parámetros y placeholders
  de una clase rechazada.

**`Verificaciones`**

- `registrarHerencia(...)` llama a `entorno.invalidarClase(clase)` en todos los
  errores de herencia.
- La base inexistente ya no se anota como pendiente.

**`GeneradorTercetos`**

- `marcasClase` guarda la posición del terceto `CLASE` de cada clase abierta.
- Al cerrar una clase inválida se descartan los tercetos emitidos desde
  `CLASE` y no se emite `FIN_CLASE`.

### Resultado

Para `test/tp2/verificacion_r28_r31_atributos/extends_clase_no_definida.txt`:

```
Línea 14: Error: clase base no declarada 'padre'
Línea 20: Error: tipo no declarado 'hija'
Línea 22: Error: Error de ambito 'a1' no es alcanzable
```

Tercetos:

```
(CLASE, padre, --)
(FIN_CLASE, padre, --)
```

No se emiten los tercetos de `hija`. En la Tabla de Símbolos, `hija:main`
puede aparecer solo como placeholder `Declarado: no`, creado por el léxico al
leer el uso posterior `hija a1;`; no es una declaración de clase.

En `herencia_circular_mutua.txt`, `a` queda inválida por no existir `b` y
tampoco se registra. Cuando `b` hereda de `a`, el error vuelve a ser
`clase base no declarada 'a'`, no una herencia circular.

### Pruebas

Casos en `test/tp2/verificacion_r28_r31_atributos/`:

- `extends_clase_no_definida.txt`: base declarada después del `extends`; la
  clase hija no se declara y no se emite su código.
- `herencia_circular_mutua.txt`: dos errores de base no declarada, sin espera
  entre ambas definiciones.
- `base_repetida.txt`, `herencia_circular.txt` y
  `clase_base_no_declarada.txt`: el error de herencia impide declarar la
  clase.
- Los casos válidos de herencia (`herencia_transitiva.txt`,
  `dos_bases_mismo_miembro.txt`, `miembro_propio_con_herencia.txt`,
  `atributos_heredados_en_tabla.txt`) conservan sus entradas y tercetos.
