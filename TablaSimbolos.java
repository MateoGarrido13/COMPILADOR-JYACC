import java.util.HashMap;

public class TablaSimbolos {
    private HashMap<String, EntradaTabla> tabla;

    public TablaSimbolos() {
        tabla = new HashMap<>();
        precargarReservada("if", Globals.PR_IF);
        precargarReservada("else", Globals.PR_ELSE);
        precargarReservada("end_if", Globals.PR_END_IF);
        precargarReservada("begin", Globals.PR_BEGIN);
        precargarReservada("end", Globals.PR_END);
        precargarReservada("pout", Globals.PR_POUT);
        precargarReservada("ret", Globals.PR_RET);
        precargarReservada("class", Globals.PR_CLASS);
        precargarReservada("function", Globals.PR_FUNCTION);
        precargarReservada("repeat", Globals.PR_REPEAT);
        precargarReservada("while", Globals.PR_WHILE);
        precargarReservada("todf", Globals.TODF);
    }

    private void precargarReservada(String lexema, int tokenID) {
        tabla.put(lexema, new EntradaTabla(lexema, tokenID));
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
    public void imprimirTabla() {
        System.out.println("Contenido de la tabla de simbolos:");
        for (EntradaTabla entrada : tabla.values()) {
            System.out.println(entrada.lexema + " -> " + entrada.tokenID);
            System.out.println("Numero de linea: " + entrada.numeroLinea);
            System.out.println("Tipo de dato: " + entrada.tipoDato);
            System.out.println("Direccion de memoria: " + entrada.direccionMemoria);
            System.out.println("--------------------------------");
        }
    }
}
