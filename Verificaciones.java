import java.util.List;

/**
 * Chequeos del PDF que necesitan contexto. No guarda declaraciones:
 * consulta y, si hace falta, lee el Entorno.
 *
 * - reportar: canal unico de las producciones ERR_*
 * - tema 17: asignacion en expresion anidada
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

    /** Tema 17: la RHS de ID = (expr) no puede contener otra asignacion. */
    public void verificarAsignacionAnidada(ExpresionDiferida expresion, int linea) {
        if (expresion != null && expresion.contieneAsignacion()) {
            reportar(Reglas.ERR_ASIGNACION_ANIDADA, linea);
        }
    }

    /** Tema 19: falta [orden] o la lista vino vacia. */
    public void verificarOrdenEvaluacion(List<Object> constantes, int linea) {
        if (constantes == null || constantes.isEmpty()) {
            reportar(Reglas.ERR_FALTA_ORDEN_EVALUACION, linea);
        }
    }

    /** TP2 consideracion c): el signo unario puede sacar la constante de rango. */
    public void verificarRangoConstante(EntradaTabla constante, int linea) {
        if (constante == null || constante.lexema == null) {
            return;
        }
        String tipo = constante.tipoDato != null
                ? constante.tipoDato
                : Tipos.tipoDeConstante(constante.lexema);
        if (tipo != null && !Tipos.rangoValido(constante.lexema, tipo)) {
            reporte.error(linea, "Constante fuera de rango '" + constante.lexema + "'");
        }
    }

    /** Tema 23: typedef sin valores. */
    public void verificarValoresEnumerado(List<Object> valores, int linea) {
        if (valores == null || valores.isEmpty()) {
            reportar(Reglas.ERR_FALTA_VALORES_ENUMERADO, linea);
        }
    }
}
