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
            int estado = 0;
            int ultimaAccion = MatrizTransiciones.SIN_SEM;
            ContextoLexico contexto = new ContextoLexico(fuente.linea());

            while (true) {
                int columna = columnaPara(estado, c);
                if (columna < 0) {
                    reporte.error(fuente.linea(), "Caracter no reconocido '" + c + "'");
                    break;
                }

                int siguienteEstado = MatrizTransiciones.MATRIZ_ESTADOS[estado][columna];
                int accion = MatrizTransiciones.MATRIZ_SEMANTICAS_ORIGINAL[estado][columna];

                if (siguienteEstado == MatrizTransiciones.ERROR) {
                    fuente.retroceder(c);
                    if (estado != 0) {
                        return EjecutorAccionesSemanticas.finalizar(
                                accionDeCierre(estado, ultimaAccion),
                                contexto, tablaSimbolos, reporte);
                    }
                    break;
                }

                boolean delimitador = siguienteEstado == MatrizTransiciones.ESTADO_F
                        && estado != 0 && esDelimitador(c);
                if (delimitador) {
                    fuente.retroceder(c);
                    return EjecutorAccionesSemanticas.finalizar(
                            accionDeCierre(estado, ultimaAccion),
                            contexto, tablaSimbolos, reporte);
                }

                EjecutorAccionesSemanticas.ejecutar(accion, contexto, c);
                if (accion != MatrizTransiciones.SIN_SEM) {
                    ultimaAccion = accion;
                }
                estado = siguienteEstado;

                if (estado == MatrizTransiciones.ESTADO_F) {
                    return EjecutorAccionesSemanticas.finalizar(
                            accion, contexto, tablaSimbolos, reporte);
                }
                if (estado == 0) {
                    if (accion == 15) {
                        reporte.error(contexto.lineaInicio(),
                                "Cadena de 1 linea { ... } no puede contener saltos de linea");
                    }
                    if (accion >= 16 && accion <= 18) {
                        break;
                    }
                    break;
                }
                c = fuente.leer();
                if (c == 0) {
                    if (estado == 5 || estado == 6 || estado == 7 || estado == 8) {
                        reporte.error(contexto.lineaInicio(),
                                "Cadena o comentario no cerrado antes del fin de archivo");
                        return 0;
                    }
                    return EjecutorAccionesSemanticas.finalizar(
                            accionDeCierre(estado, ultimaAccion),
                            contexto, tablaSimbolos, reporte);
                }
            }
        }
    }

    private int columnaPara(int estado, char c) {
        if (c == '<' || c == '>') return 3;
        if (c == '=') return 4;
        if (c == ':') return 5;
        if (c == '!') return 6;
        if (c == '*') return 7;
        if (c == '+') return 8;
        if (c == '-') return 9;
        if (c == '/') return 10;
        if (c == '{') return 11;
        if (c == '}') return 12;
        if (c == '.') return 13;
        if (c == '$') return 14;
        if (c == '(' || c == ')' || c == ',' || c == ';'
                || c == '[' || c == ']') return 15;
        if (c == '_') return 16;
        if (estado == 11 && (c == 'u' || c == 'U')) return 17;
        if (estado == 17 && (c == 'l' || c == 'L')) return 18;
        if (estado == 12 && (c == 'd' || c == 'D')) return 19;
        if (Character.isDigit(c)) return 0;
        if (Character.isWhitespace(c)) return c == '\n' ? 22 : 21;
        if ((estado == 0 || estado == 15) && c == 'M') return 2;
        if (Character.isLetter(c)) return 1;
        return 20;
    }

    private static boolean esDelimitador(char c) {
        return Character.isWhitespace(c) || c == '$';
    }

    private static int accionDeCierre(int estado, int ultimaAccion) {
        if (ultimaAccion != MatrizTransiciones.SIN_SEM) {
            return ultimaAccion;
        }
        switch (estado) {
            case 1: return 1;
            case 2: return 3;
            case 3: return 4;
            case 4: return 5;
            case 5:
            case 6: return 12;
            case 7:
            case 8: return 17;
            case 9: return 20;
            case 10:
            case 11: return 26;
            case 12:
            case 13:
            case 14: return 27;
            case 15: return 36;
            case 16: return 8;
            default: return ultimaAccion;
        }
    }

}
