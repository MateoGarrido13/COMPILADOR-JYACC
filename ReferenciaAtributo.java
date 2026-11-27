public class ReferenciaAtributo {

    public final EntradaTabla objeto;
    public final EntradaTabla atributo;
    public final String texto;
    private final String tipo;

    public ReferenciaAtributo(EntradaTabla objeto, EntradaTabla atributo, String texto, String tipo) {
        this.objeto = objeto;
        this.atributo = atributo;
        this.texto = texto;
        this.tipo = tipo == null ? Tipos.INDEFINIDO : tipo;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return texto;
    }
}
