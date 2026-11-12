/*
  Representa una invocacion de funcion lista para generar tercetos.
  Contiene la funcion a llamar, los parametros reales y el orden de evaluacion.
 */
import java.util.List;

public class LlamadaFuncion {
    public final EntradaTabla funcion;
    public final List<ParametroReal> parametros;
    public final List<Integer> ordenEvaluacion; // posiciones 1-based, o null
    public final int linea;
    private Integer numeroCall; // numero del terceto CALL, asignado por GeneradorTercetos

    public LlamadaFuncion(EntradaTabla funcion, List<ParametroReal> parametros,
                          List<Integer> ordenEvaluacion, int linea) {
        this.funcion = funcion;
        this.parametros = parametros;
        this.ordenEvaluacion = ordenEvaluacion;
        this.linea = linea;
    }

    public Integer getNumeroCall() {
        return numeroCall;
    }

    public void setNumeroCall(int numeroCall) {
        this.numeroCall = numeroCall;
    }
}
