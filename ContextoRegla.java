import java.util.ArrayList;
import java.util.List;

/*
Informacion que recibe una accion semantica al reducirse una regla.
 Los valores son los que en YACC se acceden como $1, $2, $3; aca llegan como
 arreglo, de modo que valor(0) equivale a $1.
 */
public class ContextoRegla {

    private final Reglas regla;
    private final int linea;
    private final Object[] valores;

    public ContextoRegla(Reglas regla, int linea, Object[] valores) {
        this.regla = regla;
        this.linea = linea;
        this.valores = valores == null ? new Object[0] : valores; // si valores es null, inicializo un array de 0 elementos
    }

    public Reglas getRegla() {
        return regla;
    }

    public int getLinea() {
        return linea;
    }

    public int cantidad() {
        return valores.length;
    }

    /** Equivalente a $(indice + 1). */
    public Object valor(int indice) {
        if (indice < 0 || indice >= valores.length) {
            return null;
        }
        return valores[indice];
    }

    public EntradaTabla entrada(int indice) {
        return comoEntrada(valor(indice));
    }

    public String texto(int indice) {
        Object valor = valor(indice);
        if (valor instanceof EntradaTabla) {
            return ((EntradaTabla) valor).lexema;
        }
        return valor == null ? null : valor.toString();
    }

    public ExpresionDiferida expresion(int indice) {
        return comoExpresion(valor(indice));
    }

    // Devuelve el valor como lista; si el valor es un elemento suelto, lo envuelve.
    public List<Object> lista(int indice) {
        return comoLista(valor(indice));
    }

    // ===== Conversores reutilizables =====

    public static EntradaTabla comoEntrada(Object valor) {
        if (valor instanceof EntradaTabla) {
            return (EntradaTabla) valor;
        }
        if (valor instanceof ExpresionDiferida) {
            return ((ExpresionDiferida) valor).operandoUnico();
        }
        return null;
    }

    public static ExpresionDiferida comoExpresion(Object valor) {
        if (valor instanceof ExpresionDiferida) {
            return (ExpresionDiferida) valor;
        }
        if (valor == null) {
            return null;
        }
        return ExpresionDiferida.deOperando(valor);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> comoLista(Object valor) {
        if (valor == null) {
            return new ArrayList<>();
        }
        if (valor instanceof List) {
            return (List<Object>) valor;
        }
        List<Object> lista = new ArrayList<>();
        lista.add(valor);
        return lista;
    }
}
