public final class ContextoLexico {

    private final StringBuilder lexema = new StringBuilder();
    private final int lineaInicio;

    public ContextoLexico(int lineaInicio) {
        this.lineaInicio = lineaInicio;
    }

    public void agregar(char c) {
        lexema.append(c);
    }

    public String lexema() {
        return lexema.toString();
    }

    public int lineaInicio() {
        return lineaInicio;
    }

    public void limpiar() {
        lexema.setLength(0);
    }
}
