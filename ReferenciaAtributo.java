/*
  Referencia a un atributo de un objeto. La usa el motor de tipos para conocer
  el tipo del atributo y el generador para mostrar la referencia completa.

  El tipo del atributo queda en 'tipo'. Si el atributo no se pudo resolver, la
  referencia conserva el nombre y el tipo queda indefinido para no cascadear
  errores; la validacion completa de atributos y herencia es del Tema 28/31.
 */
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
