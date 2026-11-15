/*
  Asignacion dentro de una expresion. El tipo de la asignacion es el tipo del
  destino, porque el valor que entrega la expresion es el valor asignado.

  La accion ASIGNACION_EN_EXPRESION la devuelve envuelta en una
  ExpresionDiferida. El generador emite el valor del lado derecho y despues
  el terceto (:=, destino, valor) antes de usar el resultado.
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
