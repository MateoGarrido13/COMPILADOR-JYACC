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
  Generador de archivo assembler MASM32 32-bit (TP4, Prompts 2 y 3).

  Emite un archivo .asm con:
  - encabezado .386 / .model flat, stdcall / option casemap:none /
    include masm32rt.inc
  - seccion .DATA construida desde la Tabla de Simbolos (colectarSimbolosData)
    y los temporales @auxN con tipo conocido (ResultadoTerceto).
  - seccion .CODE con traduccion basica de tercetos (Prompt 3):
    asignacion :=, aritmetica + - * / (enteros via EAX, floats via FPU),
    conversion todf. El resto de operadores se emiten como comentario.

  No altera la salida observable de TP3 (consola/tercetos/TS).
 */
public class GeneradorAssembler {

    private final List<Terceto> tercetos;
    private final TablaSimbolos tablaSimbolos;
    private final Entorno entorno;
    private final Map<Integer, ResultadoTerceto> resultados;
    private final Map<Integer, String> auxPorTerceto = new HashMap<>();
    private final StringBuilder salida = new StringBuilder();

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
                default:
                    // Operadores estructurales, control, IO, llamadas: comentario en Prompt 3
                    salida.append("    ; ").append(t.visualizar()).append("\n");
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
            return ((ReferenciaAtributo) op).texto;
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
}
