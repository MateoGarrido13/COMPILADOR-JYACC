import java.util.ArrayList;
import java.util.List;

public class Terceto {

    final String operador;
    Object op1;
    Object op2;

    public Terceto(String operador, Object op1, Object op2) {
        this.operador = operador;
        this.op1 = op1;
        this.op2 = op2;
    }

    void completar(int posicion, Object valor) {
        if (posicion == 1) {
            op1 = valor;
        } else {
            op2 = valor;
        }
    }

    public String visualizar() {
        return "(" + operador + ", " + texto(op1) + ", " + texto(op2) + ")";
    }

    static String texto(Object operando) {
        if (operando == null) {
            return "--";
        }
        if (operando instanceof EntradaTabla) {
            return ((EntradaTabla) operando).lexema;
        }
        if (operando instanceof String) {
            return (String) operando;
        }
        if (operando instanceof Integer) {
            return "[" + operando + "]";
        }
        return "?";
    }
}

class ListaTercetos {

    private final List<Terceto> tercetos = new ArrayList<>();
    private int siguienteEtiqueta = 1;

    public int crear(String operador, Object op1, Object op2) {
        tercetos.add(new Terceto(operador, op1, op2));
        return tercetos.size();
    }

    public int cantidad() {
        return tercetos.size();
    }

    public String reservarEtiqueta() {
        String nombre = "label_" + siguienteEtiqueta;
        siguienteEtiqueta++;
        return nombre;
    }

    public void crearEtiqueta(String nombre) {
        crear(nombre, null, null);
    }
    //(?)
    public void completar(int numero, int posicion, Object valor) {
        if (numero < 1 || numero > tercetos.size()) {
            return;
        }
        tercetos.get(numero - 1).completar(posicion, valor);
    }

    public String contenido() {
        StringBuilder texto = new StringBuilder("Código intermedio:\n");
        for (int i = 0; i < tercetos.size(); i++) {
            texto.append(i + 1).append(". ").append(tercetos.get(i).visualizar()).append("\n");
        }
        return texto.toString();
    }
}
