import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
  Generador de archivo assembler MASM32 32-bit (TP4, Prompts 2, 3 y 4).

  Emite un archivo .asm con:
  - encabezado .386 / .model flat, stdcall / option casemap:none /
    include masm32rt.inc y prototipo de printf;
  - seccion .DATA construida desde la Tabla de Simbolos (colectarSimbolosData),
    los temporales @auxN con tipo conocido (ResultadoTerceto), los formatos de
    pout y los slots de retorno de funciones/metodos;
  - seccion .CODE con:
    * asignacion :=, aritmetica + - * /, conversion todf (Prompt 3);
    * etiquetas, comparadores, BF/BI, IF/ELSE/END_IF y REPEAT-WHILE (Prompt 4);
    * pout para ulongint/doublef/cadena mediante printf (Prompt 4);
    * PAR/CALL/CALLMET/ret y FUNCION/METODO con parametros globales y slot de
      retorno (Prompt 4, convencion propia no reentrante);
    * COPY de objetos y resolucion de ReferenciaAtributo a las entradas por
      objeto materializadas en la TS (Prompt 4).

  Las clases (CLASE/FIN_CLASE) son marcadores no ejecutables: no generan
  instrucciones. El layout de objeto es un conjunto de variables .DATA por
  atributo materializado; COPY copia posicionalmente esos slots.

  No altera la salida observable de TP3 (consola/tercetos/TS).
 */
public class GeneradorAssembler {

    private static final String FMT_UINT = "@tp4_fmt_uint";
    private static final String FMT_DOUBLE = "@tp4_fmt_double";
    private static final String FMT_STRING = "@tp4_fmt_string";
    private static final String ETIQUETA_MAIN = "@tp4_main";
    private static final String PREFIJO_RET = "@tp4_ret_";

    /*
      Region de codigo de una funcion o metodo. El cuerpo conserva el orden de
      los tercetos; los marcadores FUNCION/FIN_FUNCION no se ejecutan.
     */
    private static final class BloqueFuncion {
        final String nombre;
        final boolean esMetodo;
        // Ambito propio completo (ambitoDeclaracion:nombre). Es la clave exacta
        // de la funcion en Entorno, por lo que distingue homonimas en scopes
        // anidados (main:a:fun_c vs main:a:b:fun_c).
        final String ambitoPropio;
        final String tipoRetorno;
        final String label;
        final List<Terceto> cuerpo = new ArrayList<>();

        BloqueFuncion(String nombre, boolean esMetodo, String ambitoPropio,
                String tipoRetorno, String label) {
            this.nombre = nombre;
            this.esMetodo = esMetodo;
            this.ambitoPropio = ambitoPropio;
            this.tipoRetorno = tipoRetorno;
            this.label = label;
        }

        String slotRetorno() {
            return PREFIJO_RET + label;
        }
    }

    private final List<Terceto> tercetos;
    private final TablaSimbolos tablaSimbolos;
    private final Entorno entorno;
    private final Map<Integer, ResultadoTerceto> resultados;
    private final Map<Integer, String> auxPorTerceto = new LinkedHashMap<>();
    // label de funcion/metodo -> DD|DQ del slot de retorno
    private final Map<String, String> retSlotPorFuncion = new LinkedHashMap<>();
    private final List<BloqueFuncion> bloques = new ArrayList<>();
    private final List<Terceto> principales = new ArrayList<>();
    private final StringBuilder salida = new StringBuilder();
    // Evita el epilogo de seguridad duplicado cuando el cuerpo ya termina en
    // un ret (el ultimo codigo emitido de la funcion fue su epilogo).
    private boolean epilogoAlFinal;

    public GeneradorAssembler(List<Terceto> tercetos,
            TablaSimbolos tablaSimbolos,
            Entorno entorno,
            Map<Integer, ResultadoTerceto> resultados) {
        this.tercetos = tercetos != null ? tercetos : Collections.emptyList();
        this.tablaSimbolos = tablaSimbolos;
        this.entorno = entorno;
        this.resultados = resultados != null ? resultados : Collections.emptyMap();
    }

    public String generar() {
        salida.setLength(0);
        preparar();
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

    /*
      Primera pasada: separa el cuerpo principal de las funciones/metodos y
      calcula labels y slots de retorno. Es necesario porque en el IR las
      definiciones aparecen antes del cuerpo principal y no deben ejecutarse
      por caida desde start.
     */
    private void preparar() {
        bloques.clear();
        principales.clear();
        retSlotPorFuncion.clear();
        auxPorTerceto.clear();

        for (ResultadoTerceto r : resultados.values()) {
            if (r.produceValor() && r.tipoAsmDato() != null) {
                auxPorTerceto.put(r.getIndice(), r.nombreAuxiliar());
                if ("CALL".equals(r.getOperador()) || "CALLMET".equals(r.getOperador())) {
                    // El slot se completa luego de conocer la EntradaTabla del llamado.
                }
            }
        }

        Deque<BloqueFuncion> pilaBloques = new ArrayDeque<>();
        // Pila unificada de ambitos propios abiertos (clases y
        // funciones/metodos) en el mismo orden en que aparecen en el IR. El
        // tope es el ambito de declaracion del siguiente miembro.
        Deque<String> pilaAmbitos = new ArrayDeque<>();

        for (int i = 0; i < tercetos.size(); i++) {
            Terceto t = tercetos.get(i);
            String op = t.getOperador();

            if ("CLASE".equals(op)) {
                if (t.getOp1() instanceof String) {
                    String nombreClase = (String) t.getOp1();
                    String declaracion = ambitoActual(pilaAmbitos);
                    pilaAmbitos.push(declaracion + ":" + nombreClase);
                }
                continue;
            }
            if ("FIN_CLASE".equals(op)) {
                if (!pilaAmbitos.isEmpty()) {
                    pilaAmbitos.pop();
                }
                continue;
            }
            if ("PROGRAMA".equals(op) || "FIN_PROGRAMA".equals(op)) {
                continue;
            }
            if ("FUNCION".equals(op) || "METODO".equals(op)) {
                String nombre = nombreDeFuncion(t.getOp1());
                boolean esMetodo = "METODO".equals(op);
                String declaracion = ambitoActual(pilaAmbitos);
                BloqueFuncion bloque = crearBloqueFuncion(nombre, esMetodo, declaracion);
                if (bloque.tipoRetorno != null && tipoAsm(bloque.tipoRetorno) != null) {
                    retSlotPorFuncion.put(bloque.label, tipoAsm(bloque.tipoRetorno));
                }
                pilaAmbitos.push(bloque.ambitoPropio);
                pilaBloques.push(bloque);
                continue;
            }
            if ("FIN_FUNCION".equals(op) || "FIN_METODO".equals(op)) {
                if (!pilaBloques.isEmpty()) {
                    bloques.add(pilaBloques.pop());
                }
                if (!pilaAmbitos.isEmpty()) {
                    pilaAmbitos.pop();
                }
                continue;
            }

            if (!pilaBloques.isEmpty()) {
                pilaBloques.peek().cuerpo.add(t);
            } else {
                principales.add(t);
            }
        }

        // Restos por cierre incompleto (no deberia ocurrir en programas validos).
        while (!pilaBloques.isEmpty()) {
            bloques.add(pilaBloques.pop());
        }

        recolectarRetornosDeLlamadas();
    }

    private String nombreDeFuncion(Object op1) {
        if (op1 instanceof String) {
            return (String) op1;
        }
        if (op1 instanceof EntradaTabla) {
            return TablaSimbolos.nombreDe(((EntradaTabla) op1).lexema);
        }
        return "?";
    }

    /*
      Ambito de declaracion del siguiente miembro: el tope de la pila de
      ambitos abiertos o main en el nivel global.
     */
    private String ambitoActual(Deque<String> pilaAmbitos) {
        return pilaAmbitos.isEmpty() ? "main" : pilaAmbitos.peek();
    }

    private BloqueFuncion crearBloqueFuncion(String nombre, boolean esMetodo,
            String ambitoDeclaracion) {
        // La clave exacta de FuncionDeclarada es nombre:ambitoDeclaracion, que
        // coincide con el ambito propio del bloque.
        String ambitoPropio = ambitoDeclaracion + ":" + nombre;
        EntradaTabla entrada = null;
        String tipoRetorno = null;

        if (esMetodo) {
            Entorno.ClaseDeclarada declarante = entorno.getClase(ambitoDeclaracion);
            if (declarante != null) {
                Entorno.FuncionDeclarada metodo = declarante.metodos.get(nombre);
                if (metodo != null) {
                    tipoRetorno = metodo.tipoRetorno;
                    entrada = metodo.entrada;
                }
            }
            if (entrada == null) {
                entrada = buscarEntradaPorAmbito(nombre, EntradaTabla.Uso.METODO,
                        ambitoDeclaracion);
            }
        } else {
            Entorno.FuncionDeclarada funcion = entorno.getFuncion(ambitoPropio);
            if (funcion != null) {
                tipoRetorno = funcion.tipoRetorno;
                entrada = funcion.entrada;
            }
            if (entrada == null) {
                entrada = buscarEntradaPorAmbito(nombre, EntradaTabla.Uso.FUNCION,
                        ambitoDeclaracion);
            }
        }

        if (tipoRetorno == null && entrada != null) {
            tipoRetorno = entrada.tipoDato;
        }
        String label = entrada != null
                ? tablaSimbolos.nombreAsmDe(entrada)
                : "fn_" + sanitizarAsm(nombre);
        return new BloqueFuncion(nombre, esMetodo, ambitoPropio,
                tipoRetorno, label);
    }

    /*
      Respaldo cuando Entorno no conserva la declaracion (por ejemplo una clase
      descartada): busca en la TS la entrada declarada con nombre, uso y ambito
      exactos, sin aceptar una homonima de otro scope.
     */
    private EntradaTabla buscarEntradaPorAmbito(String nombre, EntradaTabla.Uso uso,
            String ambito) {
        for (EntradaTabla e : tablaSimbolos.entradas()) {
            if (e != null && e.uso == uso
                    && nombre.equals(TablaSimbolos.nombreDe(e.lexema))
                    && ambito.equals(TablaSimbolos.ambitoDe(e.lexema))) {
                return e;
            }
        }
        return null;
    }

    /*
      Completa los slots de retorno de CALL/CALLMET. El tipo se toma del
      ResultadoTerceto (tipo de retorno de la funcion) y el label de la
      EntradaTabla del llamado, que coincide con el usado en su definicion.
     */
    private void recolectarRetornosDeLlamadas() {
        for (int i = 0; i < tercetos.size(); i++) {
            Terceto t = tercetos.get(i);
            String op = t.getOperador();
            EntradaTabla llamado = null;
            if ("CALL".equals(op) && t.getOp1() instanceof EntradaTabla) {
                llamado = (EntradaTabla) t.getOp1();
            } else if ("CALLMET".equals(op) && t.getOp2() instanceof EntradaTabla) {
                llamado = (EntradaTabla) t.getOp2();
            }
            if (llamado == null) {
                continue;
            }
            String tipo = tipoDeResultado(i + 1);
            String tipoAsm = tipoAsm(tipo);
            if (tipoAsm != null) {
                String label = tablaSimbolos.nombreAsmDe(llamado);
                retSlotPorFuncion.putIfAbsent(label, tipoAsm);
            }
        }
    }

    /* ---------- Encabezado ---------- */

    private void emitirEncabezado() {
        salida.append(".386\n");
        salida.append(".model flat, stdcall\n");
        salida.append("option casemap:none\n");
        salida.append("include \\masm32\\include\\masm32rt.inc\n");
        salida.append("includelib \\masm32\\lib\\kernel32.lib\n");
        salida.append("includelib \\masm32\\lib\\masm32.lib\n");
        salida.append("dll_dllcrt0 PROTO C\n");
        salida.append("printf PROTO C : VARARG\n");
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

        // Formatos y slots de retorno para las rutinas de Prompt 4.
        salida.append("    ").append(FMT_UINT).append(" DB \"%u\",10,0\n");
        salida.append("    ").append(FMT_DOUBLE).append(" DB \"%.20Lf\",10,0\n");
        salida.append("    ").append(FMT_STRING).append(" DB \"%s\",10,0\n");
        for (Map.Entry<String, String> slot : retSlotPorFuncion.entrySet()) {
            salida.append("    ").append(PREFIJO_RET).append(slot.getKey())
                  .append(" ").append(slot.getValue()).append(" ?\n");
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
        salida.append("    jmp ").append(ETIQUETA_MAIN).append("\n\n");

        for (BloqueFuncion bloque : bloques) {
            emitirBloqueFuncion(bloque);
        }

        salida.append(ETIQUETA_MAIN).append(":\n");
        for (Terceto t : principales) {
            emitirTerceto(t, null);
        }
        salida.append("    invoke ExitProcess, 0\n");
        salida.append("end start\n");
    }

    private void emitirBloqueFuncion(BloqueFuncion bloque) {
        salida.append("; ").append(bloque.esMetodo ? "METODO " : "FUNCION ")
              .append(bloque.nombre).append("\n");
        salida.append(bloque.label).append(":\n");
        // Prologo estandar: printf puede alterar registros, pero el estado del
        // programa vive en .DATA y no depende de ellos entre tercetos.
        salida.append("    push ebp\n");
        salida.append("    mov ebp, esp\n");
        salida.append("    push edi\n");
        salida.append("    push esi\n");

        epilogoAlFinal = false;
        for (Terceto t : bloque.cuerpo) {
            emitirTerceto(t, bloque);
        }

        // Epilogo de seguridad solo si el cuerpo no termino ya en un ret.
        if (!epilogoAlFinal) {
            emitirEpilogo();
        }
        salida.append("\n");
    }

    private void emitirEpilogo() {
        salida.append("    pop esi\n");
        salida.append("    pop edi\n");
        salida.append("    mov esp, ebp\n");
        salida.append("    pop ebp\n");
        salida.append("    ret\n");
        epilogoAlFinal = true;
    }

    private void emitirTerceto(Terceto t, BloqueFuncion bloque) {
        String op = t.getOperador();
        Object op1 = t.getOp1();
        Object op2 = t.getOp2();

        if (t.esEtiqueta()) {
            epilogoAlFinal = false;
            salida.append(op).append(":\n");
            return;
        }
        // Cualquier terceto que no sea ret deja una posicion de codigo despues
        // del ultimo epilogo (incluido un salto de BI), asi que hace falta el
        // epilogo de seguridad al final del bloque.
        if (!"ret".equals(op)) {
            epilogoAlFinal = false;
        }

        switch (op) {
            case "+":
            case "-":
            case "*":
            case "/":
                emitirAritmetica(op, op1, op2, numeroDe(t));
                break;
            case "todf":
                emitirTodf(op1, numeroDe(t));
                break;
            case ":=":
                if (produceValor(numeroDe(t))) {
                    emitirAsignacionExpresion(op1, op2, numeroDe(t));
                } else {
                    emitirAsignacionSentencia(op1, op2);
                }
                break;
            case "COPY":
                emitirCopy(op1, op2);
                break;
            case "<":
            case ">":
            case "<=":
            case ">=":
            case "==":
            case "!=":
                emitirComparacion(op, op1, op2, numeroDe(t));
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
            case "PAR":
                emitirPar(op1, op2);
                break;
            case "CALL":
                emitirCall(op1, numeroDe(t));
                break;
            case "CALLMET":
                emitirCallMet(op1, op2, numeroDe(t));
                break;
            case "ret":
                emitirRet(op1, bloque);
                break;
            case "PROGRAMA":
            case "FIN_PROGRAMA":
            case "CLASE":
            case "FIN_CLASE":
            case "FUNCION":
            case "FIN_FUNCION":
            case "METODO":
            case "FIN_METODO":
                // Marcadores estructurales: no producen codigo ejecutable.
                break;
            default:
                salida.append("    ; ").append(t.visualizar()).append("\n");
                break;
        }
    }

    private int numeroDe(Terceto buscado) {
        for (int i = 0; i < tercetos.size(); i++) {
            if (tercetos.get(i) == buscado) {
                return i + 1;
            }
        }
        return -1;
    }

    private boolean produceValor(int numero) {
        ResultadoTerceto r = resultados.get(numero);
        return r != null && r.produceValor();
    }

    /* ---------- Control de flujo ---------- */

    private void emitirComparacion(String operador, Object op1, Object op2, int numero) {
        String o1 = operandoAsm(op1);
        String o2 = operandoAsm(op2);
        String destino = auxPorTerceto.get(numero);
        if ("--".equals(o1) || "--".equals(o2) || destino == null) {
            salida.append("    ; comparacion sin operandos/auxiliar\n");
            return;
        }

        String condicion = condicionDe(operador);
        boolean flotante = esFlotante(tipoDe(op1)) || esFlotante(tipoDe(op2));

        if (flotante) {
            // x87: FCOMP deja C0/C2/C3; FSTSW+SAHF los lleva a CF/PF/ZF.
            salida.append("    fld ").append(o1).append("\n");
            salida.append("    fcomp ").append(o2).append("\n");
            salida.append("    fstsw ax\n");
            salida.append("    sahf\n");
        } else {
            salida.append("    mov eax, ").append(o1).append("\n");
            salida.append("    cmp eax, ").append(o2).append("\n");
        }
        salida.append("    ").append(condicion).append(" al\n");
        salida.append("    movzx eax, al\n");
        salida.append("    mov ").append(destino).append(", eax\n");
    }

    /*
      Condiciones unsigned/sin signo. Tras SAHF, CF/ZF de x87 se corresponden
      con las mismas condiciones que usa la comparacion entera sin signo.
     */
    private String condicionDe(String operador) {
        switch (operador) {
            case "<": return "setb";
            case ">": return "seta";
            case "<=": return "setbe";
            case ">=": return "setae";
            case "==": return "sete";
            case "!=": return "setne";
            default: return "setne";
        }
    }

    private void emitirBF(Object condicion, Object destino) {
        String label = destino instanceof String ? (String) destino : null;
        String cond = operandoAsm(condicion);
        if (label == null || "--".equals(cond)) {
            salida.append("    ; BF sin condicion/destino\n");
            return;
        }
        salida.append("    cmp ").append(cond).append(", 0\n");
        salida.append("    je ").append(label).append("\n");
    }

    private void emitirBI(Object destino) {
        String label = destino instanceof String ? (String) destino : null;
        if (label == null) {
            salida.append("    ; BI sin destino\n");
            return;
        }
        salida.append("    jmp ").append(label).append("\n");
    }

    /* ---------- pout ---------- */

    private void emitirPout(Object operando) {
        String asmOperando = operandoAsm(operando);
        if ("--".equals(asmOperando)) {
            salida.append("    ; pout sin operando\n");
            return;
        }
        String tipo = tipoDe(operando);
        if (Tipos.CADENA.equals(tipo)) {
            salida.append("    invoke printf, addr ").append(FMT_STRING)
                  .append(", addr ").append(asmOperando).append("\n");
        } else if (Tipos.DOUBLEF.equals(tipo)) {
            salida.append("    invoke printf, addr ").append(FMT_DOUBLE)
                  .append(", ").append(asmOperando).append("\n");
        } else if (Tipos.ULONGINT.equals(tipo)) {
            salida.append("    invoke printf, addr ").append(FMT_UINT)
                  .append(", ").append(asmOperando).append("\n");
        } else {
            salida.append("    ; pout de tipo no soportado: ").append(tipo).append("\n");
        }
    }

    /* ---------- Funciones, parametros y retorno ---------- */

    private void emitirPar(Object formal, Object real) {
        String tipoFormal = tipoDe(formal);
        if (Tipos.INDEFINIDO.equals(tipoFormal)) {
            tipoFormal = tipoDe(real);
        }
        // Un parametro formal de tipo clase no ocupa un DD/DQ unico: se copia
        // atributo por atributo, igual que COPY, usando las entradas
        // materializadas del parametro (p.x:ambito) y del objeto real.
        if (formal instanceof EntradaTabla && real instanceof EntradaTabla
                && entorno.claseDeObjeto((EntradaTabla) formal) != null) {
            emitirCopy(formal, real);
            return;
        }
        copiarSimple(operandoAsm(formal), operandoAsm(real), tipoFormal);
    }

    private void emitirCall(Object funcion, int numero) {
        if (!(funcion instanceof EntradaTabla)) {
            salida.append("    ; CALL sin funcion\n");
            return;
        }
        String label = tablaSimbolos.nombreAsmDe((EntradaTabla) funcion);
        salida.append("    call ").append(label).append("\n");
        copiarRetorno(label, numero);
    }

    private void emitirCallMet(Object objeto, Object metodo, int numero) {
        if (!(metodo instanceof EntradaTabla)) {
            salida.append("    ; CALLMET sin metodo\n");
            return;
        }
        // No hay 'this' explicito en el lenguaje: el receptor no se pasa.
        String label = tablaSimbolos.nombreAsmDe((EntradaTabla) metodo);
        salida.append("    call ").append(label).append("\n");
        copiarRetorno(label, numero);
    }

    private void copiarRetorno(String label, int numero) {
        String aux = auxPorTerceto.get(numero);
        String slot = PREFIJO_RET + label;
        String tipoSlot = retSlotPorFuncion.get(label);
        if (aux == null || tipoSlot == null) {
            return;
        }
        if ("DQ".equals(tipoSlot)) {
            salida.append("    fld ").append(slot).append("\n");
            salida.append("    fstp ").append(aux).append("\n");
        } else {
            salida.append("    mov eax, ").append(slot).append("\n");
            salida.append("    mov ").append(aux).append(", eax\n");
        }
    }

    private void emitirRet(Object operando, BloqueFuncion bloque) {
        if (bloque == null) {
            salida.append("    ; ret fuera de funcion\n");
            return;
        }
        String slot = bloque.slotRetorno();
        String tipo = bloque.tipoRetorno;
        if (tipo == null || Tipos.INDEFINIDO.equals(tipo)) {
            tipo = tipoDe(operando);
        }
        copiarSimple(slot, operandoAsm(operando), tipo);
        emitirEpilogo();
    }

    /* ---------- Objetos y atributos ---------- */

    private void emitirCopy(Object destino, Object origen) {
        if (!(destino instanceof EntradaTabla) || !(origen instanceof EntradaTabla)) {
            salida.append("    ; COPY con operandos no soportados\n");
            return;
        }
        EntradaTabla objetoDestino = (EntradaTabla) destino;
        EntradaTabla objetoOrigen = (EntradaTabla) origen;
        Entorno.ClaseDeclarada claseDestino = entorno.claseDeObjeto(objetoDestino);
        Entorno.ClaseDeclarada claseOrigen = entorno.claseDeObjeto(objetoOrigen);
        if (claseDestino == null || claseOrigen == null) {
            salida.append("    ; COPY sin clase resuelta\n");
            return;
        }

        List<Entorno.AtributoEfectivo> atributosDestino =
                entorno.atributosEfectivosConDeclarante(claseDestino);
        List<Entorno.AtributoEfectivo> atributosOrigen =
                entorno.atributosEfectivosConDeclarante(claseOrigen);
        if (atributosDestino.size() != atributosOrigen.size()) {
            salida.append("    ; COPY incompatible (cantidad de atributos)\n");
            return;
        }

        salida.append("    ; COPY ").append(objetoDestino.lexema).append(" := ")
              .append(objetoOrigen.lexema).append(" (copia posicional)\n");
        for (int i = 0; i < atributosDestino.size(); i++) {
            Entorno.AtributoEfectivo atributoDestino = atributosDestino.get(i);
            Entorno.AtributoEfectivo atributoOrigen = atributosOrigen.get(i);
            EntradaTabla destinoSlot = entradaAtributoMaterializado(
                    objetoDestino, atributoDestino, claseDestino);
            EntradaTabla origenSlot = entradaAtributoMaterializado(
                    objetoOrigen, atributoOrigen, claseOrigen);
            if (destinoSlot == null || origenSlot == null) {
                salida.append("    ; COPY: no se encontro slot para ")
                      .append(atributoDestino.nombre).append("\n");
                continue;
            }
            String tipo = entorno.tipoEfectivo(destinoSlot.tipoDato);
            copiarSimple(tablaSimbolos.nombreAsmDe(destinoSlot),
                    tablaSimbolos.nombreAsmDe(origenSlot), tipo);
        }
    }

    /*
      Entrada materializada por objeto: objeto.atr:ambito (propio) u
      objeto.declarante.atr:ambito (heredado). El ambito es el del objeto.
     */
    private EntradaTabla entradaAtributoMaterializado(EntradaTabla objeto,
            Entorno.AtributoEfectivo atributo, Entorno.ClaseDeclarada claseObjeto) {
        if (objeto == null || atributo == null || claseObjeto == null) {
            return null;
        }
        String nombreObjeto = TablaSimbolos.nombreDe(objeto.lexema);
        String ambito = objeto.ambito != null ? objeto.ambito
                : TablaSimbolos.ambitoDe(objeto.lexema);
        String prefijo = atributo.declarante != null
                && atributo.declarante.clave().equals(claseObjeto.clave())
                ? nombreObjeto
                : nombreObjeto + "." + (atributo.declarante == null
                        ? claseObjeto.nombre : atributo.declarante.nombre);
        String calificado = prefijo + "." + atributo.nombre;

        EntradaTabla entrada = tablaSimbolos.buscarIdentificador(calificado, ambito);
        if (entrada != null) {
            return entrada;
        }
        for (EntradaTabla e : tablaSimbolos.entradas()) {
            if (e != null && e.uso == EntradaTabla.Uso.ATRIBUTO
                    && calificado.equals(TablaSimbolos.nombreDe(e.lexema))
                    && ambito.equals(e.ambito)) {
                return e;
            }
        }
        return null;
    }

    private EntradaTabla entradaDeReferenciaAtributo(ReferenciaAtributo referencia) {
        if (referencia == null || referencia.texto == null) {
            return null;
        }
        String ambito = "";
        if (referencia.objeto != null) {
            ambito = referencia.objeto.ambito != null ? referencia.objeto.ambito
                    : TablaSimbolos.ambitoDe(referencia.objeto.lexema);
        }
        EntradaTabla entrada = tablaSimbolos.buscarIdentificador(referencia.texto, ambito);
        if (entrada != null) {
            return entrada;
        }
        for (EntradaTabla e : tablaSimbolos.entradas()) {
            if (e != null && e.uso == EntradaTabla.Uso.ATRIBUTO
                    && referencia.texto.equals(TablaSimbolos.nombreDe(e.lexema))
                    && (ambito.isEmpty() || ambito.equals(e.ambito))) {
                return e;
            }
        }
        return null;
    }

    /* ---------- Aritmetica y asignacion ---------- */

    private void emitirAritmetica(String operador, Object op1, Object op2, int numero) {
        String o1 = operandoAsm(op1);
        String o2 = operandoAsm(op2);
        String destino = auxPorTerceto.get(numero);
        String tipo = tipoDeResultado(numero);

        if (destino == null) {
            salida.append("    ; ").append(new Terceto(operador, op1, op2).visualizar())
                  .append("  (sin auxiliar)\n");
            return;
        }

        if (esFlotante(tipo)) {
            salida.append("    fld ").append(o1).append("\n");
            switch (operador) {
                case "+": salida.append("    fadd ").append(o2).append("\n"); break;
                case "-": salida.append("    fsub ").append(o2).append("\n"); break;
                case "*": salida.append("    fmul ").append(o2).append("\n"); break;
                case "/": salida.append("    fdiv ").append(o2).append("\n"); break;
            }
            salida.append("    fstp ").append(destino).append("\n");
        } else {
            salida.append("    mov eax, ").append(o1).append("\n");
            if ("/".equals(operador)) {
                // Division entera sin signo: edx:eax / operando -> eax.
                salida.append("    xor edx, edx\n");
                salida.append("    div ").append(o2).append("\n");
            } else {
                switch (operador) {
                    case "+": salida.append("    add eax, ").append(o2).append("\n"); break;
                    case "-": salida.append("    sub eax, ").append(o2).append("\n"); break;
                    case "*": salida.append("    imul eax, ").append(o2).append("\n"); break;
                }
            }
            salida.append("    mov ").append(destino).append(", eax\n");
        }
    }

    private void emitirTodf(Object op1, int numero) {
        String origen = operandoAsm(op1);
        String destino = auxPorTerceto.get(numero);
        if (destino == null || "--".equals(origen)) {
            salida.append("    ; todf ").append(origen).append("  (sin auxiliar)\n");
            return;
        }
        salida.append("    fild ").append(origen).append("\n");
        salida.append("    fstp ").append(destino).append("\n");
    }

    private void emitirAsignacionExpresion(Object destino, Object origen, int numero) {
        String dst = operandoAsm(destino);
        String src = operandoAsm(origen);
        String aux = auxPorTerceto.get(numero);
        if ("--".equals(dst) || "--".equals(src)) {
            salida.append("    ; := expr  (operandos invalidos)\n");
            return;
        }
        String tipo = tipoDe(destino);
        if (Tipos.INDEFINIDO.equals(tipo)) {
            tipo = tipoDeResultado(numero);
        }

        if (esFlotante(tipo)) {
            salida.append("    fld ").append(src).append("\n");
            if (aux != null) {
                salida.append("    fst ").append(dst).append("\n");
                salida.append("    fstp ").append(aux).append("\n");
            } else {
                salida.append("    fstp ").append(dst).append("\n");
            }
        } else {
            salida.append("    mov eax, ").append(src).append("\n");
            salida.append("    mov ").append(dst).append(", eax\n");
            if (aux != null) {
                salida.append("    mov ").append(aux).append(", eax\n");
            }
        }
    }

    private void emitirAsignacionSentencia(Object destino, Object origen) {
        String tipo = tipoDe(destino);
        copiarSimple(operandoAsm(destino), operandoAsm(origen), tipo);
    }

    private void copiarSimple(String destino, String origen, String tipo) {
        if (destino == null || origen == null || "--".equals(destino) || "--".equals(origen)) {
            salida.append("    ; copia con operandos invalidos\n");
            return;
        }
        if (esFlotante(tipo)) {
            salida.append("    fld ").append(origen).append("\n");
            salida.append("    fstp ").append(destino).append("\n");
        } else {
            salida.append("    mov eax, ").append(origen).append("\n");
            salida.append("    mov ").append(destino).append(", eax\n");
        }
    }

    /* ---------- Resolucion de tipos y operandos ---------- */

    private String tipoDeResultado(int numero) {
        ResultadoTerceto r = resultados.get(numero);
        if (r == null) {
            return Tipos.INDEFINIDO;
        }
        return efectivo(r.getTipo());
    }

    private String tipoDe(Object op) {
        if (op == null) {
            return Tipos.INDEFINIDO;
        }
        if (op instanceof EntradaTabla) {
            EntradaTabla entrada = (EntradaTabla) op;
            if (entrada.uso == EntradaTabla.Uso.CADENA) {
                return Tipos.CADENA;
            }
            return efectivo(entrada.tipoDato);
        }
        if (op instanceof Integer) {
            return tipoDeResultado((Integer) op);
        }
        if (op instanceof ReferenciaAtributo) {
            ReferenciaAtributo referencia = (ReferenciaAtributo) op;
            String tipo = efectivo(referencia.getTipo());
            if (!Tipos.INDEFINIDO.equals(tipo)) {
                return tipo;
            }
            EntradaTabla entrada = entradaDeReferenciaAtributo(referencia);
            return entrada != null ? efectivo(entrada.tipoDato) : Tipos.INDEFINIDO;
        }
        return Tipos.INDEFINIDO;
    }

    private String efectivo(String tipo) {
        if (tipo == null) {
            return Tipos.INDEFINIDO;
        }
        return entorno != null ? entorno.tipoEfectivo(tipo) : tipo;
    }

    private String tipoAsm(String tipo) {
        String efectivo = efectivo(tipo);
        if (Tipos.DOUBLEF.equals(efectivo)) {
            return "DQ";
        }
        if (Tipos.ULONGINT.equals(efectivo)) {
            return "DD";
        }
        return null;
    }

    /*
      Devuelve la referencia ASM para un operando de terceto:
      - EntradaTabla -> nombre ASM de la tabla de simbolos
      - Integer -> nombre del auxiliar @auxN del terceto referenciado
      - ReferenciaAtributo -> entrada materializada objeto.atr / objeto.clase.atr
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
            return aux != null ? aux : "@aux" + n;
        }
        if (op instanceof ReferenciaAtributo) {
            EntradaTabla entrada = entradaDeReferenciaAtributo((ReferenciaAtributo) op);
            if (entrada != null) {
                return tablaSimbolos.nombreAsmDe(entrada);
            }
            return sanitizarAsm(((ReferenciaAtributo) op).texto);
        }
        if (op instanceof String) {
            return (String) op;
        }
        return "?";
    }

    private boolean esFlotante(String tipo) {
        return Tipos.DOUBLEF.equals(efectivo(tipo));
    }

    private String sanitizarAsm(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "sym";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        String limpio = sb.toString();
        while (limpio.contains("__")) {
            limpio = limpio.replace("__", "_");
        }
        if (limpio.isEmpty()) {
            return "sym";
        }
        if (Character.isDigit(limpio.charAt(0))) {
            limpio = "v_" + limpio;
        }
        return limpio;
    }
}
