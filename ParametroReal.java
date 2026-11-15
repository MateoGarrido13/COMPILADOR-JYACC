/*
  Representa un parametro real en una invocacion de funcion o metodo.
  nombreFormal es null cuando el parametro se pasa por posicion.
  valor es una EntradaTabla o una ExpresionDiferida.
  formal es la EntradaTabla del parametro formal resuelto, cuando se conoce;
  el generador la usa como primer operando del terceto (PAR, formal, real).
  tipoReal es el tipo del real, para compararlo con el del formal sin
  conversiones implicitas (Tema 19).
 */
public class ParametroReal {
    public final String nombreFormal;
    public final Object valor;
    public final int linea;
    public final EntradaTabla formal;
    public final String tipoReal;

    public ParametroReal(String nombreFormal, Object valor, int linea) {
        this(nombreFormal, valor, linea, null, null);
    }

    public ParametroReal(String nombreFormal, Object valor, int linea, EntradaTabla formal) {
        this(nombreFormal, valor, linea, formal, null);
    }

    public ParametroReal(String nombreFormal, Object valor, int linea,
            EntradaTabla formal, String tipoReal) {
        this.nombreFormal = nombreFormal;
        this.valor = valor;
        this.linea = linea;
        this.formal = formal;
        this.tipoReal = tipoReal;
    }

    public ParametroReal conFormal(EntradaTabla formal) {
        return new ParametroReal(nombreFormal, valor, linea, formal, tipoReal);
    }

    public ParametroReal conTipoReal(String tipoReal) {
        return new ParametroReal(nombreFormal, valor, linea, formal, tipoReal);
    }
}
