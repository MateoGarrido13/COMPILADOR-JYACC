import java.util.ArrayList;
import java.util.List;

/**
 * Fachada de registro de acciones de reduccion.
 *
 * La logica vive en otras_acciones; aca se cablean las tres familias y se
 * exponen los helpers que ellas reutilizan.
 */
public class AccionesSemanticas {

    public static void registrar(TablaAcciones tabla, Entorno entorno, Verificaciones v) {
        AccionesErrores.registrar(tabla, v);
        AccionesDeclaraciones.registrar(tabla, entorno, v);
        AccionesExpresiones.registrar(tabla, entorno, v);
    }

    /** Listas recursivas a derecha: el elemento actual mas lo ya acumulado. */
    public static Object concatenarLista(ContextoRegla ctx) {
        List<Object> elementos = new ArrayList<>();
        if (ctx.cantidad() > 0 && ctx.valor(0) != null) {
            elementos.add(ctx.valor(0));
        }
        if (ctx.cantidad() > 1) {
            elementos.addAll(ctx.lista(1));
        }
        return elementos;
    }

    /** Acumula "termino OP expresion" sin agrupar; el plegado es posterior. */
    public static Object combinar(ContextoRegla ctx, char operador) {
        ExpresionDiferida derecha = ctx.expresion(1);
        if (derecha == null) {
            derecha = new ExpresionDiferida();
        }
        return derecha.anteponer(ctx.valor(0), operador);
    }
}
