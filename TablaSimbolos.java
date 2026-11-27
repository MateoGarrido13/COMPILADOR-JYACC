import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TablaSimbolos {

    public static class SimboloDataAsm {
        public final EntradaTabla entrada;
        public final String categoria;
        public final String tipoDato;
        public final String tipoAsm;
        public final String nombreAsm;
        public final String valorInicial;
        public final String origen;

        public SimboloDataAsm(EntradaTabla entrada, String categoria, String tipoDato,
                String tipoAsm, String nombreAsm, String valorInicial, String origen) {
            this.entrada = entrada;
            this.categoria = categoria;
            this.tipoDato = tipoDato;
            this.tipoAsm = tipoAsm;
            this.nombreAsm = nombreAsm;
            this.valorInicial = valorInicial;
            this.origen = origen;
        }
    }

    private static final String PREFIJO_CONSTANTE = "$const:";
    private static final String PREFIJO_CADENA = "$cadena:";

    private final Map<String, EntradaTabla> tabla;

    private final List<EntradaTabla> porPuntero;

    private final Set<String> nombresAsmUsados = new HashSet<>();
    private final Map<String, String> nombreAsmPorClave = new LinkedHashMap<>();

    private static final Set<String> RESERVADAS_ASM = new HashSet<>(Arrays.asList(
            "ax","bx","cx","dx","eax","ebx","ecx","edx","esi","edi","ebp","esp",
            "al","ah","bl","bh","cl","ch","dl","dh",
            "cs","ds","es","ss","fs","gs",
            "st","mm0","mm1","mm2","mm3","mm4","mm5","mm6","mm7",
            "mov","add","sub","mul","imul","div","idiv","cmp","jmp","je","jne","jg","jl","jge","jle","ja","jb","jae","jbe",
            "push","pop","call","ret","proc","endp","end","db","dw","dd","dq","byte","word","dword","qword","ptr",
            "offset","addr","invoke","include","data","code","assume","segment","ends","equ","dup",
            "fld","fstp","fst","fild","fistp","fadd","fsub","fmul","fdiv","fcom","fcomp","ftst","fxam","fstsw","sahf",
            "and","or","xor","not","neg","inc","dec","lea","nop","test",
            "if","else","endif","while","endw","repeat","until",".data",".code",".386",".model","flat","stdcall","option","casemap","none"));

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

    private EntradaTabla altaConClave(String clave, String lexema, int tokenID, int numeroLinea) {
        EntradaTabla entrada = crear(lexema, tokenID, numeroLinea);
        tabla.put(clave, entrada);
        return entrada;
    }

    public EntradaTabla obtenerPorPuntero(int puntero) {
        if (puntero <= 0 || puntero >= porPuntero.size()) {
            return null;
        }
        return porPuntero.get(puntero);
    }

    public Collection<EntradaTabla> entradas() {
        return tabla.values();
    }

    public String nombreAsmDe(EntradaTabla entrada) {
        if (entrada == null) {
            return null;
        }
        if (entrada.getNombreAsm() != null) {
            return entrada.getNombreAsm();
        }
        String lexema = entrada.lexema != null ? entrada.lexema : ("ptr" + entrada.puntero);
        String clave = entrada.uso.name() + ":" + lexema;
        String existente = nombreAsmPorClave.get(clave);
        if (existente != null) {
            entrada.setNombreAsm(existente);
            return existente;
        }
        String base = sanitizarBaseAsm(entrada);
        String candidato = base;
        int sufijo = 1;
        while (!nombreAsmDisponible(candidato)) {
            candidato = base + "_" + sufijo;
            sufijo++;
        }
        nombresAsmUsados.add(candidato.toLowerCase(Locale.ROOT));
        nombreAsmPorClave.put(clave, candidato);
        entrada.setNombreAsm(candidato);
        return candidato;
    }

    private boolean nombreAsmDisponible(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return false;
        }
        String n = nombre.toLowerCase(Locale.ROOT);
        return !nombresAsmUsados.contains(n) && !RESERVADAS_ASM.contains(n);
    }

    private String sanitizarBaseAsm(EntradaTabla entrada) {
        String origen = entrada.lexema == null ? "sym" : entrada.lexema;
        String prefijo = "v_";
        if (entrada.uso == EntradaTabla.Uso.CONSTANTE) {
            prefijo = "c_";
        } else if (entrada.uso == EntradaTabla.Uso.CADENA) {
            prefijo = "s_";
        } else if (entrada.uso == EntradaTabla.Uso.FUNCION || entrada.uso == EntradaTabla.Uso.METODO) {
            prefijo = "fn_";
        } else if (entrada.uso == EntradaTabla.Uso.CLASE || entrada.uso == EntradaTabla.Uso.TIPO
                || entrada.uso == EntradaTabla.Uso.TIPO_ENUMERADO) {
            prefijo = "t_";
        }
        String s = origen;

        if (s.startsWith(PREFIJO_CONSTANTE)) {
            s = s.substring(PREFIJO_CONSTANTE.length());
        } else if (s.startsWith(PREFIJO_CADENA)) {
            s = s.substring(PREFIJO_CADENA.length());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '_') {
                sb.append(ch);
            } else {
                switch (ch) {
                    case ':': sb.append("_"); break;
                    case '.': sb.append("_"); break;
                    case '$': sb.append("_ul_"); break;
                    case '+': sb.append("_pos_"); break;
                    case '-': sb.append("_neg_"); break;
                    case '{': case '}': case ' ': case '"': case '\'': sb.append('_'); break;
                    default: sb.append('_');
                }
            }
        }
        String limpio = sb.toString();
        while (limpio.contains("__")) {
            limpio = limpio.replace("__", "_");
        }
        limpio = limpio.replaceAll("^_+|_+$", "");
        if (limpio.isEmpty()) {
            limpio = "sym" + entrada.puntero;
        }
        if (Character.isDigit(limpio.charAt(0))) {
            limpio = prefijo + limpio;
        } else if (RESERVADAS_ASM.contains(limpio.toLowerCase(Locale.ROOT))) {
            limpio = prefijo + limpio;
        }
        if (limpio.length() > 120) {
            limpio = limpio.substring(0, 100) + "_" + Math.abs(origen.hashCode());
        }
        return limpio;
    }

    public boolean esConstante(EntradaTabla e) {
        return e != null && e.uso == EntradaTabla.Uso.CONSTANTE;
    }

    public boolean esCadena(EntradaTabla e) {
        return e != null && e.uso == EntradaTabla.Uso.CADENA;
    }

    public boolean esSimboloDatoUsuario(EntradaTabla e) {
        if (e == null || !e.declarado) {
            return false;
        }
        return e.uso == EntradaTabla.Uso.VARIABLE
                || e.uso == EntradaTabla.Uso.PARAMETRO
                || e.uso == EntradaTabla.Uso.ATRIBUTO;
    }

    public List<SimboloDataAsm> colectarSimbolosData(Entorno entorno) {
        List<SimboloDataAsm> lista = new ArrayList<>();
        for (EntradaTabla e : tabla.values()) {
            if (e == null) continue;
            if (esConstante(e)) {
                String t = tipoEfectivoDato(e, entorno);
                lista.add(new SimboloDataAsm(e, "constante", t, tipoAsmDe(t, e),
                        nombreAsmDe(e), valorInicialConstante(e, t), e.lexema));
            } else if (esCadena(e)) {
                lista.add(new SimboloDataAsm(e, "cadena", Tipos.CADENA, "DB",
                        nombreAsmDe(e), null, e.lexema));
            } else if (esSimboloDatoUsuario(e)) {
                String t = tipoEfectivoDato(e, entorno);
                String tipoAsm = tipoAsmDe(t, e);
                if (tipoAsm != null) {
                    lista.add(new SimboloDataAsm(e, "dato", t, tipoAsm,
                            nombreAsmDe(e), "?", e.lexema));
                }
            }
        }
        return lista;
    }

    private String tipoEfectivoDato(EntradaTabla e, Entorno entorno) {
        if (e == null) return Tipos.INDEFINIDO;
        String t = e.tipoDato;
        if (entorno != null && t != null) {
            return entorno.tipoEfectivo(t);
        }
        if (e.uso == EntradaTabla.Uso.CONSTANTE) {
            String inf = Tipos.tipoDeConstante(e.lexema);
            return inf == null ? t : inf;
        }
        return t == null ? Tipos.INDEFINIDO : t;
    }

    private String tipoAsmDe(String tipoEfectivo, EntradaTabla e) {
        if (Tipos.DOUBLEF.equals(tipoEfectivo)) return "DQ";
        if (Tipos.ULONGINT.equals(tipoEfectivo)) return "DD";
        if (e != null && e.uso == EntradaTabla.Uso.CADENA) return "DB";

        return null;
    }

    private String valorInicialConstante(EntradaTabla e, String tipoEfectivo) {
        if (e == null) return "?";
        if (Tipos.ULONGINT.equals(tipoEfectivo)) {
            java.math.BigInteger v = Tipos.valorEntero(e.lexema);
            return v == null ? "0" : v.toString();
        }
        if (Tipos.DOUBLEF.equals(tipoEfectivo)) {

            String s = e.lexema;
            if (s == null) return "0.0";
            return s.replace(Tipos.SUFIJO_ULONGINT, "");
        }
        return "?";
    }

    public void limpiarCacheNombresAsm() {
        nombresAsmUsados.clear();
        nombreAsmPorClave.clear();
        for (EntradaTabla e : tabla.values()) {
            if (e != null) e.setNombreAsm(null);
        }
    }

    public EntradaTabla buscarIdentificador(String nombre, String ambito) {
        if (nombre == null || ambito == null) {
            return null;
        }
        return tabla.get(nombre + ":" + ambito);
    }

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

    public boolean estaDeclarado(String nombre, String ambito) {
        EntradaTabla entrada = buscarIdentificador(nombre, ambito);
        return entrada != null && entrada.declarado;
    }

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

    public EntradaTabla declararAtributoDeObjeto(String nombreCalificado, String ambito,
            String tipoDato, int numeroLinea) {
        if (nombreCalificado == null || ambito == null) {
            return null;
        }
        String clave = nombreCalificado + ":" + ambito;
        EntradaTabla entrada = tabla.get(clave);
        if (entrada == null) {
            entrada = alta(clave, Globals.IDENTIFICADOR, numeroLinea);
        }
        entrada.ambito = ambito;
        entrada.tipoDato = tipoDato;
        entrada.uso = EntradaTabla.Uso.ATRIBUTO;
        entrada.declarado = true;
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
        String lexema = entrada.lexema != null ? entrada.lexema : ("ptr" + entrada.puntero);
        nombreAsmPorClave.remove(entrada.uso.name() + ":" + lexema);
        if (entrada.getNombreAsm() != null) {
            nombresAsmUsados.remove(entrada.getNombreAsm().toLowerCase(Locale.ROOT));
            entrada.setNombreAsm(null);
        }
    }

    public void eliminarAmbito(String ambito) {
        if (ambito == null) {
            return;
        }
        List<EntradaTabla> aEliminar = new ArrayList<>();
        for (EntradaTabla entrada : tabla.values()) {
            if (entrada.uso == EntradaTabla.Uso.CONSTANTE
                    || entrada.uso == EntradaTabla.Uso.CADENA
                    || entrada.uso == EntradaTabla.Uso.RESERVADA) {
                continue;
            }
            String ambitoEntrada = entrada.ambito != null
                    ? entrada.ambito
                    : ambitoDe(entrada.lexema);
            if (ambitoEntrada.equals(ambito) || ambitoEntrada.startsWith(ambito + ":")) {
                aEliminar.add(entrada);
            }
        }
        for (EntradaTabla entrada : aEliminar) {
            eliminar(entrada);
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

    public EntradaTabla buscarOInsertarCadena(String cadena) {
        String clave = PREFIJO_CADENA + cadena;
        EntradaTabla entrada = tabla.get(clave);
        if (entrada == null) {
            entrada = altaConClave(clave, cadena, Globals.CADENA, Globals.numeroLinea);
        }
        entrada.tipoDato = Tipos.CADENA;
        entrada.uso = EntradaTabla.Uso.CADENA;
        entrada.declarado = true;
        return entrada;
    }

    public EntradaTabla insertarConstante(String lexema, String tipoDato, int numeroLinea) {
        String clave = PREFIJO_CONSTANTE + lexema;
        EntradaTabla entrada = tabla.get(clave);
        if (entrada == null) {
            entrada = altaConClave(clave, lexema, Globals.CONSTANTE_NUMERICA, numeroLinea);
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
            if (entrada.uso == EntradaTabla.Uso.TIPO_ENUMERADO) {
                texto.append("Ambito: ").append(entrada.ambito).append("\n");
            }
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
