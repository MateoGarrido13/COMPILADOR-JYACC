public class AccionComparador implements AccionSemantica {
    private final ContextoLexico contexto;

    public AccionComparador(ContextoLexico contexto) {
        this.contexto = contexto;
    }

    @Override
    public int ejecutar() {
        String lexema = contexto.lexema.toString();
        Globals.yylval = null;
        switch (lexema) {
            case "<": return '<';
            case ">": return '>';
            case "==": return Globals.IGUAL_IGUAL;
            case "!=": return Globals.DISTINTO;
            case "<=": return Globals.MENOR_IGUAL;
            case ">=": return Globals.MAYOR_IGUAL;
            default:
                contexto.reporte.error(contexto.fuente.linea(),
                        "Comparador invalido '" + lexema + "'");
                return -1;
        }
    }
}