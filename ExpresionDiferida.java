import java.util.ArrayList;
import java.util.List;

public class ExpresionDiferida {

    private final List<Object> operandos = new ArrayList<>();
    private final List<Character> operadores = new ArrayList<>();
    private boolean contieneAsignacion = false;

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

        if (operando instanceof LlamadaFuncion && !((LlamadaFuncion) operando).esValida()) {
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

    public ExpresionDiferida anteponer(Object operando, char operador) {
        String tipoIzquierdo = tipoDe(operando);
        if (operando instanceof ExpresionDiferida && !((ExpresionDiferida) operando).esValida()) {
            valida = false;
        }

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

    public EntradaTabla operandoUnico() {
        if (operandos.size() == 1 && operandos.get(0) instanceof EntradaTabla) {
            return (EntradaTabla) operandos.get(0);
        }
        return null;
    }

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
