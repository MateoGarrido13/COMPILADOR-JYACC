import java.util.List;

/*
  Chequeos semanticos que necesitan contexto. Se invocan desde AccionesSemanticas.
 
  El parser es ascendente, por eso el contexto se va armando con acciones intermedias
  y no al reducir la declaracion completa.
 
  Verificaciones que no dependen del contexto (por ejemplo, constantes fuera de rango)
  se hacen en AccionesSemanticas y no en esta clase.
 
  - reportar: canal unico de las producciones ERR_*
  - tema 17: asignacion en expresion anidada
  - tema 19: lista de orden de evaluacion ausente o vacia
  - tema 23: enumeracion sin valores
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

    // Tema 17: ID = (expr) no puede contener otra asignacion. 
    public void verificarAsignacionAnidada(ExpresionDiferida expresion, int linea) {
        if (expresion != null && expresion.contieneAsignacion()) {
            reportar(Reglas.ERR_ASIGNACION_ANIDADA, linea);
        }
    }

    // Tema 19: falta [orden] o la lista vino vacia. 
    public void verificarOrdenEvaluacion(List<Object> constantes, int linea) {
        if (constantes == null || constantes.isEmpty()) {
            reportar(Reglas.ERR_FALTA_ORDEN_EVALUACION, linea);
        }
    }

    // el signo unario puede sacar la constante de rango. 
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

    // Tema 23: typedef sin valores. 
    public void verificarValoresEnumerado(List<Object> valores, int linea) {
        if (valores == null || valores.isEmpty()) {
            reportar(Reglas.ERR_FALTA_VALORES_ENUMERADO, linea);
        }
    }
}
