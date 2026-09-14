public class Globals {
    public static EntradaTabla yylval;
    public static int numeroLinea = 1;

    // Tokens compuestos (valores > 255)
    public static final int IDENTIFICADOR = 300;
    public static final int ASIGNACION = 301; // Para ':='
    public static final int CONSTANTE_NUMERICA = 302;

    // Palabras Reservadas
    public static final int PR_IF = 400;
    public static final int PR_ELSE = 401;
    public static final int PR_END_IF = 402;
    public static final int PR_BEGIN = 403;
    public static final int PR_END = 404;
    public static final int PR_POUT = 405;
    public static final int PR_RET = 406;
    public static final int PR_CLASS = 407;
    public static final int PR_FUNCTION = 408;
    public static final int PR_REPEAT = 409;
    public static final int PR_WHILE = 410;
    public static final int TODF = 411;
}
