import java.util.ArrayDeque;
import java.util.Deque;

/*
  Mantiene Globals.AMBITO_ACTUAL mientras el lexico entrega tokens.

  Abre el ambito de una funcion o de un metodo al leer el '(' que inicia sus
  parametros. Abre el ambito de una clase al leer el BEGIN de su cuerpo. Al
  cerrar el END correspondiente restaura el ambito anterior. Los bloques
  BEGIN/END de sentencias no abren ambito.

  No lleva registro de declaraciones ni decide visibilidad: eso lo resuelve la
  tabla de simbolos por la cadena de ambitos.
 */
public class ControlAmbito {

    private static final int FUNCION = 1;
    private static final int CLASE = 2;
    private static final int BLOQUE = 3;

    private static final int NADA = 0;
    private static final int ESPERA_NOMBRE_FUNCION = 1;
    private static final int ESPERA_PARENTESIS_FUNCION = 2;
    private static final int ESPERA_NOMBRE_CLASE = 3;
    private static final int ESPERA_BEGIN_CLASE = 4;

    private static final int MIEMBRO_INICIO = 0;
    private static final int MIEMBRO_TIPO = 1;
    private static final int MIEMBRO_NOMBRE = 2;
    private static final int MIEMBRO_LISTA = 3;

    /* Declaracion que abrio el ambito actual, para que Entorno sepa donde
       declarar el nombre aunque el parser reduzca con el '(' ya leido. */
    private static final class Declaracion {
        final String nombre;
        final String ambitoPadre;
        final String ambitoPropio;

        Declaracion(String nombre, String ambitoPadre, String ambitoPropio) {
            this.nombre = nombre;
            this.ambitoPadre = ambitoPadre;
            this.ambitoPropio = ambitoPropio;
        }
    }

    private static final Deque<Integer> marcos = new ArrayDeque<>();
    private static final Deque<String> pilaAmbitos = new ArrayDeque<>();
    private static final Deque<Declaracion> pendientes = new ArrayDeque<>();
    // Cuerpos de funcion/metodo que todavia esperan su BEGIN.
    private static final Deque<Integer> cuerposPendientes = new ArrayDeque<>();

    private static int lectura = NADA;
    private static String nombre = null;
    private static String nombreClase = null;
    private static String nombreMiembro = null;
    private static int estadoMiembro = MIEMBRO_INICIO;
    private static boolean ignorarMiembros = false;

    public static void reiniciar(TablaSimbolos simbolos) {
        marcos.clear();
        pilaAmbitos.clear();
        pendientes.clear();
        cuerposPendientes.clear();
        lectura = NADA;
        nombre = null;
        nombreClase = null;
        nombreMiembro = null;
        estadoMiembro = MIEMBRO_INICIO;
        ignorarMiembros = false;
        Globals.AMBITO_ACTUAL = "main";
    }

    /*
      Ambito donde se declara el nombre que acaba de abrir el ambito actual.
      Si el parser redujo el encabezado antes de leer el '(', la reduccion vio
      el ambito padre y esta consulta devuelve ese mismo ambito.
     */
    public static String ambitoDeclaracionDe(String nombreBuscado, String ambitoActual) {
        if (nombreBuscado == null || ambitoActual == null || pendientes.isEmpty()) {
            return ambitoActual;
        }
        Declaracion declaracion = pendientes.peek();
        if (ambitoActual.equals(declaracion.ambitoPropio)
                && nombreBuscado.equals(declaracion.nombre)) {
            return declaracion.ambitoPadre;
        }
        return ambitoActual;
    }

    public static void consumir(int token) {
        if (procesarEncabezadoFuncion(token)) {
            return;
        }
        if (procesarEncabezadoClase(token)) {
            return;
        }

        if (token == Globals.PR_BEGIN) {
            abrirBegin();
            return;
        }
        if (token == Globals.PR_END) {
            cerrarEnd();
            return;
        }

        if (enCuerpoClase()) {
            procesarMiembro(token);
        }
    }

    private static boolean procesarEncabezadoFuncion(int token) {
        if (token == Globals.PR_FUNCTION) {
            lectura = ESPERA_NOMBRE_FUNCION;
            return true;
        }
        if (lectura == ESPERA_NOMBRE_FUNCION) {
            if (token == Globals.IDENTIFICADOR && Globals.yylval != null) {
                nombre = TablaSimbolos.nombreDe(Globals.yylval.lexema);
                lectura = ESPERA_PARENTESIS_FUNCION;
            } else {
                lectura = NADA;
            }
            return true;
        }
        if (lectura == ESPERA_PARENTESIS_FUNCION) {
            if (token == '(') {
                entrar(nombre);
                cuerposPendientes.push(FUNCION);
            }
            lectura = NADA;
            return true;
        }
        return false;
    }

    private static boolean procesarEncabezadoClase(int token) {
        if (token == Globals.PR_CLASS) {
            lectura = ESPERA_NOMBRE_CLASE;
            return true;
        }
        if (lectura == ESPERA_NOMBRE_CLASE) {
            if (token == Globals.IDENTIFICADOR && Globals.yylval != null) {
                nombreClase = TablaSimbolos.nombreDe(Globals.yylval.lexema);
                lectura = ESPERA_BEGIN_CLASE;
            } else {
                lectura = NADA;
            }
            return true;
        }
        if (lectura == ESPERA_BEGIN_CLASE) {
            if (token == Globals.PR_BEGIN) {
                entrar(nombreClase);
                marcos.push(CLASE);
                estadoMiembro = MIEMBRO_INICIO;
                ignorarMiembros = false;
                lectura = NADA;
                return true;
            }
            lectura = NADA;
            return false;
        }
        return false;
    }

    private static void abrirBegin() {
        if (!cuerposPendientes.isEmpty()) {
            marcos.push(cuerposPendientes.pop());
            ignorarMiembros = false;
            return;
        }
        marcos.push(BLOQUE);
    }

    private static void cerrarEnd() {
        if (marcos.isEmpty()) {
            return;
        }
        int marco = marcos.pop();
        if (marco == FUNCION) {
            salir();
        } else if (marco == CLASE) {
            salir();
            estadoMiembro = MIEMBRO_INICIO;
            ignorarMiembros = false;
        }
    }

    /*
      Dentro del cuerpo de una clase, tipo_declarado ID '(' abre el ambito del
      metodo. Los demas miembros se consumen hasta el ';' para no confundir
      atributos con metodos.
     */
    private static void procesarMiembro(int token) {
        if (ignorarMiembros) {
            return;
        }
        switch (estadoMiembro) {
            case MIEMBRO_INICIO:
                if (token == Globals.PR_EXTENDS) {
                    estadoMiembro = MIEMBRO_LISTA;
                } else if (token == Globals.IDENTIFICADOR
                        || token == Globals.PR_ULONGINT
                        || token == Globals.PR_DOUBLEF) {
                    estadoMiembro = MIEMBRO_TIPO;
                } else if (token == ';') {
                    estadoMiembro = MIEMBRO_INICIO;
                }
                break;
            case MIEMBRO_TIPO:
                if (token == Globals.IDENTIFICADOR && Globals.yylval != null) {
                    nombreMiembro = TablaSimbolos.nombreDe(Globals.yylval.lexema);
                    estadoMiembro = MIEMBRO_NOMBRE;
                } else {
                    estadoMiembro = MIEMBRO_INICIO;
                }
                break;
            case MIEMBRO_NOMBRE:
                if (token == '(') {
                    entrar(nombreMiembro);
                    cuerposPendientes.push(FUNCION);
                    ignorarMiembros = true;
                    estadoMiembro = MIEMBRO_INICIO;
                } else if (token == ',' || token == Globals.IDENTIFICADOR) {
                    estadoMiembro = MIEMBRO_LISTA;
                } else if (token == ';') {
                    estadoMiembro = MIEMBRO_INICIO;
                } else {
                    estadoMiembro = MIEMBRO_INICIO;
                }
                break;
            case MIEMBRO_LISTA:
                if (token == ';') {
                    estadoMiembro = MIEMBRO_INICIO;
                }
                break;
            default:
                estadoMiembro = MIEMBRO_INICIO;
                break;
        }
    }

    private static boolean enCuerpoClase() {
        return !marcos.isEmpty() && marcos.peek() == CLASE;
    }

    private static void entrar(String nombreAmbito) {
        if (nombreAmbito == null || nombreAmbito.isEmpty()) {
            return;
        }
        String padre = Globals.AMBITO_ACTUAL;
        String propio = padre + ":" + nombreAmbito;
        pilaAmbitos.push(padre);
        pendientes.push(new Declaracion(nombreAmbito, padre, propio));
        Globals.AMBITO_ACTUAL = propio;
    }

    private static void salir() {
        if (pilaAmbitos.isEmpty()) {
            return;
        }
        String viejo = Globals.AMBITO_ACTUAL;
        if (!pendientes.isEmpty() && viejo.equals(pendientes.peek().ambitoPropio)) {
            pendientes.pop();
        }
        Globals.AMBITO_ACTUAL = pilaAmbitos.pop();
    }
}
