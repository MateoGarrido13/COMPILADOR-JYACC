# TP3_SEMANTICAS

Casos de regresion de las semanticas de TP3. Cada tema o prompt tiene su
subdirectorio, con un `.txt` que contiene el programa y, al final, sus propias
expectativas.

## Ejecutar

```bash
bash test/TP3_SEMANTICAS/ejecutar.sh
```

El script compila el proyecto y ejecuta todos los casos. Respeta `TP3_BUILD`
para elegir el directorio de clases (por defecto `/tmp/opencode/tp3build`).

## Formato de cada `.txt`

El archivo tiene dos partes:

1. El programa, tal como se le pasa al compilador.
2. Un bloque de reglas dentro de un comentario multilinea `{{ ... }}`. La
   primera linea del comentario es `--- ESPERADO ---`. El runner no le pasa
   ese comentario al compilador, y compilar el `.txt` directamente tampoco
   incorpora las reglas a la tabla de simbolos.

Ejemplo:

```text
prog
ulongint x;
begin
    x := 1.0;
end

{{
--- ESPERADO ---
ERROR:Tipos incompatibles en la asignación a 'x': se esperaba ulongint y se encontró doublef
}}
```

Reglas disponibles, una por linea:

- `SIN ERRORES`: la salida no debe tener ningun `Error:`.
- `ERROR:<texto>`: debe aparecer el texto, por ejemplo un error de tipo o de ambito.
- `SIMBOLO:<clave>`: la tabla debe contener `<clave> -> 300`.
- `NO_SIMBOLO:<clave>`: la tabla no debe contener esa clave.
- `DECLARADO_SI:<clave>` / `DECLARADO_NO:<clave>`: estado de `declarado` de la entrada.
- `TERCETO:<texto>`: el codigo intermedio debe contener el texto.
- `SIN TERCETOS`: la salida no debe tener ningun terceto numerado. Se usa en
  programas invalidos para verificar que no se genera codigo intermedio.

Las claves de la tabla incluyen el ambito: `nombre:ambito`. Una entrada con
`Declarado: no` es un placeholder creado por un uso, no una declaracion.

## Carpetas

- `prompt-1a-ambitos/`: resolucion por cadena de ambitos. Funciones y clases
  hermanas no ven los locales/atributos de la otra; funciones y clases anidadas
  si ven las declaraciones externas; las globales se ven desde cualquier ambito.
- `prompt-1b-name-mangling/`: name mangling y apertura/cierre de ambitos.
  Funciones, metodos y clases generan claves propias; las entradas no se borran
  al cerrar ambito.
- `prompt-2-tipos-estrictos/`: igualdad exacta de tipos y prohibicion de
  conversiones implicitas. Cubre asignaciones, operadores, condiciones,
  retorno, enumerados, tipos no declarados y `todf` pendiente del Tema 33.
  Tambien verifica que funciones y clases no puedan usarse como factores y que
  el prefijo de clase mande al resolver un atributo heredado.
