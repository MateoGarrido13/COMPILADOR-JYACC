public class AccionesErrores {

    public static void registrar(TablaAcciones tabla, Verificaciones v) {
        for (Reglas regla : Reglas.values()) {
            if (regla.esError()) {
                tabla.registrar(regla, ctx -> {
                    v.reportar(ctx.getRegla(), ctx.getLinea());
                    return ctx.cantidad() > 0 ? ctx.valor(0) : null;
                });
            }
        }
    }
}
