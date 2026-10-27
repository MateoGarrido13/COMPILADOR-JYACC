/*
  Una reduccion de la gramatica, con los operandos que tenia al ejecutarse.
  Para un identificador o una constante el operando es la EntradaTabla, y con
  ella el puntero de la tabla de simbolos.
 */
public class Reduccion {

    private final Reglas regla;
    private final int linea;
    private final Object[] operandos;

    public Reduccion(Reglas regla, int linea, Object[] operandos) {
        this.regla = regla;
        this.linea = linea;
        this.operandos = operandos == null ? new Object[0] : operandos;
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
}
