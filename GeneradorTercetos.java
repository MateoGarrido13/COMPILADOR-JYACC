import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/*
  Recorre las reducciones y emite tercetos.
  Las expresiones se pliegan a izquierda dentro de cada nivel de precedencia.
  El if apila el BF al terminar la condicion. Al cerrar el then lo completa
  con una etiqueta label_N. Con else, abre un BI hacia otra etiqueta.
  El repeat abre la label de inicio al reducirse INICIO_REPEAT, antes del
  cuerpo. Al cerrar, BF sale si la condicion es falsa y BI vuelve a esa label.
 */
public class GeneradorTercetos {

    private static final class Pendiente {
        /* Esta clase es un contenedor para los datos de un salto pendiente.
        numero es el numero del terceto pendiente, incondicional es true si es un salto incondicional */
        final int numero;
        final boolean incondicional;

        Pendiente(int numero, boolean incondicional) {
            this.numero = numero;
            this.incondicional = incondicional;
        }
    }

    private final ListaTercetos lista = new ListaTercetos();
    private final Reporte reporte;
    private final Deque<Pendiente> pila = new ArrayDeque<>(); // pila de saltos pendientes
    private final Deque<String> iniciosRepeat = new ArrayDeque<>(); // pila de etiquetas de inicio de repeat

    public GeneradorTercetos(Reporte reporte) {
        this.reporte = reporte;
    }

    public void generar(List<Reduccion> reducciones) {
        if (reducciones == null) {
            return;
        }
        for (Reduccion reduccion : reducciones) {
            switch (reduccion.getRegla()) {
                case ASIGNACION:
                    generarAsignacion(reduccion);
                    break;
                case CONDICION_IF:
                    generarCondicionIf(reduccion);
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
    }

    public String contenido() {
        return lista.contenido();
    }

    private void generarAsignacion(Reduccion reduccion) {
        Object destino = reduccion.operando(0);
        if (!(destino instanceof EntradaTabla)) {
            reporte.error(reduccion.getLinea(), "No se puede asignar un valor a una cte o variable sin referencia");
            return;
        }
        Object origen = emitir(reduccion.operando(1), reduccion.getLinea());
        if (origen == null) {
            reporte.error(reduccion.getLinea(), "Asignacion sin operando");
            return;
        }
        lista.crear(":=", destino, origen);
    }

    private void generarCondicionIf(Reduccion reduccion) {
        Integer comparacion = emitirComparacion(reduccion.operando(0), reduccion.getLinea());
        if (comparacion == null) {
            return;
        }
        int bf = lista.crear("BF", comparacion, null);
        pila.push(new Pendiente(bf, false));
    }

    /* Se reduce al leer repeat, antes de generar el cuerpo. */
    private void abrirRepeat() {
        String etiqueta = lista.reservarEtiqueta();
        lista.crearEtiqueta(etiqueta);
        iniciosRepeat.push(etiqueta);
    }

    private void descartarRepeat() {
        if (!iniciosRepeat.isEmpty()) {
            iniciosRepeat.pop();
        }
    }

    /* BF sale si la condicion es falsa. Si no se toma, el BI vuelve al inicio. */
    private void generarRepeat(Reduccion reduccion) {
        if (iniciosRepeat.isEmpty()) {
            reporte.error(reduccion.getLinea(), "Iteracion incompleta");
            return;
        }
        String inicio = iniciosRepeat.pop();
        Integer comparacion = emitirComparacion(reduccion.operando(1), reduccion.getLinea());
        if (comparacion == null) {
            return;
        }
        String salida = lista.reservarEtiqueta();
        lista.crear("BF", comparacion, salida);
        lista.crear("BI", inicio, null);
        lista.crearEtiqueta(salida);
    }

    private Integer emitirComparacion(Object valor, int linea) {
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
        return lista.crear((String) partes[1], izquierdo, derecho);
    }

    /* Completa el BF con la etiqueta del else, abre el BI y deja esa
       etiqueta como primer terceto de la rama else. */
    private void cerrarThen(Reduccion reduccion) {
        Pendiente bf = desapilar(reduccion.getLinea());
        if (bf == null) {
            return;
        }
        String etiquetaElse = lista.reservarEtiqueta();
        lista.completar(bf.numero, 2, etiquetaElse);
        int bi = lista.crear("BI", null, null);
        pila.push(new Pendiente(bi, true));
        lista.crearEtiqueta(etiquetaElse);
    }

    /* If sin else, o el cierre del else: el salto pendiente apunta a una
       etiqueta nueva, que se emite a continuacion. ERR_FALTA_END_IF usa el
       mismo cierre para no dejar el BF o el BI vacios. */
    private void cerrarSalto(Reduccion reduccion) {
        Pendiente pendiente = desapilar(reduccion.getLinea()); // paso la linea por si hay error 
        if (pendiente == null) {
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
        lista.crearEtiqueta(etiqueta);
    }

    private Pendiente desapilar(int linea) {
        if (pila.isEmpty()) {
            reporte.error(linea, "Seleccion incompleta");
            return null;
        }
        return pila.pop();
    }

    // Devuelve la EntradaTabla hoja o el numero del terceto que resume la expresion.
    private Object emitir(Object valor, int linea) {
        if (valor instanceof EntradaTabla || valor instanceof Integer) {
            return valor;
        }
        if (!(valor instanceof ExpresionDiferida)) {
            return null;
        }
        ExpresionDiferida expresion = (ExpresionDiferida) valor;
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
            acumulado = lista.crear(String.valueOf(operadores.get(i)), acumulado, derecho);
        }
        return acumulado;
    }
}
