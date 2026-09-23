public class AccionCadena implements AccionSemantica {
    private final ContextoLexico contexto;

    public AccionCadena(ContextoLexico contexto) {
        this.contexto = contexto;
    }

    @Override
    public int ejecutar() {
        String lexema = contexto.lexema.toString();
        if (lexema.startsWith("{{") && lexema.endsWith("}}")) return -1; // Comentario multilinea, descarto
        if (lexema.length() < 2 || !lexema.startsWith("{") || !lexema.endsWith("}")) {
            return error("Cadena no cerrada '" + lexema + "'");
        }
        String contenido = lexema.substring(1, lexema.length() - 1);
        if (contenido.indexOf('\n') >= 0) return error("Cadena de una linea con salto de linea");
        Globals.yylval = contexto.tablaSimbolos.buscarOInsertarCadena(contenido);
        return Globals.CADENA;
    }

    private int error(String mensaje) {
        contexto.reporte.error(contexto.fuente.linea(), mensaje);
        return -1;
    }
}