import java.util.ArrayList;
import java.util.List;

public class AccionesSemanticas {

    public static void registrar(TablaAcciones tabla, Entorno entorno, Verificaciones v) {
        AccionesErrores.registrar(tabla, v);
        AccionesDeclaraciones.registrar(tabla, entorno, v);
        AccionesExpresiones.registrar(tabla, entorno, v);
    }

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

    public static Object combinar(ContextoRegla ctx, char operador) {
        ExpresionDiferida derecha = ctx.expresion(1);
        if (derecha == null) {
            derecha = new ExpresionDiferida();
        }
        return derecha.anteponer(ctx.valor(0), operador);
    }
}
