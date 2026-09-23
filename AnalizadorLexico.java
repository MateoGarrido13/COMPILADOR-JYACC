import java.io.FileNotFoundException;

/* Reconoce tokens utilizando el automata de MatrizTransiciones. */
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
            ContextoLexico contexto = new ContextoLexico(fuente, tablaSimbolos, reporte);
            int token = recorrerMatriz(contexto);
            if (token == 0) return 0;
            if (token > 0) return token;
        }
    }

    private int recorrerMatriz(ContextoLexico contexto) {
        while (true) {
            contexto.caracter = fuente.leer();
            if (contexto.caracter == 0 || (contexto.caracter == '$' && contexto.estado == 0)) {
                if (contexto.lexema.length() == 0) return 0;
                int sem = MatrizTransiciones.semCierre(contexto.estado);
                if (sem >= 0) {
                    quitarBlancosFinales(contexto.lexema);
                    contexto.accion = sem;
                    return ejecutarAccion(contexto);
                }
                return errorLexico(contexto);
            }

            int columna = contexto.columnaActual();
            contexto.estadoAnterior = contexto.estado;
            int siguiente = MatrizTransiciones.MATRIZ_ESTADOS[contexto.estado][columna];
            if (siguiente == MatrizTransiciones.ERROR) {
                return errorLexico(contexto);
            }

            boolean lookahead = esLookahead(contexto.estado, contexto.caracter, siguiente);
            if (!lookahead && !(contexto.estado == 0 && Character.isWhitespace(contexto.caracter))) {
                contexto.lexema.append(contexto.caracter);
            }
            if (lookahead) {
                fuente.retroceder(contexto.caracter);
            }

            contexto.estado = siguiente;
            if (siguiente == MatrizTransiciones.ESTADO_F) {
                quitarBlancosFinales(contexto.lexema);
                contexto.accion = MatrizTransiciones.MATRIZ_SEMANTICAS[contexto.estadoAnterior][columna];
                return ejecutarAccion(contexto);
            }
        }
    }

    /*
      El caracter cerro el token pero no forma parte de el: hay que devolverlo
      a la fuente para el proximo yylex.
     */
    private static boolean esLookahead(int estado, char caracter, int siguiente) {
        if (siguiente != MatrizTransiciones.ESTADO_F) {
            return false;
        }
        if (Character.isWhitespace(caracter)) {
            return false;
        }
        if (caracter == '=' && (estado == 1 || estado == 2 || estado == 3 || estado == 4)) {
            return false;
        }
        if (estado == 0) {
            return false;
        }
        if ((estado == 6 || estado == 8) && caracter == '}') {
            return false;
        }
        if ((estado == 5 || estado == 6) && caracter == '\n') {
            return false;
        }
        return true;
    }

    private int ejecutarAccion(ContextoLexico contexto) {
        switch (contexto.accion) {
            case 1: case 2: return new AccionEstructura(contexto).ejecutar(); //Asignación
            case 3: case 4: case 5: case 6: return new AccionComparador(contexto).ejecutar(); // Comparadores
            // Revisar estas acciones, si se puede hacer una sola acción para palabras reservadas e identificadores
            case 7: case 36: return new AccionReservada(contexto).ejecutar(); // Palabras reservadas, verifica si hacer o no entrada nueva
            case 8: case 9: return new AccionIdentificador(contexto).ejecutar(); //Identificadores, warning
            case 12: case 13: case 14: case 15: case 16: case 17: case 18: // Comentarios multilinea y cadenas
                return new AccionCadena(contexto).ejecutar();
            case 20: return new AccionSimbolo(contexto).ejecutar();
            case 22: case 23: case 24: case 25: case 26:
                return new AccionConstanteEntera(contexto).ejecutar();
            case 21: case 27: case 28: case 29: case 30: case 31: case 32: case 33: case 34: case 35:
                return new AccionConstanteFloat(contexto).ejecutar();
            default:
                return new AccionSimbolo(contexto).ejecutar();
        }
    }

    private int errorLexico(ContextoLexico contexto) { 
        reporte.error(fuente.linea(), "Lexema no reconocido '" + contexto.lexema + "'");
        return -1;
    }

    private static void quitarBlancosFinales(StringBuilder lexema) {
        while (lexema.length() > 0 && Character.isWhitespace(lexema.charAt(lexema.length() - 1))) {
            lexema.setLength(lexema.length() - 1);
        }
    }
}
