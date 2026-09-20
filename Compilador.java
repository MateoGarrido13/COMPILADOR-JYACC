import java.io.FileNotFoundException;

/**
 * Flujo del compilador.
 *
 * Arma el entorno compartido (tabla de simbolos, reporte, tabla de acciones) y
 * encadena las etapas. El Parser generado por BYACC/J desde gramatica.y conduce
 * el analisis sintactico, que pide tokens al Analisis Lexico y ejecuta las acciones semanticas.
 */
public class Compilador {

    private final TablaSimbolos tablaSimbolos = new TablaSimbolos();
    private final Reporte reporte = new Reporte();
    private final Entorno entorno;
    private final Verificaciones verificaciones;
    private final TablaAcciones tablaAcciones;
    private AnalizadorLexico lexico;

    public Compilador() {
        this.entorno = new Entorno(tablaSimbolos);
        this.verificaciones = new Verificaciones(entorno, reporte);
        this.tablaAcciones = new TablaAcciones(reporte);
        AccionesSemanticas.registrar(tablaAcciones, entorno, verificaciones);
    }

    public void compilar(String rutaFuente) {
        reporte.setEcoEnConsola(false);
        RegistroTokens.reiniciar();

        try {
            lexico = new AnalizadorLexico(tablaSimbolos, reporte, rutaFuente);
        } catch (FileNotFoundException e) {
            System.err.println("No se encontro el archivo fuente: " + rutaFuente);
            return;
        }

        // Etapas 1 y 2: el Parser generado a partir de gramatica.y conduce el analisis.
        // yyparse pide tokens con yylex (Analisis Lexico) y en cada reduccion ejecuta
        // tablaAcciones.ejecutar(Reglas.X, $1, $2, ...) (acciones semanticas).
        Parser parser = new Parser(lexico, tablaAcciones, reporte);
        parser.yyparse();
        lexico.cerrar();

        String tokens = RegistroTokens.contenido();
        String estructuras = reporte.contenidoEstructuras();
        String errores = reporte.contenidoErrores();
        String tabla = tablaSimbolos.contenido();

        System.out.print(tokens);
        System.out.println();
        System.out.print(estructuras);
        System.out.println();
        System.out.print(errores);
        System.out.println();
        System.out.print(tabla);

        Reporte.escribirArchivo(rutaFuente, "tokens.txt", tokens);
        Reporte.escribirArchivo(rutaFuente, "estructuras.txt", estructuras);
        Reporte.escribirArchivo(rutaFuente, "errores.txt", errores);
        Reporte.escribirArchivo(rutaFuente, "tabla_simbolos.txt", tabla);
    }

    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }

    public TablaAcciones getTablaAcciones() {
        return tablaAcciones;
    }

    public Entorno getEntorno() {
        return entorno;
    }

    public Verificaciones getVerificaciones() {
        return verificaciones;
    }

    public Reporte getReporte() {
        return reporte;
    }

    public AnalizadorLexico getLexico() {
        return lexico;
    }
}
