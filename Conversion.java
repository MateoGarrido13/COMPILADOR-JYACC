public class Conversion {

    private final ExpresionDiferida valor;
    private final String tipoDestino;

    public Conversion(ExpresionDiferida valor, String tipoDestino) {
        this.valor = valor;
        this.tipoDestino = tipoDestino == null ? Tipos.INDEFINIDO : tipoDestino;
    }

    public ExpresionDiferida getValor() {
        return valor;
    }

    public String getTipoDestino() {
        return tipoDestino;
    }
}
