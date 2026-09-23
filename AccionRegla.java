/*
  Codigo ejecutable asociado a una regla de la gramatica.
  Distinto de AccionSemantica (acciones del automata lexico). Esta interfaz
  es la que usa TablaAcciones al reducir una produccion de gramatica.y.
 */
@FunctionalInterface
public interface AccionRegla {

    Object ejecutar(ContextoRegla ctx);
}
