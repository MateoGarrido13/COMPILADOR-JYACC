/*
  Representa un parametro real en una invocacion de funcion o metodo.
  nombreFormal es null cuando el parametro se pasa por posicion.
  valor es una EntradaTabla o una ExpresionDiferida.
  formal es la EntradaTabla del parametro formal resuelto, cuando se conoce;
  el generador la usa como primer operando del terceto (PAR, formal, real).
 */
public class ParametroReal {
    public final String nombreFormal;
    public final Object valor;
    public final int linea;
    public final EntradaTabla formal;

    public ParametroReal(String nombreFormal, Object valor, int linea) {
        this(nombreFormal, valor, linea, null);
    }

    public ParametroReal(String nombreFormal, Object valor, int linea, EntradaTabla formal) {
        this.nombreFormal = nombreFormal;
        this.valor = valor;
        this.linea = linea;
        this.formal = formal;
    }

    public ParametroReal conFormal(EntradaTabla formal) {
        return new ParametroReal(nombreFormal, valor, linea, formal);
    }
}
