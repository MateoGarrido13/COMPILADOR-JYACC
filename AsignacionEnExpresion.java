public class AsignacionEnExpresion {

    public final EntradaTabla destino;
    public final ExpresionDiferida valor;
    private final String tipo;

    public AsignacionEnExpresion(EntradaTabla destino, ExpresionDiferida valor, String tipo) {
        this.destino = destino;
        this.valor = valor;
        this.tipo = tipo == null ? Tipos.INDEFINIDO : tipo;
    }

    public String getTipo() {
        return tipo;
    }
}
