import java.util.ArrayList;
import java.util.List;

public class ContextoRegla {

    private final Reglas regla;
    private final int linea;
    private final Object[] valores;

    public ContextoRegla(Reglas regla, int linea, Object[] valores) {
        this.regla = regla;
        this.linea = linea;
        this.valores = valores == null ? new Object[0] : valores;
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

    public List<Object> lista(int indice) {
        return comoLista(valor(indice));
    }

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
