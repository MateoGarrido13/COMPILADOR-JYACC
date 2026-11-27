import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Terceto {

    final String operador;
    Object op1;
    Object op2;

    public Terceto(String operador, Object op1, Object op2) {
        this.operador = operador;
        this.op1 = op1;
        this.op2 = op2;
    }

    public String getOperador() {
        return operador;
    }

    public Object getOp1() {
        return op1;
    }

    public Object getOp2() {
        return op2;
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

    public boolean esEtiqueta() {
        return operador != null && operador.startsWith("label_") && op1 == null && op2 == null;
    }

    public boolean esSaltoCondicional() {
        return "BF".equals(operador);
    }

    public boolean esSaltoIncondicional() {
        return "BI".equals(operador);
    }

    public String destinoSalto() {
        if (esSaltoCondicional() && op2 instanceof String) {
            return (String) op2;
        }
        if (esSaltoIncondicional() && op1 instanceof String) {
            return (String) op1;
        }
        return null;
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
        if (operando instanceof ReferenciaAtributo) {
            return operando.toString();
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

    public Terceto obtener(int numero) {
        if (numero < 1 || numero > tercetos.size()) {
            return null;
        }
        return tercetos.get(numero - 1);
    }

    public List<Terceto> elementos() {
        return Collections.unmodifiableList(tercetos);
    }

    public String reservarEtiqueta() {
        String nombre = "label_" + siguienteEtiqueta;
        siguienteEtiqueta++;
        return nombre;
    }

    public void descartarDesde(int cantidadConservada) {
        while (tercetos.size() > cantidadConservada && !tercetos.isEmpty()) {
            tercetos.remove(tercetos.size() - 1);
        }
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

    public Map<String, Integer> indicesEtiquetas() {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (int i = 0; i < tercetos.size(); i++) {
            Terceto t = tercetos.get(i);
            if (t.esEtiqueta()) {
                mapa.put(t.getOperador(), i + 1);
            }
        }
        return mapa;
    }

    public String contenido() {
        StringBuilder texto = new StringBuilder("Código intermedio:\n");
        for (int i = 0; i < tercetos.size(); i++) {
            texto.append(i + 1).append(". ").append(tercetos.get(i).visualizar()).append("\n");
        }
        return texto.toString();
    }
}
