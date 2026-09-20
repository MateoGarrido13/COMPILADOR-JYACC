import java.util.LinkedHashMap;
import java.util.Map;

public class TablaSimbolos {
    private Map<String, EntradaTabla> tabla;

    // inicializo tabla y ya cargo palabras reservadas, por convencion de nombres, las keys son en minusculas
    public TablaSimbolos() {
        tabla = new LinkedHashMap<>();
        tabla.put("if",new EntradaTabla("if", Globals.PR_IF));
        tabla.put("else",new EntradaTabla("else", Globals.PR_ELSE));
        tabla.put("end_if",new EntradaTabla("end_if", Globals.PR_END_IF));
        tabla.put("begin",new EntradaTabla("begin", Globals.PR_BEGIN));
        tabla.put("end",new EntradaTabla("end", Globals.PR_END));
        tabla.put("pout",new EntradaTabla("pout", Globals.PR_POUT));
        tabla.put("ret",new EntradaTabla("ret", Globals.PR_RET));
        tabla.put("class",new EntradaTabla("class", Globals.PR_CLASS));
        tabla.put("function",new EntradaTabla("function", Globals.PR_FUNCTION));
        tabla.put("repeat",new EntradaTabla("repeat", Globals.PR_REPEAT));
        tabla.put("while",new EntradaTabla("while", Globals.PR_WHILE));
        tabla.put("todf",new EntradaTabla("todf", Globals.TODF));
        tabla.put("typedef",new EntradaTabla("typedef", Globals.PR_TYPEDEF));
        tabla.put("extends",new EntradaTabla("extends", Globals.PR_EXTENDS));
        tabla.put("ulongint",new EntradaTabla("ulongint", Globals.PR_ULONGINT));
        tabla.put("doublef",new EntradaTabla("doublef", Globals.PR_DOUBLEF));
    }

    public EntradaTabla buscarPalabraReservada(String lexema) {
        EntradaTabla entrada = tabla.get(lexema.toLowerCase());
        if (entrada != null && esPalabraReservada(entrada.tokenID)) {
            return entrada;
        }
        return null;
    }

    public EntradaTabla buscarOInsertarIdentificador(String lexema) {
        if (tabla.containsKey(lexema)) {
            return tabla.get(lexema);
        }
        EntradaTabla nuevaEntrada = new EntradaTabla(lexema, Globals.IDENTIFICADOR, Globals.numeroLinea);
        tabla.put(lexema, nuevaEntrada);
        return nuevaEntrada;
    }

    private boolean esPalabraReservada(int tokenID) {
        return tokenID >= 400;
    }

    /** Inserta el lexema; el tipo y el rango los asigna la semantica. */
    public EntradaTabla buscarOInsertarConstante(String lexema, int tokenID) {
        EntradaTabla entrada = tabla.get(lexema);
        if (entrada == null) {
            entrada = new EntradaTabla(lexema, tokenID, Globals.numeroLinea);
            tabla.put(lexema, entrada);
        }
        return entrada;
    }

    /** Cadenas reconocidas por el lexico (tema 9). El tipo se fija porque coincide con el token. */
    public EntradaTabla buscarOInsertarCadena(String cadena) {
        EntradaTabla entrada = tabla.get(cadena);
        if (entrada == null) {
            entrada = new EntradaTabla(cadena, Globals.CADENA, Globals.numeroLinea);
            tabla.put(cadena, entrada);
        }
        entrada.tipoDato = Tipos.CADENA;
        return entrada;
    }

    /**
     * Inserta o actualiza una constante. La usa el Analisis Sintactico al detectar
     * constantes negativas, segun la consideracion c) del TP2.
     */
    public EntradaTabla insertarConstante(String lexema, String tipoDato, int numeroLinea) {
        EntradaTabla entrada = tabla.get(lexema);
        if (entrada == null) {
            entrada = new EntradaTabla(lexema, Globals.CONSTANTE_NUMERICA, numeroLinea);
            tabla.put(lexema, entrada);
        }
        entrada.tipoDato = tipoDato;
        return entrada;
    }

    public String contenido() {
        StringBuilder texto = new StringBuilder("Contenido de la tabla de simbolos:\n");
        for (EntradaTabla entrada : tabla.values()) {
            texto.append(entrada.lexema).append(" -> ").append(entrada.tokenID).append("\n");
            texto.append("Numero de linea: ").append(entrada.numeroLinea).append("\n");
            texto.append("Tipo de dato: ").append(entrada.tipoDato).append("\n");
            texto.append("Direccion de memoria: ").append(entrada.direccionMemoria).append("\n");
            texto.append("--------------------------------\n");
        }
        return texto.toString();
    }

    public void imprimirTabla() {
        System.out.print(contenido());
    }
}
