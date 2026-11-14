import java.util.ArrayList;
import java.util.List;

/*
La logica vive en otras_acciones; aca se cablean las tres familias y se
 exponen los helpers que ellas reutilizan.
 */
public class AccionesSemanticas {

    public static void registrar(TablaAcciones tabla, Entorno entorno, Verificaciones v) {
        AccionesErrores.registrar(tabla, v);
        AccionesDeclaraciones.registrar(tabla, entorno, v);
        AccionesExpresiones.registrar(tabla, entorno, v);
    }

    // Listas recursivas a derecha, el elemento actual mas lo ya acumulado.
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

    //Acumula 'termino OP expresion' sin agrupar; el plegado es posterior.
    public static Object combinar(ContextoRegla ctx, char operador, Verificaciones v) {
        ExpresionDiferida derecha = ctx.expresion(1);
        if (derecha == null) {
            derecha = new ExpresionDiferida();
        }
        Entorno entorno = v.getEntorno();
        String tipoIzquierdo = entorno.tipoEfectivo(ExpresionDiferida.tipoDe(ctx.valor(0)));
        String tipoDerecho = entorno.tipoEfectivo(derecha.getTipo());

        ExpresionDiferida resultado = derecha.anteponer(ctx.valor(0), operador);

        // Si falta un tipo, no se sigue chequeando para no cascadear errores.
        if (Tipos.INDEFINIDO.equals(tipoIzquierdo) || Tipos.INDEFINIDO.equals(tipoDerecho)) {
            return resultado.conTipo(Tipos.INDEFINIDO);
        }
        if (!Tipos.mismoTipo(tipoIzquierdo, tipoDerecho)) {
            v.errorTipos(ctx.getLinea(), "el operador '" + operador + "'",
                    tipoIzquierdo, tipoDerecho);
            return resultado.conTipo(Tipos.INDEFINIDO).marcarInvalida();
        }
        // cadena solo es valida en pout y en asignaciones de cadena.
        if (Tipos.CADENA.equals(tipoIzquierdo) || !Tipos.esNumerico(tipoIzquierdo)) {
            v.errorTipos(ctx.getLinea(), "el operador '" + operador + "'",
                    "un operando numerico", tipoIzquierdo);
            return resultado.conTipo(Tipos.INDEFINIDO).marcarInvalida();
        }
        return resultado.conTipo(tipoIzquierdo);
    }
}
