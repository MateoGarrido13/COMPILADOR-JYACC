/*
  Referencia a un atributo de un objeto. La usa el motor de tipos para conocer
  el tipo del atributo y el generador para mostrar la referencia completa.

  El tipo del atributo queda en 'tipo'. 'texto' es lo que imprime el terceto
  (objeto.atributo, o objeto.clase.atributo en el acceso prefijado). Si el
  atributo no se pudo resolver, el tipo queda indefinido para no cascadear
  errores. El acceso sin prefijo solo ve miembros propios; el heredado exige
  el prefijo de la clase que lo declara.
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
