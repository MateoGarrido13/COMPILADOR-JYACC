## DEC-5: Ambitos de clase, metodos y prefijo de herencia

### Ambitos

Una clase abre un ambito propio al consumir el `BEGIN` de su cuerpo. El ambito es el
ambito actual mas el nombre de la clase:

- `class ca` en `main` -> `main:ca`;
- `class ca` dentro de la funcion `a` en `main` -> `main:a:ca`.

El nombre de la clase se declara en el ambito que la contiene (`ca:main` o
`ca:main:a`). Sus atributos y metodos se declaran dentro de `main:ca`:

- atributo `x` -> `x:main:ca`;
- metodo `m` -> `m:main:ca`;
- parametro `p` de `m` -> `p:main:ca:m`;
- cuerpo del metodo -> `main:ca:m`.

Los atributos ya no comparten la clave `main`. Dos clases hermanas pueden declarar el
mismo atributo `x` sin colision: `x:main:ca` y `x:main:cb`.

### Herencia y prefijo (D3 revisada)

La herencia se registra en `ClaseDeclarada.heredaDe`. La resolucion de miembros
heredados quedo implementada en el Prompt 3.10 (`docs/PROMPT-31.md`). La regla
de visibilidad es:

- un miembro propio se puede usar sin prefijo;
- un miembro heredado exige el prefijo explicito `objeto.clase.miembro`;
- el uso sin prefijo de un miembro heredado es error por falta de prefijo;
- si dos clases base definen el mismo miembro, el prefijo desambigua.

Esta decision es deliberadamente estricta y puede relajarse mas adelante. La
implementacion y los casos de regresion estan en `docs/PROMPT-31.md`: las llamadas
`a1.m(...)` sobre un metodo solo heredado informan falta de prefijo; la forma valida es
`a1.clase.m(...)`.

### Experimento realizado

Comprobado con `test/TP3_SEMANTICAS/prompt-1b-name-mangling/`:

- `atributo_en_ambito_de_clase`: `x:main:ca` declarado y, al crear `a1`, la
  entrada por objeto `a1.x:main` con el tipo del atributo (ver `docs/DEC-10.md`).
  El placeholder `x:main` se descarta al acceder.
- `metodo_abre_ambito_propio`: `m:main:ca` y `p:main:ca:m`.
- `clase_dentro_funcion_ambitos`: `ca:main:a`, `x:main:a:ca`, `m:main:a:ca`,
  `p:main:a:ca:m`.
- `clases_hermanas_no_comparten_atributos`: `x:main:ca` y `x:main:cb` declarados por
  separado.
