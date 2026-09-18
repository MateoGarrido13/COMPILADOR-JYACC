public class Globals {
    public static EntradaTabla yylval;
    public static int numeroLinea = 1;

    // Tokens compuestos (valores > 255)
    public static final int IDENTIFICADOR = 300;
    public static final int ASIGNACION = 301; // ':='
    public static final int CONSTANTE_NUMERICA = 302;
    public static final int CADENA = 303; // tema 9: cadenas de una linea entre llaves
    public static final int MAYOR_IGUAL = 304;
    public static final int MENOR_IGUAL = 305;
    public static final int IGUAL_IGUAL = 306;
    public static final int DISTINTO = 307;

    // Palabras Reservadas (insensibles a mayusculas)
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
    public static final int PR_TYPEDEF = 412;
    public static final int PR_EXTENDS = 413;
    public static final int PR_ULONGINT = 414;
    public static final int PR_DOUBLEF = 415;
}
