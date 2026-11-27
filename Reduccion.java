public class Reduccion {

    private final Reglas regla;
    private final int linea;
    private final Object[] operandos;
    private final Object resultado;

    public Reduccion(Reglas regla, int linea, Object[] operandos) {
        this(regla, linea, operandos, operandos != null && operandos.length > 0 ? operandos[0] : null);
    }

    public Reduccion(Reglas regla, int linea, Object[] operandos, Object resultado) {
        this.regla = regla;
        this.linea = linea;
        this.operandos = operandos == null ? new Object[0] : operandos;
        this.resultado = resultado;
    }

    public Reglas getRegla() {
        return regla;
    }

    public int getLinea() {
        return linea;
    }

    public Object operando(int indice) {
        if (indice < 0 || indice >= operandos.length) {
            return null;
        }
        return operandos[indice];
    }

    public Object resultado() {
        return resultado;
    }
}
