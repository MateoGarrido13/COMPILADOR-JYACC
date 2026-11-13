import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TablaSimbolos {
    private final Map<String, EntradaTabla> tabla;
    // Índice 0 vacío: el puntero de una entrada es su posición y empieza en 1.
    private final List<EntradaTabla> porPuntero;

    // inicializo tabla y ya cargo palabras reservadas, por convencion de nombres, las keys son en minusculas
    public TablaSimbolos() {
        tabla = new LinkedHashMap<>();
        porPuntero = new ArrayList<>();
        porPuntero.add(null);
        altaReservada("if", Globals.PR_IF);
        altaReservada("else", Globals.PR_ELSE);
        altaReservada("end_if", Globals.PR_END_IF);
        altaReservada("begin", Globals.PR_BEGIN);
        altaReservada("end", Globals.PR_END);
        altaReservada("pout", Globals.PR_POUT);
        altaReservada("ret", Globals.PR_RET);
        altaReservada("class", Globals.PR_CLASS);
        altaReservada("function", Globals.PR_FUNCTION);
        altaReservada("repeat", Globals.PR_REPEAT);
        altaReservada("while", Globals.PR_WHILE);
        altaReservada("todf", Globals.TODF);
        altaReservada("typedef", Globals.PR_TYPEDEF);
        altaReservada("extends", Globals.PR_EXTENDS);
        altaReservada("ulongint", Globals.PR_ULONGINT);
        altaReservada("doublef", Globals.PR_DOUBLEF);
    }

    private void altaReservada(String lexema, int tokenID) {
        EntradaTabla entrada = alta(lexema, tokenID, 0);
        entrada.uso = EntradaTabla.Uso.RESERVADA;
    }

    // Única creación de entradas. El mismo lexema conserva el puntero que recibió acá.
    private EntradaTabla crear(String lexema, int tokenID, int numeroLinea) {
        EntradaTabla entrada = new EntradaTabla(lexema, tokenID, numeroLinea);
        entrada.puntero = porPuntero.size();
        porPuntero.add(entrada);
        return entrada;
    }

    private EntradaTabla alta(String lexema, int tokenID, int numeroLinea) {
        EntradaTabla entrada = crear(lexema, tokenID, numeroLinea);
        tabla.put(lexema, entrada);
        return entrada;
    }

    public EntradaTabla obtenerPorPuntero(int puntero) {
        if (puntero <= 0 || puntero >= porPuntero.size()) {
            return null;
        }
        return porPuntero.get(puntero);
    }

    public EntradaTabla buscarIdentificador(String nombre, String ambito) {
        if (nombre == null || ambito == null) {
            return null;
        }
        return tabla.get(nombre + ":" + ambito);
    }

    /*
      Busca una declaracion desde el ambito actual hacia sus padres. Una entrada creada
      por un uso previo no cuenta: solo se acepta si esta marcada como declarada.
      Ejemplo: desde `main:f:g`, prueba `x:main:f:g`, `x:main:f`, `x:main`.
     */
    public EntradaTabla resolver(String nombre, String ambito) {
        if (nombre == null || ambito == null) {
            return null;
        }
        String actual = ambito;
        while (true) {
            EntradaTabla entrada = buscarIdentificador(nombre, actual);
            if (entrada != null && entrada.declarado) {
                return entrada;
            }
            int corte = actual.lastIndexOf(':');
            if (corte < 0) {
                return null;
            }
            actual = actual.substring(0, corte);
        }
    }

    // Indica si la clave exacta corresponde a una declaracion. No sube de ambito.
    public boolean estaDeclarado(String nombre, String ambito) {
        EntradaTabla entrada = buscarIdentificador(nombre, ambito);
        return entrada != null && entrada.declarado;
    }

    /*
      Da de alta o confirma una declaracion. Si ya habia un placeholder en la misma clave
      (creado por un uso anterior), se reutiliza esa entrada para conservar el puntero.
      Devuelve null si la clave ya pertenece a otra declaracion.
     */
    public EntradaTabla declarar(EntradaTabla entrada, String nombre, String ambito,
            EntradaTabla.Uso uso) {
        if (entrada == null || nombre == null || ambito == null) {
            return null;
        }
        String clave = nombre + ":" + ambito;
        EntradaTabla existente = tabla.get(clave);
        if (existente != null && existente != entrada && existente.declarado) {
            return null;
        }
        if (existente != null && existente != entrada) {
            entrada = existente;
        }
        entrada.ambito = ambito;
        entrada.uso = uso;
        entrada.declarado = true;
        if (!clave.equals(entrada.lexema)) {
            renombrar(entrada, clave);
        }
        return entrada;
    }

    public EntradaTabla buscarPalabraReservada(String lexema) {
        EntradaTabla entrada = tabla.get(lexema.toLowerCase());
        if (entrada != null && esPalabraReservada(entrada.tokenID)) {
            return entrada;
        }
        return null;
    }

    public EntradaTabla buscarOInsertarIdentificador(String lexema) {
        String clave = lexema + ":" + Globals.AMBITO_ACTUAL;
        EntradaTabla entrada = tabla.get(clave);
        if (entrada != null) {
            return entrada;
        }
        return alta(clave, Globals.IDENTIFICADOR, Globals.numeroLinea);
    }

    public static String nombreDe(String lexema) {
        if (lexema == null) {
            return null;
        }
        int corte = lexema.indexOf(':');
        if (corte < 0) {
            return lexema;
        }
        return lexema.substring(0, corte);
    }

    public static String ambitoDe(String lexema) {
        if (lexema == null) {
            return "";
        }
        int corte = lexema.indexOf(':');
        if (corte < 0 || corte == lexema.length() - 1) {
            return "";
        }
        return lexema.substring(corte + 1);
    }

    public void renombrar(EntradaTabla entrada, String nuevoLexema) {
        if (entrada == null || nuevoLexema == null || nuevoLexema.equals(entrada.lexema)) {
            return;
        }
        tabla.remove(entrada.lexema);
        entrada.lexema = nuevoLexema;
        tabla.put(nuevoLexema, entrada);
    }

    public void eliminar(EntradaTabla entrada) {
        if (entrada == null) {
            return;
        }
        tabla.remove(entrada.lexema);
        if (entrada.puntero > 0 && entrada.puntero < porPuntero.size()) {
            porPuntero.set(entrada.puntero, null);
        }
    }

    public List<EntradaTabla> conAmbito() {
        List<EntradaTabla> copia = new ArrayList<>();
        for (EntradaTabla entrada : tabla.values()) {
            if (entrada.ambito != null) {
                copia.add(entrada);
            }
        }
        return copia;
    }

    private boolean esPalabraReservada(int tokenID) {
        return tokenID >= 400;
    }

    // Inserta el lexema; el tipo y el rango los asigna la semantica. 
    public EntradaTabla buscarOInsertarConstante(String lexema, int tokenID) {
        EntradaTabla entrada = tabla.get(lexema);
        if (entrada == null) {
            entrada = alta(lexema, tokenID, Globals.numeroLinea);
        }
        entrada.uso = EntradaTabla.Uso.CONSTANTE;
        entrada.declarado = true;
        return entrada;
    }

    // Cadenas reconocidas por el lexico (tema 9). El tipo se fija porque coincide con el token.
    public EntradaTabla buscarOInsertarCadena(String cadena) {
        EntradaTabla entrada = tabla.get(cadena);
        if (entrada == null) {
            entrada = alta(cadena, Globals.CADENA, Globals.numeroLinea);
        }
        entrada.tipoDato = Tipos.CADENA;
        entrada.uso = EntradaTabla.Uso.CADENA;
        entrada.declarado = true;
        return entrada;
    }

     // Inserta o actualiza una constante. La usa el Analisis Sintactico al detectar constantes negativas
    public EntradaTabla insertarConstante(String lexema, String tipoDato, int numeroLinea) {
        EntradaTabla entrada = tabla.get(lexema);
        if (entrada == null) {
            entrada = alta(lexema, Globals.CONSTANTE_NUMERICA, numeroLinea);
        }
        entrada.tipoDato = tipoDato;
        entrada.uso = EntradaTabla.Uso.CONSTANTE;
        entrada.declarado = true;
        return entrada;
    }

    public String contenido() {
        StringBuilder texto = new StringBuilder("Contenido de la tabla de simbolos:\n");
        for (EntradaTabla entrada : tabla.values()) {
            texto.append(entrada.lexema).append(" -> ").append(entrada.tokenID).append("\n");
            texto.append("Puntero: ").append(entrada.puntero).append("\n");
            texto.append("Numero de linea: ").append(entrada.numeroLinea).append("\n");
            texto.append("Tipo de dato: ").append(entrada.tipoDato).append("\n");
            texto.append("Uso: ").append(entrada.uso.getDescripcion()).append("\n");
            texto.append("Declarado: ").append(entrada.declarado ? "si" : "no").append("\n");
            texto.append("Direccion de memoria: ").append(entrada.direccionMemoria).append("\n");
            texto.append("--------------------------------\n");
        }
        return texto.toString();
    }

    public void imprimirTabla() {
        System.out.print(contenido());
    }
}
