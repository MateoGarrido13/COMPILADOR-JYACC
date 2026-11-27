import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeneradorAssembler {

    private final List<Terceto> tercetos;
    private final TablaSimbolos tablaSimbolos;
    private final Entorno entorno;
    private final Map<Integer, ResultadoTerceto> resultados;
    private final Map<Integer, String> auxPorTerceto = new HashMap<>();
    private final StringBuilder salida = new StringBuilder();

    private static final String FPU_STATUS = "@fpu_status";
    private boolean usaFpuStatus = false;

    private static final String FMT_ULONGINT = "fmt_ulongint";
    private static final String FMT_DOUBLEF = "fmt_doublef";
    private static final String FMT_CADENA = "fmt_cadena";
    private boolean usaFmtUlongint = false;
    private boolean usaFmtDoublef = false;
    private boolean usaFmtCadena = false;

    private static final String MSG_OVERFLOW_SUMA = "msg_overflow_suma";
    private static final String MSG_OVERFLOW_FLOAT = "msg_overflow_float";
    private static final String MSG_NEGATIVO_RESTA = "msg_negativo_resta";
    private static final String MSG_ENUM_INVALIDO = "msg_enum_invalido";
    private boolean usaMsgOverflowSuma = false;
    private boolean usaMsgOverflowFloat = false;
    private boolean usaMsgNegativoResta = false;
    private boolean usaMsgEnumInvalido = false;

    private int contadorEnumChequeo = 0;

    private static class ParPendiente {
        final EntradaTabla formal;
        final Object real;

        ParPendiente(EntradaTabla formal, Object real) {
            this.formal = formal;
            this.real = real;
        }
    }

    private final Map<Integer, Entorno.FuncionDeclarada> declaracionPorTerceto = new HashMap<>();
    private final Map<String, Entorno.FuncionDeclarada> declaracionPorClave = new HashMap<>();
    private final Map<Entorno.FuncionDeclarada, String> etiquetaPorDeclaracion = new HashMap<>();
    private final Map<Entorno.FuncionDeclarada, Integer> framePorDeclaracion = new HashMap<>();

    private final Map<EntradaTabla, String> slotStackPorSimbolo = new HashMap<>();

    private final List<ParPendiente> parametrosPendientes = new ArrayList<>();

    private final Set<String> objetosThis = new LinkedHashSet<>();
    private Entorno.FuncionDeclarada funcionActual = null;

    private final Map<String, String> cadenasErrorPorTexto = new HashMap<>();
    private int contadorCadenasError = 0;

    public GeneradorAssembler(List<Terceto> tercetos,
            TablaSimbolos tablaSimbolos,
            Entorno entorno,
            Map<Integer, ResultadoTerceto> resultados) {
        this.tercetos = tercetos != null ? tercetos : Collections.emptyList();
        this.tablaSimbolos = tablaSimbolos;
        this.entorno = entorno;
        this.resultados = resultados != null ? resultados : Collections.emptyMap();

        for (ResultadoTerceto r : resultados.values()) {
            if (r.produceValor() && r.tipoAsmDato() != null) {
                auxPorTerceto.put(r.getIndice(), r.nombreAuxiliar());
            }
        }
    }

    public String generar() {
        salida.setLength(0);

        contadorEnumChequeo = 0;
        cadenasErrorPorTexto.clear();
        contadorCadenasError = 0;
        auxPorTerceto.clear();
        declaracionPorTerceto.clear();
        declaracionPorClave.clear();
        etiquetaPorDeclaracion.clear();
        framePorDeclaracion.clear();
        slotStackPorSimbolo.clear();
        parametrosPendientes.clear();
        objetosThis.clear();
        funcionActual = null;
        for (ResultadoTerceto r : resultados.values()) {
            if (r.produceValor() && r.tipoAsmDato() != null) {
                auxPorTerceto.put(r.getIndice(), r.nombreAuxiliar());
            }
        }
        prepararDeclaraciones();
        prepararStackFrames();
        prepararObjetosThis();
        usaFpuStatus = calcularUsoFpuStatus();
        calcularFormatosPout();
        calcularChequeosRuntime();
        emitirEncabezado();
        emitirData();
        emitirCodigo();
        return salida.toString();
    }

    public void escribirArchivo(String ruta) throws IOException {
        Path path = Paths.get(ruta);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8))) {

            String contenido = generar().replace("\r\n", "\n").replace("\n", "\r\n");
            writer.write(contenido);
        }
    }

    private void emitirEncabezado() {
        salida.append(".386\n");
        salida.append(".model flat, stdcall\n");
        salida.append("option casemap:none\n");
        salida.append("include \\masm32\\include\\masm32rt.inc\n");
        salida.append("\n");
    }

    private void emitirData() {
        salida.append(".data\n");
        Set<String> nombresEnData = emitirSimbolosDeTablaDeSimbolos();
        emitirReceptoresThis(nombresEnData);
        emitirTemporalesAux();
        emitirMarcadorEstadoFPU();
        emitirFormatosPout();
        emitirMensajesErrorRuntime();
        emitirCadenasErrorGeneradas();
        salida.append("\n");
    }

    private Set<String> emitirSimbolosDeTablaDeSimbolos() {
        List<TablaSimbolos.SimboloDataAsm> simbolos = tablaSimbolos.colectarSimbolosData(entorno);
        Set<String> nombresEnData = new HashSet<>();
        for (TablaSimbolos.SimboloDataAsm s : simbolos) {

            if ("dato".equals(s.categoria) && esSimboloDeStack(s.entrada)) {
                continue;
            }
            salida.append("    ").append(s.nombreAsm).append(" ")
                  .append(s.tipoAsm).append(" ")
                  .append(valorInicial(s)).append("\n");
            nombresEnData.add(s.nombreAsm);
        }
        return nombresEnData;
    }

    private void emitirReceptoresThis(Set<String> nombresEnData) {

        for (String nombre : objetosThis) {
            if (!nombresEnData.contains(nombre)) {
                salida.append("    ").append(nombre).append(" DD ?")
                      .append(" ; this (layout en 4d)\n");
            }
        }
    }

    private void emitirTemporalesAux() {

        for (ResultadoTerceto r : resultados.values()) {
            if (r.produceValor() && r.tipoAsmDato() != null) {
                salida.append("    ").append(r.nombreAuxiliar()).append(" ")
                      .append(r.tipoAsmDato()).append(" ?\n");
            }
        }
    }

    private void emitirMarcadorEstadoFPU() {

        if (usaFpuStatus) {
            salida.append("    ").append(FPU_STATUS).append(" DW ?\n");
        }
    }

    private void emitirFormatosPout() {

        if (usaFmtUlongint) {
            salida.append("    ").append(FMT_ULONGINT).append(" DB \"%u\",0\n");
        }
        if (usaFmtDoublef) {
            salida.append("    ").append(FMT_DOUBLEF).append(" DB \"%f\",0\n");
        }
        if (usaFmtCadena) {
            salida.append("    ").append(FMT_CADENA).append(" DB \"%s\",0\n");
        }
    }

    private void emitirMensajesErrorRuntime() {

        if (usaMsgOverflowSuma) {
            salida.append("    ").append(MSG_OVERFLOW_SUMA)
                  .append(" DB \"Error: overflow en suma de ulongint\",0\n");
        }
        if (usaMsgOverflowFloat) {
            salida.append("    ").append(MSG_OVERFLOW_FLOAT)
                  .append(" DB \"Error: overflow en producto de doublef\",0\n");
        }
        if (usaMsgNegativoResta) {
            salida.append("    ").append(MSG_NEGATIVO_RESTA)
                  .append(" DB \"Error: resultado negativo en resta de ulongint\",0\n");
        }
        if (usaMsgEnumInvalido) {
            salida.append("    ").append(MSG_ENUM_INVALIDO)
                  .append(" DB \"Error: asignacion invalida a variable de tipo enumeracion\",0\n");
        }
    }

    private void emitirCadenasErrorGeneradas() {
        if (!cadenasErrorPorTexto.isEmpty()) {
            salida.append("\n");
            for (Map.Entry<String, String> entrada : cadenasErrorPorTexto.entrySet()) {
                salida.append("    ").append(entrada.getKey()).append(" DB \"")
                      .append(cadenaAsm(entrada.getValue())).append("\",0\n");
            }
        }
    }

    private String valorInicial(TablaSimbolos.SimboloDataAsm s) {
        if (s.valorInicial != null) {
            return s.valorInicial;
        }
        if ("cadena".equals(s.categoria)) {
            return cadenaAsm(s.origen);
        }
        return "?";
    }

    private String cadenaAsm(String contenido) {
        if (contenido == null) {
            return "0";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < contenido.length(); i++) {
            char c = contenido.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        sb.append("\",0");
        return sb.toString();
    }

    private void emitirCodigo() {
        salida.append(".code\n");
        salida.append("start:\n");
        salida.append("    jmp main_inicio\n");

        boolean dentroSubrutina = false;
        boolean mainInicioEmitido = false;

        for (int i = 0; i < tercetos.size(); i++) {
            Terceto t = tercetos.get(i);
            int numero = i + 1;
            String op = t.getOperador();
            Object op1 = t.getOp1();
            Object op2 = t.getOp2();

            ResultadoTerceto r = resultados.get(numero);
            boolean produceValor = r != null && r.produceValor();
            String tipo = r != null ? r.getTipo() : Tipos.INDEFINIDO;

            if (!dentroSubrutina && !mainInicioEmitido
                    && !"PROGRAMA".equals(op) && !"FIN_PROGRAMA".equals(op)
                    && !"CLASE".equals(op) && !"FIN_CLASE".equals(op)
                    && !"FUNCION".equals(op) && !"METODO".equals(op)
                    && !"FIN_FUNCION".equals(op) && !"FIN_METODO".equals(op)) {
                salida.append("main_inicio:\n");
                mainInicioEmitido = true;
            }

            switch (op) {
                case "PROGRAMA":
                case "FIN_PROGRAMA":

                    break;
                case "+":
                case "-":
                case "*":
                case "/":
                    emitirAritmetica(op, op1, op2, numero, tipo);
                    break;
                case "todf":
                    emitirTodf(op1, numero, tipo);
                    break;
                case ":=":
                    if (produceValor) {

                        emitirAsignacionExpresion(op1, op2, numero, tipo);
                    } else {

                        emitirAsignacionSentencia(op1, op2);
                    }
                    break;
                case "<":
                case ">":
                case "<=":
                case ">=":
                case "==":
                case "!=":
                    emitirComparacion(op, op1, op2, numero);
                    break;
                case "BF":
                    emitirBF(op1, op2);
                    break;
                case "BI":
                    emitirBI(op1);
                    break;
                case "pout":
                    emitirPout(op1);
                    break;
                case "FUNCION":
                case "METODO":
                    dentroSubrutina = true;
                    emitirPrologo(numero, op, op1);
                    break;
                case "FIN_FUNCION":
                case "FIN_METODO":
                    dentroSubrutina = false;
                    emitirFinFuncion(numero);
                    break;
                case "ret":
                    emitirRet(op1);
                    break;
                case "PAR":
                    emitirPar(op1, op2);
                    break;
                case "CALL":
                    emitirLlamada(textoDeOperando(op1), op2, numero, false, null);
                    break;
                case "CALLMET":
                    emitirLlamada(textoDeOperando(op2), null, numero, true, op1);
                    break;
                case "COPY":
                    emitirCopiar(op1, op2, numero);
                    break;
                case "CLASE":
                case "FIN_CLASE":

                    break;
                default:

                    if (esEtiqueta(op)) {
                        emitirEtiqueta(op);
                    } else {
                        salida.append("    ; operador no reconocido: ")
                              .append(t.visualizar()).append("\n");
                    }
                    break;
            }
        }

        if (!mainInicioEmitido) {
            salida.append("main_inicio:\n");
        }

        boolean hayChequeosRuntime = usaMsgOverflowSuma || usaMsgOverflowFloat
                || usaMsgNegativoResta || usaMsgEnumInvalido;
        if (hayChequeosRuntime) {
            salida.append("    jmp fin_normal\n");
        }
        if (usaMsgOverflowSuma) {
            salida.append("error_overflow_suma:\n");
            salida.append("    invoke crt_printf, offset ").append(MSG_OVERFLOW_SUMA).append("\n");
            salida.append("    invoke ExitProcess, 1\n");
        }
        if (usaMsgOverflowFloat) {
            salida.append("error_overflow_float:\n");
            salida.append("    invoke crt_printf, offset ").append(MSG_OVERFLOW_FLOAT).append("\n");
            salida.append("    invoke ExitProcess, 2\n");
        }
        if (usaMsgNegativoResta) {
            salida.append("error_negativo_resta:\n");
            salida.append("    invoke crt_printf, offset ").append(MSG_NEGATIVO_RESTA).append("\n");
            salida.append("    invoke ExitProcess, 3\n");
        }
        if (usaMsgEnumInvalido) {
            salida.append("error_enum_invalido:\n");
            salida.append("    invoke crt_printf, offset ").append(MSG_ENUM_INVALIDO).append("\n");
            salida.append("    invoke ExitProcess, 4\n");
        }

        if (hayChequeosRuntime) {
            salida.append("fin_normal:\n");
        }

        salida.append("    invoke ExitProcess, 0\n");
        salida.append("end start\n");
    }

    private String operandoAsm(Object op) {
        if (op == null) {
            return "--";
        }
        if (op instanceof EntradaTabla) {
            EntradaTabla entrada = (EntradaTabla) op;

            if (funcionActual != null) {
                String slot = slotStackPorSimbolo.get(entrada);
                if (slot != null) {
                    return slot;
                }
            }
            return tablaSimbolos.nombreAsmDe(entrada);
        }
        if (op instanceof Integer) {
            int n = (Integer) op;
            String aux = auxPorTerceto.get(n);
            if (aux != null) {
                return aux;
            }
            return "[aux" + n + "]";
        }
        if (op instanceof ReferenciaAtributo) {
            ReferenciaAtributo ref = (ReferenciaAtributo) op;

            if (ref.objeto != null && ref.objeto.ambito != null) {
                EntradaTabla eObjeto = tablaSimbolos.buscarIdentificador(
                        ref.texto, ref.objeto.ambito);
                if (eObjeto != null) {
                    String nombre = tablaSimbolos.nombreAsmDe(eObjeto);
                    if (nombre != null) {
                        return nombre;
                    }
                }
            }

            if (ref.atributo != null) {
                String nombre = tablaSimbolos.nombreAsmDe(ref.atributo);
                if (nombre != null) {
                    return nombre;
                }
            }
            return ref.texto;
        }
        if (op instanceof String) {
            return (String) op;
        }
        return "?";
    }

    private boolean esFlotante(String tipo) {
        return Tipos.DOUBLEF.equals(tipo);
    }

    private void emitirAritmetica(String operador, Object op1, Object op2,
            int numero, String tipoResultado) {
        String o1 = operandoAsm(op1);
        String o2 = operandoAsm(op2);
        String destino = auxPorTerceto.get(numero);

        if (destino == null) {
            salida.append("    ; ").append(new Terceto(operador, op1, op2).visualizar())
                  .append("  (sin auxiliar)\n");
            return;
        }

        boolean flotante = esFlotante(tipoResultado);

        if (flotante) {

            salida.append("    fld ").append(o1).append("\n");
            switch (operador) {
                case "+": salida.append("    fadd ").append(o2).append("\n"); break;
                case "-": salida.append("    fsub ").append(o2).append("\n"); break;
                case "*": salida.append("    fmul ").append(o2).append("\n"); break;
                case "/": salida.append("    fdiv ").append(o2).append("\n"); break;
            }
            salida.append("    fstp ").append(destino).append("\n");

            if ("*".equals(operador) && usaMsgOverflowFloat) {
                salida.append("    fld ").append(destino).append("\n");
                salida.append("    fxam\n");
                salida.append("    fstsw ax\n");

                salida.append("    fstp st(0)\n");
                salida.append("    sahf\n");
                salida.append("    jc error_overflow_float\n");
            }
        } else {

            salida.append("    mov eax, ").append(o1).append("\n");
            switch (operador) {
                case "+":
                    salida.append("    add eax, ").append(o2).append("\n");

                    if (usaMsgOverflowSuma) {
                        salida.append("    jc error_overflow_suma\n");
                    }
                    break;
                case "-":
                    salida.append("    sub eax, ").append(o2).append("\n");

                    if (usaMsgNegativoResta) {
                        salida.append("    jb error_negativo_resta\n");
                    }
                    break;
                case "*":
                    salida.append("    imul eax, ").append(o2).append("\n");
                    break;
                case "/":
                    salida.append("    div ").append(o2).append("\n");
                    break;
            }
            salida.append("    mov ").append(destino).append(", eax\n");
        }
    }

    private void emitirTodf(Object op1, int numero, String tipoResultado) {
        String origen = operandoAsm(op1);
        String destino = auxPorTerceto.get(numero);

        if (destino == null || "--".equals(origen)) {
            salida.append("    ; todf ").append(origen).append("  (sin auxiliar)\n");
            return;
        }

        salida.append("    fild ").append(origen).append("\n");
        salida.append("    fstp ").append(destino).append("\n");
    }

    private void emitirCopiaPorTipo(String tipo, String origen, String destino) {
        if (esFlotante(tipo)) {

            salida.append("    fld ").append(origen).append("\n");
            salida.append("    fstp ").append(destino).append("\n");
        } else {

            salida.append("    mov eax, ").append(origen).append("\n");
            salida.append("    mov ").append(destino).append(", eax\n");
        }
    }

    private String tipoDeDestinoAsignacion(Object destino) {
        if (destino instanceof EntradaTabla) {
            return ((EntradaTabla) destino).tipoDato;
        }
        if (destino instanceof ReferenciaAtributo) {
            return ((ReferenciaAtributo) destino).getTipo();
        }
        return null;
    }

    private Entorno.TipoEnumerado tipoEnumeradoChequeable(String tipo) {
        if (entorno == null || tipo == null) {
            return null;
        }
        Entorno.TipoEnumerado enumTipo = entorno.getTipoEnumerado(tipo);
        if (enumTipo == null) {
            return null;
        }
        return Tipos.ULONGINT.equals(enumTipo.tipoBase)
                || Tipos.DOUBLEF.equals(enumTipo.tipoBase) ? enumTipo : null;
    }

    private boolean emitirChequeoEnum(String dst, Object destino, String src) {
        Entorno.TipoEnumerado enumTipo =
                tipoEnumeradoChequeable(tipoDeDestinoAsignacion(destino));
        if (!usaMsgEnumInvalido || enumTipo == null) {
            return false;
        }
        String etiquetaOk = "enum_valido_" + contadorEnumChequeo++;
        boolean flotante = Tipos.DOUBLEF.equals(enumTipo.tipoBase);
        if (flotante) {
            emitirChequeoEnumDoublef(src, enumTipo, etiquetaOk);
        } else {
            emitirChequeoEnumEntero(src, enumTipo, etiquetaOk);
        }
        salida.append(etiquetaOk).append(":\n");
        emitirCopiaPorTipo(flotante ? Tipos.DOUBLEF : Tipos.ULONGINT, src, dst);
        return true;
    }

    private void emitirChequeoEnumEntero(String src, Entorno.TipoEnumerado enumTipo,
            String etiquetaOk) {
        salida.append("    mov eax, ").append(src).append("\n");
        for (String valorLexema : enumTipo.valores) {
            BigInteger valor = Tipos.valorEntero(valorLexema);
            if (valor != null) {
                salida.append("    cmp eax, ").append(valor.intValue()).append("\n");
                salida.append("    je ").append(etiquetaOk).append("\n");
            }
        }
        salida.append("    jmp error_enum_invalido\n");
    }

    private void emitirChequeoEnumDoublef(String src,
            Entorno.TipoEnumerado enumTipo, String etiquetaOk) {
        for (String valorLexema : enumTipo.valores) {
            String valorAsm = operandoConstanteEnum(valorLexema);
            if (valorAsm == null) {
                continue;
            }
            emitirComparacionFpu(src, valorAsm);
            salida.append("    jp error_enum_invalido\n");
            salida.append("    jz ").append(etiquetaOk).append("\n");
        }
        salida.append("    jmp error_enum_invalido\n");
    }

    private String operandoConstanteEnum(String lexema) {
        if (lexema == null) {
            return null;
        }
        for (EntradaTabla e : tablaSimbolos.entradas()) {
            if (e.uso == EntradaTabla.Uso.CONSTANTE && lexema.equals(e.lexema)) {
                return tablaSimbolos.nombreAsmDe(e);
            }
        }
        return lexema;
    }

    private void emitirAsignacionExpresion(Object destino, Object origen,
            int numero, String tipoResultado) {
        String dst = operandoAsm(destino);
        String src = operandoAsm(origen);

        if (dst.equals("--") || src.equals("--")) {
            salida.append("    ; := expr  (operandos invalidos)\n");
            return;
        }

        if (!emitirChequeoEnum(dst, destino, src)) {
            emitirCopiaPorTipo(tipoResultado, src, dst);
        }

        String resultado = auxPorTerceto.get(numero);
        if (resultado != null) {
            emitirCopiaPorTipo(tipoResultado, dst, resultado);
        }
    }

    private void emitirAsignacionSentencia(Object destino, Object origen) {
        String dst = operandoAsm(destino);
        String src = operandoAsm(origen);

        if (dst.equals("--") || src.equals("--")) {
            salida.append("    ; := sentencia  (operandos invalidos)\n");
            return;
        }

        if (emitirChequeoEnum(dst, destino, src)) {
            return;
        }

        boolean flotante = esFlotante(tipoDeDestinoAsignacion(destino));
        emitirCopiaPorTipo(flotante ? Tipos.DOUBLEF : Tipos.ULONGINT, src, dst);
    }

    private boolean esEtiqueta(String op) {
        return op != null && op.startsWith("label_");
    }

    private boolean esComparador(String op) {
        return "<".equals(op) || ">".equals(op) || "<=".equals(op)
                || ">=".equals(op) || "==".equals(op) || "!=".equals(op);
    }

    private String tipoDeOperando(Object op) {
        if (op instanceof Integer) {
            ResultadoTerceto r = resultados.get((Integer) op);
            return r == null ? Tipos.INDEFINIDO : r.getTipo();
        }
        if (op instanceof EntradaTabla) {
            String t = ((EntradaTabla) op).tipoDato;
            if (t == null) {
                return Tipos.INDEFINIDO;
            }
            return entorno != null ? entorno.tipoEfectivo(t) : t;
        }
        if (op instanceof ReferenciaAtributo) {
            String t = ((ReferenciaAtributo) op).getTipo();
            return entorno != null ? entorno.tipoEfectivo(t) : t;
        }
        if (op instanceof String) {
            String t = Tipos.tipoDeConstante((String) op);
            return t == null ? Tipos.INDEFINIDO : t;
        }
        return Tipos.INDEFINIDO;
    }

    private boolean esFlotanteOperando(Object op) {
        return Tipos.DOUBLEF.equals(tipoDeOperando(op));
    }

    private Terceto tercetoPorReferencia(Object op) {
        if (!(op instanceof Integer)) {
            return null;
        }
        int n = (Integer) op;
        if (n < 1 || n > tercetos.size()) {
            return null;
        }
        return tercetos.get(n - 1);
    }

    private boolean calcularUsoFpuStatus() {
        for (Terceto t : tercetos) {
            String op = t.getOperador();
            if (esComparador(op)) {
                if (esFlotanteOperando(t.getOp1()) || esFlotanteOperando(t.getOp2())) {
                    return true;
                }
            } else if ("BF".equals(op)) {
                Terceto cmp = tercetoPorReferencia(t.getOp1());
                if (cmp != null && esComparador(cmp.getOperador())
                        && (esFlotanteOperando(cmp.getOp1()) || esFlotanteOperando(cmp.getOp2()))) {
                    return true;
                }
            } else if (":=".equals(op)) {

                Entorno.TipoEnumerado enumTipo =
                        tipoEnumeradoChequeable(tipoDeDestinoAsignacion(t.getOp1()));
                if (enumTipo != null && Tipos.DOUBLEF.equals(enumTipo.tipoBase)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String setccVerdadero(String comparador) {
        switch (comparador) {
            case "<":  return "setb";
            case "<=": return "setbe";
            case ">":  return "seta";
            case ">=": return "setae";
            case "==": return "sete";
            case "!=": return "setne";
            default:   return null;
        }
    }

    private String jccFalso(String comparador) {
        switch (comparador) {
            case "<":  return "jae";
            case "<=": return "ja";
            case ">":  return "jbe";
            case ">=": return "jb";
            case "==": return "jne";
            case "!=": return "je";
            default:   return null;
        }
    }

    private void emitirComparacionFpu(String o1, String o2) {
        salida.append("    fld ").append(o1).append("\n");
        salida.append("    fcomp ").append(o2).append("\n");
        salida.append("    fstsw ").append(FPU_STATUS).append("\n");
        salida.append("    mov ax, ").append(FPU_STATUS).append("\n");
        salida.append("    sahf\n");
    }

    private void emitirComparacion(String operador, Object op1, Object op2, int numero) {
        String destino = auxPorTerceto.get(numero);
        String o1 = operandoAsm(op1);
        String o2 = operandoAsm(op2);
        String setcc = setccVerdadero(operador);

        if (destino == null || setcc == null || "--".equals(o1) || "--".equals(o2)) {
            salida.append("    ; ").append(operador)
                  .append("  (sin auxiliar u operandos)\n");
            return;
        }

        if (esFlotanteOperando(op1) || esFlotanteOperando(op2)) {
            emitirComparacionFpu(o1, o2);
        } else {
            salida.append("    mov eax, ").append(o1).append("\n");
            salida.append("    cmp eax, ").append(o2).append("\n");
        }
        salida.append("    ").append(setcc).append(" al\n");
        salida.append("    movzx eax, al\n");
        salida.append("    mov ").append(destino).append(", eax\n");
    }

    private void emitirBF(Object condicion, Object destino) {
        String etiqueta = destino instanceof String ? (String) destino : null;
        if (etiqueta == null) {
            salida.append("    ; BF sin destino resuelto\n");
            return;
        }

        Terceto cmp = tercetoPorReferencia(condicion);
        if (cmp != null && esComparador(cmp.getOperador())) {
            String o1 = operandoAsm(cmp.getOp1());
            String o2 = operandoAsm(cmp.getOp2());
            String jcc = jccFalso(cmp.getOperador());
            if (jcc != null && !"--".equals(o1) && !"--".equals(o2)) {
                if (esFlotanteOperando(cmp.getOp1()) || esFlotanteOperando(cmp.getOp2())) {
                    emitirComparacionFpu(o1, o2);
                } else {
                    salida.append("    mov eax, ").append(o1).append("\n");
                    salida.append("    cmp eax, ").append(o2).append("\n");
                }
                salida.append("    ").append(jcc).append(" ").append(etiqueta).append("\n");
                return;
            }
        }

        String cond = operandoAsm(condicion);
        salida.append("    cmp ").append(cond).append(", 0\n");
        salida.append("    je ").append(etiqueta).append("\n");
    }

    private void emitirBI(Object destino) {
        if (!(destino instanceof String)) {
            salida.append("    ; BI sin destino resuelto\n");
            return;
        }
        salida.append("    jmp ").append((String) destino).append("\n");
    }

    private void emitirEtiqueta(String etiqueta) {
        salida.append(etiqueta).append(":\n");
    }

    private void calcularFormatosPout() {
        usaFmtUlongint = false;
        usaFmtDoublef = false;
        usaFmtCadena = false;
        for (Terceto t : tercetos) {
            if (!"pout".equals(t.getOperador())) {
                continue;
            }
            String tipo = tipoDeOperando(t.getOp1());
            if (Tipos.CADENA.equals(tipo)) {
                usaFmtCadena = true;
            } else if (Tipos.DOUBLEF.equals(tipo)) {
                usaFmtDoublef = true;
            } else if (Tipos.ULONGINT.equals(tipo)) {
                usaFmtUlongint = true;
            }
        }
    }

    private void calcularChequeosRuntime() {
        usaMsgOverflowSuma = false;
        for (Terceto t : tercetos) {
            if (!"+".equals(t.getOperador())) {
                continue;
            }
            int numero = -1;
            for (int i = 0; i < tercetos.size(); i++) {
                if (tercetos.get(i) == t) {
                    numero = i + 1;
                    break;
                }
            }
            if (numero < 0) {
                continue;
            }
            ResultadoTerceto r = resultados.get(numero);
            String tipo = r != null ? r.getTipo() : Tipos.INDEFINIDO;
            if (Tipos.ULONGINT.equals(tipo)) {
                usaMsgOverflowSuma = true;
                break;
            }
        }

        usaMsgOverflowFloat = false;
        for (Terceto t : tercetos) {
            if (!"*".equals(t.getOperador())) {
                continue;
            }
            int numero = -1;
            for (int i = 0; i < tercetos.size(); i++) {
                if (tercetos.get(i) == t) {
                    numero = i + 1;
                    break;
                }
            }
            if (numero < 0) {
                continue;
            }

            ResultadoTerceto r = resultados.get(numero);
            String tipo = r != null ? r.getTipo() : Tipos.INDEFINIDO;
            if (Tipos.DOUBLEF.equals(tipo)) {
                usaMsgOverflowFloat = true;
                break;
            }
        }

        usaMsgNegativoResta = false;
        for (Terceto t : tercetos) {
            if (!"-".equals(t.getOperador())) {
                continue;
            }
            int numero = -1;
            for (int i = 0; i < tercetos.size(); i++) {
                if (tercetos.get(i) == t) {
                    numero = i + 1;
                    break;
                }
            }
            if (numero < 0) {
                continue;
            }
            ResultadoTerceto r = resultados.get(numero);
            String tipo = r != null ? r.getTipo() : Tipos.INDEFINIDO;
            if (Tipos.ULONGINT.equals(tipo)) {
                usaMsgNegativoResta = true;
                break;
            }
        }

        usaMsgEnumInvalido = false;
        for (Terceto t : tercetos) {
            if (!":=".equals(t.getOperador())) {
                continue;
            }

            if (tipoEnumeradoChequeable(tipoDeDestinoAsignacion(t.getOp1())) != null) {
                usaMsgEnumInvalido = true;
                break;
            }
        }
    }

    private void emitirPout(Object operando) {
        String valor = operandoAsm(operando);
        if ("--".equals(valor) || "?".equals(valor)) {
            salida.append("    ; pout sin operando valido\n");
            return;
        }

        String tipo = tipoDeOperando(operando);
        String formato;
        if (Tipos.CADENA.equals(tipo)) {
            formato = FMT_CADENA;
            valor = "offset " + valor;
        } else if (Tipos.DOUBLEF.equals(tipo)) {
            formato = FMT_DOUBLEF;
        } else if (Tipos.ULONGINT.equals(tipo)) {
            formato = FMT_ULONGINT;
        } else {
            salida.append("    ; pout de tipo no resuelto (").append(tipo).append(")\n");
            return;
        }

        salida.append("    invoke crt_printf, offset ").append(formato)
              .append(", ").append(valor).append("\n");
    }

    private void prepararDeclaraciones() {
        if (tablaSimbolos == null || entorno == null) {
            return;
        }
        Map<String, Deque<Entorno.FuncionDeclarada>> porNombreApertura = new HashMap<>();
        Map<String, Deque<Entorno.FuncionDeclarada>> porNombreCierre = new HashMap<>();
        Set<Entorno.FuncionDeclarada> vistas =
                Collections.newSetFromMap(new HashMap<Entorno.FuncionDeclarada, Boolean>());

        for (EntradaTabla e : tablaSimbolos.entradas()) {
            if (e == null || !e.declarado || e.uso != EntradaTabla.Uso.FUNCION) {
                continue;
            }
            registrarDeclaracion(entorno.getFuncion(e.lexema),
                    porNombreApertura, porNombreCierre, vistas);
        }

        for (EntradaTabla e : tablaSimbolos.entradas()) {
            if (e == null || !e.declarado || e.uso != EntradaTabla.Uso.CLASE) {
                continue;
            }
            Entorno.ClaseDeclarada clase = entorno.getClase(e.lexema);
            if (clase == null) {
                continue;
            }
            for (Entorno.FuncionDeclarada metodo : clase.metodos.values()) {
                registrarDeclaracion(metodo, porNombreApertura, porNombreCierre, vistas);
            }
        }

        for (int i = 0; i < tercetos.size(); i++) {
            Terceto t = tercetos.get(i);
            String op = t.getOperador();
            boolean apertura = "FUNCION".equals(op) || "METODO".equals(op);
            boolean cierre = "FIN_FUNCION".equals(op) || "FIN_METODO".equals(op);
            if (!apertura && !cierre) {
                continue;
            }
            String nombre = textoDeOperando(t.getOp1());
            Map<String, Deque<Entorno.FuncionDeclarada>> porNombre =
                    apertura ? porNombreApertura : porNombreCierre;
            Deque<Entorno.FuncionDeclarada> cola = porNombre.get(nombre);
            Entorno.FuncionDeclarada f = cola != null && !cola.isEmpty() ? cola.pollFirst() : null;
            declaracionPorTerceto.put(i + 1, f);
        }
    }

    private void registrarDeclaracion(Entorno.FuncionDeclarada f,
            Map<String, Deque<Entorno.FuncionDeclarada>> porNombreApertura,
            Map<String, Deque<Entorno.FuncionDeclarada>> porNombreCierre,
            Set<Entorno.FuncionDeclarada> vistas) {
        if (f == null || !vistas.add(f)) {
            return;
        }
        String clave = f.entrada != null && f.entrada.lexema != null
                ? f.entrada.lexema : f.clave();
        declaracionPorClave.put(clave, f);
        declaracionPorClave.put(f.clave(), f);
        etiquetaPorDeclaracion.put(f, nombreEtiqueta(f));
        agregarCola(porNombreApertura, f);
        agregarCola(porNombreCierre, f);
    }

    private void agregarCola(Map<String, Deque<Entorno.FuncionDeclarada>> mapa,
            Entorno.FuncionDeclarada f) {
        Deque<Entorno.FuncionDeclarada> cola = mapa.get(f.nombre);
        if (cola == null) {
            cola = new ArrayDeque<>();
            mapa.put(f.nombre, cola);
        }
        cola.addLast(f);
    }

    private static String nombreEtiqueta(Entorno.FuncionDeclarada f) {
        String clave = f.entrada != null && f.entrada.lexema != null
                ? f.entrada.lexema : f.clave();
        return (f.esMetodo ? "mt_" : "fn_") + sanitizarEtiqueta(clave);
    }

    private static String sanitizarEtiqueta(String texto) {
        if (texto == null) {
            return "anon";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            sb.append(Character.isLetterOrDigit(c) || c == '_' ? c : '_');
        }
        String limpio = sb.toString();
        while (limpio.contains("__")) {
            limpio = limpio.replace("__", "_");
        }
        limpio = limpio.replaceAll("^_+|_+$", "");
        if (limpio.isEmpty()) {
            return "anon";
        }
        return Character.isDigit(limpio.charAt(0)) ? "s" + limpio : limpio;
    }

    private void prepararStackFrames() {
        framePorDeclaracion.clear();
        Set<Entorno.FuncionDeclarada> vistas =
                Collections.newSetFromMap(new HashMap<Entorno.FuncionDeclarada, Boolean>());
        for (Entorno.FuncionDeclarada f : declaracionPorTerceto.values()) {
            if (f == null || !vistas.add(f)) {
                continue;
            }
            int corriente = f.esMetodo ? 12 : 8;
            for (Entorno.Parametro p : f.parametros) {
                boolean doble = esFlotante(tipoEfectivo(p.tipo));
                int tamanio = doble ? 8 : 4;
                if (p.entrada != null) {
                    slotStackPorSimbolo.put(p.entrada,
                            (doble ? "qword ptr " : "dword ptr ") + "[ebp+" + corriente + "]");
                }
                corriente += tamanio;
            }

            int locales = 0;
            if (tablaSimbolos != null) {
                for (EntradaTabla s : tablaSimbolos.entradas()) {
                    if (s == null || !s.declarado || s.uso != EntradaTabla.Uso.VARIABLE) {
                        continue;
                    }
                    if (f.ambitoPropio == null || !f.ambitoPropio.equals(s.ambito)) {
                        continue;
                    }
                    boolean doble = esFlotante(tipoEfectivo(s.tipoDato));
                    int tamanio = doble ? 8 : 4;
                    if (doble && locales % 8 != 0) {
                        locales += 4;
                    }
                    locales += tamanio;
                    slotStackPorSimbolo.put(s,
                            (doble ? "qword ptr " : "dword ptr ") + "[ebp-" + locales + "]");
                }
            }
            framePorDeclaracion.put(f, ((locales + 7) / 8) * 8);
        }
    }

    private void prepararObjetosThis() {
        for (Terceto t : tercetos) {
            if (!"CALLMET".equals(t.getOperador()) || !(t.getOp1() instanceof EntradaTabla)) {
                continue;
            }
            EntradaTabla receptor = (EntradaTabla) t.getOp1();
            if (esSimboloDeStack(receptor)) {
                continue;
            }
            String nombre = tablaSimbolos.nombreAsmDe(receptor);
            if (nombre != null) {
                objetosThis.add(nombre);
            }
        }
    }

    private boolean esSimboloDeStack(EntradaTabla entrada) {
        return entrada != null && slotStackPorSimbolo.containsKey(entrada);
    }

    private String tipoEfectivo(String tipo) {
        if (tipo == null) {
            return Tipos.INDEFINIDO;
        }
        return entorno != null ? entorno.tipoEfectivo(tipo) : tipo;
    }

    private String textoDeOperando(Object op) {
        if (op == null) {
            return "--";
        }
        if (op instanceof EntradaTabla) {
            String lexema = ((EntradaTabla) op).lexema;
            return lexema != null ? lexema : "--";
        }
        if (op instanceof Integer) {
            return "[" + op + "]";
        }
        if (op instanceof ReferenciaAtributo) {
            return ((ReferenciaAtributo) op).texto;
        }
        return op.toString();
    }

    private void emitirPrologo(int numero, String operador, Object nombre) {
        Entorno.FuncionDeclarada f = declaracionPorTerceto.get(numero);
        funcionActual = f;
        String etiqueta = f != null ? etiquetaPorDeclaracion.get(f) : null;
        if (etiqueta == null) {
            etiqueta = ("METODO".equals(operador) ? "mt_" : "fn_")
                    + sanitizarEtiqueta(textoDeOperando(nombre));
        }
        Integer frame = f != null ? framePorDeclaracion.get(f) : null;
        int bytes = frame != null ? frame : 0;

        salida.append(etiqueta).append(":\n");
        salida.append("    push ebp\n");
        salida.append("    mov ebp, esp\n");
        if (bytes > 0) {
            salida.append("    sub esp, ").append(bytes).append("\n");
        }
        if (f != null && f.esMetodo) {
            salida.append("    ; this: dword ptr [ebp+8]\n");
        }
    }

    private void emitirFinFuncion(int numero) {
        Entorno.FuncionDeclarada f = declaracionPorTerceto.get(numero);
        String etiqueta = f != null ? etiquetaPorDeclaracion.get(f) : null;
        String sufijo = etiqueta != null && etiqueta.length() > 3
                ? etiqueta.substring(3) : "anon";
        salida.append("fin_").append(sufijo).append(":\n");
        salida.append("    leave\n");
        salida.append("    ret\n");
        funcionActual = null;
    }

    private void emitirRet(Object valorIR) {
        String valor = operandoAsm(valorIR);
        String tipo = funcionActual != null
                ? tipoEfectivo(funcionActual.tipoRetorno) : tipoDeOperando(valorIR);
        if ("--".equals(valor) || "?".equals(valor)) {
            salida.append("    ; ret sin valor resuelto\n");
        } else if (esFlotante(tipo)) {

            salida.append("    fld ").append(valor).append("\n");
        } else {
            salida.append("    mov eax, ").append(valor).append("\n");
        }
        salida.append("    leave\n");
        salida.append("    ret\n");
    }

    private void emitirPar(Object formal, Object real) {
        parametrosPendientes.add(new ParPendiente(
                formal instanceof EntradaTabla ? (EntradaTabla) formal : null, real));

        salida.append("    ; ").append(new Terceto("PAR", formal, real).visualizar()).append("\n");
    }

    private void emitirLlamada(String clave, Object cantidadObj, int numero,
            boolean esMetodo, Object receptor) {
        int cantidad = parametrosPendientes.size();
        String textoCantidad = textoDeOperando(cantidadObj);
        if (!"--".equals(textoCantidad)) {
            try {
                cantidad = Integer.parseInt(textoCantidad.trim());
            } catch (NumberFormatException ignorado) {

            }
        }
        List<ParPendiente> args = extraerParametros(cantidad);
        Entorno.FuncionDeclarada f = declaracionPorClave.get(clave);

        Map<EntradaTabla, Integer> offsetsPorFormal = new HashMap<>();
        int totalBytes = esMetodo ? 4 : 0;
        if (f != null) {
            int corrienteFormal = totalBytes;
            for (Entorno.Parametro p : f.parametros) {
                boolean doble = esFlotante(tipoEfectivo(p.tipo));
                int tamanio = doble ? 8 : 4;
                if (p.entrada != null) {
                    offsetsPorFormal.put(p.entrada, corrienteFormal);
                }
                corrienteFormal += tamanio;
                totalBytes += tamanio;
            }
        } else {
            for (int i = 0; i < args.size(); i++) {
                totalBytes += esParametroFlotante(args.get(i), f, i) ? 8 : 4;
            }
        }
        if (totalBytes > 0) {
            salida.append("    sub esp, ").append(totalBytes).append("\n");
        }

        if (esMetodo) {
            String obj = operandoAsm(receptor);
            String base = baseMemoria(obj);
            if (base.startsWith("[") && base.contains("[ebp-")) {

                salida.append("    lea eax, ").append(base).append("\n");
            } else if (base.startsWith("[")) {

                salida.append("    mov eax, dword ptr ").append(base).append("\n");
            } else {
                salida.append("    mov eax, offset ").append(base).append("\n");
            }
            salida.append("    mov dword ptr [esp], eax\n");
        }

        int corrienteLibre = esMetodo ? 4 : 0;
        for (int i = 0; i < args.size(); i++) {
            ParPendiente p = args.get(i);
            String valor = operandoAsm(p.real);
            boolean doble = esParametroFlotante(p, f, i);
            int tamanio = doble ? 8 : 4;
            Integer destino = p.formal != null ? offsetsPorFormal.get(p.formal) : null;
            if (destino == null) {
                destino = corrienteLibre;
                corrienteLibre += tamanio;
            }
            if (doble) {
                String base = baseMemoria(valor);

                salida.append("    mov eax, dword ptr ").append(conDesplazamiento4(base)).append("\n");
                salida.append("    mov dword ptr [esp+" + (destino + 4) + "], eax\n");
                salida.append("    mov eax, dword ptr ").append(comoMemoria(base)).append("\n");
                salida.append("    mov dword ptr [esp+" + destino + "], eax\n");
            } else {
                salida.append("    mov eax, ").append(valor).append("\n");
                salida.append("    mov dword ptr [esp+" + destino + "], eax\n");
            }
        }

        String etiqueta = f != null ? etiquetaPorDeclaracion.get(f) : null;
        if (etiqueta == null) {
            etiqueta = (esMetodo ? "mt_" : "fn_") + sanitizarEtiqueta(clave);
        }
        salida.append("    call ").append(etiqueta).append("\n");
        if (totalBytes > 0) {
            salida.append("    add esp, ").append(totalBytes).append("\n");
        }

        String aux = auxPorTerceto.get(numero);
        if (aux != null) {
            ResultadoTerceto r = resultados.get(numero);
            String tipo = r != null ? r.getTipo() : (f != null ? f.tipoRetorno : null);
            if (esFlotante(tipoEfectivo(tipo))) {
                salida.append("    fstp ").append(aux).append("\n");
            } else {
                salida.append("    mov ").append(aux).append(", eax\n");
            }
        }
    }

    private List<ParPendiente> extraerParametros(int cantidad) {
        List<ParPendiente> args = new ArrayList<>();
        int n = Math.max(0, Math.min(cantidad, parametrosPendientes.size()));
        int desde = parametrosPendientes.size() - n;
        for (int i = desde; i < parametrosPendientes.size(); i++) {
            args.add(parametrosPendientes.get(i));
        }
        for (int i = parametrosPendientes.size() - 1; i >= desde; i--) {
            parametrosPendientes.remove(i);
        }
        return args;
    }

    private boolean esParametroFlotante(ParPendiente p,
            Entorno.FuncionDeclarada f, int indice) {
        String tipo = p.formal != null ? p.formal.tipoDato : null;
        if (tipo == null && f != null && indice < f.parametros.size()) {
            tipo = f.parametros.get(indice).tipo;
        }
        return esFlotante(tipoEfectivo(tipo));
    }

    private String baseMemoria(String operando) {
        if (operando.startsWith("qword ptr ")) {
            return operando.substring("qword ptr ".length());
        }
        if (operando.startsWith("dword ptr ")) {
            return operando.substring("dword ptr ".length());
        }
        return operando;
    }

    private String conDesplazamiento4(String base) {
        String memoria = comoMemoria(base);
        return memoria.substring(0, memoria.length() - 1) + "+4]";
    }

    private String comoMemoria(String base) {
        return base.startsWith("[") ? base : "[" + base + "]";
    }

    private String registrarCadenaError(String texto) {
        if (cadenasErrorPorTexto.containsKey(texto)) {
            return cadenasErrorPorTexto.get(texto);
        }
        String nombre = "msg_error_" + (++contadorCadenasError);
        cadenasErrorPorTexto.put(texto, nombre);
        return nombre;
    }

    private void emitirErrorYSaliendo(String nombreCadena, int codigoSalida) {
        salida.append("    invoke StdOut, addr ").append(nombreCadena).append("\n");
        salida.append("    invoke ExitProcess, ").append(codigoSalida).append("\n");
    }

    private void emitirCopiar(Object destino, Object origen, int numero) {
        if (!(destino instanceof EntradaTabla) || !(origen instanceof EntradaTabla)) {
            String msg = registrarCadenaError("Error: COPY - operandos invalidos (destino/origen no son EntradaTabla)");
            emitirErrorYSaliendo(msg, 1);
            return;
        }
        EntradaTabla objDest = (EntradaTabla) destino;
        EntradaTabla objOrigen = (EntradaTabla) origen;

        String tipoDest = objDest.tipoDato;
        String tipoOrigen = objOrigen.tipoDato;
        Entorno.ClaseDeclarada claseDest = entorno != null ? entorno.claseDe(tipoDest) : null;
        Entorno.ClaseDeclarada claseOrigen = entorno != null ? entorno.claseDe(tipoOrigen) : null;
        if (claseDest == null || claseOrigen == null) {
            String msg = registrarCadenaError("Error: COPY - clases no resueltas: " + tipoDest + " / " + tipoOrigen);
            emitirErrorYSaliendo(msg, 1);
            return;
        }

        String ambitoDest = objDest.ambito != null ? objDest.ambito : "";
        String ambitoOrigen = objOrigen.ambito != null ? objOrigen.ambito : "";
        String nombreObjDest = TablaSimbolos.nombreDe(objDest.lexema);
        String nombreObjOrigen = TablaSimbolos.nombreDe(objOrigen.lexema);

        List<AtributoPosicional> attrsDest = atributosEfectivos(claseDest);
        List<AtributoPosicional> attrsOrigen = atributosEfectivos(claseOrigen);
        if (attrsDest.size() != attrsOrigen.size()) {
            String msg = registrarCadenaError("Error: COPY - layouts de distinto tamanio: "
                    + claseDest.clave() + " / " + claseOrigen.clave());
            emitirErrorYSaliendo(msg, 1);
            return;
        }

        for (int i = 0; i < attrsDest.size(); i++) {
            AtributoPosicional aDest = attrsDest.get(i);
            AtributoPosicional aOrigen = attrsOrigen.get(i);
            String nombreAttrDest = claveAtributoDeObjeto(nombreObjDest, claseDest, aDest);
            String nombreAttrOrigen = claveAtributoDeObjeto(nombreObjOrigen, claseOrigen, aOrigen);

            EntradaTabla eDest = tablaSimbolos.buscarIdentificador(nombreAttrDest, ambitoDest);
            EntradaTabla eOrigen = tablaSimbolos.buscarIdentificador(nombreAttrOrigen, ambitoOrigen);

            if (eDest == null || eOrigen == null) {
                String msg = registrarCadenaError("Error: COPY - atributo no encontrado en TS: "
                        + nombreAttrDest + " / " + nombreAttrOrigen);
                emitirErrorYSaliendo(msg, 1);
                continue;
            }

            String asmDest = tablaSimbolos.nombreAsmDe(eDest);
            String asmOrigen = tablaSimbolos.nombreAsmDe(eOrigen);
            String tipoEfectivo = tipoEfectivo(aDest.tipo);

            if (esFlotante(tipoEfectivo)) {

                String baseOrigen = comoMemoria(asmOrigen);
                String baseDest = comoMemoria(asmDest);
                salida.append("    mov eax, dword ptr ").append(conDesplazamiento4(baseOrigen)).append("\n");
                salida.append("    mov dword ptr ").append(conDesplazamiento4(baseDest)).append(", eax\n");
                salida.append("    mov eax, dword ptr ").append(baseOrigen).append("\n");
                salida.append("    mov dword ptr ").append(baseDest).append(", eax\n");
            } else {

                salida.append("    mov eax, ").append(asmOrigen).append("\n");
                salida.append("    mov ").append(asmDest).append(", eax\n");
            }
        }
    }

    private static class AtributoPosicional {
        final Entorno.ClaseDeclarada declarante;
        final String nombre;
        final String tipo;

        AtributoPosicional(Entorno.ClaseDeclarada declarante, String nombre, String tipo) {
            this.declarante = declarante;
            this.nombre = nombre;
            this.tipo = tipo;
        }
    }

    private List<AtributoPosicional> atributosEfectivos(Entorno.ClaseDeclarada clase) {
        Map<String, AtributoPosicional> efectivos = new LinkedHashMap<>();
        recolectarAtributosPosicionales(clase, efectivos, new HashSet<String>());
        return new ArrayList<>(efectivos.values());
    }

    private void recolectarAtributosPosicionales(Entorno.ClaseDeclarada clase,
            Map<String, AtributoPosicional> destino, Set<String> visitadas) {
        if (clase == null || !visitadas.add(clase.clave())) {
            return;
        }
        for (String claveBase : clase.heredaDe) {
            if (entorno != null) {
                recolectarAtributosPosicionales(entorno.getClase(claveBase), destino, visitadas);
            }
        }
        for (Map.Entry<String, String> atributo : clase.atributos.entrySet()) {
            destino.put(atributo.getKey(),
                    new AtributoPosicional(clase, atributo.getKey(), atributo.getValue()));
        }
    }

    private String claveAtributoDeObjeto(String nombreObjeto,
            Entorno.ClaseDeclarada claseObjeto, AtributoPosicional atributo) {
        boolean esPropia = atributo.declarante.clave().equals(claseObjeto.clave());
        return esPropia
                ? nombreObjeto + "." + atributo.nombre
                : nombreObjeto + "." + atributo.declarante.nombre + "." + atributo.nombre;
    }

    private void emitirGuardia(String condicionSalto, String labelError) {
        salida.append("    ").append(condicionSalto).append(" ").append(labelError).append("\n");
    }
}
