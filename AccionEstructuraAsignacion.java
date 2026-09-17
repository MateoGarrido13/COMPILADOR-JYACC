public final class AccionEstructuraAsignacion extends AccionSemantica {

    private AccionEstructuraAsignacion() {
    }

    public static void agregar(ContextoLexico contexto, char caracter) {
        contexto.agregar(caracter);
    }

    public static int finalizar(ContextoLexico contexto) {
        String lexema = contexto.lexema();
        Globals.yylval = null;
        return ":=".equals(lexema) ? Globals.ASIGNACION : lexema.charAt(0);
    }

    public static int procesar(LectorFuente fuente) {
        char siguiente = fuente.leer();
        if (siguiente == '=') {
            Globals.yylval = null;
            return Globals.ASIGNACION;
        }
        fuente.retroceder(siguiente);
        return ':';
    }
}
