# Prompt 1B: name mangling, ControlAmbito y Entorno

Verifica las claves con ambito que produce la apertura/cierre de funciones,
metodos y clases, y que las entradas se conservan al cerrar un ambito.

Casos:

- `funcion_anidada_ambitos`: `a:main`, `n:main:a`, `b:main:a`, `m:main:a:b`.
- `funciones_iguales_local_solo_en_hija`: `var_1` existe declarada solo en la
  `fun_c` anidada; en la `fun_c` hermana queda como placeholder no declarado y
  el uso sigue siendo error.
- `variable_global_desde_funcion`: la funcion resuelve `global:main` y el
  terceto usa la clave resuelta.
- `atributo_en_ambito_de_clase`: el atributo se declara en `main:ca`, no en
  `main`.
- `metodo_abre_ambito_propio`: metodo `m:main:ca`, parametro `p:main:ca:m`.
- `clase_dentro_funcion_ambitos`: clase `ca:main:a`, atributo en `main:a:ca` y
  metodo en `main:a:ca`.
- `clases_hermanas_no_comparten_atributos`: dos clases con el mismo atributo
  generan dos declaraciones distintas.
