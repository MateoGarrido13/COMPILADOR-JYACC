import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LectorFuente {

    private static final int MAXIMO_RETROCESO = 16;

    private BufferedReader lector;

    private String linea;
    private int posicion;
    private int numeroDeLinea;
    private boolean finDeArchivo;

    private final char[] retrocedidos = new char[MAXIMO_RETROCESO];
    private final int[] lineasRetrocedidas = new int[MAXIMO_RETROCESO];
    private int tope = -1;

    public void abrir(String rutaArchivo) throws FileNotFoundException {
        cerrar();
        lector = new BufferedReader(new FileReader(rutaArchivo));
        linea = null;
        posicion = 0;
        numeroDeLinea = 0;
        finDeArchivo = false;
        tope = -1;
        Globals.numeroLinea = 1;
    }

    public char leer() {
        if (tope >= 0) {
            Globals.numeroLinea = lineasRetrocedidas[tope];
            return retrocedidos[tope--];
        }
        if (!quedaCaracterEnLinea()) {
            return 0;
        }
        Globals.numeroLinea = numeroDeLinea;
        return linea.charAt(posicion++);
    }

    public void retroceder(char c) {
        if (c == 0 || tope + 1 >= MAXIMO_RETROCESO) {
            return;
        }
        tope++;
        retrocedidos[tope] = c;
        lineasRetrocedidas[tope] = Globals.numeroLinea;
    }

    public int linea() {
        return Globals.numeroLinea;
    }

    public void cerrar() {
        if (lector == null) {
            return;
        }
        try {
            lector.close();
        } catch (IOException e) {
        }
        lector = null;
    }

    private boolean quedaCaracterEnLinea() {
        while (linea == null || posicion >= linea.length()) {
            if (!cargarSiguienteLinea()) {
                return false;
            }
        }
        return true;
    }

    private boolean cargarSiguienteLinea() {
        if (finDeArchivo || lector == null) {
            return false;
        }
        try {
            String leida = lector.readLine();
            if (leida == null) {
                finDeArchivo = true;
                linea = null;
                return false;
            }
            linea = leida + '\n';
            posicion = 0;
            numeroDeLinea++;
            return true;
        } catch (IOException e) {
            finDeArchivo = true;
            linea = null;
            return false;
        }
    }
}
