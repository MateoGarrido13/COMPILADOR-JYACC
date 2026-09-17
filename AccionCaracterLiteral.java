public final class AccionCaracterLiteral  implements AccionSemantica {

    private AccionCaracterLiteral() {
    }

    public static void agregar(ContextoLexico contexto, char caracter) {
        contexto.agregar(caracter);
    }

    public static int finalizar(ContextoLexico contexto, Reporte reporte) {
        String lexema = contexto.lexema();
        Globals.yylval = null;
        if ("<=".equals(lexema)) return Globals.MENOR_IGUAL;
        if (">=".equals(lexema)) return Globals.MAYOR_IGUAL;
        if ("==".equals(lexema)) return Globals.IGUAL_IGUAL;
        if ("!=".equals(lexema)) return Globals.DISTINTO;
        if ("!".equals(lexema)) {
            reporte.error(contexto.lineaInicio(), "Caracter no reconocido '!'");
            return 0;
        }
        return lexema.charAt(0);
    }

    /** SEM 3 a 6: reconoce comparadores compuestos y caracteres literales. */
    public static Integer procesar(char c, LectorFuente fuente, Reporte reporte) {
        if (c != '<' && c != '>' && c != '=' && c != '!') {
            return null;
        }
        char siguiente = fuente.leer();
        if (c == '<' && siguiente == '=') {
            return Globals.MENOR_IGUAL;
        }
        if (c == '>' && siguiente == '=') {
            return Globals.MAYOR_IGUAL;
        }
        if (c == '=' && siguiente == '=') {
            return Globals.IGUAL_IGUAL;
        }
        if (c == '!' && siguiente == '=') {
            return Globals.DISTINTO;
        }
        fuente.retroceder(siguiente);
        if (c == '!') {
            reporte.error(fuente.linea(), "Caracter no reconocido '!'");
            return 0;
        }
        return (int) c;
    }
}
