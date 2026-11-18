/*
  Punto de entrada del compilador.

  Uso: java Main [rutaFuente]. Sin argumento se usa input.txt. Toda la
  politica de impresion por consola vive en Compilador.compilar(); Main solo
  resuelve la ruta y delega.

  AJUSTE POST-6.0 (pedido del usuario): se restauro el stdout TP3 para que el
  usuario lo elimine manualmente antes de entregar. Main sigue terminando en 0.
 */
public class Main {

    private static final String FUENTE_POR_DEFECTO = "input.txt";

    public static void main(String[] args) {
        String rutaFuente = args.length > 0 ? args[0] : FUENTE_POR_DEFECTO;

        Compilador compilador = new Compilador();
        compilador.compilar(rutaFuente);
    }
}
