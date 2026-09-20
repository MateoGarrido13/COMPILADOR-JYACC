public class AccionIdentificador implements AccionSemantica {
    private final ContextoLexico contexto;

    public AccionIdentificador(ContextoLexico contexto) {
        this.contexto = contexto;
    }

    @Override
    public int ejecutar() {
        String texto = contexto.lexema.toString();
        if (!texto.matches("^[a-z][a-zA-Z0-9_]*$")) {
            contexto.reporte.error(contexto.fuente.linea(),
                    "Identificador invalido '" + texto + "'");
            return -1;
        }

        if (texto.length() > Tipos.LONGITUD_MAXIMA_IDENTIFICADOR) {
            String truncado = texto.substring(0, Tipos.LONGITUD_MAXIMA_IDENTIFICADOR);
            contexto.reporte.warning(contexto.fuente.linea(),
                    "El identificador " + texto + " fue truncado a: " + truncado);
            texto = truncado;
        }

        EntradaTabla reservada = contexto.tablaSimbolos.buscarPalabraReservada(texto); //POSIBLE BUSQUEDA INNECESARIA; SIEMPRE SE EJECUTA 
        if (reservada != null) {
            Globals.yylval = reservada;
            return reservada.tokenID;
        }

        Globals.yylval = contexto.tablaSimbolos.buscarOInsertarIdentificador(texto);
        return Globals.yylval.tokenID;
    }
}