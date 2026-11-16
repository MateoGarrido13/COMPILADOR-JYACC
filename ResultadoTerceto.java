/*
  Metadata de resultado de un terceto para backend ASM (TP4).
  El IR usa referencias [n] como temporales implicitos; aqui se registra si
  el terceto produce valor, su tipo logico y el nombre auxiliar sugerido @auxN.
  No altera la emision ni la visualizacion del IR.
 */
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
