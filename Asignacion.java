/*
  Asignacion entre dos objetos de clases compatibles.

  La accion ASIGNACION la devuelve cuando destino y origen son clases y
  Entorno.clasesCompatibles acepta el par. El generador emite
  (COPY, destino, origen): una copia de la estructura, no un (:=) por atributo.
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
