# TP3_SEMANTICAS

Casos de regresion de las semanticas de TP3. Cada tema o prompt tiene su
subdirectorio, con un `.txt` que contiene la salida esperada y el programa.

## Ejecutar

```bash
bash test/TP3_SEMANTICAS/ejecutar.sh
```

El script compila el proyecto y ejecuta los casos. Respeta `TP3_BUILD`
para elegir el directorio de clases (por defecto `/tmp/opencode/tp3build`).

> **Nota:** el runner espera el formato anterior con reglas
> `--- ESPERADO ---`; los casos actuales son descriptivos y se revisan
> comparando la salida real con el comentario inicial. La compilación del
> script sigue siendo útil para validar que el proyecto compila.

## Ejecución individual de cada test

Luego de compilar: 
cd /home/rafael/Documents/Compiladores/COMPILADOR/TP3
javac -encoding UTF-8 -d /tmp/opencode/tp3build *.java "acciones semanticas"/*.java otras_acciones/*.java

Ejemplo: 
java -cp /tmp/opencode/tp3build Main test/TP3_SEMANTICAS/prompt-2b-codegen-base/asignacion_en_expresion.txt 2>&1

## Formato de cada `.txt`

El archivo tiene dos partes:

1. Un comentario multilínea `{{ ... }}` al principio con la salida esperada:
   tercetos, variables/constantes de la tabla de símbolos y errores.
2. El programa, tal como se le pasa al compilador.

Ejemplo:

```text
{{ Salida ideal esperada (tercetos y variables de la TS, sin palabras clave):
Tercetos:
1. (PROGRAMA, --, --)
2. (:=, x:main, 1$ul)
3. (FIN_PROGRAMA, --, --)
Variables de la tabla de simbolos (declaradas, sin palabras clave):
- x:main -> ulongint, variable
}}

prog
ulongint x;
begin
    x := 1$ul;
end
```

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
- `prompt-3-1-constantes/`: constantes `ulongint` y `doublef` tipadas en el
  lexico, negativas, limites y convivencia cadena/constante con el mismo
  lexema visible.
