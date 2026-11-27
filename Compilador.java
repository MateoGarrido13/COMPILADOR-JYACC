import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        ControlAmbito.reiniciar();

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

        generarArchivoAsm(generador, rutaAsmPara(rutaFuente));

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
        System.out.print(errores);
        System.out.println();
        System.out.print(tabla);
        System.out.println();
        System.out.print(intermedio);

    }

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

        }
        return texto.toString();
    }

    private void generarArchivoAsm(GeneradorTercetos generador, String rutaAsm) {
        if (reporte.hayErrores()) {
            return;
        }
        try {
            GeneradorAssembler ensamblador = new GeneradorAssembler(
                    generador.getTercetos(),
                    this.tablaSimbolos,
                    this.entorno,
                    generador.getResultados());
            ensamblador.escribirArchivo(rutaAsm);
        } catch (IOException e) {
            System.err.println("No se pudo generar el archivo ASM: " + e.getMessage());
        }
    }

    private String rutaAsmPara(String rutaFuente) {
        int separador = Math.max(rutaFuente.lastIndexOf('/'), rutaFuente.lastIndexOf('\\'));
        int punto = rutaFuente.lastIndexOf('.');
        String base = punto > separador ? rutaFuente.substring(0, punto) : rutaFuente;
        return base + ".asm";
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
