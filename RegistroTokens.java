import java.util.ArrayList;
import java.util.List;

public class RegistroTokens {

    private static final List<String> tokens = new ArrayList<>();
    private static boolean ecoEnConsola = false;

    public static void reiniciar() {
        tokens.clear();
    }

    public static void setEcoEnConsola(boolean eco) {
        ecoEnConsola = eco;
    }

    public static void registrar(int tokenID, EntradaTabla yylval, int linea) {
        String descripcion = describir(tokenID, yylval);
        tokens.add(descripcion);
        if (ecoEnConsola) {
            System.out.println(descripcion);
        }
    }

    public static String describir(int tokenID, EntradaTabla yylval) {
        return "[" + tokenID + "] " + descripcionSinCodigo(tokenID, yylval);
    }

    private static String descripcionSinCodigo(int tokenID, EntradaTabla yylval) {
        String lexema = yylval == null ? "" : yylval.lexema;

        if (tokenID < 256) {
            return String.valueOf((char) tokenID);
        }
        if (tokenID >= 400) {

            return lexema.isEmpty() ? "Palabra reservada" : "Palabra reservada " + lexema;
        }

        switch (tokenID) {
            case Globals.IDENTIFICADOR:
                return "Identificador " + lexema;
            case Globals.CONSTANTE_NUMERICA:
                return "Constante " + nombreTipoConstante(lexema) + " " + lexema;
            case Globals.CADENA:
                return "Cadena {" + lexema + "}";
            case Globals.ASIGNACION:
                return "Operador de asignacion :=";
            case Globals.MAYOR_IGUAL:
                return "Comparador >=";
            case Globals.MENOR_IGUAL:
                return "Comparador <=";
            case Globals.IGUAL_IGUAL:
                return "Comparador ==";
            case Globals.DISTINTO:
                return "Comparador !=";
            default:

                return lexema.isEmpty() ? "Token" : "Token " + lexema;
        }
    }

    private static String nombreTipoConstante(String lexema) {
        String tipo = Tipos.tipoDeConstante(lexema);
        return tipo == null ? Tipos.INDEFINIDO : tipo;
    }

    public static List<String> getTokens() {
        return tokens;
    }

    public static String contenido() {
        StringBuilder texto = new StringBuilder("Tokens detectados por el Analizador Léxico:\n");
        for (String token : tokens) {
            texto.append(token).append("\n");
        }
        return texto.toString();
    }
}
