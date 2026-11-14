import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
  Estado semantico que se va armando mientras el parser reduce.

  Guarda declaraciones y el contexto abierto (funcion/metodo/clase). No emite
  errores, eso queda en Verificaciones. El parser es ascendente, por eso la
  funcion y la clase se abren con acciones intermedias y no al reducir la
  declaracion completa.

  Todas las claves de los mapas incluyen el ambito, para que dos declaraciones
  con el mismo nombre en funciones distintas no se pisen.
 */
public class Entorno {

    public static class Parametro {
        public final String nombre;
        public final String tipo;
        public final EntradaTabla entrada;

        public Parametro(String nombre, String tipo, EntradaTabla entrada) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.entrada = entrada;
        }
    }

    public static class FuncionDeclarada {
        public final String nombre;
        public final String tipoRetorno;
        public final List<Parametro> parametros = new ArrayList<>();
        public final int linea;
        public final EntradaTabla entrada;
        public final boolean esMetodo;
        public final String ambitoDeclaracion;
        public final String ambitoPropio;

        public FuncionDeclarada(String nombre, String tipoRetorno, int linea,
                EntradaTabla entrada, boolean esMetodo, String ambitoDeclaracion) {
            this.nombre = nombre;
            this.tipoRetorno = tipoRetorno;
            this.linea = linea;
            this.entrada = entrada;
            this.esMetodo = esMetodo;
            this.ambitoDeclaracion = ambitoDeclaracion;
            this.ambitoPropio = ambitoDeclaracion + ":" + nombre;
        }

        public String clave() {
            return nombre + ":" + ambitoDeclaracion;
        }
    }

    public static class ClaseDeclarada {
        public final String nombre;
        public final int linea;
        public final EntradaTabla entrada;
        public final String ambitoDeclaracion;
        public final String ambitoPropio;
        public final Map<String, String> atributos = new LinkedHashMap<>();
        public final Map<String, FuncionDeclarada> metodos = new LinkedHashMap<>();
        public final List<String> heredaDe = new ArrayList<>();

        public ClaseDeclarada(String nombre, int linea, EntradaTabla entrada,
                String ambitoDeclaracion) {
            this.nombre = nombre;
            this.linea = linea;
            this.entrada = entrada;
            this.ambitoDeclaracion = ambitoDeclaracion;
            this.ambitoPropio = ambitoDeclaracion + ":" + nombre;
        }

        public String clave() {
            return nombre + ":" + ambitoDeclaracion;
        }
    }

    public static class TipoEnumerado {
        public final String nombre;
        public final String tipoBase;
        public final List<String> valores = new ArrayList<>();
        public final int linea;
        public final EntradaTabla entrada;
        public final String ambito;

        public TipoEnumerado(String nombre, String tipoBase, int linea,
                EntradaTabla entrada, String ambito) {
            this.nombre = nombre;
            this.tipoBase = tipoBase;
            this.linea = linea;
            this.entrada = entrada;
            this.ambito = ambito;
        }

        public String clave() {
            return nombre + ":" + ambito;
        }
    }

    private final TablaSimbolos tablaSimbolos;

    private final Map<String, String> variables = new LinkedHashMap<>();
    private final Map<String, FuncionDeclarada> funciones = new LinkedHashMap<>();
    private final Map<String, ClaseDeclarada> clases = new LinkedHashMap<>();
    private final Map<String, TipoEnumerado> enumerados = new LinkedHashMap<>();

    private final Deque<FuncionDeclarada> pilaFunciones = new ArrayDeque<>();
    private final Deque<ClaseDeclarada> pilaClases = new ArrayDeque<>();

    public Entorno(TablaSimbolos tablaSimbolos) {
        this.tablaSimbolos = tablaSimbolos;
    }

    public FuncionDeclarada getFuncionActual() {
        return pilaFunciones.peek();
    }

    // Busca por clave completa nombre:ambito, no por nombre pelado.
    public FuncionDeclarada getFuncion(String clave) {
        if (clave == null) {
            return null;
        }
        return funciones.get(clave);
    }

    public EntradaTabla buscarIdentificador(String nombre, String ambito) {
        return tablaSimbolos.buscarIdentificador(nombre, ambito);
    }

    // Resuelve por la cadena de ambitos y solo acepta declaraciones (DEC-3).
    public EntradaTabla resolver(String nombre, String ambito) {
        return tablaSimbolos.resolver(nombre, ambito);
    }

    public ClaseDeclarada getClaseActual() {
        return pilaClases.peek();
    }

    public ClaseDeclarada getClase(String clave) {
        if (clave == null) {
            return null;
        }
        return clases.get(clave);
    }

    public void declararVariable(EntradaTabla id, String tipo, int linea) {
        if (id == null) {
            return;
        }
        if (pilaFunciones.isEmpty() && !pilaClases.isEmpty()) {
            declararAtributo(id, tipo, linea);
            return;
        }
        String ambito = Globals.AMBITO_ACTUAL;
        String nombre = TablaSimbolos.nombreDe(id.lexema);
        EntradaTabla entrada = tablaSimbolos.declarar(id, nombre, ambito, EntradaTabla.Uso.VARIABLE);
        if (entrada == null) {
            return;
        }
        entrada.tipoDato = tipo;
        variables.put(nombre + ":" + ambito, tipo);
    }

    public String tipoDeVariable(String clave) {
        return variables.get(clave);
    }

    public EntradaTabla tiparConstante(EntradaTabla constante) {
        if (constante == null) {
            return null;
        }
        String tipo = Tipos.tipoDeConstante(constante.lexema);
        if (tipo != null) {
            constante.tipoDato = tipo;
        }
        return constante;
    }

    public EntradaTabla aplicarSignoNegativo(EntradaTabla constante, int linea) {
        if (constante == null) {
            return null;
        }
        String tipo = Tipos.tipoDeConstante(constante.lexema);
        return tablaSimbolos.insertarConstante("-" + constante.lexema, tipo, linea);
    }

    public FuncionDeclarada abrirFuncion(EntradaTabla id, String tipoRetorno, int linea) {
        String nombre = id == null ? null : TablaSimbolos.nombreDe(id.lexema);
        String ambitoDeclaracion = ControlAmbito.ambitoDeclaracionDe(nombre, Globals.AMBITO_ACTUAL);
        boolean esMetodo = !pilaClases.isEmpty()
                && pilaClases.peek().ambitoPropio.equals(ambitoDeclaracion);

        EntradaTabla entrada = id;
        if (id != null) {
            EntradaTabla.Uso uso = esMetodo ? EntradaTabla.Uso.METODO : EntradaTabla.Uso.FUNCION;
            entrada = tablaSimbolos.declarar(id, nombre, ambitoDeclaracion, uso);
            if (entrada != null) {
                entrada.tipoDato = tipoRetorno;
            }
        }

        FuncionDeclarada funcion = new FuncionDeclarada(nombre, tipoRetorno, linea,
                entrada, esMetodo, ambitoDeclaracion);
        pilaFunciones.push(funcion);
        return funcion;
    }

    public void agregarParametroFormal(EntradaTabla id, String tipo) {
        FuncionDeclarada funcion = pilaFunciones.peek();
        if (funcion == null) {
            return;
        }
        String ambito = Globals.AMBITO_ACTUAL;
        String nombre = id == null ? null : TablaSimbolos.nombreDe(id.lexema);
        EntradaTabla entrada = id;
        if (id != null) {
            entrada = tablaSimbolos.declarar(id, nombre, ambito, EntradaTabla.Uso.PARAMETRO);
            if (entrada != null) {
                entrada.tipoDato = tipo;
            }
        }
        funcion.parametros.add(new Parametro(nombre, tipo, entrada));
    }

    // Cierra el contexto y pasa la funcion a la tabla o a la clase actual.
    public void cerrarFuncion() {
        FuncionDeclarada funcion = pilaFunciones.poll();
        if (funcion == null || funcion.nombre == null) {
            return;
        }
        if (funcion.esMetodo) {
            ClaseDeclarada clase = pilaClases.peek();
            if (clase != null) {
                clase.metodos.put(funcion.nombre, funcion);
            }
        } else {
            funciones.put(funcion.clave(), funcion);
        }
    }

    public ClaseDeclarada abrirClase(EntradaTabla id, int linea) {
        if (id == null) {
            return null;
        }
        String nombre = TablaSimbolos.nombreDe(id.lexema);
        String ambitoDeclaracion = ControlAmbito.ambitoDeclaracionDe(nombre, Globals.AMBITO_ACTUAL);
        EntradaTabla entrada = tablaSimbolos.declarar(id, nombre, ambitoDeclaracion,
                EntradaTabla.Uso.CLASE);
        if (entrada != null) {
            entrada.tipoDato = nombre;
        }
        ClaseDeclarada clase = new ClaseDeclarada(nombre, linea, entrada, ambitoDeclaracion);
        clases.put(clase.clave(), clase);
        pilaClases.push(clase);
        return clase;
    }

    public void cerrarClase() {
        pilaClases.poll();
    }

    public void declararAtributo(EntradaTabla id, String tipo, int linea) {
        ClaseDeclarada clase = pilaClases.peek();
        if (id == null || clase == null) {
            return;
        }
        String nombre = TablaSimbolos.nombreDe(id.lexema);
        EntradaTabla entrada = tablaSimbolos.declarar(id, nombre, clase.ambitoPropio,
                EntradaTabla.Uso.ATRIBUTO);
        if (entrada == null) {
            return;
        }
        entrada.tipoDato = tipo;
        clase.atributos.put(nombre, tipo);
    }

    public void registrarHerencia(List<Object> clasesPadre) {
        ClaseDeclarada clase = pilaClases.peek();
        if (clase == null || clasesPadre == null) {
            return;
        }
        for (Object elemento : clasesPadre) {
            EntradaTabla padre = ContextoRegla.comoEntrada(elemento);
            if (padre == null) {
                continue;
            }
            if (!clase.heredaDe.contains(padre.lexema)) {
                clase.heredaDe.add(padre.lexema);
            }
        }
    }

    public void declararObjetos(List<Object> identificadores, String nombreClase, int linea) {
        if (identificadores == null) {
            return;
        }
        String nombre = TablaSimbolos.nombreDe(nombreClase);
        EntradaTabla declaracion = resolver(nombre, Globals.AMBITO_ACTUAL);
        String tipo = nombreClase;
        if (declaracion != null && declaracion.uso == EntradaTabla.Uso.CLASE) {
            tipo = declaracion.lexema;
        }
        for (Object elemento : identificadores) {
            declararVariable(ContextoRegla.comoEntrada(elemento), tipo, linea);
        }
    }

    public void registrarTipoEnumerado(EntradaTabla id, List<Object> valores, int linea) {
        if (id == null) {
            return;
        }
        String ambito = Globals.AMBITO_ACTUAL;
        String nombre = TablaSimbolos.nombreDe(id.lexema);
        EntradaTabla entrada = tablaSimbolos.declarar(id, nombre, ambito, EntradaTabla.Uso.TIPO);
        if (entrada == null) {
            return;
        }
        entrada.tipoDato = "tipo enumerado";
        if (valores == null || valores.isEmpty()) {
            enumerados.put(nombre + ":" + ambito,
                    new TipoEnumerado(nombre, null, linea, entrada, ambito));
            return;
        }

        String tipoBase = null;
        List<String> valoresEnumerado = new ArrayList<>();
        for (Object elemento : valores) {
            EntradaTabla valor = ContextoRegla.comoEntrada(elemento);
            if (valor == null) {
                continue;
            }
            tiparConstante(valor);
            if (tipoBase == null) {
                tipoBase = Tipos.tipoDeConstante(valor.lexema);
            }
            valoresEnumerado.add(valor.lexema);
        }

        TipoEnumerado definitivo = new TipoEnumerado(nombre, tipoBase, linea, entrada, ambito);
        definitivo.valores.addAll(valoresEnumerado);
        enumerados.put(definitivo.clave(), definitivo);
    }

    public boolean esTipoConocido(String nombre) {
        if (nombre == null) {
            return false;
        }
        if (Tipos.esTipoBasico(nombre)) {
            return true;
        }
        EntradaTabla entrada = resolverTipo(nombre);
        return entrada != null
                && (entrada.uso == EntradaTabla.Uso.CLASE || entrada.uso == EntradaTabla.Uso.TIPO);
    }

    public boolean esClase(String nombre) {
        EntradaTabla entrada = resolverTipo(nombre);
        return entrada != null && entrada.uso == EntradaTabla.Uso.CLASE;
    }

    /*
      Tipo que participa en una operacion. Un enumerado vale por su tipo base;
      una clase conserva su clave completa para que dos clases distintas no se
      confundan. Tipos.tipoEfectivo queda como normalizacion para los basicos.
     */
    public String tipoEfectivo(String tipo) {
        if (tipo == null) {
            return Tipos.INDEFINIDO;
        }
        EntradaTabla entrada = resolverTipo(tipo);
        if (entrada != null) {
            if (entrada.uso == EntradaTabla.Uso.TIPO) {
                TipoEnumerado enumerado = enumerados.get(entrada.lexema);
                if (enumerado != null && enumerado.tipoBase != null) {
                    return enumerado.tipoBase;
                }
            }
            if (entrada.uso == EntradaTabla.Uso.CLASE) {
                return entrada.lexema;
            }
        }
        return Tipos.tipoEfectivo(tipo);
    }

    /*
      Devuelve el atributo declarado en la clase indicada por el prefijo o, si
      no hay prefijo, en la clase del objeto; en ambos casos busca tambien en las
      bases. No reporta errores: el motor de tipos solo necesita el tipo y la
      validacion completa de miembros es de los temas 28/31. Devuelve null si no
      se puede resolver.
     */
    public EntradaTabla resolverAtributo(EntradaTabla objeto, String nombreClase, String nombreAtributo) {
        if (nombreAtributo == null) {
            return null;
        }
        // Con prefijo explicito (objeto.Clase.atributo) manda la clase prefijada:
        // asi `a1.padre.y` busca en `padre` y no en la clase del objeto. Sin
        // prefijo se parte de la clase del objeto.
        ClaseDeclarada clase = nombreClase == null || nombreClase.isEmpty()
                ? claseDeObjeto(objeto)
                : clasePorNombre(nombreClase);
        if (clase == null) {
            return null;
        }
        return buscarAtributo(clase, TablaSimbolos.nombreDe(nombreAtributo), new HashSet<>());
    }

    /*
      Busca un metodo en la clase del objeto o en la clase prefijada, y en sus
      bases. La convencion de codigo intermedio para una invocacion de metodo es
      (CALLMET, objeto, metodo); esta busqueda solo resuelve la firma.
     */
    public FuncionDeclarada buscarMetodo(EntradaTabla objeto, String nombreClase, String nombreMetodo) {
        if (nombreMetodo == null) {
            return null;
        }
        ClaseDeclarada clase = nombreClase == null || nombreClase.isEmpty()
                ? claseDeObjeto(objeto)
                : clasePorNombre(nombreClase);
        if (clase == null) {
            return null;
        }
        return buscarMetodo(clase, TablaSimbolos.nombreDe(nombreMetodo), new HashSet<>());
    }

    private FuncionDeclarada buscarMetodo(ClaseDeclarada clase, String nombre, Set<String> visitadas) {
        if (clase == null || !visitadas.add(clase.clave())) {
            return null;
        }
        FuncionDeclarada propia = clase.metodos.get(nombre);
        if (propia != null) {
            return propia;
        }
        for (String base : clase.heredaDe) {
            FuncionDeclarada heredada = buscarMetodo(clasePorNombre(base), nombre, visitadas);
            if (heredada != null) {
                return heredada;
            }
        }
        return null;
    }

    private ClaseDeclarada claseDeObjeto(EntradaTabla objeto) {
        if (objeto == null) {
            return null;
        }
        ClaseDeclarada clase = getClase(objeto.lexema);
        if (clase != null) {
            return clase;
        }
        clase = getClase(objeto.tipoDato);
        if (clase != null) {
            return clase;
        }
        return clasePorNombre(objeto.tipoDato);
    }

    private ClaseDeclarada clasePorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        EntradaTabla entrada = resolverTipo(nombre);
        if (entrada != null && entrada.uso == EntradaTabla.Uso.CLASE) {
            ClaseDeclarada clase = getClase(entrada.lexema);
            if (clase != null) {
                return clase;
            }
        }
        String buscado = TablaSimbolos.nombreDe(nombre);
        for (ClaseDeclarada clase : clases.values()) {
            if (clase.nombre.equals(buscado)) {
                return clase;
            }
        }
        return null;
    }

    private EntradaTabla buscarAtributo(ClaseDeclarada clase, String nombre, Set<String> visitadas) {
        if (clase == null || !visitadas.add(clase.clave())) {
            return null;
        }
        EntradaTabla entrada = tablaSimbolos.buscarIdentificador(nombre, clase.ambitoPropio);
        if (entrada != null && entrada.declarado && entrada.uso == EntradaTabla.Uso.ATRIBUTO) {
            return entrada;
        }
        for (String base : clase.heredaDe) {
            EntradaTabla heredada = buscarAtributo(clasePorNombre(base), nombre, visitadas);
            if (heredada != null) {
                return heredada;
            }
        }
        return null;
    }

    // Acepta un nombre pelado o una clave ya resuelta nombre:ambito.
    private EntradaTabla resolverTipo(String nombre) {
        String base = TablaSimbolos.nombreDe(nombre);
        String ambito = nombre.indexOf(':') >= 0
                ? TablaSimbolos.ambitoDe(nombre)
                : Globals.AMBITO_ACTUAL;
        if (nombre.indexOf(':') >= 0) {
            return tablaSimbolos.buscarIdentificador(base, ambito);
        }
        return tablaSimbolos.resolver(base, ambito);
    }

    public String contenidoDeclaraciones() {
        StringBuilder texto = new StringBuilder();
        texto.append("Variables declaradas (").append(variables.size()).append("):\n");
        for (Map.Entry<String, String> entrada : variables.entrySet()) {
            texto.append("  ").append(entrada.getKey()).append(" : ").append(entrada.getValue()).append("\n");
        }
        texto.append("\nFunciones declaradas (").append(funciones.size()).append("):\n");
        for (FuncionDeclarada funcion : funciones.values()) {
            texto.append("  ").append(funcion.tipoRetorno).append(" ").append(funcion.nombre).append("(");
            for (int i = 0; i < funcion.parametros.size(); i++) {
                Parametro parametro = funcion.parametros.get(i);
                texto.append(i > 0 ? ", " : "").append(parametro.tipo).append(" ").append(parametro.nombre);
            }
            texto.append(") - linea ").append(funcion.linea).append("\n");
        }
        texto.append("\nClases declaradas (").append(clases.size()).append("):\n");
        for (ClaseDeclarada clase : clases.values()) {
            texto.append("  ").append(clase.nombre).append(" - linea ").append(clase.linea).append("\n");
            texto.append("    atributos: ").append(clase.atributos.keySet()).append("\n");
            texto.append("    metodos: ").append(clase.metodos.keySet()).append("\n");
            texto.append("    hereda de: ").append(clase.heredaDe).append("\n");
        }
        texto.append("\nTipos enumerados (").append(enumerados.size()).append("):\n");
        for (TipoEnumerado enumerado : enumerados.values()) {
            texto.append("  ").append(enumerado.nombre).append(" [").append(enumerado.tipoBase)
                 .append("] = ").append(enumerado.valores).append("\n");
        }
        return texto.toString();
    }
}
