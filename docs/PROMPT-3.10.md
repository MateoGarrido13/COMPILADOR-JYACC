# PROMPT-3.10: decision D3 revisada (acceso a miembros heredados)

## Contexto

La decision D3 original proponia desambiguar atributos y metodos heredados aceptando
tanto la forma tradicional (`objeto.miembro`) como la prefijada (`objeto.clase.miembro`).
El usuario reviso esa decision: en esta primera etapa, **todo miembro heredado debe
nombrarse con el prefijo de la clase que lo declara**. El uso sin prefijo de un miembro
heredado es un error.

Esta decision puede cambiar mas adelante; por ahora el compilador debe ser estricto.

## Regla adoptada

Sea `C` la clase declarada del objeto y `B` una clase base de `C`.

1. `objeto.miembro` busca `miembro` **solo** entre los miembros declarados directamente
   en `C` (atributos y metodos propios).
2. Si `miembro` no existe en `C` pero existe en `B` o en otra clase heredada, se informa
   un error: el acceso debe llevar el prefijo `objeto.B.miembro`.
3. `objeto.B.miembro` es valido solo si:
   - `B` es una clase base directa o transitiva de `C`;
   - `miembro` esta declarado en `B` (no en otra base);
   - el miembro es visible segun las reglas de TP3.
4. El prefijo `B` debe corresponder a una clase efectivamente heredada. Un prefijo que
   no pertenece a la jerarquia de `C` es error.
5. La misma regla vale para atributos y para metodos, en lectura y en asignacion.

## Consecuencias

- Se elimina la ambiguedad de la herencia multiple: si dos bases declaran `m`, el prefijo
  es obligatorio y selecciona una de las dos.
- Un miembro propio de `C` puede seguir usandose sin prefijo aunque `C` herede otro
  miembro con el mismo nombre; el miembro propio tiene prioridad en el ambito de `C`.
- Los metodos heredados se invocan como `objeto.B.metodo(reales)[orden]`.
- Los atributos heredados se leen y asignan como `objeto.B.atributo`.
- No se permite `objeto.metodo(...)` cuando `metodo` solo existe en una base.

## Impacto en las pruebas del baseline

| Caso | Situacion con D3 revisada |
|---|---|
| `metodo_heredado_prefijado_con_orden.txt` | Valido: todas las llamadas ya usan `a1.cb.m` o `a1.cc.m`. |
| `metodo_heredado_prefijado_sin_orden.txt` | Invalido por falta de orden; ademas la llamada `a1.m(...)` debe informar miembro heredado sin prefijo. |
| `metodo_heredado_prefijado_un_parametro.txt` | Mixto: las formas `a1.padre.m(...)` son validas; `a1.m(...)` debe pasar a error. |
| `desambiguado_tradicional_y.txt` | Valido: usa `a1.padre.y` y `a1.hija.y`, ambos prefijados. |

Los casos con `a1.m(...)` deberan actualizarse cuando se implemente el tema 31 o, si se
prefiere conservarlos como regresion, mantener el error esperado.

## Implementacion prevista

- En `Entorno`, separar los miembros propios de los heredados.
- En la resolucion de `ACCESO_ATRIBUTO` e `INVOCACION_METODO` sin prefijo, buscar solo en
  los miembros propios de la clase del objeto.
- En las variantes prefijadas, validar que el prefijo sea una base de la clase y que el
  miembro exista en esa base.
- Reportar un error claro cuando el miembro exista solo en una base y falte el prefijo.

## Relacion con otras decisiones

- D2: las clases abren su propio ambito; los metodos abren un ambito hijo.
- D4: la compatibilidad estructural de objetos usa el conjunto efectivo de atributos y
  firmas, incluyendo heredados.
- D5: las entradas de ambitos cerrados se conservan; la visibilidad se resuelve por la
  cadena de ambitos.
