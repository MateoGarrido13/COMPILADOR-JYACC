/*
  Conversion explicita. La unica conversion prevista por el lenguaje es todf
  (Tema 33): un entero (ulongint o un enumerado con base ulongint) a doublef.
  La accion CONVERSION_TODF valida el tipo del operando y crea esta clase; el
  motor de tipos sabe que una conversion tiene el tipo destino.
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
