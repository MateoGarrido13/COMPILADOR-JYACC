import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/*
  Tabla de simbolos del compilador. Ademas del rol clasico de TP3 (alta,
  busqueda, resolucion por ambito y volcado por consola) es la fuente de los
  simbolos de .DATA del TP4.

  Convenciones y precondiciones:
    - Clave de identificador: 'nombre:ambito'. ambito es la cadena de contencion
      con ':' de separador; nombreDe()/ambitoDe() la descomponen. Las claves se
      guardan en minusculas por convencion del lexico.
    - Constantes y cadenas NO comparten clave con identificadores: usan
      $const: y $cadena:. Sin esos prefijos, el contenido de una cadena podria
      coincidir con 'if' o con 'x:main' y se sobreescribiria una entrada.
    - porPuntero[0] es null: el puntero de una entrada es su indice real y
      empieza en 1. Los tercetos guardan punteros, no strings, por eso este
      indice es parte del contrato del IR.
    - resolver() sube por la cadena de ambitos pero solo acepta entradas con
      'declarado == true'; buscarIdentificador() no sube y no filtra. La
      distincion importa: un uso previo crea la entrada antes de la declaracion.
    - Efecto colateral de nombreAsmDe(): asigna y cachea el nombre ASM en la
      propia EntradaTabla y por categoria/lexema. Esa cache garantiza unicidad
      global; limpiarCacheNombresAsm() la reinicia entre generaciones.

  Decisiones no obvias:
    - Se prefieren simbolos planos (DD/DQ/DB con nombre sanitizado) antes que
      STRUCT: el layout de objetos se aplana en el nombre calificado y COPY se
      hace campo a campo en orden de declaracion. Evita alineaciones internas y
      mantiene la herencia multiple visible en .DATA.
    - La sanitizacion reserva palabras de MASM y compara en minusculas, porque
      MASM es case-insensitive. Un nombre valido en el fuente puede volverse
      invalido al manglarlo (':' a '_', '$ul' a '_ul_'), de ahi el sufijo _N.
    - DD para ulongint, DQ para doublef, DB para cadenas; no se anade ALIGN 8
      (paquete 6.1): con .386 la alineacion de segmento es DWORD y los qword
      se manipulan por mitades.

  Riesgo conocido: colectarSimbolosData() recorre la tabla en orden de insercion
  (LinkedHashMap). Ese orden es estable y determinista, pero no es el orden de
  declaracion del fuente si las acciones semanticas insertan antes de declarar.
 */
public class TablaSimbolos {

    /*
      Descriptor de simbolo/dato para emision .DATA (TP4 ASM). No modifica
      contenido ni claves de la TS; conserva trazabilidad al origen.
     */
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
    /*
      Constantes y cadenas comparten la tabla con identificadores y palabras
      reservadas, pero no pueden compartir clave: el contenido de una cadena
      puede coincidir con una reservada ("if"), con la clave de una variable
      ("x:main") o con el lexema de una constante ("1$ul"). Se las aísla con
      prefijos que no aparecen en las claves de identificadores.
     */
    private static final String PREFIJO_CONSTANTE = "$const:";
    private static final String PREFIJO_CADENA = "$cadena:";

    private final Map<String, EntradaTabla> tabla;
    // Índice 0 vacío: el puntero de una entrada es su posición y empieza en 1.
    private final List<EntradaTabla> porPuntero;

    // Nombres ASM ya asignados (en minusculas) para unicidad global case-insensitive.
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

    // Única creación de entradas. La misma clave conserva el puntero que recibió acá.
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

    /*
      Da de alta con una clave interna distinta del lexema visible. Se usa para
      constantes y cadenas: el lexema que muestran la TS y el codegen es el
      original, mientras que la clave evita colisiones con otros espacios.
     */
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

    /*
      Devuelve (y la cachea) la forma sanitizada del simbolo para MASM.
      Precondicion: entrada no truncada; con null devuelve null sin efecto.
      Efecto: muta EntradaTabla.setNombreAsm() y las dos caches de unicidad.
      Dos entradas distintas que sanitizen al mismo texto obtienen _1, _2...
      La comparacion es case-insensitive porque MASM no distingue mayusculas.
     */
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
        // quitar prefijos internos de clave TS
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

    /*
      Colecta simbolos candidatos para .DATA: variables/parametros/atributos
      declarados, constantes y cadenas. Orden estable por insercion en TS.
      Los temporales @auxN no viven en TS; se derivan de metadata de tercetos.
     */
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
        // Clases y objetos no se emiten desde aqui: la referencia al objeto se
        // reserva como DD en emitirData() via objetosThis, y sus atributos ya
        // estan materializados en la TS con tipo efectivo (VARIABLE/ATRIBUTO).
        // Devolver null excluye al simbolo de .DATA sin perder su trazabilidad.
        return null;
    }

    private String valorInicialConstante(EntradaTabla e, String tipoEfectivo) {
        if (e == null) return "?";
        if (Tipos.ULONGINT.equals(tipoEfectivo)) {
            java.math.BigInteger v = Tipos.valorEntero(e.lexema);
            return v == null ? "0" : v.toString();
        }
        if (Tipos.DOUBLEF.equals(tipoEfectivo)) {
            // mantener literal fuente compatible lo mas posible; backend puede normalizar
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

    /*
      Declara una entrada de atributo concreta de un objeto. El nombre ya viene
      calificado por el objeto, por ejemplo `a1.x` o `a1.padre.y`, y el ambito
      es el de la declaracion del objeto. Asi cada objeto tiene sus propias
      entradas y no comparte la de otro objeto del mismo tipo.
     */
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

    /*
      Elimina las entradas declaradas o usadas dentro de un ambito y sus
      subambitos. Se usa al rechazar una clase con herencia invalida para no
      dejar atributos, metodos ni parametros a medio declarar.
     */
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

    // Cadenas reconocidas por el lexico (tema 9). El tipo se fija porque coincide con el token.
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

    /*
      Unico punto de alta de constantes numericas. El tipo se recibe siempre
      explicito: lo fija el lexico para las constantes positivas y el sintactico
      para las negativas (con el tipo ya registrado por el lexico).
     */
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
