import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Estado semantico que se va armando mientras el parser reduce.
 *
 * Guarda declaraciones y el contexto abierto (funcion/clase). No emite errores:
 * eso queda en Verificaciones. El parser es ascendente, por eso la funcion y la
 * clase se abren con acciones intermedias y no al reducir la declaracion completa.
 */
public class Entorno {

    public static class Parametro {
        public final String nombre;
        public final String tipo;

        public Parametro(String nombre, String tipo) {
            this.nombre = nombre;
            this.tipo = tipo;
        }
    }

    public static class FuncionDeclarada {
        public final String nombre;
        public final String tipoRetorno;
        public final List<Parametro> parametros = new ArrayList<>();
        public final int linea;

        public FuncionDeclarada(String nombre, String tipoRetorno, int linea) {
            this.nombre = nombre;
            this.tipoRetorno = tipoRetorno;
            this.linea = linea;
        }
    }

    public static class ClaseDeclarada {
        public final String nombre;
        public final int linea;
        public final Map<String, String> atributos = new LinkedHashMap<>();
        public final Map<String, FuncionDeclarada> metodos = new LinkedHashMap<>();
        public final List<String> heredaDe = new ArrayList<>();

        public ClaseDeclarada(String nombre, int linea) {
            this.nombre = nombre;
            this.linea = linea;
        }
    }

    public static class TipoEnumerado {
        public final String nombre;
        public final String tipoBase;
        public final List<String> valores = new ArrayList<>();
        public final int linea;

        public TipoEnumerado(String nombre, String tipoBase, int linea) {
            this.nombre = nombre;
            this.tipoBase = tipoBase;
            this.linea = linea;
        }
    }

    private final TablaSimbolos tablaSimbolos;

    private final Map<String, String> variables = new LinkedHashMap<>();
    private final Map<String, FuncionDeclarada> funciones = new LinkedHashMap<>();
    private final Map<String, ClaseDeclarada> clases = new LinkedHashMap<>();
    private final Map<String, TipoEnumerado> enumerados = new LinkedHashMap<>();

    private ClaseDeclarada claseActual = null;
    private FuncionDeclarada funcionActual = null;

    public Entorno(TablaSimbolos tablaSimbolos) {
        this.tablaSimbolos = tablaSimbolos;
    }

    public FuncionDeclarada getFuncionActual() {
        return funcionActual;
    }

    public ClaseDeclarada getClaseActual() {
        return claseActual;
    }

    public void declararVariable(EntradaTabla id, String tipo, int linea) {
        if (id == null) {
            return;
        }
        if (claseActual != null) {
            declararAtributo(id, tipo, linea);
            return;
        }
        variables.put(id.lexema, tipo);
        id.tipoDato = tipo;
    }

    public String tipoDeVariable(String nombre) {
        return variables.get(nombre);
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
        String nombre = id == null ? null : id.lexema;
        FuncionDeclarada funcion = new FuncionDeclarada(nombre, tipoRetorno, linea);
        funcionActual = funcion;
        if (id != null) {
            id.tipoDato = tipoRetorno;
        }
        return funcion;
    }

    public void agregarParametroFormal(EntradaTabla id, String tipo) {
        if (funcionActual == null) {
            return;
        }
        String nombre = id == null ? null : id.lexema;
        funcionActual.parametros.add(new Parametro(nombre, tipo));
        if (id != null) {
            id.tipoDato = tipo;
        }
    }

    /** Cierra el contexto y pasa la funcion a la tabla (o a la clase actual). */
    public void cerrarFuncion() {
        if (funcionActual == null) {
            return;
        }
        if (claseActual != null) {
            if (funcionActual.nombre != null) {
                claseActual.metodos.put(funcionActual.nombre, funcionActual);
            }
        } else if (funcionActual.nombre != null) {
            funciones.put(funcionActual.nombre, funcionActual);
        }
        funcionActual = null;
    }

    public ClaseDeclarada abrirClase(EntradaTabla id, int linea) {
        if (id == null) {
            return null;
        }
        ClaseDeclarada clase = new ClaseDeclarada(id.lexema, linea);
        clases.put(id.lexema, clase);
        claseActual = clase;
        return clase;
    }

    public void cerrarClase() {
        claseActual = null;
    }

    public void declararAtributo(EntradaTabla id, String tipo, int linea) {
        if (id == null || claseActual == null) {
            return;
        }
        claseActual.atributos.put(id.lexema, tipo);
        id.tipoDato = tipo;
    }

    public void registrarHerencia(List<Object> clasesPadre) {
        if (claseActual == null || clasesPadre == null) {
            return;
        }
        for (Object elemento : clasesPadre) {
            EntradaTabla padre = ContextoRegla.comoEntrada(elemento);
            if (padre == null) {
                continue;
            }
            if (!claseActual.heredaDe.contains(padre.lexema)) {
                claseActual.heredaDe.add(padre.lexema);
            }
        }
    }

    public void declararObjetos(List<Object> identificadores, String nombreClase, int linea) {
        if (identificadores == null) {
            return;
        }
        for (Object elemento : identificadores) {
            declararVariable(ContextoRegla.comoEntrada(elemento), nombreClase, linea);
        }
    }

    public void registrarTipoEnumerado(EntradaTabla id, List<Object> valores, int linea) {
        if (id == null) {
            return;
        }
        if (valores == null || valores.isEmpty()) {
            enumerados.put(id.lexema, new TipoEnumerado(id.lexema, null, linea));
            id.tipoDato = "tipo enumerado";
            return;
        }

        String tipoBase = null;
        TipoEnumerado enumerado = new TipoEnumerado(id.lexema, null, linea);
        for (Object elemento : valores) {
            EntradaTabla valor = ContextoRegla.comoEntrada(elemento);
            if (valor == null) {
                continue;
            }
            tiparConstante(valor);
            if (tipoBase == null) {
                tipoBase = Tipos.tipoDeConstante(valor.lexema);
            }
            enumerado.valores.add(valor.lexema);
        }

        TipoEnumerado definitivo = new TipoEnumerado(id.lexema, tipoBase, linea);
        definitivo.valores.addAll(enumerado.valores);
        enumerados.put(id.lexema, definitivo);
        id.tipoDato = "tipo enumerado";
    }

    public boolean esTipoConocido(String nombre) {
        if (nombre == null) {
            return false;
        }
        return Tipos.esTipoBasico(nombre)
                || clases.containsKey(nombre)
                || enumerados.containsKey(nombre);
    }

    public boolean esClase(String nombre) {
        return clases.containsKey(nombre);
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
