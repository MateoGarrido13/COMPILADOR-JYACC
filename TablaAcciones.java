import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/*
  Estructura que almacena las referencias al codigo ejecutable de cada regla.
 
  gramatica.y no contiene logica: cada produccion invoca ejecutar(regla, ...) y
  esta tabla resuelve que accion semantica corresponde. Ademas registra la lista
  de reglas reducidas, que es la salida del Analisis Sintactico hacia las etapas
  siguientes.
 */
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

    /*
      Registra la reduccion, informa la estructura detectada si corresponde y
      ejecuta el codigo asociado. Si la regla no tiene accion registrada se
      aplica la accion por defecto de YACC: $$ = $1.
     */
    public Object ejecutar(Reglas regla, Object... valores) {
        int linea = Globals.numeroLinea;
        reducciones.add(new Reduccion(regla, linea, valores));

        if (regla.esEstructura()) {
            reporte.estructura(linea, regla.getDescripcion());
        }

        AccionRegla accion = acciones.get(regla);
        if (accion == null) {
            if (regla.esError()) {
                reporte.error(linea, regla.getDescripcion());
            }
            return valores != null && valores.length > 0 ? valores[0] : null;
        }
        return accion.ejecutar(new ContextoRegla(regla, linea, valores));
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

    // Reglas del catalogo que todavia no tienen codigo asociado.
    public List<Reglas> reglasSinAccion() {
        List<Reglas> pendientes = new ArrayList<>();
        for (Reglas regla : Reglas.values()) {
            if (!acciones.containsKey(regla)) {
                pendientes.add(regla);
            }
        }
        return pendientes;
    }

    public String contenidoListaReglas() { // un getlistareglas implica un recorrido mas y un costo O(2n)
        StringBuilder texto = new StringBuilder();
        texto.append("Lista de reglas reducidas (").append(reducciones.size()).append("):\n");
        for(Reduccion reduccion : reducciones) {
            texto.append(reduccion.getRegla().name())
                 .append(" - ").append(reduccion.getRegla().getDescripcion()).append("\n");
        }
        return texto.toString();
    }
}
