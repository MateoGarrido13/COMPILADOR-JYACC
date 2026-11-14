import java.util.ArrayList;
import java.util.List;

/*
  Valor semantico que viaja por las reglas de expresion.
 
  La gramatica es recursiva a derecha, por lo que un parser ascendente reduce de
  adentro hacia afuera y agrupar en el momento de cada reduccion daria
  asociatividad a derecha. En lugar de eso se acumulan operandos y operadores, y
  el plegado a izquierda se hace cuando la expresion se consume por completo.
 
  Tambien transporta la marca de 'contiene asignacion', necesaria para detectar
  el anidamiento que el tema 17 prohibe, y el tipo resultante de la expresion.
 */
public class ExpresionDiferida {

    private final List<Object> operandos = new ArrayList<>();
    private final List<Character> operadores = new ArrayList<>();
    private boolean contieneAsignacion = false;
    // Una expresion invalida ya reporto un error semantico y no debe generar
    // tercetos: evita emitir codigo intermedio para codigo que no compila.
    private boolean valida = true;
    private String tipo = Tipos.INDEFINIDO;

    public static ExpresionDiferida deOperando(Object operando) {
        return deOperando(operando, tipoDe(operando));
    }

    public static ExpresionDiferida deOperando(Object operando, String tipo) {
        ExpresionDiferida expresion = new ExpresionDiferida();
        expresion.operandos.add(operando);
        expresion.tipo = normalizarTipo(tipo);
        if (operando instanceof ExpresionDiferida && !((ExpresionDiferida) operando).esValida()) {
            expresion.valida = false;
        }
        return expresion;
    }

    public boolean esValida() {
        return valida;
    }

    public ExpresionDiferida marcarInvalida() {
        this.valida = false;
        return this;
    }

    public String getTipo() {
        return tipo;
    }

    public ExpresionDiferida conTipo(String tipo) {
        this.tipo = normalizarTipo(tipo);
        return this;
    }

    /*
      Tipo de un operando suelto. Una expresion anidada aporta su propio tipo;
      una entrada de tabla su tipoDato (con la forma lexica como fallback para
      constantes); una llamada el tipo de retorno de la funcion; una referencia
      de atributo su tipo; y una conversion el tipo destino.
     */
    public static String tipoDe(Object operando) {
        if (operando == null) {
            return Tipos.INDEFINIDO;
        }
        if (operando instanceof ExpresionDiferida) {
            return ((ExpresionDiferida) operando).getTipo();
        }
        if (operando instanceof EntradaTabla) {
            EntradaTabla entrada = (EntradaTabla) operando;
            if (entrada.tipoDato != null && !entrada.tipoDato.isEmpty()
                    && !Tipos.INDEFINIDO.equals(entrada.tipoDato)) {
                return entrada.tipoDato;
            }
            if (entrada.uso == EntradaTabla.Uso.CADENA) {
                return Tipos.CADENA;
            }
            String inferido = Tipos.tipoDeConstante(entrada.lexema);
            return inferido == null ? Tipos.INDEFINIDO : inferido;
        }
        if (operando instanceof LlamadaFuncion) {
            return ((LlamadaFuncion) operando).getTipoRetorno();
        }
        if (operando instanceof ReferenciaAtributo) {
            return ((ReferenciaAtributo) operando).getTipo();
        }
        if (operando instanceof Conversion) {
            return ((Conversion) operando).getTipoDestino();
        }
        if (operando instanceof AsignacionEnExpresion) {
            return ((AsignacionEnExpresion) operando).getTipo();
        }
        return Tipos.INDEFINIDO;
    }

    private static String normalizarTipo(String tipo) {
        return tipo == null ? Tipos.INDEFINIDO : tipo;
    }

    /*
      Reduccion de 'termino OP expresion'. Por la recursion a derecha, el termino
      que se acaba de reducir es el que esta mas a la izquierda en el fuente, asi
      que se antepone para que la lista quede en orden de codigo fuente.
     */
    public ExpresionDiferida anteponer(Object operando, char operador) {
        String tipoIzquierdo = tipoDe(operando);
        if (operando instanceof ExpresionDiferida && !((ExpresionDiferida) operando).esValida()) {
            valida = false;
        }

        // Un + o - no se aplana sobre un producto: el * o / queda como un solo hijo.
        if (esSuma(operador) && tieneProducto()) {
            ExpresionDiferida agrupada = new ExpresionDiferida();
            agrupada.operandos.add(operando);
            agrupada.operandos.add(this);
            agrupada.operadores.add(operador);
            agrupada.tipo = tipoIzquierdo;
            agrupada.absorber(this);
            if (operando instanceof ExpresionDiferida) {
                agrupada.absorber((ExpresionDiferida) operando);
            }
            return agrupada;
        }
        operandos.add(0, operando);
        operadores.add(0, operador);
        // El resultado de la operacion conserva el tipo del operando izquierdo.
        // Si ese tipo no se conoce, se mantiene el que ya traia la expresion.
        if (!Tipos.INDEFINIDO.equals(tipoIzquierdo)) {
            tipo = tipoIzquierdo;
        }
        if (operando instanceof ExpresionDiferida) {
            absorber((ExpresionDiferida) operando);
        }
        return this;
    }

    private boolean tieneProducto() {
        for (char operador : operadores) {
            if (operador == '*' || operador == '/') {
                return true;
            }
        }
        return false;
    }

    private static boolean esSuma(char operador) {
        return operador == '+' || operador == '-';
    }

    public void marcarAsignacion() {
        contieneAsignacion = true;
    }

    public boolean contieneAsignacion() {
        return contieneAsignacion;
    }

    public void absorber(ExpresionDiferida otra) {
        if (otra == null) {
            return;
        }
        if (otra.contieneAsignacion) {
            contieneAsignacion = true;
        }
        if (!otra.valida) {
            valida = false;
        }
        // Conservar el tipo: solo se completa cuando la expresion aun no lo tiene.
        if (Tipos.INDEFINIDO.equals(tipo) && !Tipos.INDEFINIDO.equals(otra.tipo)) {
            tipo = otra.tipo;
        }
    }

    public List<Object> getOperandos() {
        return operandos;
    }

    public List<Character> getOperadores() {
        return operadores;
    }

    // Devuelve el unico operando cuando la expresion no tiene operadores. 
    public EntradaTabla operandoUnico() {
        if (operandos.size() == 1 && operandos.get(0) instanceof EntradaTabla) {
            return (EntradaTabla) operandos.get(0);
        }
        return null;
    }

    // Todas las entradas de tabla de simbolos que participan de la expresion. 
    public List<EntradaTabla> operandosSimples() {
        List<EntradaTabla> entradas = new ArrayList<>();
        for (Object operando : operandos) {
            if (operando instanceof EntradaTabla) {
                entradas.add((EntradaTabla) operando);
            } else if (operando instanceof ExpresionDiferida) {
                entradas.addAll(((ExpresionDiferida) operando).operandosSimples());
            }
        }
        return entradas;
    }

    /*
      Agrupacion a izquierda de la expresion acumulada: para 'a - b - c' devuelve
     '((a - b) - c)', que es la asociatividad correcta pese a la recursion a derecha.
     */
    public String plegarAIzquierda() {
        if (operandos.isEmpty()) {
            return "";
        }
        StringBuilder acumulado = new StringBuilder(descripcion(operandos.get(0)));
        for (int i = 0; i < operadores.size() && i + 1 < operandos.size(); i++) {
            acumulado.insert(0, "(")
                     .append(" ").append(operadores.get(i)).append(" ")
                     .append(descripcion(operandos.get(i + 1)))
                     .append(")");
        }
        return acumulado.toString();
    }

    private static String descripcion(Object operando) {
        if (operando instanceof EntradaTabla) {
            return ((EntradaTabla) operando).lexema;
        }
        if (operando instanceof ExpresionDiferida) {
            return ((ExpresionDiferida) operando).plegarAIzquierda();
        }
        if (operando instanceof ReferenciaAtributo) {
            return operando.toString();
        }
        return String.valueOf(operando);
    }

    @Override
    public String toString() {
        return plegarAIzquierda();
    }
}
