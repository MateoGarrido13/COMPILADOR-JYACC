import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TablaSimbolos {
    private final Map<String, EntradaTabla> tabla;
    // Índice 0 vacío: el puntero de una entrada es su posición y empieza en 1.
    private final List<EntradaTabla> porPuntero;

    // inicializo tabla y ya cargo palabras reservadas, por convencion de nombres, las keys son en minusculas
    public TablaSimbolos() {
        tabla = new LinkedHashMap<>();
        porPuntero = new ArrayList<>();
        porPuntero.add(null);
        alta("if", Globals.PR_IF, 0);
        alta("else", Globals.PR_ELSE, 0);
        alta("end_if", Globals.PR_END_IF, 0);
        alta("begin", Globals.PR_BEGIN, 0);
        alta("end", Globals.PR_END, 0);
        alta("pout", Globals.PR_POUT, 0);
        alta("ret", Globals.PR_RET, 0);
        alta("class", Globals.PR_CLASS, 0);
        alta("function", Globals.PR_FUNCTION, 0);
        alta("repeat", Globals.PR_REPEAT, 0);
        alta("while", Globals.PR_WHILE, 0);
        alta("todf", Globals.TODF, 0);
        alta("typedef", Globals.PR_TYPEDEF, 0);
        alta("extends", Globals.PR_EXTENDS, 0);
        alta("ulongint", Globals.PR_ULONGINT, 0);
        alta("doublef", Globals.PR_DOUBLEF, 0);
    }

    // Única creación de entradas. El mismo lexema conserva el puntero que recibió acá.
    private EntradaTabla crear(String lexema, int tokenID, int numeroLinea) {
        EntradaTabla entrada = new EntradaTabla(lexema, tokenID, numeroLinea);
        entrada.puntero = porPuntero.size();
        porPuntero.add(entrada);
        return entrada;
    }

    private EntradaTabla alta(String lexema, int tokenID, int numeroLinea) {
        EntradaTabla entrada = crear(lexema, tokenID, numeroLinea);
        tabla.put(lexema, entrada);
        return entrada;
    }

    public EntradaTabla obtenerPorPuntero(int puntero) {
        if (puntero <= 0 || puntero >= porPuntero.size()) {
            return null;
        }
        return porPuntero.get(puntero);
    }

    public EntradaTabla buscarPalabraReservada(String lexema) {
        EntradaTabla entrada = tabla.get(lexema.toLowerCase());
        if (entrada != null && esPalabraReservada(entrada.tokenID)) {
            return entrada;
        }
        return null;
    }

    public EntradaTabla buscarOInsertarIdentificador(String lexema) {
        EntradaTabla entrada = tabla.get(lexema);
        if (entrada != null) {
            return entrada;
        }
        return alta(lexema, Globals.IDENTIFICADOR, Globals.numeroLinea);
    }

    private boolean esPalabraReservada(int tokenID) {
        return tokenID >= 400;
    }

    // Inserta el lexema; el tipo y el rango los asigna la semantica. 
    public EntradaTabla buscarOInsertarConstante(String lexema, int tokenID) {
        EntradaTabla entrada = tabla.get(lexema);
        if (entrada == null) {
            entrada = alta(lexema, tokenID, Globals.numeroLinea);
        }
        return entrada;
    }

    // Cadenas reconocidas por el lexico (tema 9). El tipo se fija porque coincide con el token.
    public EntradaTabla buscarOInsertarCadena(String cadena) {
        EntradaTabla entrada = tabla.get(cadena);
        if (entrada == null) {
            entrada = alta(cadena, Globals.CADENA, Globals.numeroLinea);
        }
        entrada.tipoDato = Tipos.CADENA;
        return entrada;
    }

     // Inserta o actualiza una constante. La usa el Analisis Sintactico al detectar constantes negativas
    public EntradaTabla insertarConstante(String lexema, String tipoDato, int numeroLinea) {
        EntradaTabla entrada = tabla.get(lexema);
        if (entrada == null) {
            entrada = alta(lexema, Globals.CONSTANTE_NUMERICA, numeroLinea);
        }
        entrada.tipoDato = tipoDato;
        return entrada;
    }

    public String contenido() {
        StringBuilder texto = new StringBuilder("Contenido de la tabla de simbolos:\n");
        for (EntradaTabla entrada : tabla.values()) {
            texto.append(entrada.lexema).append(" -> ").append(entrada.tokenID).append("\n");
            texto.append("Puntero: ").append(entrada.puntero).append("\n");
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
