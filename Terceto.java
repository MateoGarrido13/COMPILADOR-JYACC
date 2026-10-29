import java.util.ArrayList;
import java.util.List;

/*
  Instruccion de codigo intermedio: operador y dos operandos.
  Un operando es una EntradaTabla (referencia a la tabla de simbolos), el
  numero de otro terceto entre corchetes, o el nombre de una etiqueta.
  Al imprimir, la entrada se muestra por su lexema. Un operando vacio es "--".
  Una etiqueta es un terceto sin operandos: (label_N, --, --).
 */
public class Terceto {

    final String operador;
    Object op1;
    Object op2;

    public Terceto(String operador, Object op1, Object op2) {
        this.operador = operador;
        this.op1 = op1;
        this.op2 = op2;
    }

    void completar(int posicion, Object valor) { // una especie de setter
        if (posicion == 1) {
            op1 = valor;
        } else {
            op2 = valor;
        }
    }

    public String visualizar() { // serializacion 
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

/*
  Lista 1-based. crear devuelve el numero del terceto recien agregado, que es
  el puntero al resultado intermedio. completar escribe un operando que nacio
  vacio, cuando ya se conoce la etiqueta del salto.
  Las etiquetas de salto usan un contador propio: label_1, label_2, ...
 */
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
