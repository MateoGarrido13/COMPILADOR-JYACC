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
