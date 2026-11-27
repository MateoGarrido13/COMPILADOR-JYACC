public class ResultadoTerceto {

    private final int indice;
    private final String operador;
    private final String tipo;
    private final boolean produceValor;

    public ResultadoTerceto(int indice, String operador, String tipo, boolean produceValor) {
        this.indice = indice;
        this.operador = operador;
        this.tipo = tipo == null ? Tipos.INDEFINIDO : tipo;
        this.produceValor = produceValor;
    }

    public int getIndice() {
        return indice;
    }

    public String getOperador() {
        return operador;
    }

    public String getTipo() {
        return tipo;
    }

    public boolean produceValor() {
        return produceValor;
    }

    public String nombreAuxiliar() {
        return "@aux" + indice;
    }

    public String tipoAsmDato() {
        if (Tipos.DOUBLEF.equals(tipo)) {
            return "DQ";
        }
        if (Tipos.ULONGINT.equals(tipo)) {
            return "DD";
        }
        return null;
    }
}
