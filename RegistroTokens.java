import java.util.ArrayList;
import java.util.List;

/*
  Tira de tokens detectados por el Analizador Lexico.
 
  Es la primera de las salidas del compilador. El lexico registra cada token
  en el momento de reconocerlo.
 */
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
        String lexema = yylval == null ? "" : yylval.lexema;

        if (tokenID < 256) {
            return String.valueOf((char) tokenID);
        }
        if (tokenID >= 400) {
            return "Palabra reservada " + (lexema.isEmpty() ? Integer.toString(tokenID) : lexema);
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
                return "Token " + tokenID + " " + lexema;
        }
    }

    // Describe la forma lexica; el control de rango lo hace la semantica.
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
