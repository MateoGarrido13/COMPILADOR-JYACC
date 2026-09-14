import java.io.*;

public class AnalizadorLexico {
    private PushbackReader reader;
    private TablaSimbolos tablaSimbolos;

    //CAMBIO
    // El constructor recibe la ruta del archivo fuente a compilar
    public AnalizadorLexico(TablaSimbolos ts, String rutaArchivo) throws FileNotFoundException {
        this.tablaSimbolos = ts;
        this.reader = new PushbackReader(new FileReader(rutaArchivo));
        Globals.numeroLinea = 1;
    }

    private char leerSiguienteCaracter() {
        try {
            int c = reader.read();
            if (c == -1) return (char) 0; // EOF
            if (c == '\n') {
                Globals.numeroLinea++;
            }
            return (char) c;
        } catch (IOException e) {
            return (char) 0;
        }
    }

    private void retrocederUnCaracter(char c) {
        try {
            if (c != 0) {
                if (c == '\n') {
                    Globals.numeroLinea--;
                }
                reader.unread(c);
            }
        } catch (IOException e) {
            System.err.println("Error al retroceder caracter");
        }
    }
    
    public int siguienteToken() {
        while (true) { // Bucle para permitir descartar comentarios sin retornar token
            char c = leerSiguienteCaracter();

            while (Character.isWhitespace(c)) {
                c = leerSiguienteCaracter();
            }

            if (c == 0 || c == '$') return 0;

            // TEMA 16 y TEMA 9
            if (c == '{') { 
                char sig = leerSiguienteCaracter(); 
                if (sig == '{') {
                    // TEMA 16: Comentario multilínea {{ ... }} Consumir y descartar 
                    boolean cerrado = false;
                    while (!cerrado) { 
                        c = leerSiguienteCaracter(); 
                        if (c == 0) { 
                            System.err.println("Línea " + Globals.numeroLinea + ": Error léxico: Comentario multilínea {{ ... }} no cerrado antes del fin de archivo"); 
                            return -1; 
                        } 
                        if (c == '}') { 
                            char sig2 = leerSiguienteCaracter(); 
                            if (sig2 == '}') { 
                                cerrado = true; 
                            } else { 
                                retrocederUnCaracter(sig2); 
                            } 
                        } 
                    } 
                    continue; // Comentario descartado: vuelve al inicio del bucle 
                } else { 
                    // TEMA 9: Cadena de 1 línea { ... } 
                    retrocederUnCaracter(sig); 
                    String cadena = ""; 
                    c = leerSiguienteCaracter(); 
                    boolean cerrada = false; 
                    while (c != 0) { 
                        if (c == '\\n') { 
                            System.err.println("Línea " + (Globals.numeroLinea - 1) + ": Error léxico: Cadena de 1 línea { ... } no puede contener saltos de línea"); 
                            return -1; 
                        } 
                        if (c == '}') { 
                            cerrada = true; 
                            break; 
                        } 
                        cadena += c; 
                        c = leerSiguienteCaracter(); 
                    } 
                    if (!cerrada) { 
                        System.err.println("Línea " + Globals.numeroLinea + ": Error léxico: Cadena de 1 línea no cerrada antes del fin de archivo"); 
                        return -1; 
                    } 
                    Globals.yylval = tablaSimbolos.buscarOInsertarCadena(cadena); 
                    return Globals.CADENA; 
                } 
            }

            // CASO 1: secuencia alfabética → primero palabras reservadas, luego identificadores
            if (Character.isLetter(c)) {
                String lexema = "" + c;
                c = leerSiguienteCaracter();

                while (Character.isLetterOrDigit(c) || c == '_') {
                    lexema += c;
                    c = leerSiguienteCaracter();
                }
                retrocederUnCaracter(c);

                EntradaTabla reservada = tablaSimbolos.buscarPalabraReservada(lexema);
                if (reservada != null) {
                    Globals.yylval = reservada;
                    return reservada.tokenID;
                }

                //CAMBIO
                if (lexema.length() > 22) {
                    System.out.println("Línea " + Globals.numeroLinea + ": Warning: El identificador '" + lexema + "' fue truncado a: " + lexema.substring(0, 22));
                    lexema = lexema.substring(0, 22);
                }

                Globals.yylval = tablaSimbolos.buscarOInsertarIdentificador(lexema);
                return Globals.yylval.tokenID;
            }

            
            // CASO 2: Constantes numéricas (TEMA 6: $ul y TEMA 8: DOUBLEF con . y d)
            if (Character.isDigit(c) || c == '.') {
                String lexemaNum = "";
                boolean tienePunto = false;
                boolean esDoublef = false;

                if (c == '.') {
                    char sig = leerSiguienteCaracter();
                    if (Character.isDigit(sig)) {
                        lexemaNum += '.';
                        lexemaNum += sig;
                        tienePunto = true;
                        c = leerSiguienteCaracter();
                    } else {
                        retrocederUnCaracter(sig);
                        Globals.yylval = null;
                        return (int) '.'; // Castea a entero y devuelve el codigo ASCII de '.' (46)
                    }
                } else {
                    lexemaNum += c;
                    c = leerSiguienteCaracter();
                }

                // Lectura de parte entera y decimal
                while (Character.isDigit(c) || (c == '.' && !tienePunto)) {
                    if (c == '.') {
                        tienePunto = true;
                    }
                    lexemaNum += c;
                    c = leerSiguienteCaracter();
                }

                /*
                * En el Léxico no se genera un error léxico directo; 
                la entrada mal formada `10.423.2` se descompone en dos tokens válidos consecutivos: 10.423 y .2
                En el Sintáctico al recibir dos números seguidos sin un operador que los vincule (ejemplo: `10.423 .2`), 
                la gramática de YACC falla y reporta un **Error Sintáctico**.
                */

                // TEMA 8: Exponente con 'd' para DOUBLEF
                if (tienePunto && (c == 'd' || c == 'D')) {
                    esDoublef = true;
                    lexemaNum += c;
                    c = leerSiguienteCaracter();
                    if (c == '+' || c == '-') {
                        lexemaNum += c;
                        c = leerSiguienteCaracter();
                    }
                    while (Character.isDigit(c)) {
                        lexemaNum += c;
                        c = leerSiguienteCaracter();
                    }
                }

                retrocederUnCaracter(c);

                // TEMA 6: Verificación del sufijo $ul / $UL para enteros 32-bit sin signo
                if (!tienePunto && !esDoublef) {
                    c = leerSiguienteCaracter();
                    if (c == '$') {
                        char u = leerSiguienteCaracter();
                        char l = leerSiguienteCaracter();
                        if ((u == 'u' || u == 'U') && (l == 'l' || l == 'L')) {
                            String lexemaWithSuffix = lexemaNum + "$ul";
                            try {
                                long val = Long.parseLong(lexemaNum);
                                if (val < 0 || val > 4294967295L) { // 2^32 - 1
                                    System.err.println("Línea " + Globals.numeroLinea + 
                                        ": Error léxico: Constante entera $ul fuera de rango (0 a 4294967295): " + lexemaWithSuffix);
                                    return -1;
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("Línea " + Globals.numeroLinea + 
                                    ": Error léxico: Formato numérico inválido en constante $ul: " + lexemaWithSuffix);
                                return -1;
                            }
                            Globals.yylval = tablaSimbolos.buscarOInsertarConstante(lexemaWithSuffix, Globals.CONSTANTE_NUMERICA);
                            return Globals.CONSTANTE_NUMERICA;
                        } else {
                            retrocederUnCaracter(l);
                            retrocederUnCaracter(u);
                            retrocederUnCaracter('$');
                        }
                    } else {
                        retrocederUnCaracter(c);
                    }
                }

                // TEMA 8: Validación de rango para DOUBLEF
                if (tienePunto) {
                    try {
                        String numStrForParsing = lexemaNum.replace('d', 'e').replace('D', 'E');
                        double val = Double.parseDouble(numStrForParsing);
                        double absVal = Math.abs(val);
                        if (val != 0.0 && (absVal < 2.2250738585072014e-308 || absVal > 1.7976931348623157e308)) {
                            System.err.println("Línea " + Globals.numeroLinea + 
                                ": Error léxico: Constante DOUBLEF fuera del rango permitido: " + lexemaNum);
                            return -1;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Línea " + Globals.numeroLinea + 
                            ": Error léxico: Formato de constante DOUBLEF inválido: " + lexemaNum);
                        return -1;
                    }
                }

                Globals.yylval = tablaSimbolos.buscarOInsertarConstante(lexemaNum, Globals.CONSTANTE_NUMERICA);
                return Globals.CONSTANTE_NUMERICA;
            }

            // CASO 3: Asignación ':='
            if (c == ':') {
                char siguiente = leerSiguienteCaracter();
                if (siguiente == '=') {
                    Globals.yylval = null;
                    return Globals.ASIGNACION;
                } else {
                    retrocederUnCaracter(siguiente);
                    return c;
                }
            }

            // CASO 4: Caracteres únicos (mapeo ASCII)
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '<' || c == '>'
                    || c == '=' || c == '(' || c == ')' || c == ',' || c == ';'
                    || c == '[' || c == ']') {
                Globals.yylval = null;
                return (int) c;
            }

            System.err.println("Error léxico: Carácter no reconocido '" + c + "' en línea " + Globals.numeroLinea);
            return -1;
        }
    }

    public int yylex() {
        int tokenID = siguienteToken(); // Lógica de lectura de caracteres

        // Si no es Fin de Archivo ni error, registramos el token para el informe de salidas
        if (tokenID > 0) {
            registrarTokenDetectado(tokenID); // FUNCION A IMPLEMENTAR, imprime en consola o escribe en archivo de log
        }

        return tokenID; // Le entrega el token a yyparse() de BYACC/J
    }

    /*
    public void procesarArchivo(String rutaEntrada, String rutaSalida) {
        try {
            this.reader = new PushbackReader(new FileReader(rutaEntrada));
            Globals.numeroLinea = 1;
            BufferedWriter writer = new BufferedWriter(new FileWriter(rutaSalida));

            int tokenID;
            while ((tokenID = siguienteToken()) != 0) {
                if (tokenID == -1) {
                    writer.write("ERROR(?) ");
                    continue;
                }

                if (tokenID < 256) {
                    writer.write((char) tokenID);
                    continue;
                }

                switch (tokenID) {
                    case Globals.IDENTIFICADOR:
                        writer.write("ID(" + Globals.yylval.lexema + ") ");
                        break;
                    case Globals.ASIGNACION:
                        writer.write("ASIG(:=)");
                        break;
                    case Globals.CONSTANTE_NUMERICA:
                        writer.write("CTE(" + Globals.yylval.lexema + ")");
                        break;
                    case Globals.PR_IF:
                        writer.write("if");
                        break;
                    case Globals.PR_ELSE:
                        writer.write("else");
                        break;
                    case Globals.PR_END_IF:
                        writer.write("end_if");
                        break;
                    case Globals.PR_BEGIN:
                        writer.write("begin");
                        break;
                    case Globals.PR_END:
                        writer.write("end");
                        break;
                    case Globals.PR_POUT:
                        writer.write("pout");
                        break;
                    case Globals.PR_RET:
                        writer.write("ret");
                        break;
                    case Globals.PR_CLASS:
                        writer.write("class ");
                        break;
                    case Globals.PR_FUNCTION:
                        writer.write("function ");
                        break;
                    case Globals.PR_REPEAT:
                        writer.write("PR(repeat) ");
                        break;
                    case Globals.PR_WHILE:
                        writer.write("while");
                        break;
                    case Globals.TODF:
                        writer.write("todf");
                        break;
                    default:
                        writer.write("ERROR(?) ");
                        break;
                }
            }

            writer.close();
            reader.close();
            System.out.println("Análisis léxico finalizado. Resultados en: " + rutaSalida);

        } catch (IOException e) {
            System.err.println("Error manejando los archivos: " + e.getMessage());
        }
    }
    */
}
