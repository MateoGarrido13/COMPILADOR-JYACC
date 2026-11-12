import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
  - Paso de parametros: cantidad, orden de evaluacion y alcance de funcion
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

    // El identificador no esta declarado en el ambito actual ni en uno que lo envuelve.
    public void verificarAlcance(EntradaTabla id) {
        if (id == null) {
            return;
        }
        String nombre = TablaSimbolos.nombreDe(id.lexema);
        if (ControlAmbito.alcanzable(nombre)) {
            return;
        }
        int linea = id.lineaAparicion > 0 ? id.lineaAparicion : id.numeroLinea;
        reporte.error(linea, "Error de ambito '" + nombre + "' no es alcanzable");
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

    /*
      Valida el orden de evaluacion de los parametros reales contra la cantidad
      de parametros de la funcion.
      
      Reglas:
      - cada posicion indicada debe estar en el rango [1, cantidad]
      - no debe repetirse ninguna posicion
      - la cantidad de posiciones indicadas debe coincidir con la cantidad de
        parametros reales
     */
    public void verificarOrdenEvaluacion(List<Object> constantes, int cantidadParametros, int linea) {
        if (constantes == null || constantes.isEmpty()) {
            reportar(Reglas.ERR_FALTA_ORDEN_EVALUACION, linea);
            return;
        }

        List<Integer> posiciones = extraerPosiciones(constantes, linea);
        if (posiciones == null) {
            return;
        }

        Set<Integer> vistos = new HashSet<>();
        for (Integer pos : posiciones) {
            if (pos < 1 || pos > cantidadParametros) {
                reporte.error(linea, "Posicion de orden de evaluacion fuera de rango: " + pos);
                continue;
            }
            if (!vistos.add(pos)) {
                reporte.error(linea, "Posicion de orden de evaluacion duplicada: " + pos);
            }
        }

        if (vistos.size() != cantidadParametros) {
            reporte.error(linea, "Cantidad de posiciones de orden de evaluacion (" + vistos.size()
                    + ") no coincide con la cantidad de parametros (" + cantidadParametros + ")");
        }
    }

    /*
      Extrae los valores enteros de una lista de constantes (EntradaTabla) del orden
      de evaluacion. Devuelve null si hubo un error de conversion.
     */
    private List<Integer> extraerPosiciones(List<Object> constantes, int linea) {
        List<Integer> posiciones = new ArrayList<>();
        for (Object c : constantes) {
            EntradaTabla entrada = ContextoRegla.comoEntrada(c);
            if (entrada == null) {
                reporte.error(linea, "Posicion de orden de evaluacion invalida");
                return null;
            }
            BigInteger bi = Tipos.valorEntero(entrada.lexema);
            if (bi == null) {
                reporte.error(linea, "Posicion de orden de evaluacion no es un entero: " + entrada.lexema);
                return null;
            }
            posiciones.add(bi.intValue());
        }
        return posiciones;
    }

    // Verifica que la cantidad de parametros reales coincida con los formales declarados.
    public void verificarCantidadParametros(EntradaTabla funcion, List<ParametroReal> parametros, int linea) {
        if (funcion == null) {
            return;
        }
        String nombre = TablaSimbolos.nombreDe(funcion.lexema);
        Entorno.FuncionDeclarada fd = entorno.getFuncion(nombre);
        if (fd == null) {
            reporte.error(linea, "Funcion no declarada '" + nombre + "'");
            return;
        }
        int formales = fd.parametros.size();
        int reales = parametros.size();
        if (reales != formales) {
            reporte.error(linea, "Cantidad de parametros reales (" + reales
                    + ") no coincide con los formales declarados (" + formales + ")");
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
