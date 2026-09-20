import java.util.List;

/**
 * Acciones que arman ExpresionDiferida y aplican los chequeos de los temas 17 y 19.
 */
public class AccionesExpresiones {

    public static void registrar(TablaAcciones tabla, Entorno entorno, Verificaciones v) {
        tabla.registrar(Reglas.SUMA, ctx -> AccionesSemanticas.combinar(ctx, '+'));
        tabla.registrar(Reglas.RESTA, ctx -> AccionesSemanticas.combinar(ctx, '-'));
        tabla.registrar(Reglas.MULTIPLICACION, ctx -> AccionesSemanticas.combinar(ctx, '*'));
        tabla.registrar(Reglas.DIVISION, ctx -> AccionesSemanticas.combinar(ctx, '/'));

        tabla.registrar(Reglas.FACTOR_ID, ctx ->
                ExpresionDiferida.deOperando(ctx.entrada(0)));

        tabla.registrar(Reglas.FACTOR_CONSTANTE, ctx ->
                ExpresionDiferida.deOperando(entorno.tiparConstante(ctx.entrada(0))));

        tabla.registrar(Reglas.FACTOR_CONSTANTE_NEGATIVA, ctx -> {
            EntradaTabla constante = entorno.aplicarSignoNegativo(ctx.entrada(0), ctx.getLinea());
            v.verificarRangoConstante(constante, ctx.getLinea());
            return ExpresionDiferida.deOperando(constante);
        });

        tabla.registrar(Reglas.FACTOR_CADENA, ctx ->
                ExpresionDiferida.deOperando(ctx.entrada(0)));

        tabla.registrar(Reglas.ASIGNACION_EN_EXPRESION, ctx -> {
            ExpresionDiferida expresion = ctx.expresion(1);
            if (expresion == null) {
                expresion = new ExpresionDiferida();
            }
            v.verificarAsignacionAnidada(expresion, ctx.getLinea());
            expresion.marcarAsignacion();
            return expresion;
        });

        tabla.registrar(Reglas.CONVERSION_TODF, ctx -> ctx.expresion(0));

        tabla.registrar(Reglas.INVOCACION_FUNCION, ctx -> {
            List<Object> ordenEvaluacion = ctx.valor(2) == null ? null : ctx.lista(2);
            v.verificarOrdenEvaluacion(ordenEvaluacion, ctx.getLinea());
            return ExpresionDiferida.deOperando(ctx.entrada(0));
        });

        tabla.registrar(Reglas.PARAMETRO_REAL_NOMBRADO, ctx -> ctx.expresion(1));

        tabla.registrar(Reglas.LISTA_PARAMETROS_REALES, AccionesSemanticas::concatenarLista);
        tabla.registrar(Reglas.LISTA_ORDEN_EVALUACION, AccionesSemanticas::concatenarLista);

        tabla.registrar(Reglas.ACCESO_ATRIBUTO, ctx ->
                ExpresionDiferida.deOperando(ctx.entrada(1)));

        tabla.registrar(Reglas.ACCESO_ATRIBUTO_PREFIJADO, ctx ->
                ExpresionDiferida.deOperando(ctx.entrada(2)));

        tabla.registrar(Reglas.INVOCACION_METODO, ctx ->
                ExpresionDiferida.deOperando(ctx.entrada(1)));
    }
}
