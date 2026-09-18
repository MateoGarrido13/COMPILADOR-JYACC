public class AccionConstanteFloat implements AccionSemantica {
    private final ContextoLexico contexto;

    public AccionConstanteFloat(ContextoLexico contexto) {
        this.contexto = contexto;
    }

    @Override
    public int ejecutar() {
        String lexema = contexto.lexema.toString();
        String normalizado = lexema.toLowerCase();

        if (!normalizado.matches("[+-]?(([0-9]+\\.[0-9]*|\\.[0-9]+)([dD][+-]?[0-9]+)?)")) {
            contexto.reporte.error(contexto.fuente.linea(),
                "Constante doublef invalida '" + lexema + "'");
            return -1;
        }

        if (!Tipos.rangoValido(normalizado, Tipos.DOUBLEF)) {
            contexto.reporte.error(contexto.fuente.linea(),
                "Constante doublef fuera de rango '" + lexema + "'");
            return -1;
        }

        Globals.yylval = contexto.tablaSimbolos.buscarOInsertarConstante(
            lexema, Globals.CONSTANTE_NUMERICA);
        return Globals.CONSTANTE_NUMERICA;
    }
}