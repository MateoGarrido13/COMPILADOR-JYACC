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

  AJUSTE POST-6.0 (pedido del usuario): stdout vuelve al formato TP3
  (fuente numerado, tokens, estructuras, errores, tabla de simbolos y tercetos).
  El usuario quitara esas impresiones manualmente antes de entregar. Por eso
  NO se imprime aqui ningun mensaje nuevo de estado del .asm:
  solo se conserva la compuerta que evita escribir un .asm cuando Reporte
  tiene errores, y la ruta derivada robusta de rutaAsmPara().

  Contrato de compilar(String) (precondiciones y efectos):
    - Precondicion: rutaFuente apunta a un archivo legible. Si no existe, se
      informa por System.err y el metodo termina sin lanzar: no se escriben
      .asm ni se imprime el listado TP3.
    - Estado compartido: RegistroTokens y ControlAmbito son estaticos, por eso
      se reinician al inicio de cada compilacion. Sin ese reinicio, una segunda
      compilacion en la misma JVM arrastraria tokens y ambitos de la anterior
      (las suites lo detectan ejecutando cada fuente en su propio proceso, pero
      la API publica debe ser reentrable).
    - Orden de etapas: el lexico se abre antes del parser; el parser dispara
      las acciones semanticas que llenan la TS y el Entorno; recien despues se
      recorren las reducciones para construir los tercetos. Ese orden es la
      precondicion de GeneradorAssembler: cuando colecta simbolos para .DATA,
      la TS y las firmas ya estan completas.
    - Efecto colateral controlado: el unico archivo que se crea es el .asm, y
      solo si Reporte.hayErrores() == false. Las excepciones de escritura van
      a stderr para no alterar el contrato de consola TP3.
    - Los getters exponen el estado resultante para pruebas y para el
      generador; no inician etapas ni imprimen nada.
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

    /*
      Ejecuta el pipeline completo para un fuente. No devuelve valor: el
      resultado observable es el .asm (si no hay errores) y el stdout TP3 del
      bloque marcado mas abajo. El lexico se cierra en esta misma llamada, incluso
      si el parser encontro errores: no se delega su cierre al llamador.
     */
    public void compilar(String rutaFuente) {
        reporte.setEcoEnConsola(false);
        RegistroTokens.reiniciar();
        ControlAmbito.reiniciar(tablaSimbolos);

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

        // 6.0: escribir el .asm solo si no hay errores. Sin mensaje en stdout;
        // el contrato de consola pedido sigue siendo el de TP3.
        generarArchivoAsm(generador, rutaAsmPara(rutaFuente));

        // --- Eliminar estas lineas antes de entregar, para que la salida sea solo el .asm. ---

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

        // -------------------------------------------------------------------------------------

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

    /*
      Escribe el .asm solo si la compilacion no tiene errores. Los errores de
      escritura van a stderr, como en el Codigo original, para no ensuciar el
      stdout TP3 que el usuario sacara antes de entregar.
     */
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

    /*
      Mismo directorio y nombre base que el fuente, extension .asm. Se busca
      el ultimo punto DESPUES del ultimo separador para no confundir un punto
      en el nombre de una carpeta con la extension del archivo.
     */
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
