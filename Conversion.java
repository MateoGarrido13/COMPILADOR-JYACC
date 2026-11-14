/*
  Conversion explicita. La unica conversion prevista por el lenguaje es todf
  (Tema 33); la accion CONVERSION_TODF todavia no genera esta clase, pero el
  motor de tipos ya sabe que una conversion tiene el tipo destino.
 */
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
