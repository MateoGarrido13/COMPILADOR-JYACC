public final class AccionSimboloLiteral {

    private AccionSimboloLiteral() {
    }

    public static void agregar(ContextoLexico contexto, char caracter) {
        contexto.agregar(caracter);
    }

    public static int finalizar(ContextoLexico contexto) {
        Globals.yylval = null;
        return contexto.lexema().charAt(0);
    }

    /** SEM 20: reconoce símbolos de un solo carácter. */
    public static boolean esSimbolo(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/'
                || c == '(' || c == ')' || c == ',' || c == ';'
                || c == '[' || c == ']';
    }
}
