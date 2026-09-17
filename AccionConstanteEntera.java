public final class AccionConstanteEntera {

    private AccionConstanteEntera() {
    }

    public static void agregar(ContextoLexico contexto, char caracter) {
        contexto.agregar(caracter);
    }

    public static int finalizar(ContextoLexico contexto, TablaSimbolos tablaSimbolos) {
        Globals.yylval = tablaSimbolos.buscarOInsertarConstante(
                contexto.lexema(), Globals.CONSTANTE_NUMERICA);
        return Globals.CONSTANTE_NUMERICA;
    }

    /** SEM 21 a 26: lee enteros y deriva a float cuando aparece un punto. */
    public static int procesar(char inicial, LectorFuente fuente,
            TablaSimbolos tablaSimbolos) {
        StringBuilder lexema = new StringBuilder();
        char c = inicial;
        lexema.append(c);
        c = fuente.leer();

        while (Character.isDigit(c)) {
            lexema.append(c);
            c = fuente.leer();
        }
        if (c == '.') {
            lexema.append(c);
            return AccionConstanteFloat.procesarDesdeParteEntera(
                    lexema, fuente.leer(), fuente, tablaSimbolos);
        }
        fuente.retroceder(c);

        c = fuente.leer();
        if (c == '$') {
            char u = fuente.leer();
            char l = fuente.leer();
            if (Character.toLowerCase(u) == 'u' && Character.toLowerCase(l) == 'l') {
                lexema.append(Tipos.SUFIJO_ULONGINT);
            } else {
                fuente.retroceder(l);
                fuente.retroceder(u);
                fuente.retroceder('$');
            }
        } else {
            fuente.retroceder(c);
        }

        Globals.yylval = tablaSimbolos.buscarOInsertarConstante(
                lexema.toString(), Globals.CONSTANTE_NUMERICA);
        return Globals.CONSTANTE_NUMERICA;
    }
}
