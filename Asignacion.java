/*
  Asignacion ya validada.

  Si el destino es una variable de clase, el generador emite
  (COPY, destino, origen): una copia de la estructura, no un (:=) por atributo.
  Si el destino es una ReferenciaAtributo, emite
  (:=, objeto.atributo, origen).
 */
public class Asignacion {

    public final Object destino;
    public final String tipoDestino;
    public final String tipoOrigen;

    public Asignacion(Object destino, String tipoDestino, String tipoOrigen) {
        this.destino = destino;
        this.tipoDestino = tipoDestino == null ? Tipos.INDEFINIDO : tipoDestino;
        this.tipoOrigen = tipoOrigen == null ? Tipos.INDEFINIDO : tipoOrigen;
    }
}
