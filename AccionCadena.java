public final class AccionCadena {

    private AccionCadena() {
    }

    public static void agregar(ContextoLexico contexto, char caracter) {
        contexto.agregar(caracter);
    }

    public static int finalizar(ContextoLexico contexto, TablaSimbolos tablaSimbolos,
            Reporte reporte) {
        String lexema = contexto.lexema();
        if (lexema.length() >= 2 && lexema.charAt(0) == '{'
                && lexema.charAt(lexema.length() - 1) == '}') {
            String cadena = lexema.substring(1, lexema.length() - 1);
            Globals.yylval = tablaSimbolos.buscarOInsertarCadena(cadena);
            return Globals.CADENA;
        }
        if (reporte != null) {
            reporte.error(contexto.lineaInicio(),
                    "Cadena de 1 linea no cerrada antes del fin de archivo");
        }
        return 0;
    }

    /** SEM 12 a 18: descarta comentarios multilínea o entrega una cadena. */
    public static Integer procesarLlave(LectorFuente fuente,
            TablaSimbolos tablaSimbolos, Reporte reporte) {
        char siguiente = fuente.leer();
        if (siguiente == '{') {
            return consumirComentario(fuente, reporte) ? null : 0;
        }
        fuente.retroceder(siguiente);
        return procesarCadena(fuente, tablaSimbolos, reporte);
    }

    private static boolean consumirComentario(LectorFuente fuente, Reporte reporte) {
        while (true) {
            char c = fuente.leer();
            if (c == 0) {
                reporte.error(fuente.linea(),
                        "Comentario multilinea {{ ... }} no cerrado antes del fin de archivo");
                return false;
            }
            if (c == '}') {
                char siguiente = fuente.leer();
                if (siguiente == '}') {
                    return true;
                }
                fuente.retroceder(siguiente);
            }
        }
    }

    private static Integer procesarCadena(LectorFuente fuente,
            TablaSimbolos tablaSimbolos, Reporte reporte) {
        StringBuilder cadena = new StringBuilder();
        int lineaInicio = fuente.linea();
        while (true) {
            char c = fuente.leer();
            if (c == 0) {
                reporte.error(lineaInicio, "Cadena de 1 linea no cerrada antes del fin de archivo");
                return 0;
            }
            if (c == '\n') {
                reporte.error(lineaInicio,
                        "Cadena de 1 linea { ... } no puede contener saltos de linea");
                return null;
            }
            if (c == '}') {
                Globals.yylval = tablaSimbolos.buscarOInsertarCadena(cadena.toString());
                return Globals.CADENA;
            }
            cadena.append(c);
        }
    }
}
