import java.math.BigInteger;

/*
  Tipos de dato del lenguaje segun los temas particulares asignados al grupo.
 
  Tema 6:  ulongint  - enteros largos sin signo de 32 bits, sufijo "$ul", rango 0 .. 2^32 - 1
  Tema 8:  doublef   - punto flotante de 64 bits, exponente con la letra "d"
  Tema 9:  cadena    - cadenas de una linea delimitadas por llaves
 */
public class Tipos {

    public static final String ULONGINT = "ulongint";
    public static final String DOUBLEF = "doublef";
    public static final String CADENA = "cadena";
    public static final String INDEFINIDO = "indefinido";

    public static final String SUFIJO_ULONGINT = "$ul";
    public static final char LETRA_EXPONENTE_DOUBLEF = 'd';

    public static final BigInteger ULONGINT_MINIMO = BigInteger.ZERO;
    public static final BigInteger ULONGINT_MAXIMO = new BigInteger("4294967295"); // 2^32 - 1

    public static final double DOUBLEF_MINIMO_ABSOLUTO = 2.2250738585072014e-308;
    public static final double DOUBLEF_MAXIMO_ABSOLUTO = 1.7976931348623157e+308;

    public static final int LONGITUD_MAXIMA_IDENTIFICADOR = 22;

    public static boolean esTipoBasico(String nombre) {
        if (nombre == null) {
            return false;
        }
        String normalizado = nombre.toLowerCase();
        return ULONGINT.equals(normalizado) || DOUBLEF.equals(normalizado);
    }

    // Deduce el tipo de una constante a partir de su forma lexica. 
    public static String tipoDeConstante(String lexema) {
        if (lexema == null) {
            return null;
        }
        String texto = sinSigno(lexema.toLowerCase().trim());
        if (texto.isEmpty()) {
            return null;
        }
        if (texto.endsWith(SUFIJO_ULONGINT)) {
            return ULONGINT;
        }
        // El tema 8 exige el punto y la parte decimal; el exponente es opcional.
        if (texto.indexOf('.') >= 0) {
            return DOUBLEF;
        }
        if (esSecuenciaDeDigitos(texto)) {
            return ULONGINT;
        }
        return null;
    }

    public static boolean rangoValido(String lexema, String tipo) {
        if (ULONGINT.equals(tipo)) {
            BigInteger valor = valorEntero(lexema);
            return valor != null
                    && valor.compareTo(ULONGINT_MINIMO) >= 0
                    && valor.compareTo(ULONGINT_MAXIMO) <= 0;
        }
        if (DOUBLEF.equals(tipo)) {
            Double valor = valorDoublef(lexema);
            if (valor == null || valor.isNaN() || valor.isInfinite()) {
                return false;
            }
            double absoluto = Math.abs(valor);
            if (absoluto == 0.0) {
                return true;
            }
            return absoluto >= DOUBLEF_MINIMO_ABSOLUTO && absoluto <= DOUBLEF_MAXIMO_ABSOLUTO;
        }
        return true;
    }

    // Valor entero de una constante, sin el sufijo. Devuelve null si no es entera.
    public static BigInteger valorEntero(String lexema) {
        if (lexema == null) {
            return null;
        }
        String texto = lexema.toLowerCase().trim();
        boolean negativo = texto.startsWith("-");
        texto = sinSigno(texto);
        if (texto.endsWith(SUFIJO_ULONGINT)) {
            texto = texto.substring(0, texto.length() - SUFIJO_ULONGINT.length());
        }
        if (!esSecuenciaDeDigitos(texto)) {
            return null;
        }
        BigInteger valor = new BigInteger(texto);
        return negativo ? valor.negate() : valor;
    }

    // Valor de una constante doublef. El exponente del tema 8 usa 'd' en lugar de 'e'. 
    public static Double valorDoublef(String lexema) {
        if (lexema == null) {
            return null;
        }
        String texto = lexema.toLowerCase().trim().replace(LETRA_EXPONENTE_DOUBLEF, 'e');
        try {
            return Double.valueOf(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String sinSigno(String texto) {
        if (texto == null) {
            return null;
        }
        if (texto.startsWith("-") || texto.startsWith("+")) {
            return texto.substring(1);
        }
        return texto;
    }

    public static boolean esSecuenciaDeDigitos(String texto) {
        if (texto == null || texto.isEmpty()) {
            return false;
        }
        for (int i = 0; i < texto.length(); i++) {
            if (!Character.isDigit(texto.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
