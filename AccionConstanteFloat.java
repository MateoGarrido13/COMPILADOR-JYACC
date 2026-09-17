public final class AccionConstanteFloat implements AccionSemantica {

    private AccionConstanteFloat() {
    }

    public static void agregar(ContextoLexico contexto, char caracter) {
        contexto.agregar(caracter);
    }

    public static int finalizar(ContextoLexico contexto, TablaSimbolos tablaSimbolos) {
        Globals.yylval = tablaSimbolos.buscarOInsertarConstante(
                contexto.lexema(), Globals.CONSTANTE_NUMERICA);
        return Globals.CONSTANTE_NUMERICA;
    }

    /** SEM 26 a 35: completa la parte decimal y el exponente opcional. */
    public static int procesar(char inicial, LectorFuente fuente,
            TablaSimbolos tablaSimbolos) {
        StringBuilder lexema = new StringBuilder();
        char c = inicial;
        if (c == '.') {
            char siguiente = fuente.leer();
            if (!Character.isDigit(siguiente)) {
                fuente.retroceder(siguiente);
                Globals.yylval = null;
                return '.';
            }
            lexema.append('.').append(siguiente);
            c = fuente.leer();
        } else {
            lexema.append(c);
            c = fuente.leer();
        }
        return completar(lexema, c, fuente, tablaSimbolos);
    }

    public static int procesarDesdeParteEntera(StringBuilder lexema, char siguiente,
            LectorFuente fuente, TablaSimbolos tablaSimbolos) {
        return completar(lexema, siguiente, fuente, tablaSimbolos);
    }

    private static int completar(StringBuilder lexema, char c, LectorFuente fuente,
            TablaSimbolos tablaSimbolos) {
        while (Character.isDigit(c)) {
            lexema.append(c);
            c = fuente.leer();
        }

        if (Character.toLowerCase(c) == Tipos.LETRA_EXPONENTE_DOUBLEF) {
            lexema.append(c);
            c = fuente.leer();
            if (c == '+' || c == '-') {
                lexema.append(c);
                c = fuente.leer();
            }
            while (Character.isDigit(c)) {
                lexema.append(c);
                c = fuente.leer();
            }
        }
        fuente.retroceder(c);
        Globals.yylval = tablaSimbolos.buscarOInsertarConstante(
                lexema.toString(), Globals.CONSTANTE_NUMERICA);
        return Globals.CONSTANTE_NUMERICA;
    }
}
