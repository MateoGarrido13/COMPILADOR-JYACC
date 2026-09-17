public final class AccionIdentificador implements AccionSemantica {

    private AccionIdentificador() {
    }

    public static void agregar(ContextoLexico contexto, char caracter) {
        contexto.agregar(caracter);
    }

    public static int finalizar(ContextoLexico contexto, TablaSimbolos tablaSimbolos,
            Reporte reporte) {
        String texto = contexto.lexema();
        EntradaTabla reservada = AccionPalabraReservada.buscar(texto, tablaSimbolos);
        if (reservada != null) {
            Globals.yylval = reservada;
            return reservada.tokenID;
        }
        if (texto.length() > Tipos.LONGITUD_MAXIMA_IDENTIFICADOR) {
            String truncado = texto.substring(0, Tipos.LONGITUD_MAXIMA_IDENTIFICADOR);
            if (reporte != null) {
                reporte.warning(contexto.lineaInicio(),
                        "El identificador " + texto + " fue truncado a: " + truncado);
            }
            texto = truncado;
        }
        Globals.yylval = tablaSimbolos.buscarOInsertarIdentificador(texto);
        return Globals.yylval.tokenID;
    }

    /** SEM 8 y 9: lee, trunca y registra un identificador. */
    public static int procesar(char inicial, LectorFuente fuente,
            TablaSimbolos tablaSimbolos, Reporte reporte) {
        StringBuilder lexema = new StringBuilder();
        lexema.append(inicial);

        char c = fuente.leer();
        while (Character.isLetterOrDigit(c) || c == '_') {
            lexema.append(c);
            c = fuente.leer();
        }
        fuente.retroceder(c);

        String texto = lexema.toString();
        EntradaTabla reservada = AccionPalabraReservada.buscar(texto, tablaSimbolos);
        if (reservada != null) {
            Globals.yylval = reservada;
            return reservada.tokenID;
        }

        if (texto.length() > Tipos.LONGITUD_MAXIMA_IDENTIFICADOR) {
            String truncado = texto.substring(0, Tipos.LONGITUD_MAXIMA_IDENTIFICADOR);
            reporte.warning(fuente.linea(),
                    "El identificador " + texto + " fue truncado a: " + truncado);
            texto = truncado;
        }

        Globals.yylval = tablaSimbolos.buscarOInsertarIdentificador(texto);
        return Globals.yylval.tokenID;
    }
}
