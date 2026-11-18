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

/*
  Generador de archivo assembler MASM32 32-bit (TP4, Prompts 2, 3, 4a..4e).

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
    * Prompt 4c: funciones/metodos con stack frame (EBP), argumentos en pila,
      retorno en EAX/ST(0), llamadas directas y 'this' implicito en [ebp+8].
    * Prompt 4d: objetos/atributos (ReferenciaAtributo, COPY posicional,
    *   CLASE/FIN_CLASE como marcadores; atributos en .DATA).
    * Prompt 4e: despacho explicito de todos los operadores del IR; el unico
    *   fallback a comentario es para operadores desconocidos (defensivo).
    * Prompt 5.0: infraestructura de chequeos runtime (mensajes de error en
    *   .DATA, labels de fallo en .CODE y helper emitirGuardia). Ningun chequeo
    *   esta activo aun; los paquetes 5.b/5.e/5.f/5.h activaran los suyos.

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

  Convencion de llamada interna (Prompt 4c), cdecl-like de 32 bits:
    - EL CALLER reserva y limpia el area de argumentos. No se usa PROC/ret n
      para que la limpieza sea explicita y sirva igual para varargs.
    - Prologue: 'push ebp / mov ebp,esp / sub esp,frame'. 'leave / ret' en
      cada 'ret' y como sufijo de FIN_FUNCION/FIN_METODO.
    - Argumentos por ranuras segun el ORDEN DE DECLARACION (no de evaluacion).
      Los PAR se materializan en orden IR (orden de evaluacion) pero escriben
      en su ranura formal; asi 'ebp+8' siempre es el primer parametro.
    - ulongint = 4 bytes; doublef = 8 bytes (dos dwords, high primero).
    - Metodos: 'this' es un argumento oculto en [esp] / [ebp+8]; los formales
      arrancan en [esp+4] / [ebp+12]. El receptor se pasa como OFFSET (4d
      definira el layout real del objeto; aqui se reserva un DD placeholder).
    - Despacho directo: el IR ya trae el metodo resuelto (prefijo/herencia),
      por eso no hay vtable. Etiquetas fn_<clave> y mt_<clave>.
    - Parametros y variables locales NO se emiten en .DATA: viven en el stack
      frame y 'operandoAsm' los resuelve como [ebp+desplazamiento].

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

    /* Mensajes de error runtime (Prompt 5.0). Se emiten solo los que son
       necesarios por algun chequeo activo (5.b/5.e/5.f/5.h). */
    private static final String MSG_OVERFLOW_SUMA = "msg_overflow_suma";
    private static final String MSG_OVERFLOW_FLOAT = "msg_overflow_float";
    private static final String MSG_NEGATIVO_RESTA = "msg_negativo_resta";
    private static final String MSG_ENUM_INVALIDO = "msg_enum_invalido";
    private boolean usaMsgOverflowSuma = false;
    private boolean usaMsgOverflowFloat = false;
    private boolean usaMsgNegativoResta = false;
    private boolean usaMsgEnumInvalido = false;
    /* Contador para generar etiquetas unicas de salto de exito en el chequeo
       runtime de asignacion a enumerado (Prompt 5.h). */
    private int contadorEnumChequeo = 0;

    /* ---------- Prompt 4c: funciones, metodos y stack frame ---------- */

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
    // Simbolos TS (parametros y locales) mapeados a su direccion [ebp+/-n].
    private final Map<EntradaTabla, String> slotStackPorSimbolo = new HashMap<>();
    // PAR se materializa recien en el CALL, para reservar las ranuras por formal.
    private final List<ParPendiente> parametrosPendientes = new ArrayList<>();
    // Objetos usados como receptor: placeholder DD hasta el layout del Prompt 4d.
    private final Set<String> objetosThis = new LinkedHashSet<>();
    private Entorno.FuncionDeclarada funcionActual = null;

    /* ---------- Mensajes de error por consola en el .asm generado ---------- */
    // Siguen el patron de ASSETS/asmwhello_console.asm: StdOut + ExitProcess.
    // Se emiten cuando se ejecuta el .asm, no como comentarios estaticos.
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
        Set<String> nombresEnData = emitirSimbolosDeTablaDeSimbolos();
        emitirReceptoresThis(nombresEnData);
        emitirTemporalesAux();
        emitirMarcadorEstadoFPU();
        emitirFormatosPout();
        emitirMensajesErrorRuntime();
        emitirCadenasErrorGeneradas();
        salida.append("\n");
    }

    /*
      Emite los símbolos de la tabla de símbolos (variables, parámetros,
      atributos, constantes y cadenas) que no viven en el stack frame.
      Devuelve los nombres ya emitidos para evitar duplicados con los
      receptores this.
    */
    private Set<String> emitirSimbolosDeTablaDeSimbolos() {
        List<TablaSimbolos.SimboloDataAsm> simbolos = tablaSimbolos.colectarSimbolosData(entorno);
        Set<String> nombresEnData = new HashSet<>();
        for (TablaSimbolos.SimboloDataAsm s : simbolos) {
            // 4c: parametros y variables locales viven en el stack frame.
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

    /*
      Emite receptores de CALLMET: solo un puntero mientras 4d no defina el
      layout. Se omite si ya fue emitido como símbolo de la tabla.
    */
    private void emitirReceptoresThis(Set<String> nombresEnData) {
        // Receptores de CALLMET: solo un puntero mientras 4d no defina el layout.
        for (String nombre : objetosThis) {
            if (!nombresEnData.contains(nombre)) {
                salida.append("    ").append(nombre).append(" DD ?")
                      .append(" ; this (layout en 4d)\n");
            }
        }
    }

    /*
      Emite temporales implicitos @auxN con tipo ASM conocido (DD/DQ).
    */
    private void emitirTemporalesAux() {
        // Temporales implicitos @auxN con tipo ASM conocido (DD/DQ)
        for (ResultadoTerceto r : resultados.values()) {
            if (r.produceValor() && r.tipoAsmDato() != null) {
                salida.append("    ").append(r.nombreAuxiliar()).append(" ")
                      .append(r.tipoAsmDato()).append(" ?\n");
            }
        }
    }

    /*
      Emite la palabra de estado de la FPU, solo si hay comparaciones doublef.
    */
    private void emitirMarcadorEstadoFPU() {
        // Palabra de estado de la FPU, solo si hay comparaciones doublef.
        if (usaFpuStatus) {
            salida.append("    ").append(FPU_STATUS).append(" DW ?\n");
        }
    }

    /*
      Emite los formatos de impresión, solo los que necesitan los tercetos pout.
    */
    private void emitirFormatosPout() {
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
    }

    /*
      Emite los mensajes de error runtime del Prompt 5, solo los que son
      necesarios según los chequeos activos (5.b, 5.e, 5.f, 5.h).
    */
    private void emitirMensajesErrorRuntime() {
        // Mensajes de error runtime (Prompt 5.0), solo los que son necesarios.
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

    /*
      Emite cadenas de error detectadas durante la generacion de ASM.
      Se declaran en .DATA para poder imprimirse por consola al ejecutar
      el .asm generado, siguiendo el patron de ASSETS/asmwhello_console.asm
      (StdOut + ExitProcess).
    */
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
                case "PROGRAMA":
                case "FIN_PROGRAMA":
                    // Marcadores estructurales del IR: no emiten instrucciones.
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
                case "FUNCION":
                case "METODO":
                    emitirPrologo(numero, op, op1);
                    break;
                case "FIN_FUNCION":
                case "FIN_METODO":
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
                    // Marcadores de clase: no emiten instrucciones en .CODE.
                    // Los atributos de los objetos se emiten en .DATA via emitirData().
                    break;
                default:
                    // Unico fallback: etiquetas IR label_N y operadores
                    // desconocidos (defensivo, no deberian aparecer).
                    if (esEtiqueta(op)) {
                        emitirEtiqueta(op);
                    } else {
                        salida.append("    ; operador no reconocido: ")
                              .append(t.visualizar()).append("\n");
                    }
                    break;
            }
        }

        // Labels de fallo runtime (Prompt 5.0), solo los que son necesarios.
        // Cada label imprime su mensaje y termina con ExitProcess.
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
            EntradaTabla entrada = (EntradaTabla) op;
            // Dentro de una funcion, parametros y locales son desplazamientos
            // del frame; fuera de ella se mantienen como simbolos de .DATA.
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
            // Buscar primero la entrada especifica del objeto en la TS
            // (nombre calificado "objeto.atributo" o "objeto.clase.atributo",
            // ambito del objeto). Cada objeto tiene sus propias entradas de
            // atributo creadas por Entorno.materializarAtributosDeObjeto.
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
            // Fallback: usar la entrada del atributo declarada en la clase.
            // El atributo materializado por objeto vive en la TS con un nombre
            // ASM unico. 'texto' es solo la vista IR (obj.atr / obj.clase.atr)
            // y no es un identificador MASM valido.
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
            // 5.e: despues de fmul de doublef, detectar overflow/infinito o
            // resultado fuera del rango representable (FXAM + FSTSW/SAHF ->
            // jc salta si C0=1, es decir +infinito/-infinito/NaN).
            if ("*".equals(operador) && usaMsgOverflowFloat) {
                salida.append("    fld ").append(destino).append("\n");
                salida.append("    fxam\n");
                salida.append("    fstsw ax\n");
                salida.append("    sahf\n");
                salida.append("    jc error_overflow_float\n");
            }
        } else {
            // Entero: mov eax, op1; <op> eax, op2; mov destino, eax
            salida.append("    mov eax, ").append(o1).append("\n");
            switch (operador) {
                case "+":
                    salida.append("    add eax, ").append(o2).append("\n");
                    // 5.b: chequeo de overflow en suma de ulongint (acarreo CF=1).
                    if (usaMsgOverflowSuma) {
                        salida.append("    jc error_overflow_suma\n");
                    }
                    break;
                case "-":
                    salida.append("    sub eax, ").append(o2).append("\n");
                    // 5.f: chequeo de resultado negativo en resta de ulongint (préstamo CF=1).
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

        // fild origen (entero de 32 bits); fstp destino (doublef)
        salida.append("    fild ").append(origen).append("\n");
        salida.append("    fstp ").append(destino).append("\n");
    }

    private void emitirCopiaPorTipo(String tipo, String origen, String destino) {
        if (esFlotante(tipo)) {
            // Copiar doublef: cargar en FPU y guardar en destino DQ
            salida.append("    fld ").append(origen).append("\n");
            salida.append("    fstp ").append(destino).append("\n");
        } else {
            // Copiar ulongint: cargar en EAX y guardar en destino DD
            salida.append("    mov eax, ").append(origen).append("\n");
            salida.append("    mov ").append(destino).append(", eax\n");
        }
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

        emitirCopiaPorTipo(tipoResultado, src, dst);
    }

    private void emitirAsignacionSentencia(Object destino, Object origen) {
        String dst = operandoAsm(destino);
        String src = operandoAsm(origen);

        if (dst.equals("--") || src.equals("--")) {
            salida.append("    ; := sentencia  (operandos invalidos)\n");
            return;
        }

        // 5.h: chequeo runtime para asignacion a variable de tipo enumeracion.
        // El valor ya cargado se compara contra cada valor permitido del enum;
        // si ninguno coincide, se salta a error_enum_invalido.
        if (destino instanceof EntradaTabla && usaMsgEnumInvalido) {
            EntradaTabla destEntry = (EntradaTabla) destino;
            Entorno.TipoEnumerado enumTipo =
                    entorno != null ? entorno.getTipoEnumerado(destEntry.tipoDato) : null;
            if (enumTipo != null && Tipos.ULONGINT.equals(enumTipo.tipoBase)) {
                String etiquetaOk = "enum_valido_" + contadorEnumChequeo++;
                salida.append("    mov eax, ").append(src).append("\n");
                boolean algunoValido = false;
                for (String valorLexema : enumTipo.valores) {
                    BigInteger valor = Tipos.valorEntero(valorLexema);
                    if (valor != null) {
                        salida.append("    cmp eax, ").append(valor.intValue()).append("\n");
                        salida.append("    je ").append(etiquetaOk).append("\n");
                        algunoValido = true;
                    }
                }
                if (!algunoValido) {
                    // Enumerado sin valores declarados: cualquier valor es invalido.
                    salida.append("    jmp error_enum_invalido\n");
                    salida.append(etiquetaOk).append(":\n");
                    emitirCopiaPorTipo(Tipos.ULONGINT, src, dst);
                    return;
                }
                salida.append("    jmp error_enum_invalido\n");
                salida.append(etiquetaOk).append(":\n");
                // Solo se copia al destino si el valor pertenece al enum.
                emitirCopiaPorTipo(Tipos.ULONGINT, src, dst);
                return;
            }
        }

        // Determinar el tipo del destino: EntradaTabla (variable/parametro) o
        // ReferenciaAtributo (atributo de objeto).
        boolean flotante = false;
        if (destino instanceof EntradaTabla) {
            flotante = esFlotante(((EntradaTabla) destino).tipoDato);
        } else if (destino instanceof ReferenciaAtributo) {
            flotante = esFlotante(((ReferenciaAtributo) destino).getTipo());
        }

        emitirCopiaPorTipo(flotante ? Tipos.DOUBLEF : Tipos.ULONGINT, src, dst);
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
      Calcula qué chequeos runtime del Prompt 5 deben activarse.
      Para 5.b: si hay algún terceto de suma '+' con tipo ulongint, se activa
      el mensaje y label de error_overflow_suma.
    */
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

        // 5.e: si hay al menos una multiplicacion '*' de tipo doublef, se activa
        // el mensaje y label de error_overflow_float para chequear overflow
        // (infinito / fuera de rango) tras cada fmul de punto flotante.
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

        // 5.f: si hay al menos una resta '-' de tipo ulongint, se activa
        // el mensaje y label de error_negativo_resta para chequear
        // resultado negativo en aritmética sin signo.
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

        // 5.h: si hay al menos una asignacion ':= ' a una variable de tipo
        // enumeracion, se activa el mensaje y label de error_enum_invalido para
        // chequear en runtime que el valor asignado pertenezca al enum.
        usaMsgEnumInvalido = false;
        for (Terceto t : tercetos) {
            if (!":=".equals(t.getOperador())) {
                continue;
            }
            Object op1 = t.getOp1();
            if (!(op1 instanceof EntradaTabla)) {
                continue;
            }
            EntradaTabla destino = (EntradaTabla) op1;
            if (destino.tipoDato != null
                    && entorno != null
                    && entorno.getTipoEnumerado(destino.tipoDato) != null) {
                usaMsgEnumInvalido = true;
                break;
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

    /* ---------- Funciones y metodos (Prompt 4c) ---------- */

    /*
      Las declaraciones del IR traen solo el nombre corto ('foo', 'm'), mientras
      que los CALL traen la clave completa ('foo:main', 'm:main:padre'). Se arma
      un mapo desde la TS/Entorno y se consume en orden de aparicion para no
      confundir metodos con el mismo nombre en clases distintas.
    */
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
        // Los metodos viven en la ClaseDeclarada, no en el mapa de funciones.
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
    // para que esten disponibles sin tener que instanicar a un  GeneradorA
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

    /*
      Frame por declaracion. Los formales se numeran desde [ebp+8] (o +12 en
      metodos, por 'this') en orden de DECLARACION; las locales crecen hacia
      direcciones menores. doublef ocupa 8 bytes y se alinea a 8.
    */
    private void prepararStackFrames() {
        framePorDeclaracion.clear();
        Set<Entorno.FuncionDeclarada> vistas =
                Collections.newSetFromMap(new HashMap<Entorno.FuncionDeclarada, Boolean>());
        for (Entorno.FuncionDeclarada f : declaracionPorTerceto.values()) {
            if (f == null || !vistas.add(f)) {
                continue;
            }
            int corriente = f.esMetodo ? 12 : 8; // 'this' ocupa [ebp+8]
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
                        locales += 4; // alineacion a 8 del doublef local
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
            // doublef: se deja en ST(0); el CALL lo baja a su @aux con FSTP.
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
        // Se materializa recien en el CALL: aqui solo se registra el orden IR.
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
                // Se cae al calculo por los PAR pendientes.
            }
        }
        List<ParPendiente> args = extraerParametros(cantidad);
        Entorno.FuncionDeclarada f = declaracionPorClave.get(clave);

        /*
          Ranuras por FORMAL (no por orden de evaluacion). Asi un PAR que
          proviene de un [2,1] escribe igual en el primer parametro declarado.
        */
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
                // Objeto local: aun no hay layout (4d), se pasa la direccion del slot.
                salida.append("    lea eax, ").append(base).append("\n");
            } else if (base.startsWith("[")) {
                // Parametro/'this': el slot ya guarda el offset del objeto.
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
                // qword: high dword en la direccion alta, low dword en la baja.
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

    /* ---------- Mensajes de error por consola (patron ASSETS/asmwhello_console.asm) ---------- */

    /*
      Registra un mensaje de error que se declarara en .DATA y se imprimira por
      consola al ejecutar el .asm generado, usando StdOut + ExitProcess.
      Devuelve el nombre ASM de la cadena para poder invocarla luego.
    */
    private String registrarCadenaError(String texto) {
        if (cadenasErrorPorTexto.containsKey(texto)) {
            return cadenasErrorPorTexto.get(texto);
        }
        String nombre = "msg_error_" + (++contadorCadenasError);
        cadenasErrorPorTexto.put(texto, nombre);
        return nombre;
    }

    /*
      Emite en .CODE: imprime el mensaje por consola con StdOut y termina con
      el codigo de salida dado. Sigue el patron de ASSETS/asmwhello_console.asm.
    */
    private void emitirErrorYSaliendo(String nombreCadena, int codigoSalida) {
        salida.append("    invoke StdOut, addr ").append(nombreCadena).append("\n");
        salida.append("    invoke ExitProcess, ").append(codigoSalida).append("\n");
    }

    /* ---------- Prompt 4d: objetos y atributos ---------- */

    /*
      Copia estructural (COPY) entre objetos compatibles. La compatibilidad la
      decidio la semantica con Entorno.clasesCompatibles(): mismo layout de
      tipos, bases primero y propios despues; los NOMBRES de los atributos no
      tienen que coincidir. Por eso la copia es POSICIONAL: se recorren los
      layouts efectivos de ambas clases en paralelo y se busca, para cada
      posicion, la entrada materializada de cada objeto en la TS.

      Como los atributos se emiten en .DATA como simbolos planos y dispersos
      (uno por atributo, no como bloque contiguo), la copia se hace campo-a-campo:
      un mov por atributo ulongint/enum y dos mov por atributo doublef
      (high dword primero).
    */
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
                // doublef: dos dwords; high en la direccion alta.
                String baseOrigen = comoMemoria(asmOrigen);
                String baseDest = comoMemoria(asmDest);
                salida.append("    mov eax, dword ptr ").append(conDesplazamiento4(baseOrigen)).append("\n");
                salida.append("    mov dword ptr ").append(conDesplazamiento4(baseDest)).append(", eax\n");
                salida.append("    mov eax, dword ptr ").append(baseOrigen).append("\n");
                salida.append("    mov dword ptr ").append(baseDest).append(", eax\n");
            } else {
                // ulongint / enumerado: copiar un dword.
                salida.append("    mov eax, ").append(asmOrigen).append("\n");
                salida.append("    mov ").append(asmDest).append(", eax\n");
            }
        }
    }

    /* Un atributo del layout efectivo, con la clase que lo declara. */
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

    /*
      Layout efectivo adoptado de Entorno.clasesCompatibles(): bases primero
      (heredaDe, en orden), luego atributos propios. Un atributo propio con el
      mismo nombre que uno heredado reemplaza al heredado SIN moverlo de
      posicion (LinkedHashMap.put conserva el orden de insercion).
    */
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

    /*
      Clave TS del atributo materializado para un objeto. Entorno crea
      `objeto.atributo` para los propios y `objeto.base.atributo` para los
      heredados; se usa el NOMBRE CORTO de la clase declarante como prefijo.
    */
    private String claveAtributoDeObjeto(String nombreObjeto,
            Entorno.ClaseDeclarada claseObjeto, AtributoPosicional atributo) {
        boolean esPropia = atributo.declarante.clave().equals(claseObjeto.clave());
        return esPropia
                ? nombreObjeto + "." + atributo.nombre
                : nombreObjeto + "." + atributo.declarante.nombre + "." + atributo.nombre;
    }

    /* ---------- Prompt 5.0: infraestructura de chequeos runtime ---------- */

    /*
      Emitir una guardia condicional: si se cumple la condicion, salta al label
      de fallo correspondiente. Se usa desde los paquetes 5.b/5.e/5.f/5.h para
      insertar chequeos runtime antes/despues de operaciones aritmeticas o de
      asignacion.

      Ejemplo de uso (paquete 5.b, overflow en suma de ulongint):
        emitirGuardia("jc", "error_overflow_suma");   // salta si acarreo CF=1

      La condicion ASM (p.ej. "je", "jne", "jc") la decide el llamante segun
      el tipo de comparacion previa (FPU, enteros, etc.).
    */
    private void emitirGuardia(String condicionSalto, String labelError) {
        salida.append("    ").append(condicionSalto).append(" ").append(labelError).append("\n");
    }
}
