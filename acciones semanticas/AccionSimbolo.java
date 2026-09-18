public class AccionSimbolo implements AccionSemantica {
    private final ContextoLexico contexto;

    public AccionSimbolo(ContextoLexico contexto) {
        this.contexto = contexto;
    }

    @Override
    public int ejecutar() {
        Globals.yylval = null;
        if (contexto.lexema.length() == 1) { // Solo acepta simbolos de un caracter, todos los literales (menos letras)
            return contexto.lexema.charAt(0);
        }
        contexto.reporte.error(contexto.fuente.linea(), // Semantica generica errores de tipeo
                "Simbolo invalido '" + contexto.lexema + "'");
        return -1;
    }
}