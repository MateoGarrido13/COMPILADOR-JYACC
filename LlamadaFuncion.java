/*
  Representa una invocacion de funcion o de metodo lista para generar tercetos.
  Contiene la funcion a llamar, los parametros reales, el orden de evaluacion
  y la firma resuelta con su tipo de retorno.

  Para metodos guarda ademas el objeto receptor. La convencion de codigo
  intermedio es (CALLMET, objeto, metodo), precedida por los PAR de los
  parametros reales.
 */
import java.util.List;

public class LlamadaFuncion {
    public final EntradaTabla funcion;
    public final List<ParametroReal> parametros;
    public final List<Integer> ordenEvaluacion; // posiciones 1-based, o null
    public final int linea;
    public final Entorno.FuncionDeclarada declaracion;
    private final String tipoRetorno;
    public final EntradaTabla objeto;
    public final boolean esMetodo;
    private Integer numeroCall; // numero del terceto CALL/CALLMET, asignado por GeneradorTercetos
    private boolean valida = true;

    public LlamadaFuncion(EntradaTabla funcion, List<ParametroReal> parametros,
                          List<Integer> ordenEvaluacion, int linea) {
        this(funcion, parametros, ordenEvaluacion, linea, null, null, null, false);
    }

    public LlamadaFuncion(EntradaTabla funcion, List<ParametroReal> parametros,
                          List<Integer> ordenEvaluacion, int linea,
                          Entorno.FuncionDeclarada declaracion, String tipoRetorno) {
        this(funcion, parametros, ordenEvaluacion, linea, declaracion, tipoRetorno, null, false);
    }

    public LlamadaFuncion(EntradaTabla funcion, List<ParametroReal> parametros,
                          List<Integer> ordenEvaluacion, int linea,
                          Entorno.FuncionDeclarada declaracion, String tipoRetorno,
                          EntradaTabla objeto, boolean esMetodo) {
        this.funcion = funcion;
        this.parametros = parametros;
        this.ordenEvaluacion = ordenEvaluacion;
        this.linea = linea;
        this.declaracion = declaracion;
        this.objeto = objeto;
        this.esMetodo = esMetodo;
        this.tipoRetorno = tipoRetorno != null ? tipoRetorno
                : declaracion != null ? declaracion.tipoRetorno
                : funcion != null ? funcion.tipoDato
                : Tipos.INDEFINIDO;
    }

    public String getTipoRetorno() {
        return tipoRetorno == null ? Tipos.INDEFINIDO : tipoRetorno;
    }

    public Integer getNumeroCall() {
        return numeroCall;
    }

    public void setNumeroCall(int numeroCall) {
        this.numeroCall = numeroCall;
    }

    public boolean esValida() {
        return valida;
    }

    public void marcarInvalida() {
        this.valida = false;
    }
}
