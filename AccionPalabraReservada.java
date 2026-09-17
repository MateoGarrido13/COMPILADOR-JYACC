public final class AccionPalabraReservada implements AccionSemantica {

    private AccionPalabraReservada() {
    }

    public static void agregar(ContextoLexico contexto, char caracter) {
        contexto.agregar(caracter);
    }

        public static int finalizar(ContextoLexico contexto, TablaSimbolos tablaSimbolos,
            Reporte reporte) {
        EntradaTabla entrada = buscar(contexto.lexema(), tablaSimbolos);
        if (entrada == null) {
            return AccionIdentificador.finalizar(contexto, tablaSimbolos, reporte);
        }
        Globals.yylval = entrada;
        return entrada.tokenID;
    }

    /** SEM 36 y 7: busca una palabra reservada sin crear identificadores. */
    public static EntradaTabla buscar(String lexema, TablaSimbolos tablaSimbolos) {
        return tablaSimbolos.buscarPalabraReservada(lexema);
    }
}
