import java.util.ArrayList;
import java.util.List;

public class ExpresionDiferida {

    private final List<Object> operandos = new ArrayList<>();
    private final List<Character> operadores = new ArrayList<>();
    private boolean contieneAsignacion = false;

    public static ExpresionDiferida deOperando(Object operando) {
        ExpresionDiferida expresion = new ExpresionDiferida();
        expresion.operandos.add(operando);
        return expresion;
    }

    public ExpresionDiferida anteponer(Object operando, char operador) {
        if (esSuma(operador) && tieneProducto()) {
            ExpresionDiferida agrupada = new ExpresionDiferida();
            agrupada.operandos.add(operando);
            agrupada.operandos.add(this);
            agrupada.operadores.add(operador);
            agrupada.absorber(this);
            if (operando instanceof ExpresionDiferida) {
                agrupada.absorber((ExpresionDiferida) operando);
            }
            return agrupada;
        }
        operandos.add(0, operando);
        operadores.add(0, operador);
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
        if (otra != null && otra.contieneAsignacion) {
            contieneAsignacion = true;
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
        return String.valueOf(operando);
    }

    @Override
    public String toString() {
        return plegarAIzquierda();
    }
}
