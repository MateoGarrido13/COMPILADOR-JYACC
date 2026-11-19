/*
  Punto de entrada del compilador.

  Uso: java Main [rutaFuente]. Sin argumento se usa input.txt. Toda la
  politica de impresion por consola vive en Compilador.compilar(); Main solo
  resuelve la ruta y delega.

  AJUSTE POST-6.0 (pedido del usuario): se restauro el stdout TP3 para que el
  usuario lo elimine manualmente antes de entregar. Main sigue terminando en 0.

  Contrato: con argumento usa args[0] como ruta; sin argumento usa input.txt
  del directorio de trabajo actual. No valida la ruta ni interpreta el resultado
  de compilar(): Compilador decide si escribe el .asm y que se imprime.
  Efectos: delega todo el estado a una instancia nueva de Compilador. No hay
  estado estatico en Main, por lo que cada invocacion del proceso es independiente.
  Codigo de salida: siempre 0, incluso cuando el fuente tiene errores; los
  errores se reportan en el stdout TP3 y, si los hay, impiden el .asm. Los
  scripts de prueba comprueban texto, no el exit status.
 */
public class Main {

    private static final String FUENTE_POR_DEFECTO = "input.txt";

    public static void main(String[] args) {
        String rutaFuente = args.length > 0 ? args[0] : FUENTE_POR_DEFECTO;

        Compilador compilador = new Compilador();
        compilador.compilar(rutaFuente);
    }
}
