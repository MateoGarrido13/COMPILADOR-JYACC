# Prompt 1A: resolucion por cadena de ambitos

Verifica que la tabla de simbolos resuelva un identificador subiendo por los
ambitos padres (`main:f:g -> main:f -> main`), sin compartir locales entre
hermanos.

Casos:

- `funcion_hermana_no_ve_local`: una funcion hermana no ve el local de otra.
- `funcion_hija_ve_externa`: una funcion anidada ve el local de la funcion que
  la contiene y las globales.
- `global_visible_todos`: una global se ve desde funciones y metodos.
- `clase_hermana_no_ve_atributo`: un metodo de otra clase no ve el atributo.
- `clase_dentro_funcion_ve_local`: una clase declarada dentro de una funcion ve
  las declaraciones de esa funcion.
- `clase_hermana_en_funcion_no_ve_atributo`: dos clases hermanas dentro de una
  funcion no comparten atributos.
