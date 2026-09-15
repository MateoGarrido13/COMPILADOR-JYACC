import java.io.FileNotFoundException;

/**
 * Reconoce tokens y los entrega de a uno (yylex).
 *
 * No valida rangos, tipos ni declaraciones: eso pertenece a las acciones
 * semanticas. El lexico solo clasifica la forma del lexema, trunca
 * identificadores segun el TP1 e informa errores estrictamente lexicos
 * (caracter invalido, comentario o cadena mal cerrados).
 */
public class AnalizadorLexico {

    private final LectorFuente fuente = new LectorFuente();
    private final TablaSimbolos tablaSimbolos;
    private final Reporte reporte;

    public AnalizadorLexico(TablaSimbolos tablaSimbolos, Reporte reporte, String rutaArchivo)
            throws FileNotFoundException {
        this.tablaSimbolos = tablaSimbolos;
        this.reporte = reporte;
        fuente.abrir(rutaArchivo);
    }

    /**
     * Punto de entrada para YACC/BYACC: un token por invocacion.
     * 0 = fin de archivo, valor positivo = token reconocido.
     */
    public int yylex() {
        int tokenID = siguienteToken();
        if (tokenID > 0) {
            RegistroTokens.registrar(tokenID, Globals.yylval, fuente.linea());
        }
        return tokenID;
    }

    public void cerrar() {
        fuente.cerrar();
    }

    private int siguienteToken() {
        while (true) {
            char c = fuente.leer();
            while (Character.isWhitespace(c)) {
                c = fuente.leer();
            }

            if (c == 0 || c == '$') {
                return 0;
            }

            if (c == '{') {
                Integer token = procesarLlave();
                if (token == null) {
                    continue;
                }
                return token;
            }

            if (Character.isLetter(c)) {
                return procesarIdentificador(c);
            }

            if (Character.isDigit(c) || c == '.') {
                return procesarNumero(c);
            }

            if (c == ':') {
                return procesarAsignacion();
            }

            Integer comparador = procesarComparador(c);
            if (comparador != null) {
                if (comparador == 0) {
                    continue;
                }
                Globals.yylval = null;
                return comparador;
            }

            if (esSimboloSimple(c)) {
                Globals.yylval = null;
                return (int) c;
            }

            reporte.error(fuente.linea(), "Caracter no reconocido '" + c + "'");
        }
    }

    /** Tema 16: comentario {{ ... }}. Tema 9: cadena de una linea { ... }. */
    private Integer procesarLlave() {
        char siguiente = fuente.leer();
        if (siguiente == '{') {
            return consumirComentario() ? null : 0;
        }
        fuente.retroceder(siguiente);
        return procesarCadena();
    }

    private boolean consumirComentario() {
        while (true) {
            char c = fuente.leer();
            if (c == 0) {
                reporte.error(fuente.linea(),
                        "Comentario multilinea {{ ... }} no cerrado antes del fin de archivo");
                return false;
            }
            if (c == '}') {
                char siguiente = fuente.leer();
                if (siguiente == '}') {
                    return true;
                }
                fuente.retroceder(siguiente);
            }
        }
    }

    private Integer procesarCadena() {
        StringBuilder cadena = new StringBuilder();
        int lineaInicio = fuente.linea();
        while (true) {
            char c = fuente.leer();
            if (c == 0) {
                reporte.error(lineaInicio, "Cadena de 1 linea no cerrada antes del fin de archivo");
                return 0;
            }
            if (c == '\n') {
                reporte.error(lineaInicio, "Cadena de 1 linea { ... } no puede contener saltos de linea");
                return null;
            }
            if (c == '}') {
                Globals.yylval = tablaSimbolos.buscarOInsertarCadena(cadena.toString());
                return Globals.CADENA;
            }
            cadena.append(c);
        }
    }

    private int procesarIdentificador(char inicial) {
        StringBuilder lexema = new StringBuilder();
        lexema.append(inicial);

        char c = fuente.leer();
        while (Character.isLetterOrDigit(c) || c == '_') {
            lexema.append(c);
            c = fuente.leer();
        }
        fuente.retroceder(c);

        String texto = lexema.toString();
        EntradaTabla reservada = tablaSimbolos.buscarPalabraReservada(texto);
        if (reservada != null) {
            Globals.yylval = reservada;
            return reservada.tokenID;
        }

        if (texto.length() > Tipos.LONGITUD_MAXIMA_IDENTIFICADOR) {
            String truncado = texto.substring(0, Tipos.LONGITUD_MAXIMA_IDENTIFICADOR);
            reporte.warning(fuente.linea(),
                    "El identificador " + texto + " fue truncado a: " + truncado);
            texto = truncado;
        }

        Globals.yylval = tablaSimbolos.buscarOInsertarIdentificador(texto);
        return Globals.yylval.tokenID;
    }

    /**
     * Reconoce la forma de enteros ($ul) y doublef (punto y exponente d).
     * Una entrada mal formada como 10.423.2 se parte en dos tokens validos;
     * el desajuste lo reporta el sintactico. El rango lo controla la semantica.
     */
    private int procesarNumero(char inicial) {
        StringBuilder lexema = new StringBuilder();
        boolean tienePunto = false;
        char c = inicial;

        if (c == '.') {
            char siguiente = fuente.leer();
            if (Character.isDigit(siguiente)) {
                lexema.append('.').append(siguiente);
                tienePunto = true;
                c = fuente.leer();
            } else {
                fuente.retroceder(siguiente);
                Globals.yylval = null;
                return (int) '.';
            }
        } else {
            lexema.append(c);
            c = fuente.leer();
        }

        while (Character.isDigit(c) || (c == '.' && !tienePunto)) {
            if (c == '.') {
                tienePunto = true;
            }
            lexema.append(c);
            c = fuente.leer();
        }

        if (tienePunto && Character.toLowerCase(c) == Tipos.LETRA_EXPONENTE_DOUBLEF) {
            lexema.append(c);
            c = fuente.leer();
            if (c == '+' || c == '-') {
                lexema.append(c);
                c = fuente.leer();
            }
            while (Character.isDigit(c)) {
                lexema.append(c);
                c = fuente.leer();
            }
        }
        fuente.retroceder(c);

        if (!tienePunto) {
            c = fuente.leer();
            if (c == '$') {
                char u = fuente.leer();
                char l = fuente.leer();
                if (Character.toLowerCase(u) == 'u' && Character.toLowerCase(l) == 'l') {
                    lexema.append(Tipos.SUFIJO_ULONGINT);
                } else {
                    fuente.retroceder(l);
                    fuente.retroceder(u);
                    fuente.retroceder('$');
                }
            } else {
                fuente.retroceder(c);
            }
        }

        Globals.yylval = tablaSimbolos.buscarOInsertarConstante(lexema.toString(), Globals.CONSTANTE_NUMERICA);
        return Globals.CONSTANTE_NUMERICA;
    }

    private int procesarAsignacion() {
        char siguiente = fuente.leer();
        if (siguiente == '=') {
            Globals.yylval = null;
            return Globals.ASIGNACION;
        }
        fuente.retroceder(siguiente);
        return ':';
    }

    /** null = no era comparador; 0 = '!' suelto, ya informado, seguir. */
    private Integer procesarComparador(char c) {
        if (c != '<' && c != '>' && c != '=' && c != '!') {
            return null;
        }
        char siguiente = fuente.leer();
        if (c == '<' && siguiente == '=') {
            return Globals.MENOR_IGUAL;
        }
        if (c == '>' && siguiente == '=') {
            return Globals.MAYOR_IGUAL;
        }
        if (c == '=' && siguiente == '=') {
            return Globals.IGUAL_IGUAL;
        }
        if (c == '!' && siguiente == '=') {
            return Globals.DISTINTO;
        }
        fuente.retroceder(siguiente);
        if (c == '!') {
            reporte.error(fuente.linea(), "Caracter no reconocido '!'");
            return 0;
        }
        return (int) c;
    }

    private static boolean esSimboloSimple(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/'
                || c == '(' || c == ')' || c == ',' || c == ';'
                || c == '[' || c == ']';
    }
}
