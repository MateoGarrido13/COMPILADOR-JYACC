import java.util.ArrayList;
import java.util.List;

/*
  Valor semantico que viaja por las reglas de expresion.
 
  La gramatica es recursiva a derecha, por lo que un parser ascendente reduce de
  adentro hacia afuera y agrupar en el momento de cada reduccion daria
  asociatividad a derecha. En lugar de eso se acumulan operandos y operadores, y
  el plegado a izquierda se hace cuando la expresion se consume por completo.
 
  Tambien transporta la marca de 'contiene asignacion', necesaria para detectar
  el anidamiento que el tema 17 prohibe.
 */
public class ExpresionDiferida {

    private final List<Object> operandos = new ArrayList<>();
    private final List<Character> operadores = new ArrayList<>();
    private boolean contieneAsignacion = false;

    public static ExpresionDiferida deOperando(Object operando) {
        ExpresionDiferida expresion = new ExpresionDiferida();
        expresion.operandos.add(operando);
        return expresion;
    }

    /*
      Reduccion de 'termino OP expresion'. Por la recursion a derecha, el termino
      que se acaba de reducir es el que esta mas a la izquierda en el fuente, asi
      que se antepone para que la lista quede en orden de codigo fuente.
     */
    public ExpresionDiferida anteponer(Object operando, char operador) {
        operandos.add(0, operando);
        operadores.add(0, operador);
        if (operando instanceof ExpresionDiferida && ((ExpresionDiferida) operando).contieneAsignacion) {
            contieneAsignacion = true;
        }
        return this;
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
        return String.valueOf(operando);
    }

    @Override
    public String toString() {
        return plegarAIzquierda();
    }
}
