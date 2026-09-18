public class AccionConstanteEntera implements AccionSemantica {
    private final ContextoLexico contexto;

    public AccionConstanteEntera(ContextoLexico contexto) {
        this.contexto = contexto;
    }

    @Override
    public int ejecutar() {
        String lexema = contexto.lexema.toString();
        String normalizado = lexema.toLowerCase();

        if (!normalizado.matches("[+-]?[0-9]+\\$ul")) {
            contexto.reporte.error(contexto.fuente.linea(),
                "Constante entera invalida '" + lexema + "'");
            return -1;
        }

        if (!Tipos.rangoValido(normalizado, Tipos.ULONGINT)) {
            contexto.reporte.error(contexto.fuente.linea(),
                "Constante entera fuera de rango '" + lexema + "'");
            return -1;
        }

        Globals.yylval = contexto.tablaSimbolos.buscarOInsertarConstante(
            lexema, Globals.CONSTANTE_NUMERICA);
        return Globals.CONSTANTE_NUMERICA;
    }
}