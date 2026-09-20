public class Main {

    private static final String FUENTE_POR_DEFECTO = "input.txt";

    public static void main(String[] args) {
        // El TP pide que la ruta del codigo a compilar se pase como parametro.
        String rutaFuente = args.length > 0 ? args[0] : FUENTE_POR_DEFECTO;

        Compilador compilador = new Compilador();
        compilador.compilar(rutaFuente);
    }
}
