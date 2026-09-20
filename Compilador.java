import java.io.File;
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
        System.out.println("Compilando: " + rutaFuente);
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

        // Etapa 3: salidas del compilador.
        Reporte.escribirArchivo(rutaFuente, "tokens.txt", RegistroTokens.contenido());
        Reporte.escribirArchivo(rutaFuente, "estructuras.txt", reporte.contenidoEstructuras());
        Reporte.escribirArchivo(rutaFuente, "errores.txt", reporte.contenidoErrores());
        Reporte.escribirArchivo(rutaFuente, "tabla_simbolos.txt", tablaSimbolos.contenido());
        Reporte.escribirArchivo(rutaFuente, "declaraciones.txt", entorno.contenidoDeclaraciones());
        Reporte.escribirArchivo(rutaFuente, "lista_reglas.txt", tablaAcciones.contenidoListaReglas());

        System.out.println("Tokens detectados: " + RegistroTokens.getTokens().size());
        System.out.println("Estructuras detectadas: " + reporte.getEstructuras().size());
        System.out.println("Reglas reducidas: " + tablaAcciones.getListaReglas().size());
        System.out.println("Errores: " + reporte.cantidadErrores()
                + " - Warnings: " + reporte.cantidadWarnings());
        System.out.println("Salidas escritas en: "
                + new File(rutaFuente).getAbsoluteFile().getParent());
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
