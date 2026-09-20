import java.util.List;

/**
 * Chequeos del PDF que necesitan contexto. No guarda declaraciones:
 * consulta y, si hace falta, lee el Entorno.
 *
 * - reportar: canal unico de las producciones ERR_*
 * - tema 21: funcion sin RET
 * - tema 19: lista de orden de evaluacion ausente o vacia
 * - tema 23: enumeracion sin valores
 */
public class Verificaciones {

    private final Entorno entorno;
    private final Reporte reporte;

    public Verificaciones(Entorno entorno, Reporte reporte) {
        this.entorno = entorno;
        this.reporte = reporte;
    }

    public Entorno getEntorno() {
        return entorno;
    }

    public void reportar(Reglas regla, int linea) {
        if (regla == null) {
            return;
        }
        reporte.error(linea, regla.getDescripcion());
    }

    /** Tema 21: se invoca al reducir DECL_FUNCION / DECL_METODO, antes de cerrar el entorno. */
    public void verificarRetorno(Entorno.FuncionDeclarada funcion, int linea) {
        if (funcion == null || funcion.tuvoRetorno) {
            return;
        }
        int lineaError = funcion.linea != 0 ? funcion.linea : linea;
        reportar(Reglas.ERR_FALTA_RETORNO, lineaError);
    }

    /** Tema 19: falta [orden] o la lista vino vacia. */
    public void verificarOrdenEvaluacion(List<Object> constantes, int linea) {
        if (constantes == null || constantes.isEmpty()) {
            reportar(Reglas.ERR_FALTA_ORDEN_EVALUACION, linea);
        }
    }

    /** Tema 23: typedef sin valores. */
    public void verificarValoresEnumerado(List<Object> valores, int linea) {
        if (valores == null || valores.isEmpty()) {
            reportar(Reglas.ERR_FALTA_VALORES_ENUMERADO, linea);
        }
    }
}
