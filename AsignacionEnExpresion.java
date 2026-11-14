/*
  Asignacion dentro de una expresion. El tipo de la asignacion es el tipo del
  destino, porque el valor que entrega la expresion es el valor asignado.

  La accion ASIGNACION_EN_EXPRESION la devuelve envuelta en una
  ExpresionDiferida. El terceto propio de la asignacion interna queda para el
  Tema 17; por ahora el generador emite el valor del lado derecho.
 */
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
