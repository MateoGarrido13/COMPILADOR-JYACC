
import java.util.ArrayList;
import java.util.List;

/*
  Salidas del compilador. estructuras sintacticas detectadas, errores y warnings,
  siempre con el numero de linea del codigo fuente.
 */
public class Reporte {

    public static class Mensaje {
        public final int linea;
        public final String texto;

        public Mensaje(int linea, String texto) {
            this.linea = linea;
            this.texto = texto;
        }

        public String formateado(String clase) {
            return "Línea " + linea + ": " + clase + ": " + texto;
        }
    }

    private final List<Mensaje> errores = new ArrayList<>();
    private final List<Mensaje> warnings = new ArrayList<>();
    private final List<Mensaje> estructuras = new ArrayList<>();
    private boolean ecoEnConsola = false;

    public void setEcoEnConsola(boolean ecoEnConsola) {
        this.ecoEnConsola = ecoEnConsola;
    }

    public void error(int linea, String descripcion) {
        Mensaje mensaje = new Mensaje(linea, descripcion);
        errores.add(mensaje);
        if (ecoEnConsola) {
            System.out.println(mensaje.formateado("Error"));
        }
    }

    public void warning(int linea, String descripcion) {
        Mensaje mensaje = new Mensaje(linea, descripcion);
        warnings.add(mensaje);
        if (ecoEnConsola) {
            System.out.println(mensaje.formateado("Warning"));
        }
    }

    public void estructura(int linea, String descripcion) {
        estructuras.add(new Mensaje(linea, descripcion));
    }

    public boolean hayErrores() {
        return !errores.isEmpty();
    }

    public int cantidadErrores() {
        return errores.size();
    }

    public int cantidadWarnings() {
        return warnings.size();
    }

    public List<Mensaje> getErrores() {
        return errores;
    }

    public List<Mensaje> getWarnings() {
        return warnings;
    }

    public List<Mensaje> getEstructuras() {
        return estructuras;
    }

    public String contenidoEstructuras() {
        StringBuilder texto = new StringBuilder();
        for (Mensaje mensaje : estructuras) {
            texto.append("Línea ").append(mensaje.linea).append(": ").append(mensaje.texto).append("\n");
        }
        return texto.toString();
    }

    public String contenidoErrores() {
        StringBuilder texto = new StringBuilder();
        for (Mensaje mensaje : errores) {
            texto.append(mensaje.formateado("Error")).append("\n");
        }
        for (Mensaje mensaje : warnings) {
            texto.append(mensaje.formateado("Warning")).append("\n");
        }
        return texto.toString();
    }
}
