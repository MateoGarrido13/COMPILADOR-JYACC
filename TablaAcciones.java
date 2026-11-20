import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class TablaAcciones {

    private final EnumMap<Reglas, AccionRegla> acciones = new EnumMap<>(Reglas.class);
    private final List<Reduccion> reducciones = new ArrayList<>();
    private final Reporte reporte;

    public TablaAcciones(Reporte reporte) {
        this.reporte = reporte;
    }

    public void registrar(Reglas regla, AccionRegla accion) {
        acciones.put(regla, accion);
    }

    public Object ejecutar(Reglas regla, Object... valores) {
        int linea = Globals.numeroLinea;

        AccionRegla accion = acciones.get(regla);
        Object resultado;
        if (accion == null) {
            if (regla.esError()) {
                reporte.error(linea, regla.getDescripcion());
            }
            resultado = valores != null && valores.length > 0 ? valores[0] : null;
        } else {
            resultado = accion.ejecutar(new ContextoRegla(regla, linea, valores));
        }

        reducciones.add(new Reduccion(regla, linea, valores, resultado));

        if (regla.esEstructura()) {
            reporte.estructura(linea, regla.getDescripcion());
        }

        return resultado;
    }

    public List<Reglas> getListaReglas() {
        List<Reglas> reglas = new ArrayList<>();
        for (Reduccion reduccion : reducciones) {
            reglas.add(reduccion.getRegla());
        }
        return reglas;
    }

    public List<Reduccion> getReducciones() {
        return reducciones;
    }

    public boolean tieneAccion(Reglas regla) {
        return acciones.containsKey(regla);
    }

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
        texto.append("Lista de reglas reducidas (").append(reducciones.size()).append("):\n");
        for(Reduccion reduccion : reducciones) {
            texto.append(reduccion.getRegla().name())
                 .append(" - ").append(reduccion.getRegla().getDescripcion()).append("\n");
        }
        return texto.toString();
    }
}
