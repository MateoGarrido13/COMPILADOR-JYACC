public interface AccionSemantica {

    void agregar(ContextoLexico contexto, char caracter);

    int finalizar(ContextoLexico contexto, TablaSimbolos tablaSimbolos, Reporte reporte);
}