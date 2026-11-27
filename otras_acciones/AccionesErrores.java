public class AccionesErrores {

    public static void registrar(TablaAcciones tabla, Verificaciones v) {
        for (Reglas regla : Reglas.values()) {
            if (regla.esError()) {
                tabla.registrar(regla, ctx -> {
                    v.reportar(ctx.getRegla(), ctx.getLinea());
                    Object resultado = ctx.cantidad() > 0 ? ctx.valor(0) : null;

                    if (resultado instanceof ExpresionDiferida) {
                        return ((ExpresionDiferida) resultado).conTipo(Tipos.INDEFINIDO);
                    }
                    if (regla == Reglas.ERR_FALTA_ORDEN_EVALUACION && resultado instanceof EntradaTabla) {
                        return ExpresionDiferida.deOperando(resultado, Tipos.INDEFINIDO);
                    }
                    return resultado;
                });
            }
        }
    }
}
