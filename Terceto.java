import java.util.ArrayList;
import java.util.List;

/*
  Instruccion de codigo intermedio: operador y dos operandos.
  Un operando es una EntradaTabla (referencia a la tabla de simbolos) o el
  numero de otro terceto. Al imprimir, la entrada se muestra por su lexema.
 */
public class Terceto {

    final String operador;
    final Object op1;
    final Object op2;

    public Terceto(String operador, Object op1, Object op2) {
        this.operador = operador;
        this.op1 = op1;
        this.op2 = op2;
    }

    public String visualizar() {
        return "(" + operador + ", " + texto(op1) + ", " + texto(op2) + ")";
    }

    static String texto(Object operando) {
        if (operando instanceof EntradaTabla) {
            return ((EntradaTabla) operando).lexema;
        }
        if (operando instanceof Integer) {
            return "[" + operando + "]";
        }
        return "?";
    }
}

/*
  Lista 1-based. crear devuelve el numero del terceto recien agregado, que es
  el puntero al resultado intermedio.
 */
class ListaTercetos {

    private final List<Terceto> tercetos = new ArrayList<>();

    public int crear(String operador, Object op1, Object op2) {
        tercetos.add(new Terceto(operador, op1, op2));
        return tercetos.size();
    }

    public String contenido() {
        StringBuilder texto = new StringBuilder("Código intermedio:\n");
        for (int i = 0; i < tercetos.size(); i++) {
            texto.append(i + 1).append(". ").append(tercetos.get(i).visualizar()).append("\n");
        }
        return texto.toString();
    }
}
