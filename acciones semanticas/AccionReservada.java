public class AccionReservada implements AccionSemantica {
    private final ContextoLexico contexto;

    public AccionReservada(ContextoLexico contexto) {
        this.contexto = contexto;
    }

    @Override
    public int ejecutar() {
        EntradaTabla reservada = contexto.tablaSimbolos.buscarPalabraReservada(contexto.lexema.toString());
        if (reservada == null) {
            contexto.reporte.error(contexto.fuente.linea(),
                    "Palabra reservada invalida '" + contexto.lexema + "'");
            return -1;
        }
        Globals.yylval = reservada;
        return reservada.tokenID;
    }
}