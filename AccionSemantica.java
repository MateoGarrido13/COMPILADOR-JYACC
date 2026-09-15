/**
 * Codigo ejecutable asociado a una regla de la gramatica.
 *
 * Las acciones de gramatica.y no contienen logica: delegan en la TablaAcciones,
 * que busca la implementacion registrada para la regla reducida y la ejecuta.
 */
public interface AccionSemantica {

    /**
     * @param ctx valores de los simbolos del lado derecho de la regla y numero de linea por si hay que corregir la precedncia 
     * @return valor que la regla devuelve al lado izquierdo (equivalente a $$)
     */
    Object ejecutar(ContextoRegla ctx);
}
