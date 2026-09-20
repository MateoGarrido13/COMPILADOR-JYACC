import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Corre cada caso de pruebas_pdf en un directorio aislado y resume
 * si el error esperado del PDF aparece en la salida.
 */
public class PruebaErroresPdf {

    private static final String[][] CASOS = {
        {"01_falta_nombre_programa.txt", "Falta de nombre de programa"},
        {"02_falta_begin.txt", "BEGIN"},
        {"03_falta_end.txt", "END"},
        {"04_falta_punto_y_coma.txt", "Falta ';'"},
        {"05_falta_nombre_funcion.txt", "Falta de nombre en funcion"},
        {"06_falta_coma.txt", "Falta de ','"},
        {"07_falta_nombre_parametro.txt", "Falta de nombre de parametro formal"},
        {"08_falta_tipo_parametro.txt", "Falta de tipo de parametro formal"},
        {"09_falta_operando.txt", "Falta de operando"},
        {"10_falta_operador.txt", "Falta de operador"},
        {"11_falta_arg_pout.txt", "Falta argumento en sentencia pout"},
        {"12_falta_paren_apertura.txt", "parentesis de apertura"},
        {"13_falta_paren_cierre.txt", "parentesis de cierre"},
        {"14_falta_cuerpo_iter.txt", "Falta de cuerpo en iteracion"},
        {"15_falta_end_if.txt", "Falta de end_if"},
        {"16_falta_while.txt", "Falta while"},
        {"17_asig_donde_igual.txt", "donde debe usarse '='"},
        {"23_asig_anidada.txt", "Anidamiento de asignacion"},
        {"18_falta_orden.txt", "orden de evaluacion"},
        {"21_enum_vacio.txt", "Ausencia de valores para la enumeracion"},
        {"22_extends_vacio.txt", "despues de extends"},
    };

    public static void main(String[] args) throws Exception {
        Path origen = Path.of("pruebas_pdf");
        Path trabajo = Path.of("pruebas", "salida_pdf");
        Files.createDirectories(trabajo);

        System.out.println("CASO | ESPERADO | DETECTADO | ERRORES | WARNINGS | TOKENS | ESTRUCTURAS");
        System.out.println("-----+----------+-----------+---------+----------+--------+------------");

        Map<String, String> detalle = new LinkedHashMap<>();
        int detectados = 0;
        for (String[] caso : CASOS) {
            Path fuenteOrig = origen.resolve(caso[0]);
            Path dirCaso = trabajo.resolve(caso[0].replace(".txt", ""));
            Files.createDirectories(dirCaso);
            Path fuente = dirCaso.resolve(caso[0]);
            Files.copy(fuenteOrig, fuente, StandardCopyOption.REPLACE_EXISTING);

            Compilador compilador = new Compilador();
            compilador.getReporte().setEcoEnConsola(false);
            compilador.compilar(fuente.toString());

            String errores = compilador.getReporte().contenidoErrores();
            Files.writeString(dirCaso.resolve("resumen.txt"), errores);

            boolean ok = errores.toLowerCase().contains(caso[1].toLowerCase());
            if (ok) {
                detectados++;
            }
            detalle.put(caso[0], errores.replace('\n', ' '));

            System.out.printf("%s | %s | %s | %d | %d | %d | %d%n",
                    caso[0],
                    caso[1],
                    ok ? "SI" : "NO",
                    compilador.getReporte().cantidadErrores(),
                    compilador.getReporte().cantidadWarnings(),
                    RegistroTokens.getTokens().size(),
                    compilador.getReporte().getEstructuras().size());
        }

        System.out.println();
        System.out.println("Detectados " + detectados + "/" + CASOS.length);
        System.out.println();
        System.out.println("=== DETALLE DE ERRORES ===");
        for (Map.Entry<String, String> entrada : detalle.entrySet()) {
            System.out.println("--- " + entrada.getKey() + " ---");
            System.out.println(entrada.getValue());
        }

        Path validoOrig = origen.resolve("00_programa_valido.txt");
        if (Files.exists(validoOrig)) {
            Path dirValido = trabajo.resolve("00_programa_valido");
            Files.createDirectories(dirValido);
            Path fuente = dirValido.resolve("00_programa_valido.txt");
            Files.copy(validoOrig, fuente, StandardCopyOption.REPLACE_EXISTING);
            Compilador compilador = new Compilador();
            compilador.getReporte().setEcoEnConsola(false);
            compilador.compilar(fuente.toString());
            System.out.println();
            System.out.println("=== PROGRAMA VALIDO ===");
            System.out.println("Errores: " + compilador.getReporte().cantidadErrores()
                    + " Warnings: " + compilador.getReporte().cantidadWarnings()
                    + " Tokens: " + RegistroTokens.getTokens().size()
                    + " Estructuras: " + compilador.getReporte().getEstructuras().size());
            System.out.println(compilador.getReporte().contenidoErrores());
            System.out.println(RegistroTokens.contenido());
            System.out.println(compilador.getReporte().contenidoEstructuras());
        }
    }
}
