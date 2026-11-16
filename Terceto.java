import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
  Instruccion de codigo intermedio: operador y dos operandos.
  Un operando es una EntradaTabla (referencia a la tabla de simbolos), el
  numero de otro terceto entre corchetes, o el nombre de una etiqueta.
  Al imprimir, la entrada se muestra por su lexema. Un operando vacio es "--".
  Una etiqueta es un terceto sin operandos: (label_N, --, --).

  Contrato IR y convenciones ASM-level documentados en docs/TP4/IR_CONTRATO.md.
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

    public String getOperador() {
        return operador;
    }

    public Object getOp1() {
        return op1;
    }

    public Object getOp2() {
        return op2;
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

    public boolean esEtiqueta() {
        return operador != null && operador.startsWith("label_") && op1 == null && op2 == null;
    }

    public boolean esSaltoCondicional() {
        return "BF".equals(operador);
    }

    public boolean esSaltoIncondicional() {
        return "BI".equals(operador);
    }

    /*
      Devuelve el nombre de etiqueta destino si este terceto es un salto BF/BI
      con destino ya resuelto como String. En BF el destino va en op2; en BI en op1.
     */
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

    /*
      Descarta los tercetos desde la posicion indicada hasta el final. Se usa
      para retirar el codigo de una construccion que resulto invalida despues
      de haber emitido parte de sus tercetos, como el cuerpo de un repeat cuya
      condicion reporto un error de tipos.
     */
    public void descartarDesde(int cantidadConservada) {
        while (tercetos.size() > cantidadConservada && !tercetos.isEmpty()) {
            tercetos.remove(tercetos.size() - 1);
        }
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

    /*
      Mapa nombre de etiqueta -> numero de terceto (1-based). Util para backend ASM.
     */
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
