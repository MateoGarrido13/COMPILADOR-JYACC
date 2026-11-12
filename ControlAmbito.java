import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/*
  Mantiene Globals.AMBITO_ACTUAL segun los tokens, sin esperar la reduccion.
  El encabezado de la funcion sigue en el ambito de afuera: el nombre se apila
  despues del ')' de los parametros. El end que cierra el begin de esa funcion
  restaura el ambito anterior. Una clase usa el mismo begin/end y no abre ambito.
 */
public class ControlAmbito {

    private static final int FUNCION = 1;
    private static final int CLASE = 2;
    private static final int BEGIN = 3;

    private static final int NADA = 0;
    private static final int ESPERA_NOMBRE = 1;
    private static final int ESPERA_PARENTESIS = 2;
    private static final int EN_PARAMETROS = 3;

    private static final Deque<Integer> marcos = new ArrayDeque<>();
    private static final Deque<String> pila = new ArrayDeque<>();
    // Nombres declarados en cada ambito. La clave es el string de AMBITO_ACTUAL.
    private static final Map<String, Set<String>> declarados = new LinkedHashMap<>();

    private static int lectura = NADA;
    private static int parentesis = 0;
    private static String nombre = null;
    // El parser reduce el parametro antes de leer el ')'. Se reclaman al entrar.
    private static final List<EntradaTabla> parametrosPendientes = new ArrayList<>();

    public static void reiniciar(TablaSimbolos simbolos) {
        marcos.clear();
        pila.clear();
        declarados.clear();
        lectura = NADA;
        parentesis = 0;
        nombre = null;
        parametrosPendientes.clear();
        Globals.AMBITO_ACTUAL = "MAIN";
        MonitorAmbito.usar(simbolos);
    }

    public static void consumir(int token) {
        if (token == Globals.PR_FUNCTION) {
            lectura = ESPERA_NOMBRE;
            return;
        }
        if (lectura == ESPERA_NOMBRE) {
            if (token == Globals.IDENTIFICADOR && Globals.yylval != null) {
                nombre = TablaSimbolos.nombreDe(Globals.yylval.lexema);
                lectura = ESPERA_PARENTESIS;
            } else {
                lectura = NADA;
            }
            return;
        }
        if (lectura == ESPERA_PARENTESIS) {
            if (token == '(') {
                lectura = EN_PARAMETROS;
                parentesis = 1;
            } else {
                lectura = NADA;
            }
            return;
        }
        if (lectura == EN_PARAMETROS) {
            if (token == '(') {
                parentesis++;
            } else if (token == ')') {
                parentesis--;
                if (parentesis == 0) {
                    entrar(nombre);
                    marcos.push(FUNCION);
                    lectura = NADA;
                }
            }
            return;
        }

        if (token == Globals.PR_CLASS) {
            marcos.push(CLASE);
            return;
        }
        if (token == Globals.PR_BEGIN) {
            marcos.push(BEGIN);
            return;
        }
        if (token == Globals.PR_END) {
            cerrarBegin();
        }
    }

    public static void anotarParametro(EntradaTabla id) {
        if (id == null || lectura != EN_PARAMETROS) {
            return;
        }
        parametrosPendientes.add(id);
    }

    public static void declarar(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return;
        }
        Set<String> nombres = declarados.get(Globals.AMBITO_ACTUAL);
        if (nombres == null) {
            nombres = new HashSet<>();
            declarados.put(Globals.AMBITO_ACTUAL, nombres); // para este ambito, es alcanzable 
        }
        nombres.add(nombre);
    }

    // Alcanzable si esta declarado en este ambito o en un ambito anidado
    public static boolean alcanzable(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return true;
        }
        String ambito = Globals.AMBITO_ACTUAL;
        while (true) {
            Set<String> nombres = declarados.get(ambito);
            if (nombres != null && nombres.contains(nombre)) {
                return true;
            }
            int corte = ambito.lastIndexOf(':');
            if (corte < 0) {
                return false;
            }
            ambito = ambito.substring(0, corte);
        }
    }

    private static void entrar(String nombreFuncion) {
        if (nombreFuncion == null || nombreFuncion.isEmpty()) {
            return;
        }
        String anterior = Globals.AMBITO_ACTUAL;
        pila.push(anterior);
        Globals.AMBITO_ACTUAL = anterior + ":" + nombreFuncion.toUpperCase(Locale.ROOT);
        reclamarParametros(anterior);
        MonitorAmbito.alEntrar(anterior, Globals.AMBITO_ACTUAL);
    }

    // El parametro se anoto en el ambito de afuera. Pasa al ambito de la funcion.
    private static void reclamarParametros(String ambitoAnterior) {
        Set<String> origen = declarados.get(ambitoAnterior);
        for (EntradaTabla id : parametrosPendientes) {
            String nombreParametro = TablaSimbolos.nombreDe(id.lexema);
            if (origen != null && nombreParametro != null) {
                origen.remove(nombreParametro);
            }
            declarar(nombreParametro);
        }
        MonitorAmbito.reclamar(parametrosPendientes, Globals.AMBITO_ACTUAL);
        parametrosPendientes.clear();
    }

    private static void salir() {
        if (pila.isEmpty()) {
            return;
        }
        String viejo = Globals.AMBITO_ACTUAL;
        MonitorAmbito.alSalir(viejo);
        Globals.AMBITO_ACTUAL = pila.pop();
    }

    // El end cierra el begin de arriba. Si ese begin era el cuerpo de una
    // funcion o de una clase, cierra tambien ese marco.
    private static void cerrarBegin() {
        if (marcos.isEmpty() || marcos.peek() != BEGIN) {
            return;
        }
        marcos.pop();
        if (marcos.isEmpty()) {
            return;
        }
        int tope = marcos.peek();
        if (tope != FUNCION && tope != CLASE) {
            return;
        }
        marcos.pop();
        if (tope == FUNCION) {
            salir();
        }
    }
}
