import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
  Generador de archivo assembler MASM32 32-bit (TP4, Prompts 2, 3, 4a y 4b).

  Emite un archivo .asm con:
  - encabezado .386 / .model flat, stdcall / option casemap:none /
    include masm32rt.inc
  - seccion .DATA construida desde la Tabla de Simbolos (colectarSimbolosData)
    y los temporales @auxN con tipo conocido (ResultadoTerceto). Ademas
    @fpu_status DW ?, usado al comparar doublef (FSTSW/SAHF), y los formatos
    fmt_ulongint / fmt_doublef / fmt_cadena, emitidos solo si se usan (4b).
  - seccion .CODE:
    * Prompt 3: asignacion :=, aritmetica + - * / (EAX / FPU), conversion todf.
    * Prompt 4a: comparadores (< > <= >= == !=) como booleano 0/1 en @auxN,
      etiquetas label_N, BF (salto inverso del comparador) y BI (jmp).
    * Prompt 4b: pout -> INVOKE crt_printf con formato segun el tipo del operando.
    * Resto de operadores (llamadas, COPY, CLASE): comentario, reservado
      para Prompts 4c/4d.

  Convenciones del Tema 13 (control de flujo, filminas 10.2 y 11.1):
    - Los destinos de BF/BI son etiquetas IR label_N ya resueltas por
      GeneradorTercetos; se emiten como "label_N:" en .CODE.
    - BF sobre un comparador emite el JCC INVERSO del operador (p.ej. '>'
      -> JBE), rehaciendo la comparacion sobre los operandos originales.
      Es el patron de la catedra: (BF, [n],Label) -> Jcc Label.
    - BF sobre un booleano ya materializado (no comparador) usa
      CMP aux,0 / JE label ("BF salta si falso").
    - Comparaciones doublef: FLD/FCOMP + FSTSW aux + MOV AX,aux + SAHF,
      luego SETcc (booleano) o Jcc (BF). CF/ZF de la FPU se interpretan
      como comparacion sin signo, segun la filmina 11.1.

  Convencion de salida (Prompt 4b, filmina printf-variables):
    - ulongint: INVOKE crt_printf, offset fmt_ulongint, <dword>
    - doublef : INVOKE crt_printf, offset fmt_doublef, <qword en memoria>
      (printf recibe el double de 64 bits directamente; no hace falta FPU)
    - cadena  : INVOKE crt_printf, offset fmt_cadena, offset <simbolo DB>
    No se usa buffer temporal: los operandos ya son simbolos de memoria o
    auxiliares tipados y crt_printf los toma por valor desde la pila.

  No altera la salida observable de TP3 (consola/tercetos/TS).
 */
public class GeneradorAssembler {

    private final List<Terceto> tercetos;
    private final TablaSimbolos tablaSimbolos;
    private final Entorno entorno;
    private final Map<Integer, ResultadoTerceto> resultados;
    private final Map<Integer, String> auxPorTerceto = new HashMap<>();
    private final StringBuilder salida = new StringBuilder();

    /* Auxiliar de 2 bytes para FSTSW en comparaciones doublef (filmina 11.1). */
    private static final String FPU_STATUS = "@fpu_status";
    private boolean usaFpuStatus = false;

    /* Formatos para pout (Prompt 4b). Se emiten solo los que se usan. */
    private static final String FMT_ULONGINT = "fmt_ulongint";
    private static final String FMT_DOUBLEF = "fmt_doublef";
    private static final String FMT_CADENA = "fmt_cadena";
    private boolean usaFmtUlongint = false;
    private boolean usaFmtDoublef = false;
    private boolean usaFmtCadena = false;

    public GeneradorAssembler(List<Terceto> tercetos,
            TablaSimbolos tablaSimbolos,
            Entorno entorno,
            Map<Integer, ResultadoTerceto> resultados) {
        this.tercetos = tercetos != null ? tercetos : Collections.emptyList();
        this.tablaSimbolos = tablaSimbolos;
        this.entorno = entorno;
        this.resultados = resultados != null ? resultados : Collections.emptyMap();

        // Pre-calcular mapeo numero de terceto -> nombre de auxiliar
        for (ResultadoTerceto r : resultados.values()) {
            if (r.produceValor() && r.tipoAsmDato() != null) {
                auxPorTerceto.put(r.getIndice(), r.nombreAuxiliar());
            }
        }
    }

    public String generar() {
        salida.setLength(0);
        auxPorTerceto.clear();
        for (ResultadoTerceto r : resultados.values()) {
            if (r.produceValor() && r.tipoAsmDato() != null) {
                auxPorTerceto.put(r.getIndice(), r.nombreAuxiliar());
            }
        }
        usaFpuStatus = calcularUsoFpuStatus();
        calcularFormatosPout();
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
            writer.write(generar());
        }
    }

    /* ---------- Encabezado ---------- */

    private void emitirEncabezado() {
        salida.append(".386\n");
        salida.append(".model flat, stdcall\n");
        salida.append("option casemap:none\n");
        salida.append("include \\masm32\\include\\masm32rt.inc\n");
        salida.append("\n");
    }

    /* ---------- Seccion .DATA ---------- */

    private void emitirData() {
        salida.append(".data\n");
        List<TablaSimbolos.SimboloDataAsm> simbolos = tablaSimbolos.colectarSimbolosData(entorno);
        for (TablaSimbolos.SimboloDataAsm s : simbolos) {
            salida.append("    ").append(s.nombreAsm).append(" ")
                  .append(s.tipoAsm).append(" ")
                  .append(valorInicial(s)).append("\n");
        }
        // Temporales implicitos @auxN con tipo ASM conocido (DD/DQ)
        for (ResultadoTerceto r : resultados.values()) {
            if (r.produceValor() && r.tipoAsmDato() != null) {
                salida.append("    ").append(r.nombreAuxiliar()).append(" ")
                      .append(r.tipoAsmDato()).append(" ?\n");
            }
        }
        // Palabra de estado de la FPU, solo si hay comparaciones doublef.
        if (usaFpuStatus) {
            salida.append("    ").append(FPU_STATUS).append(" DW ?\n");
        }
        // Formatos de impresion, solo los que necesitan los tercetos pout.
        if (usaFmtUlongint) {
            salida.append("    ").append(FMT_ULONGINT).append(" DB \"%u\",0\n");
        }
        if (usaFmtDoublef) {
            salida.append("    ").append(FMT_DOUBLEF).append(" DB \"%f\",0\n");
        }
        if (usaFmtCadena) {
            salida.append("    ").append(FMT_CADENA).append(" DB \"%s\",0\n");
        }
        salida.append("\n");
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

    /* ---------- Seccion .CODE ---------- */

    private void emitirCodigo() {
        salida.append(".code\n");
        salida.append("start:\n");

        for (int i = 0; i < tercetos.size(); i++) {
            Terceto t = tercetos.get(i);
            int numero = i + 1;
            String op = t.getOperador();
            Object op1 = t.getOp1();
            Object op2 = t.getOp2();

            ResultadoTerceto r = resultados.get(numero);
            boolean produceValor = r != null && r.produceValor();
            String tipo = r != null ? r.getTipo() : Tipos.INDEFINIDO;

            switch (op) {
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
                        // Asignacion dentro de expresion: el destino es op1, el valor viene de op2
                        emitirAsignacionExpresion(op1, op2, numero, tipo);
                    } else {
                        // Asignacion sentencia simple: destino op1, origen op2
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
                default:
                    if (esEtiqueta(op)) {
                        emitirEtiqueta(op);
                    } else {
                        // Operadores estructurales y llamadas: Prompts 4c/4d
                        salida.append("    ; ").append(t.visualizar()).append("\n");
                    }
                    break;
            }
        }

        salida.append("    invoke ExitProcess, 0\n");
        salida.append("end start\n");
    }

    /* ---------- Resolucion de operandos ---------- */

    /*
      Devuelve la referencia ASM para un operando de terceto:
      - EntradaTabla -> nombre ASM de la tabla de simbolos
      - Integer -> nombre del auxiliar @auxN del terceto referenciado
      - ReferenciaAtributo -> texto completo (objeto.atributo)
      - String -> literal tal cual (etiqueta o valor textual)
      - null -> --
     */
    private String operandoAsm(Object op) {
        if (op == null) {
            return "--";
        }
        if (op instanceof EntradaTabla) {
            return tablaSimbolos.nombreAsmDe((EntradaTabla) op);
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
            // El atributo materializado por objeto vive en la TS con un nombre
            // ASM unico. 'texto' es solo la vista IR (obj.atr / obj.clase.atr)
            // y no es un identificador MASM valido. Este mapeo prepara el
            // direccionamiento de atributos del Prompt 4d sin implementarlo.
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

    /*
      Indica si el tipo dado es punto flotante (doublef).
     */
    private boolean esFlotante(String tipo) {
        return Tipos.DOUBLEF.equals(tipo);
    }

    /* ---------- Emision de instrucciones ---------- */

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
            // FPU: fld op1; f<op> op2; fstp destino
            salida.append("    fld ").append(o1).append("\n");
            switch (operador) {
                case "+": salida.append("    fadd ").append(o2).append("\n"); break;
                case "-": salida.append("    fsub ").append(o2).append("\n"); break;
                case "*": salida.append("    fmul ").append(o2).append("\n"); break;
                case "/": salida.append("    fdiv ").append(o2).append("\n"); break;
            }
            salida.append("    fstp ").append(destino).append("\n");
        } else {
            // Entero: mov eax, op1; <op> eax, op2; mov destino, eax
            salida.append("    mov eax, ").append(o1).append("\n");
            switch (operador) {
                case "+": salida.append("    add eax, ").append(o2).append("\n"); break;
                case "-": salida.append("    sub eax, ").append(o2).append("\n"); break;
                case "*": salida.append("    imul eax, ").append(o2).append("\n"); break;
                case "/": salida.append("    div ").append(o2).append("\n"); break;
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

        // fild origen (entero de 32 bits); fstp destino (doublef)
        salida.append("    fild ").append(origen).append("\n");
        salida.append("    fstp ").append(destino).append("\n");
    }

    private void emitirAsignacionExpresion(Object destino, Object origen,
            int numero, String tipoResultado) {
        String dst = operandoAsm(destino);
        String src = operandoAsm(origen);
        String aux = auxPorTerceto.get(numero);

        if (dst.equals("--") || src.equals("--")) {
            salida.append("    ; := expr  (operandos invalidos)\n");
            return;
        }

        boolean flotante = esFlotante(tipoResultado);

        if (flotante) {
            // El origen ya es un auxiliar DQ; copiarlo al destino
            salida.append("    fld ").append(src).append("\n");
            salida.append("    fstp ").append(dst).append("\n");
        } else {
            // Copiar entero de 32 bits
            salida.append("    mov eax, ").append(src).append("\n");
            salida.append("    mov ").append(dst).append(", eax\n");
        }
    }

    private void emitirAsignacionSentencia(Object destino, Object origen) {
        String dst = operandoAsm(destino);
        String src = operandoAsm(origen);

        if (dst.equals("--") || src.equals("--")) {
            salida.append("    ; := sentencia  (operandos invalidos)\n");
            return;
        }

        // Determinar el tipo del destino si es una EntradaTabla de la TS
        boolean flotante = false;
        if (destino instanceof EntradaTabla) {
            flotante = esFlotante(((EntradaTabla) destino).tipoDato);
        }

        if (flotante) {
            salida.append("    fld ").append(src).append("\n");
            salida.append("    fstp ").append(dst).append("\n");
        } else {
            salida.append("    mov eax, ").append(src).append("\n");
            salida.append("    mov ").append(dst).append(", eax\n");
        }
    }

    /* ---------- Control de flujo y comparaciones (Prompt 4a) ---------- */

    private boolean esEtiqueta(String op) {
        return op != null && op.startsWith("label_");
    }

    private boolean esComparador(String op) {
        return "<".equals(op) || ">".equals(op) || "<=".equals(op)
                || ">=".equals(op) || "==".equals(op) || "!=".equals(op);
    }

    /*
      Tipo logico de un operando de comparacion. Se usa para decidir entre
      CMP (ulongint) y la secuencia FPU (doublef). El tipo del terceto
      comparador siempre es ulongint (es un booleano), por eso se consulta
      el tipo de sus OPERANDOS, no el ResultadoTerceto del comparador.
     */
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

    /*
      @fpu_status solo se necesita cuando alguna comparacion (o su BF)
      opera sobre doublef. Se calcula antes de emitir .DATA.
     */
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
            }
        }
        return false;
    }

    /* SETcc que deja 1 cuando la relacion es verdadera. El mismo orden
       sin signo sirve para la FPU tras FSTSW/SAHF (filmina 11.1). */
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

    /* Jcc que salta cuando la relacion es FALSA (BF = branch on false). */
    private String jccFalso(String comparador) {
        switch (comparador) {
            case "<":  return "jae";   // NOT(<)  = >=
            case "<=": return "ja";    // NOT(<=) = >
            case ">":  return "jbe";   // NOT(>)  = <=
            case ">=": return "jb";    // NOT(>=) = <
            case "==": return "jne";
            case "!=": return "je";
            default:   return null;
        }
    }

    /*
      Secuencia comun de comparacion doublef segun la filmina 11.1:
        FLD op1 / FCOMP op2 / FSTSW aux / MOV AX,aux / SAHF
      FCOMP (en lugar de FCOM + FSTP) deja la pila FPU balanceada.
     */
    private void emitirComparacionFpu(String o1, String o2) {
        salida.append("    fld ").append(o1).append("\n");
        salida.append("    fcomp ").append(o2).append("\n");
        salida.append("    fstsw ").append(FPU_STATUS).append("\n");
        salida.append("    mov ax, ").append(FPU_STATUS).append("\n");
        salida.append("    sahf\n");
    }

    /* Comparador: materializa 0/1 en el auxiliar del terceto (@auxN : DD). */
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

    /*
      BF (branch on false). Dos formas:
      1) cond = ref a un terceto comparador: se rehace la comparacion sobre
         los operandos originales y se salta con el JCC INVERSO. Es el patron
         de las filminas 10.2/11.1: (BF,[n],Label) -> Jcc Label.
      2) cond = booleano ya materializado: CMP aux,0 / JE label.
     */
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

    /* ---------- Salida por consola (Prompt 4b) ---------- */

    /*
      Los formatos se deciden ANTES de emitir .DATA. Se reutiliza el mismo
      resolvedor de tipos del Prompt 4a: una referencia [n] toma el tipo del
      ResultadoTerceto (incluye el tipo de retorno de un CALL pendiente de 4c),
      una EntradaTabla consulta la TS/entorno y una ReferenciaAtributo su tipo.
     */
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

    /*
      pout -> INVOKE crt_printf. La unica diferencia entre tipos es el formato
      y el modo de pasar el operando:
        - ulongint / doublef: valor (MASM pasa dword / qword segun el simbolo)
        - cadena: offset del simbolo DB terminado en 0
      No se mueve nada a EAX ni se carga la FPU: crt_printf ya recibe el valor
      por valor en la pila, tal como muestra la filmina printf-variables.
     */
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
}
