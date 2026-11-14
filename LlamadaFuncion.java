/*
  Representa una invocacion de funcion lista para generar tercetos.
  Contiene la funcion a llamar, los parametros reales, el orden de evaluacion
  y la firma resuelta con su tipo de retorno.
 */
import java.util.List;

public class LlamadaFuncion {
    public final EntradaTabla funcion;
    public final List<ParametroReal> parametros;
    public final List<Integer> ordenEvaluacion; // posiciones 1-based, o null
    public final int linea;
    public final Entorno.FuncionDeclarada declaracion;
    private final String tipoRetorno;
    private Integer numeroCall; // numero del terceto CALL, asignado por GeneradorTercetos

    public LlamadaFuncion(EntradaTabla funcion, List<ParametroReal> parametros,
                          List<Integer> ordenEvaluacion, int linea) {
        this(funcion, parametros, ordenEvaluacion, linea, null, null);
    }

    public LlamadaFuncion(EntradaTabla funcion, List<ParametroReal> parametros,
                          List<Integer> ordenEvaluacion, int linea,
                          Entorno.FuncionDeclarada declaracion, String tipoRetorno) {
        this.funcion = funcion;
        this.parametros = parametros;
        this.ordenEvaluacion = ordenEvaluacion;
        this.linea = linea;
        this.declaracion = declaracion;
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
}
