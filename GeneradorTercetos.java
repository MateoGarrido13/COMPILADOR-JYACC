import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
  Recorre las reducciones y emite tercetos.
  Las expresiones se pliegan a izquierda dentro de cada nivel de precedencia.
  El if apila el BF al terminar la condicion. Al cerrar el then lo completa
  con una etiqueta label_N. Con else, abre un BI hacia otra etiqueta.
  El repeat abre la label de inicio al reducirse INICIO_REPEAT, antes del
  cuerpo. Al cerrar, BF sale si la condicion es falsa y BI vuelve a esa label.

  Convencion de invocaciones:
    funcion: (PAR, formal, real) ... (CALL, funcion, cantidadDeParametros)
    metodo:  (PAR, formal, real) ... (CALLMET, objeto, metodo)
  El resultado de la invocacion es el numero del terceto CALL/CALLMET.
 */
public class GeneradorTercetos {

    private static final class Pendiente {
        /* Esta clase es un contenedor para los datos de un salto pendiente.
        numero es el numero del terceto pendiente, incondicional es true si es un salto incondicional */
        final int numero;
        final boolean incondicional;

        Pendiente(int numero, boolean incondicional) {
            this.numero = numero;
            this.incondicional = incondicional;
        }
    }

    private final ListaTercetos lista = new ListaTercetos();
    private final Reporte reporte;
    private final Deque<Pendiente> pila = new ArrayDeque<>(); // pila de saltos pendientes
    private final Deque<String> iniciosRepeat = new ArrayDeque<>(); // pila de etiquetas de inicio de repeat
    private final Deque<Integer> marcasRepeat = new ArrayDeque<>(); // tercetos previos al inicio de cada repeat
    private final Deque<Integer> marcasClase = new ArrayDeque<>(); // terceto CLASE de cada clase abierta
    // Metadata de resultado por terceto (TP4/ASM): tipo y si produce valor [n].
    private final Map<Integer, ResultadoTerceto> resultados = new LinkedHashMap<>();
    // Numero centinela para un salto que no debe emitirse porque la condicion ya
    // reporto un error. Mantiene balanceada la pila de saltos en ifs anidados.
    private static final int SALTO_INVALIDO = -1;

    public GeneradorTercetos(Reporte reporte) {
        this.reporte = reporte;
    }

    public ListaTercetos getLista() {
        return lista;
    }

    public List<Terceto> getTercetos() {
        return lista.elementos();
    }

    public Map<Integer, ResultadoTerceto> getResultados() {
        return Collections.unmodifiableMap(resultados);
    }

    public ResultadoTerceto getResultado(int indice) {
        return resultados.get(indice);
    }

    private int crearTerceto(String operador, Object op1, Object op2, String tipoResultado, boolean produceValor) {
        int n = lista.crear(operador, op1, op2);
        resultados.put(n, new ResultadoTerceto(n, operador, tipoResultado, produceValor));
        return n;
    }

    private int crearNoProductor(String operador, Object op1, Object op2) {
        return crearTerceto(operador, op1, op2, null, false);
    }

    private int crearEtiquetaConResultado(String nombre) {
        return crearTerceto(nombre, null, null, null, false);
    }

    private String tipoDeOperando(Object operando) {
        if (operando == null) {
            return Tipos.INDEFINIDO;
        }
        if (operando instanceof Integer) {
            ResultadoTerceto r = resultados.get((Integer) operando);
            return r == null ? Tipos.INDEFINIDO : r.getTipo();
        }
        if (operando instanceof ReferenciaAtributo) {
            return ((ReferenciaAtributo) operando).getTipo();
        }
        if (operando instanceof EntradaTabla) {
            return ExpresionDiferida.tipoDe(operando);
        }
        if (operando instanceof LlamadaFuncion) {
            return ((LlamadaFuncion) operando).getTipoRetorno();
        }
        return ExpresionDiferida.tipoDe(operando);
    }

    private String tipoAritmetico(Object izq, Object der) {
        String t = tipoDeOperando(izq);
        if (!Tipos.INDEFINIDO.equals(t)) {
            return t;
        }
        return tipoDeOperando(der);
    }

    public void generar(List<Reduccion> reducciones) {
        if (reducciones == null) {
            return;
        }
        resultados.clear();
        crearTerceto("PROGRAMA", null, null, null, false);
        for (Reduccion reduccion : reducciones) {
            switch (reduccion.getRegla()) {
                case INICIO_FUNCION:
                    abrirFuncion(reduccion);
                    break;
                case DECL_FUNCION:
                case DECL_METODO:
                    cerrarFuncion(reduccion);
                    break;
                case INICIO_CLASE:
                    abrirClase(reduccion);
                    break;
                case DECL_CLASE:
                    cerrarClase(reduccion);
                    break;
                case ASIGNACION:
                    generarAsignacion(reduccion);
                    break;
                case INVOCACION_FUNCION:
                case INVOCACION_METODO:
                case INVOCACION_METODO_PREFIJADO:
                    generarInvocacion(reduccion);
                    break;
                case CONDICION_IF:
                    generarCondicionIf(reduccion);
                    break;
                case SENTENCIA_POUT:
                    generarPout(reduccion);
                    break;
                case SENTENCIA_RET:
                    generarRet(reduccion);
                    break;
                case BLOQUE_THEN:
                    cerrarThen(reduccion);
                    break;
                case SENTENCIA_IF:
                case SENTENCIA_IF_ELSE:
                case ERR_FALTA_END_IF:
                    cerrarSalto(reduccion);
                    break;
                case INICIO_REPEAT:
                    abrirRepeat();
                    break;
                case SENTENCIA_REPEAT_WHILE:
                    generarRepeat(reduccion);
                    break;
                case CERRAR_REPEAT:
                    descartarRepeat();
                    break;
                default:
                    break;
            }
        }
        crearNoProductor("FIN_PROGRAMA", null, null);
    }

    public String contenido() {
        return lista.contenido();
    }

    /* Delimitadores de funcion y metodo tomados de la firma que devuelve la accion. */
    private void abrirFuncion(Reduccion reduccion) {
        Object valor = reduccion.resultado();
        if (!(valor instanceof Entorno.FuncionDeclarada)) {
            return;
        }
        Entorno.FuncionDeclarada funcion = (Entorno.FuncionDeclarada) valor;
        if (funcion.nombre == null) {
            return;
        }
        crearNoProductor(funcion.esMetodo ? "METODO" : "FUNCION", funcion.nombre, null);
    }

    private void cerrarFuncion(Reduccion reduccion) {
        Object valor = reduccion.resultado();
        if (!(valor instanceof Entorno.FuncionDeclarada)) {
            return;
        }
        Entorno.FuncionDeclarada funcion = (Entorno.FuncionDeclarada) valor;
        if (funcion.nombre == null) {
            return;
        }
        crearNoProductor(funcion.esMetodo ? "FIN_METODO" : "FIN_FUNCION", funcion.nombre, null);
    }

    private void abrirClase(Reduccion reduccion) {
        Object valor = reduccion.resultado();
        if (!(valor instanceof Entorno.ClaseDeclarada)) {
            return;
        }
        Entorno.ClaseDeclarada clase = (Entorno.ClaseDeclarada) valor;
        if (clase.nombre != null) {
            marcasClase.push(crearNoProductor("CLASE", clase.nombre, null));
        } else {
            marcasClase.push(0);
        }
    }

    private void cerrarClase(Reduccion reduccion) {
        Object valor = reduccion.resultado();
        if (!(valor instanceof Entorno.ClaseDeclarada)) {
            return;
        }
        Entorno.ClaseDeclarada clase = (Entorno.ClaseDeclarada) valor;
        int marca = marcasClase.isEmpty() ? 0 : marcasClase.pop();
        if (!clase.valida) {
            // La clase no quedo declarada: tambien se descarta su codigo.
            if (marca > 0) {
                lista.descartarDesde(marca - 1);
            }
            return;
        }
        if (clase.nombre != null) {
            crearNoProductor("FIN_CLASE", clase.nombre, null);
        }
    }

    private void generarAsignacion(Reduccion reduccion) {
        Object resultado = reduccion.resultado();
        // La accion semantica devuelve null cuando la asignacion ya reporto un
        // error (destino no asignable, tipos incompatibles o expresion invalida).
        if (resultado == null) {
            return;
        }
        // Entre objetos compatibles el resultado es una Asignacion con destino
        // EntradaTabla y el terceto es (COPY, destino, origen). Una asignacion
        // a objeto.atributo tambien es una Asignacion, pero el destino es una
        // ReferenciaAtributo y el terceto es (:=, objeto.atributo, valor).
        boolean copiaEstructural = false;
        Object destino = resultado;
        if (resultado instanceof Asignacion) {
            destino = ((Asignacion) resultado).destino;
            copiaEstructural = !(destino instanceof ReferenciaAtributo);
        }
        if (!(destino instanceof EntradaTabla) && !(destino instanceof ReferenciaAtributo)) {
            destino = reduccion.operando(0);
        }
        if (!(destino instanceof EntradaTabla) && !(destino instanceof ReferenciaAtributo)) {
            reporte.error(reduccion.getLinea(), "No se puede asignar un valor a una cte o variable sin referencia");
            return;
        }
        Object valorOrigen = reduccion.operando(1);
        Object origen = emitir(valorOrigen, reduccion.getLinea());
        if (origen == null) {
            // Si habia un valor semantico (invocacion o expresion) pero resulto
            // invalido, su error ya se reporto y no se agrega ruido. Solo se
            // reporta cuando falta el operando por un error de sintaxis.
            if (valorOrigen == null) {
                reporte.error(reduccion.getLinea(), "Asignacion sin operando");
            }
            return;
        }
        crearNoProductor(copiaEstructural ? "COPY" : ":=", destino, origen);
    }

    private void generarPout(Reduccion reduccion) {
        Object valor = reduccion.resultado();
        if (valor == null) {
            return;
        }
        Object operando = emitir(valor, reduccion.getLinea());
        if (operando != null) {
            crearNoProductor("pout", operando, null);
        }
    }

    private void generarRet(Reduccion reduccion) {
        Object valor = reduccion.resultado();
        if (valor == null) {
            return;
        }
        Object operando = emitir(valor, reduccion.getLinea());
        if (operando != null) {
            crearNoProductor("ret", operando, null);
        }
    }

    /*
      Genera los tercetos de invocacion de funcion o metodo:
      un PAR por cada parametro real (en el orden de evaluacion indicado)
      y un CALL con la cantidad de parametros, o un CALLMET con el objeto
      receptor y el metodo. El numero del terceto final queda en la llamada.
     */
    private void generarInvocacion(Reduccion reduccion) {
        Object valor = reduccion.resultado();
        if (!(valor instanceof LlamadaFuncion)) {
            return;
        }
        LlamadaFuncion llamada = (LlamadaFuncion) valor;
        if (!llamada.esValida() || llamada.funcion == null) {
            return;
        }
        if (llamada.esMetodo && llamada.objeto == null) {
            return;
        }
        List<ParametroReal> parametros = llamada.parametros;

        // Determina el orden de evaluacion. Si no hay [], se usa el orden fuente 1..n.
        List<Integer> posiciones;
        if (llamada.ordenEvaluacion == null || llamada.ordenEvaluacion.isEmpty()) {
            posiciones = new ArrayList<>();
            for (int i = 1; i <= parametros.size(); i++) {
                posiciones.add(i);
            }
        } else {
            posiciones = llamada.ordenEvaluacion;
        }

        // Emite un PAR por cada parametro, en el orden de evaluacion indicado.
        for (int pos : posiciones) {
            if (pos < 1 || pos > parametros.size()) {
                return; // ya fue reportado por Verificaciones
            }
            ParametroReal param = parametros.get(pos - 1);
            if (param.formal == null) {
                return; // sin formal resuelto no se puede emitir una copia valida
            }
            Object valorParam = emitir(param.valor, reduccion.getLinea());
            if (valorParam == null) {
                return;
            }
            crearNoProductor("PAR", param.formal, valorParam);
        }

        // Emite el CALL/CALLMET y guarda su numero para que emitir(LlamadaFuncion) lo devuelva.
        int numeroCall;
        String tipoRet = llamada.getTipoRetorno();
        if (llamada.esMetodo) {
            numeroCall = crearTerceto("CALLMET", llamada.objeto, llamada.funcion, tipoRet, true);
        } else {
            numeroCall = crearTerceto("CALL", llamada.funcion,
                    String.valueOf(parametros.size()), tipoRet, true);
        }
        llamada.setNumeroCall(numeroCall);
    }

    private void generarCondicionIf(Reduccion reduccion) {
        Integer comparacion = emitirComparacion(reduccion.operando(0), reduccion.getLinea());
        if (comparacion == null) {
            // La condicion ya reporto un error: se apila un BF centinela para
            // que los cierres del if no emitan saltos ni etiquetas.
            pila.push(new Pendiente(SALTO_INVALIDO, false));
            return;
        }
        int bf = crearNoProductor("BF", comparacion, null);
        pila.push(new Pendiente(bf, false));
    }

    /* Se reduce al leer repeat, antes de generar el cuerpo. */
    private void abrirRepeat() {
        marcasRepeat.push(lista.cantidad());
        String etiqueta = lista.reservarEtiqueta();
        crearEtiquetaConResultado(etiqueta);
        iniciosRepeat.push(etiqueta);
    }

    private void descartarRepeat() {
        if (!iniciosRepeat.isEmpty()) {
            iniciosRepeat.pop();
        }
        if (!marcasRepeat.isEmpty()) {
            marcasRepeat.pop();
        }
    }

    /* BF sale si la condicion es falsa. Si no se toma, el BI vuelve al inicio. */
    private void generarRepeat(Reduccion reduccion) {
        if (iniciosRepeat.isEmpty()) {
            reporte.error(reduccion.getLinea(), "Iteracion incompleta");
            return;
        }
        String inicio = iniciosRepeat.pop();
        int marca = marcasRepeat.isEmpty() ? lista.cantidad() : marcasRepeat.pop();
        Integer comparacion = emitirComparacion(reduccion.operando(1), reduccion.getLinea());
        if (comparacion == null) {
            // La condicion ya reporto un error: se retira la etiqueta de inicio
            // y el cuerpo del ciclo para no dejar codigo intermedio incompleto.
            lista.descartarDesde(marca);
            return;
        }
        String salida = lista.reservarEtiqueta();
        crearNoProductor("BF", comparacion, salida);
        crearNoProductor("BI", inicio, null);
        crearEtiquetaConResultado(salida);
    }

    private Integer emitirComparacion(Object valor, int linea) {
        if (valor == null) {
            // CONDICION devuelve null cuando ya reporto un error de tipos.
            return null;
        }
        if (!(valor instanceof Object[]) || ((Object[]) valor).length < 3) {
            reporte.error(linea, "Condicion sin operando");
            return null;
        }
        Object[] partes = (Object[]) valor;
        Object izquierdo = emitir(partes[0], linea);
        Object derecho = emitir(partes[2], linea);
        if (izquierdo == null || derecho == null || !(partes[1] instanceof String)) {
            reporte.error(linea, "Condicion sin operando");
            return null;
        }
        return crearTerceto((String) partes[1], izquierdo, derecho, Tipos.ULONGINT, true);
    }

    /* Completa el BF con la etiqueta del else, abre el BI y deja esa
       etiqueta como primer terceto de la rama else. */
    private void cerrarThen(Reduccion reduccion) {
        Pendiente bf = desapilar(reduccion.getLinea());
        if (bf == null) {
            return;
        }
        if (bf.numero == SALTO_INVALIDO) {
            // No hay BF que completar; se apila el BI centinela que cerrara la
            // sentencia, sin etiquetas ni saltos.
            pila.push(new Pendiente(SALTO_INVALIDO, true));
            return;
        }
        String etiquetaElse = lista.reservarEtiqueta();
        lista.completar(bf.numero, 2, etiquetaElse);
        int bi = crearNoProductor("BI", null, null);
        pila.push(new Pendiente(bi, true));
        crearEtiquetaConResultado(etiquetaElse);
    }

    /* If sin else, o el cierre del else: el salto pendiente apunta a una
       etiqueta nueva, que se emite a continuacion. ERR_FALTA_END_IF usa el
       mismo cierre para no dejar el BF o el BI vacios. */
    private void cerrarSalto(Reduccion reduccion) {
        Pendiente pendiente = desapilar(reduccion.getLinea()); // paso la linea por si hay error 
        if (pendiente == null || pendiente.numero == SALTO_INVALIDO) {
            return;
        }

 
        String etiqueta = lista.reservarEtiqueta();
        int posicion;
        if (pendiente.incondicional) {
            posicion = 1;
        } else {
            posicion = 2;
        }
        lista.completar(pendiente.numero, posicion, etiqueta);
        crearEtiquetaConResultado(etiqueta);
    }

    private Pendiente desapilar(int linea) {
        if (pila.isEmpty()) {
            reporte.error(linea, "Seleccion incompleta");
            return null;
        }
        return pila.pop();
    }

    // Devuelve la EntradaTabla hoja o el numero del terceto que resume la expresion.
    private Object emitir(Object valor, int linea) {
        if (valor instanceof EntradaTabla || valor instanceof Integer
                || valor instanceof ReferenciaAtributo || valor instanceof String) {
            return valor;
        }
        if (valor instanceof Conversion) {
            Conversion conversion = (Conversion) valor;
            Object convertido = emitir(conversion.getValor(), linea);
            if (convertido == null) {
                return null;
            }
            return crearTerceto("todf", convertido, null, Tipos.DOUBLEF, true);
        }
        if (valor instanceof AsignacionEnExpresion) {
            AsignacionEnExpresion asignacion = (AsignacionEnExpresion) valor;
            if (asignacion.destino == null) {
                return null;
            }
            Object derecho = emitir(asignacion.valor, linea);
            if (derecho == null) {
                return null;
            }
            String tipoDest = Tipos.INDEFINIDO;
            Object dest = asignacion.destino;
            if (dest instanceof EntradaTabla) {
                tipoDest = ((EntradaTabla) dest).tipoDato;
            } else if (dest instanceof ReferenciaAtributo) {
                tipoDest = ((ReferenciaAtributo) dest).getTipo();
            }
            return crearTerceto(":=", asignacion.destino, derecho, tipoDest, true);
        }
        if (valor instanceof LlamadaFuncion) {
            LlamadaFuncion llamada = (LlamadaFuncion) valor;
            if (!llamada.esValida()) {
                return null;
            }
            Integer numero = llamada.getNumeroCall();
            if (numero == null) {
                reporte.error(linea, "Invocacion de funcion sin terceto CALL");
                return null;
            }
            return numero;
        }
        if (!(valor instanceof ExpresionDiferida)) {
            return null;
        }
        ExpresionDiferida expresion = (ExpresionDiferida) valor;
        if (!expresion.esValida()) {
            // La expresion ya reporto un error; no se emite codigo intermedio.
            return null;
        }
        List<Object> operandos = expresion.getOperandos();
        List<Character> operadores = expresion.getOperadores();
        if (operandos.isEmpty()) {
            return null;
        }
        Object acumulado = emitir(operandos.get(0), linea);
        if (acumulado == null) {
            return null;
        }
        for (int i = 0; i < operadores.size(); i++) {
            if (i + 1 >= operandos.size()) {
                reporte.error(linea, "Expresion con menos operandos que operadores");
                return null;
            }
            Object derecho = emitir(operandos.get(i + 1), linea);
            if (derecho == null) {
                return null;
            }
            String tipoOp = tipoAritmetico(acumulado, derecho);
            acumulado = crearTerceto(String.valueOf(operadores.get(i)), acumulado, derecho, tipoOp, true);
        }
        return acumulado;
    }
}
