import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Estructura que almacena las referencias al codigo ejecutable de cada regla.
 *
 * gramatica.y no contiene logica: cada produccion invoca ejecutar(regla, ...) y
 * esta tabla resuelve que accion semantica corresponde. Ademas registra la lista
 * de reglas reducidas, que es la salida del Analisis Sintactico hacia las etapas
 * siguientes.
 */
public class TablaAcciones {

    private final EnumMap<Reglas, AccionSemantica> acciones = new EnumMap<>(Reglas.class);
    private final List<Reglas> listaReglas = new ArrayList<>();
    private final Reporte reporte;

    public TablaAcciones(Reporte reporte) {
        this.reporte = reporte;
    }

    public void registrar(Reglas regla, AccionSemantica accion) {
        acciones.put(regla, accion);
    }

    /**
     * Punto de entrada unico desde las acciones de gramatica.y.
     *
     * Registra la reduccion, informa la estructura detectada si corresponde y
     * ejecuta el codigo asociado. Si la regla no tiene accion registrada se
     * aplica la accion por defecto de YACC: $$ = $1.
     */
    public Object ejecutar(Reglas regla, Object... valores) {
        int linea = Globals.numeroLinea;
        listaReglas.add(regla);

        if (regla.esEstructura()) {
            reporte.estructura(linea, regla.getDescripcion());
        }

        AccionSemantica accion = acciones.get(regla);
        if (accion == null) {
            if (regla.esError()) {
                reporte.error(linea, regla.getDescripcion());
            }
            return valores != null && valores.length > 0 ? valores[0] : null;
        }
        return accion.ejecutar(new ContextoRegla(regla, linea, valores));
    }

    public List<Reglas> getListaReglas() {
        return listaReglas;
    }

    public boolean tieneAccion(Reglas regla) {
        return acciones.containsKey(regla);
    }

    /** Reglas del catalogo que todavia no tienen codigo asociado. */
    public List<Reglas> reglasSinAccion() {
        List<Reglas> pendientes = new ArrayList<>();
        for (Reglas regla : Reglas.values()) {
            if (!acciones.containsKey(regla)) {
                pendientes.add(regla);
            }
        }
        return pendientes;
    }

    public String contenidoListaReglas() {
        StringBuilder texto = new StringBuilder();
        texto.append("Lista de reglas reducidas (").append(listaReglas.size()).append("):\n");
        for (int i = 0; i < listaReglas.size(); i++) {
            Reglas regla = listaReglas.get(i);
            texto.append(i + 1).append(". ").append(regla.name())
                 .append(" - ").append(regla.getDescripcion()).append("\n");
        }
        return texto.toString();
    }
}
