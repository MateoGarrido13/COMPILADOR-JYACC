/*
  Producciones ERR_*: solo informan por Verificaciones.reportar
  y devuelven el primer valor para no cortar la recuperacion.
 */
public class AccionesErrores {

    public static void registrar(TablaAcciones tabla, Verificaciones v) {
        for (Reglas regla : Reglas.values()) {
            if (regla.esError()) {
                tabla.registrar(regla, ctx -> {
                    v.reportar(ctx.getRegla(), ctx.getLinea());
                    Object resultado = ctx.cantidad() > 0 ? ctx.valor(0) : null;
                    // Una expresion que viene de una produccion de error no tiene
                    // tipo confiable: se marca indefinida para no cascadear errores.
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
