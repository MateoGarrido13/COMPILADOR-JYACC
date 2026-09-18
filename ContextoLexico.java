public class ContextoLexico {

    public final LectorFuente fuente;
    public final TablaSimbolos tablaSimbolos;
    public final Reporte reporte;
    public final StringBuilder lexema = new StringBuilder();
    public int estado;
    public int estadoAnterior;
    public int accion;
    public char caracter;

    public ContextoLexico(LectorFuente fuente, TablaSimbolos tablaSimbolos, Reporte reporte) {
        this.fuente = fuente;
        this.tablaSimbolos = tablaSimbolos;
        this.reporte = reporte;
    }

    public static int columna(char caracter) {
        return columna(caracter, -1);
    }

    public static int columna(char caracter, int estado) {
        if (Character.isDigit(caracter)) return 0;
        if (estado == 11 && (caracter == 'u' || caracter == 'U')) return 17;
        if (estado == 17 && (caracter == 'l' || caracter == 'L')) return 18;
        if ((estado == 12 || estado == 13) && (caracter == 'd' || caracter == 'D')) return 19;
        if (Character.isLowerCase(caracter)) return 1;
        if (Character.isUpperCase(caracter)) return 2;
        if (caracter == '<' || caracter == '>') return 3;
        if (caracter == '=') return 4;
        if (caracter == ':') return 5;
        if (caracter == '!') return 6;
        if (caracter == '*') return 7;
        if (caracter == '+') return 8;
        if (caracter == '-') return 9;
        if (caracter == '/') return 10;
        if (caracter == '{') return 11;
        if (caracter == '}') return 12;
        if (caracter == '.') return 13;
        if (caracter == '$') return 14;
        if (caracter == '(' || caracter == ')' || caracter == ',' || caracter == ';') return 15;
        if (caracter == '_') return 16;
        if (caracter == 0) return 20;
        if (caracter == '\n') return 22;
        if (Character.isWhitespace(caracter)) return 21;
        return 20;
    }
}