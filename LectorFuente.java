import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/*
  Fuente de caracteres del programa.
 
  Lee el archivo de a una linea completa y entrega sus caracteres de a uno,
  porque el Analizador Lexico necesita ese detalle para reconocer los tokens.
 
  Al quedar la linea en memoria, el numero de linea se conoce de antemano y no
  hay que corregirlo cada vez que el lexico retrocede un caracter.
 */
public class LectorFuente {

    // El lexico nunca retrocede mas de tres caracteres (sufijo "$ul").
    private static final int MAXIMO_RETROCESO = 16;

    private BufferedReader lector;

    private String linea;
    private int posicion;
    private int numeroDeLinea;
    private boolean finDeArchivo;

    // Caracteres devueltos por el lexico, junto con la linea de la que salieron.
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

    // Proximo caracter del programa, o 0 si se llego al fin de archivo.
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

    //Devuelve un caracter que el lexico leyo y no consumio.
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
            // el cierre no altera el analisis
        }
        lector = null;
    }

    // Carga lineas hasta encontrar una con caracteres pendientes. 
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
            // readLine descarta el terminador, pero el lexico lo necesita para
            // cortar las cadenas de una linea (tema 9), asi que se restituye.
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
