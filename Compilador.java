import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/*
  Flujo del compilador.
 
  Arma el entorno compartido (tabla de simbolos, reporte, tabla de acciones) y
  encadena las etapas. El Parser generado por BYACC/J desde gramatica.y conduce
  el analisis sintactico, que pide tokens al Analisis Lexico y ejecuta las acciones semanticas.
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

        String fuente = contenidoFuenteNumerado(rutaFuente);

        Parser parser = new Parser(lexico, tablaAcciones, reporte);
        parser.yyparse();
        lexico.cerrar();

        GeneradorTercetos generador = new GeneradorTercetos(reporte);
        generador.generar(tablaAcciones.getReducciones());

        String tokens = RegistroTokens.contenido();
        String estructuras = reporte.contenidoEstructuras();
        String intermedio = generador.contenido();
        String errores = reporte.contenidoErrores();
        String tabla = tablaSimbolos.contenido();

        System.out.print(fuente);
        System.out.println();
        System.out.print(tokens);
        System.out.println();
        System.out.print(estructuras);
        System.out.println();
        System.out.print(intermedio);
        System.out.println();
        System.out.print(errores);
        System.out.println();
        System.out.print(tabla);

    }

    /*
      Relee el archivo fuente completo y lo devuelve con cada linea numerada
      ("N: <linea>"), para abrir la salida por consola. Se relee en lugar de
      reutilizar LectorFuente porque este solo conserva la linea actual y la
      carga a demanda del lexico: asi el listado es completo aunque el parser
      no haya consumido todo el programa.
     */
    private String contenidoFuenteNumerado(String rutaFuente) {
        StringBuilder texto = new StringBuilder("Codigo fuente:\n");
        try (BufferedReader lector = Files.newBufferedReader(Paths.get(rutaFuente), StandardCharsets.UTF_8)) {
            String linea;
            int numero = 1;
            while ((linea = lector.readLine()) != null) {
                texto.append(numero).append(": ").append(linea).append("\n");
                numero++;
            }
        } catch (IOException e) {
            // El AnalizadorLexico ya abrio el archivo: si la relectura falla,
            // se devuelve lo leido hasta el momento sin interrumpir la salida.
        }
        return texto.toString();
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
