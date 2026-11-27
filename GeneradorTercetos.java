import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeneradorTercetos {

    private static final class Pendiente {

        final int numero;
        final boolean incondicional;

        Pendiente(int numero, boolean incondicional) {
            this.numero = numero;
            this.incondicional = incondicional;
        }
    }

    private final ListaTercetos lista = new ListaTercetos();
    private final Reporte reporte;
    private final Deque<Pendiente> pila = new ArrayDeque<>();
    private final Deque<String> iniciosRepeat = new ArrayDeque<>();
    private final Deque<Integer> marcasRepeat = new ArrayDeque<>();
    private final Deque<Integer> marcasClase = new ArrayDeque<>();

    private final Map<Integer, ResultadoTerceto> resultados = new LinkedHashMap<>();

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

        if (resultado == null) {
            return;
        }

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

        List<Integer> posiciones;
        if (llamada.ordenEvaluacion == null || llamada.ordenEvaluacion.isEmpty()) {
            posiciones = new ArrayList<>();
            for (int i = 1; i <= parametros.size(); i++) {
                posiciones.add(i);
            }
        } else {
            posiciones = llamada.ordenEvaluacion;
        }

        for (int pos : posiciones) {
            if (pos < 1 || pos > parametros.size()) {
                return;
            }
            ParametroReal param = parametros.get(pos - 1);
            if (param.formal == null) {
                return;
            }
            Object valorParam = emitir(param.valor, reduccion.getLinea());
            if (valorParam == null) {
                return;
            }
            crearNoProductor("PAR", param.formal, valorParam);
        }

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

            pila.push(new Pendiente(SALTO_INVALIDO, false));
            return;
        }
        int bf = crearNoProductor("BF", comparacion, null);
        pila.push(new Pendiente(bf, false));
    }

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

    private void generarRepeat(Reduccion reduccion) {
        if (iniciosRepeat.isEmpty()) {
            reporte.error(reduccion.getLinea(), "Iteracion incompleta");
            return;
        }
        String inicio = iniciosRepeat.pop();
        int marca = marcasRepeat.isEmpty() ? lista.cantidad() : marcasRepeat.pop();
        Integer comparacion = emitirComparacion(reduccion.operando(1), reduccion.getLinea());
        if (comparacion == null) {

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

    private void cerrarThen(Reduccion reduccion) {
        Pendiente bf = desapilar(reduccion.getLinea());
        if (bf == null) {
            return;
        }
        if (bf.numero == SALTO_INVALIDO) {

            pila.push(new Pendiente(SALTO_INVALIDO, true));
            return;
        }
        String etiquetaElse = lista.reservarEtiqueta();
        lista.completar(bf.numero, 2, etiquetaElse);
        int bi = crearNoProductor("BI", null, null);
        pila.push(new Pendiente(bi, true));
        crearEtiquetaConResultado(etiquetaElse);
    }

    private void cerrarSalto(Reduccion reduccion) {
        Pendiente pendiente = desapilar(reduccion.getLinea());
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
