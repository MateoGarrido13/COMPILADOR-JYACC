import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Funciones de verificacion que ejecutan las acciones semanticas.
 *
 * Cubren lo que el lexico no debe resolver:
 -rangos de constantes (consideracion c) del TP2)
 -declaraciones duplicadas o ausentes
 -las condiciones estructurales de los temas particulares
 -los chequeos de compatibilidad de tipos (etapas 3 y 4)
 *
 * Nota sobre el orden de las verificaciones: el parser es ascendente, por lo que un cuerpo se reduce antes que su encabezado. 
   Por eso los contextos de clase y de funcion se abren con acciones intermedias (INICIO_CLASE, INICIO_FUNCION) y no al reducir la declaracion completa.
 */
public class Verificaciones {

    // ===== Estructuras auxiliares =====

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
    private final Reporte reporte;

    private final Map<String, String> variables = new LinkedHashMap<>();
    private final Map<String, FuncionDeclarada> funciones = new LinkedHashMap<>();
    private final Map<String, ClaseDeclarada> clases = new LinkedHashMap<>();
    private final Map<String, TipoEnumerado> enumerados = new LinkedHashMap<>();

    private ClaseDeclarada claseActual = null;
    private FuncionDeclarada funcionActual = null;
    private int retornosSinFuncion = 0;
    private int lineaUltimoRetorno = 0;

    public Verificaciones(TablaSimbolos tablaSimbolos, Reporte reporte) {
        this.tablaSimbolos = tablaSimbolos;
        this.reporte = reporte;
    }

    // ===================================================================
    // Identificadores (solo minusculas). El truncado a 22 lo hace el lexico.
    // ===================================================================

    public void verificarFormatoIdentificador(EntradaTabla id, int linea) {
        if (id == null) {
            return;
        }
        String lexema = id.lexema;
        if (!lexema.equals(lexema.toLowerCase())) {
            reporte.error(linea, "El identificador " + lexema
                    + " contiene mayusculas y solo se admiten minusculas");
        }
    }

    // ===================================================================
    // Variables
    // ===================================================================

    public void declararVariable(EntradaTabla id, String tipo, int linea) {
        if (id == null) {
            return;
        }
        verificarFormatoIdentificador(id, linea);

        if (claseActual != null) {
            declararAtributo(id, tipo, linea);
            return;
        }
        if (!esTipoConocido(tipo)) {
            reporte.error(linea, "Tipo de dato desconocido: " + tipo);
            return;
        }
        if (variables.containsKey(id.lexema)) {
            reporte.error(linea, "La variable " + id.lexema + " ya fue declarada");
            return;
        }
        variables.put(id.lexema, tipo);
        id.tipoDato = tipo;
    }

    public boolean verificarVariableDeclarada(EntradaTabla id, int linea) {
        if (id == null) {
            return false;
        }
        if (variables.containsKey(id.lexema)) {
            return true;
        }
        if (funcionActual != null && esParametroDe(funcionActual, id.lexema)) {
            return true;
        }
        reporte.error(linea, "La variable " + id.lexema + " no fue declarada");
        return false;
    }

    public String tipoDeVariable(String nombre) {
        return variables.get(nombre);
    }

    private boolean esParametroDe(FuncionDeclarada funcion, String nombre) {
        for (Parametro parametro : funcion.parametros) {
            if (parametro.nombre.equals(nombre)) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================
    // Constantes (tema 6: ulongint, tema 8: doublef)
    // ===================================================================

    public EntradaTabla verificarConstante(EntradaTabla constante, int linea) {
        if (constante == null) {
            return null;
        }
        String tipo = Tipos.tipoDeConstante(constante.lexema);
        if (tipo == null) {
            reporte.error(linea, "Constante mal formada: " + constante.lexema);
            return constante;
        }
        constante.tipoDato = tipo;
        if (!Tipos.rangoValido(constante.lexema, tipo)) {
            reporte.error(linea, "Constante de tipo " + tipo
                    + " fuera del rango permitido: " + constante.lexema);
        }
        return constante;
    }

    /**
     * Consideracion c) del TP2: el lexico acepta la constante sin conocer su signo,
     * asi que el rango debe volverse a controlar al detectarse el signo, y la tabla
     * de simbolos se actualiza con la constante negativa.
     */
    public EntradaTabla aplicarSignoNegativo(EntradaTabla constante, int linea) {
        if (constante == null) {
            return null;
        }
        String tipo = Tipos.tipoDeConstante(constante.lexema);
        if (Tipos.ULONGINT.equals(tipo)) {
            reporte.error(linea, "Las constantes de tipo " + Tipos.ULONGINT
                    + " no admiten signo negativo: -" + constante.lexema);
            return constante;
        }
        String lexemaNegativo = "-" + constante.lexema;
        if (!Tipos.rangoValido(lexemaNegativo, tipo)) {
            reporte.error(linea, "Constante de tipo " + tipo
                    + " fuera del rango permitido: " + lexemaNegativo);
        }
        return tablaSimbolos.insertarConstante(lexemaNegativo, tipo, linea);
    }

    // ===================================================================
    // Funciones (TP2 general y tema 19)
    // ===================================================================

    public FuncionDeclarada abrirFuncion(EntradaTabla id, String tipoRetorno, int linea) {
        if (id == null) {
            return null;
        }
        verificarFormatoIdentificador(id, linea);
        String nombre = id.lexema;

        if (claseActual == null && funciones.containsKey(nombre)) {
            reporte.error(linea, "La funcion " + nombre + " ya fue declarada");
        }
        FuncionDeclarada funcion = new FuncionDeclarada(nombre, tipoRetorno, linea);
        funcionActual = funcion;
        id.tipoDato = tipoRetorno;
        return funcion;
    }

    public void agregarParametroFormal(EntradaTabla id, String tipo, int linea) {
        if (id == null || funcionActual == null) {
            return;
        }
        verificarFormatoIdentificador(id, linea);
        if (!esTipoConocido(tipo)) {
            reporte.error(linea, "Tipo de dato desconocido en el parametro " + id.lexema + ": " + tipo);
        }
        if (esParametroDe(funcionActual, id.lexema)) {
            reporte.error(linea, "El parametro " + id.lexema + " esta repetido en la funcion "
                    + funcionActual.nombre);
            return;
        }
        funcionActual.parametros.add(new Parametro(id.lexema, tipo));
        id.tipoDato = tipo;
    }

    public void cerrarFuncion(int linea) {
        if (funcionActual == null) {
            return;
        }
        if (funcionActual.parametros.isEmpty()) {
            reporte.error(funcionActual.linea, "La funcion " + funcionActual.nombre
                    + " debe declarar al menos un parametro");
        }
        if (claseActual != null) {
            if (claseActual.metodos.containsKey(funcionActual.nombre)) {
                reporte.error(linea, "El metodo " + funcionActual.nombre
                        + " ya fue declarado en la clase " + claseActual.nombre);
            } else {
                claseActual.metodos.put(funcionActual.nombre, funcionActual);
            }
        } else {
            funciones.put(funcionActual.nombre, funcionActual);
        }
        // Los retornos vistos hasta aca pertenecen al cuerpo de esta funcion.
        retornosSinFuncion = 0;
        funcionActual = null;
    }

    public void registrarRetorno(int linea) {
        retornosSinFuncion++;
        lineaUltimoRetorno = linea;
    }

    /** Se invoca al reducir el programa: todo RET no reclamado quedo fuera de una funcion. */
    public void verificarRetornosFueraDeFuncion() {
        if (retornosSinFuncion > 0) {
            reporte.error(lineaUltimoRetorno,
                    "La sentencia RET solo puede aparecer dentro del cuerpo de una funcion");
            retornosSinFuncion = 0;
        }
    }

    public void verificarInvocacion(EntradaTabla id, List<Object> parametrosReales,
                                    List<Object> ordenEvaluacion, int linea) {
        if (id == null) {
            return;
        }
        int cantidadReales = parametrosReales == null ? 0 : parametrosReales.size();
        FuncionDeclarada funcion = funciones.get(id.lexema);

        if (funcion == null) {
            reporte.error(linea, "La funcion " + id.lexema + " no fue declarada");
        } else if (funcion.parametros.size() != cantidadReales) {
            reporte.error(linea, "La invocacion a " + id.lexema + " recibe " + cantidadReales
                    + " parametros y la funcion declara " + funcion.parametros.size());
        }
        verificarOrdenEvaluacion(ordenEvaluacion, cantidadReales, linea);
    }

    /**
     * Tema 19: el orden de evaluacion es obligatorio y debe ser una permutacion de
     * las posiciones 1..n de los parametros reales.
     */
    public void verificarOrdenEvaluacion(List<Object> constantes, int cantidadParametros, int linea) {
        if (constantes == null) {
            reporte.error(linea, "Falta la lista de orden de evaluacion de los parametros reales");
            return;
        }
        if (constantes.size() != cantidadParametros) {
            reporte.error(linea, "La lista de orden de evaluacion indica " + constantes.size()
                    + " posiciones y la invocacion tiene " + cantidadParametros + " parametros");
            return;
        }
        BigInteger maximo = BigInteger.valueOf(cantidadParametros);
        Set<Integer> posicionesUsadas = new TreeSet<>();

        for (Object elemento : constantes) {
            EntradaTabla entrada = ContextoRegla.comoEntrada(elemento);
            BigInteger valor = entrada == null ? null : Tipos.valorEntero(entrada.lexema);
            if (valor == null) {
                reporte.error(linea, "El orden de evaluacion debe indicarse con constantes enteras");
                continue;
            }
            if (valor.compareTo(BigInteger.ONE) < 0 || valor.compareTo(maximo) > 0) {
                reporte.error(linea, "La posicion " + valor + " del orden de evaluacion esta fuera del rango 1.."
                        + cantidadParametros);
                continue;
            }
            if (!posicionesUsadas.add(valor.intValue())) {
                reporte.error(linea, "La posicion " + valor + " esta repetida en el orden de evaluacion");
            }
        }
    }

    // ===================================================================
    // Clases (tema 25), acceso a atributos (tema 28) y herencia (tema 31)
    // ===================================================================

    public ClaseDeclarada abrirClase(EntradaTabla id, int linea) {
        if (id == null) {
            return null;
        }
        verificarFormatoIdentificador(id, linea);
        if (clases.containsKey(id.lexema)) {
            reporte.error(linea, "La clase " + id.lexema + " ya fue declarada");
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
        if (!esTipoConocido(tipo)) {
            reporte.error(linea, "Tipo de dato desconocido en el atributo " + id.lexema + ": " + tipo);
        }
        if (claseActual.atributos.containsKey(id.lexema)) {
            reporte.error(linea, "El atributo " + id.lexema + " ya fue declarado en la clase "
                    + claseActual.nombre);
            return;
        }
        claseActual.atributos.put(id.lexema, tipo);
        id.tipoDato = tipo;
    }

    /** Tema 31: la sentencia extends lista las clases de las que se hereda. */
    public void verificarHerencia(List<Object> clasesPadre, int linea) {
        if (claseActual == null) {
            reporte.error(linea, "La sentencia EXTENDS solo puede aparecer en el cuerpo de una clase");
            return;
        }
        if (clasesPadre == null) {
            return;
        }
        for (Object elemento : clasesPadre) {
            EntradaTabla padre = ContextoRegla.comoEntrada(elemento);
            if (padre == null) {
                continue;
            }
            if (padre.lexema.equals(claseActual.nombre)) {
                reporte.error(linea, "La clase " + claseActual.nombre + " no puede heredar de si misma");
                continue;
            }
            if (claseActual.heredaDe.contains(padre.lexema)) {
                reporte.error(linea, "La clase " + padre.lexema
                        + " esta repetida en la herencia de " + claseActual.nombre);
                continue;
            }
            if (!clases.containsKey(padre.lexema)) {
                reporte.error(linea, "La clase " + padre.lexema
                        + " no fue declarada antes de heredarse en " + claseActual.nombre);
                continue;
            }
            claseActual.heredaDe.add(padre.lexema);
        }
    }

    public void declararObjetos(List<Object> identificadores, String nombreClase, int linea) {
        if (!clases.containsKey(nombreClase)) {
            reporte.error(linea, "La clase " + nombreClase + " no fue declarada");
            return;
        }
        if (identificadores == null) {
            return;
        }
        for (Object elemento : identificadores) {
            declararVariable(ContextoRegla.comoEntrada(elemento), nombreClase, linea);
        }
    }

    /** Tema 28: acceso tradicional con punto. */
    public void verificarAccesoAtributo(EntradaTabla objeto, EntradaTabla miembro, int linea) {
        ClaseDeclarada clase = claseDelObjeto(objeto, linea);
        if (clase == null || miembro == null) {
            return;
        }
        if (!miembroVisible(clase, miembro.lexema, new HashSet<>())) {
            reporte.error(linea, "La clase " + clase.nombre + " no declara el miembro " + miembro.lexema);
        }
    }

    /** Tema 31: acceso desambiguado con el nombre de la clase que aporta el miembro. */
    public void verificarAccesoPrefijado(EntradaTabla objeto, EntradaTabla prefijo,
                                        EntradaTabla miembro, int linea) {
        ClaseDeclarada clase = claseDelObjeto(objeto, linea);
        if (clase == null || prefijo == null || miembro == null) {
            return;
        }
        if (!esAncestro(clase, prefijo.lexema, new HashSet<>())) {
            reporte.error(linea, "La clase " + clase.nombre + " no hereda de " + prefijo.lexema);
            return;
        }
        ClaseDeclarada clasePrefijo = clases.get(prefijo.lexema);
        if (clasePrefijo != null
                && !clasePrefijo.atributos.containsKey(miembro.lexema)
                && !clasePrefijo.metodos.containsKey(miembro.lexema)) {
            reporte.error(linea, "La clase " + prefijo.lexema + " no declara el miembro " + miembro.lexema);
        }
    }

    private ClaseDeclarada claseDelObjeto(EntradaTabla objeto, int linea) {
        if (objeto == null) {
            return null;
        }
        String tipo = variables.get(objeto.lexema);
        if (tipo == null) {
            reporte.error(linea, "El objeto " + objeto.lexema + " no fue declarado");
            return null;
        }
        ClaseDeclarada clase = clases.get(tipo);
        if (clase == null) {
            reporte.error(linea, "La variable " + objeto.lexema + " no es un objeto de clase");
        }
        return clase;
    }

    private boolean miembroVisible(ClaseDeclarada clase, String nombre, Set<String> visitadas) {
        if (clase == null || !visitadas.add(clase.nombre)) {
            return false;
        }
        if (clase.atributos.containsKey(nombre) || clase.metodos.containsKey(nombre)) {
            return true;
        }
        for (String padre : clase.heredaDe) {
            if (miembroVisible(clases.get(padre), nombre, visitadas)) {
                return true;
            }
        }
        return false;
    }

    private boolean esAncestro(ClaseDeclarada clase, String nombreBuscado, Set<String> visitadas) {
        if (clase == null || !visitadas.add(clase.nombre)) {
            return false;
        }
        for (String padre : clase.heredaDe) {
            if (padre.equals(nombreBuscado) || esAncestro(clases.get(padre), nombreBuscado, visitadas)) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================
    // Tipos enumerados (tema 23)
    // ===================================================================

    public void declararTipoEnumerado(EntradaTabla id, List<Object> valores, int linea) {
        if (id == null) {
            return;
        }
        verificarFormatoIdentificador(id, linea);
        if (esTipoConocido(id.lexema)) {
            reporte.error(linea, "El tipo " + id.lexema + " ya fue definido");
            return;
        }
        if (valores == null || valores.isEmpty()) {
            reporte.error(linea, "El tipo enumerado " + id.lexema + " debe definir al menos un valor");
            return;
        }

        String tipoBase = null;
        TipoEnumerado enumerado = new TipoEnumerado(id.lexema, null, linea);

        for (Object elemento : valores) {
            EntradaTabla valor = ContextoRegla.comoEntrada(elemento);
            if (valor == null) {
                continue;
            }
            verificarConstante(valor, linea);
            String tipoValor = Tipos.tipoDeConstante(valor.lexema);
            if (tipoBase == null) {
                tipoBase = tipoValor;
            } else if (tipoValor != null && !tipoBase.equals(tipoValor)) {
                reporte.error(linea, "El tipo enumerado " + id.lexema
                        + " mezcla valores de tipo " + tipoBase + " y " + tipoValor);
            }
            if (enumerado.valores.contains(valor.lexema)) {
                reporte.error(linea, "El valor " + valor.lexema + " esta repetido en el tipo enumerado "
                        + id.lexema);
                continue;
            }
            enumerado.valores.add(valor.lexema);
        }

        TipoEnumerado definitivo = new TipoEnumerado(id.lexema, tipoBase, linea);
        definitivo.valores.addAll(enumerado.valores);
        enumerados.put(id.lexema, definitivo);
        id.tipoDato = "tipo enumerado";
    }

    // ===================================================================
    // Tema 17: asignaciones de expresiones en expresiones
    // ===================================================================

    /**
     * El tema 17 admite varias asignaciones con '=' en una misma expresion, pero no
     * anidadas. Como el parser reduce de adentro hacia afuera, el anidamiento se
     * detecta consultando si la expresion asignada ya contiene una asignacion.
     */
    public ExpresionDiferida registrarAsignacionEnExpresion(EntradaTabla destino,
                                                           ExpresionDiferida expresion, int linea) {
        verificarVariableDeclarada(destino, linea);
        if (expresion != null && expresion.contieneAsignacion()) {
            reporte.error(linea, "No se admite anidamiento de asignaciones en expresiones");
        }
        ExpresionDiferida resultado = expresion == null ? new ExpresionDiferida() : expresion;
        resultado.marcarAsignacion();
        return resultado;
    }

    // ===================================================================
    // Expresiones y condiciones
    // ===================================================================

    public void verificarOperandosDeclarados(ExpresionDiferida expresion, int linea) {
        if (expresion == null) {
            return;
        }
        for (EntradaTabla operando : expresion.operandosSimples()) {
            if (esConstanteOCadena(operando)) {
                continue;
            }
            verificarVariableDeclarada(operando, linea);
        }
    }

    private boolean esConstanteOCadena(EntradaTabla entrada) {
        if (entrada == null) {
            return true;
        }
        String tipo = entrada.tipoDato;
        if (Tipos.CADENA.equals(tipo)) {
            return true;
        }
        return Tipos.tipoDeConstante(entrada.lexema) != null;
    }

    // ===================================================================
    // Tipos conocidos
    // ===================================================================

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

    // ===================================================================
    // Volcado para el informe
    // ===================================================================

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
