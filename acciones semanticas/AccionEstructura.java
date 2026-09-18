public class AccionEstructura implements AccionSemantica {
    private final ContextoLexico contexto;

    public AccionEstructura(ContextoLexico contexto) {
        this.contexto = contexto;
    }

    @Override
    public int ejecutar() {
        String lexema = contexto.lexema.toString();
        if (":=".equals(lexema)) {
            Globals.yylval = null;
            return Globals.ASIGNACION;
        }
        if (":".equals(lexema)) {
            Globals.yylval = null;
            return ':';
        }
        contexto.reporte.error(contexto.fuente.linea(),
                "Estructura invalida '" + contexto.lexema + "'");
        return -1;
    }
}