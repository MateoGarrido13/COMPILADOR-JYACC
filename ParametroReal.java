/*
  Representa un parametro real en una invocacion de funcion.
  nombreFormal es null cuando el parametro se pasa por posicion.
  valor es una EntradaTabla o una ExpresionDiferida.
 */
public class ParametroReal {
    public final String nombreFormal;
    public final Object valor;
    public final int linea;

    public ParametroReal(String nombreFormal, Object valor, int linea) {
        this.nombreFormal = nombreFormal;
        this.valor = valor;
        this.linea = linea;
    }
}
