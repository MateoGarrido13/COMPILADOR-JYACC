import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Acciones que arman ExpresionDiferida y aplican los chequeos de los temas 17 y 19.
 */
public class AccionesExpresiones {

    /*
      Envuelve un valor suelto (expresion sin nombre formal) en un ParametroReal
      para paso por posicion. Si ya es un ParametroReal lo devuelve tal cual.
     */
    private static Object envolverParametro(Object valor, int linea) {
        if (valor instanceof ParametroReal) {
            return valor;
        }
        if (valor instanceof ExpresionDiferida) {
            return new ParametroReal(null, valor, linea);
        }
        return new ParametroReal(null, ExpresionDiferida.deOperando(valor), linea);
    }

    public static void registrar(TablaAcciones tabla, Entorno entorno, Verificaciones v) {
        tabla.registrar(Reglas.SUMA, ctx -> AccionesSemanticas.combinar(ctx, '+'));
        tabla.registrar(Reglas.RESTA, ctx -> AccionesSemanticas.combinar(ctx, '-'));
        tabla.registrar(Reglas.MULTIPLICACION, ctx -> AccionesSemanticas.combinar(ctx, '*'));
        tabla.registrar(Reglas.DIVISION, ctx -> AccionesSemanticas.combinar(ctx, '/'));

        tabla.registrar(Reglas.FACTOR_ID, ctx -> {
            EntradaTabla id = ctx.entrada(0);
            EntradaTabla declaracion = v.verificarAlcance(id);
            return ExpresionDiferida.deOperando(declaracion != null ? declaracion : id);
        });

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
            v.verificarAlcance(ctx.entrada(0));
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
            EntradaTabla funcion = ctx.entrada(0);
            List<Object> parametrosReales = ctx.lista(1);
            List<Object> ordenRaw = ctx.valor(2) == null ? null : ctx.lista(2);
            List<Integer> ordenEvaluacion = comoListaEnteros(ordenRaw, ctx.getLinea());

            // Envuelve parametros posicionales sueltos en ParametroReal
            List<ParametroReal> parametros = new ArrayList<>();
            for (Object p : parametrosReales) {
                if (p instanceof ParametroReal) {
                    parametros.add((ParametroReal) p);
                } else {
                    parametros.add(new ParametroReal(null, p, ctx.getLinea()));
                }
            }

            // Verifica cantidad de parametros reales vs formales (tambien reporta si la funcion no existe)
            v.verificarCantidadParametros(funcion, parametros, ctx.getLinea());

            // Verifica orden de evaluacion: rango, duplicados, completitud
            v.verificarOrdenEvaluacion(ordenRaw, parametros.size(), ctx.getLinea());

            EntradaTabla resuelta = resolverFuncion(entorno, funcion);
            return new LlamadaFuncion(resuelta != null ? resuelta : funcion,
                    parametros, ordenEvaluacion, ctx.getLinea());
        });

        tabla.registrar(Reglas.PARAMETRO_REAL_NOMBRADO, ctx -> {
            EntradaTabla id = ctx.entrada(0);
            Object expresion = ctx.expresion(1);
            String nombreFormal = id == null ? null : TablaSimbolos.nombreDe(id.lexema);
            return new ParametroReal(nombreFormal, expresion, ctx.getLinea());
        });

        tabla.registrar(Reglas.LISTA_PARAMETROS_REALES, ctx -> {
            List<Object> elementos = new ArrayList<>();
            if (ctx.cantidad() > 0 && ctx.valor(0) != null) {
                elementos.add(envolverParametro(ctx.valor(0), ctx.getLinea()));
            }
            if (ctx.cantidad() > 1) {
                List<Object> resto = ctx.lista(1);
                for (Object e : resto) {
                    elementos.add(envolverParametro(e, ctx.getLinea()));
                }
            }
            return elementos;
        });

        tabla.registrar(Reglas.LISTA_ORDEN_EVALUACION, AccionesSemanticas::concatenarLista);

        tabla.registrar(Reglas.ACCESO_ATRIBUTO, ctx ->
                ExpresionDiferida.deOperando(ctx.entrada(1)));

        tabla.registrar(Reglas.ACCESO_ATRIBUTO_PREFIJADO, ctx ->
                ExpresionDiferida.deOperando(ctx.entrada(2)));

        tabla.registrar(Reglas.INVOCACION_METODO, ctx -> {
            List<Object> ordenEvaluacion = ctx.valor(3) == null ? null : ctx.lista(3);
            v.verificarOrdenEvaluacion(ordenEvaluacion, ctx.getLinea());
            return ExpresionDiferida.deOperando(ctx.entrada(1));
        });

        tabla.registrar(Reglas.INVOCACION_METODO_PREFIJADO, ctx -> {
            List<Object> ordenEvaluacion = ctx.valor(4) == null ? null : ctx.lista(4);
            v.verificarOrdenEvaluacion(ordenEvaluacion, ctx.getLinea());
            return ExpresionDiferida.deOperando(ctx.entrada(2));
        });
    }

    /*
      Resuelve el nombre de la funcion en el ambito de la llamada. Devuelve
      null si no hay una declaracion visible.
     */
    private static EntradaTabla resolverFuncion(Entorno entorno, EntradaTabla funcion) {
        if (funcion == null) {
            return null;
        }
        String nombre = TablaSimbolos.nombreDe(funcion.lexema);
        EntradaTabla declaracion = entorno.resolver(nombre, Globals.AMBITO_ACTUAL);
        if (declaracion == null) {
            return null;
        }
        if (declaracion.uso != EntradaTabla.Uso.FUNCION
                && declaracion.uso != EntradaTabla.Uso.METODO) {
            return null;
        }
        return declaracion;
    }

    /*
      Convierte una lista de constantes (EntradaTabla) del orden de evaluacion
      en una lista de enteros (posiciones 1-based).
     */
    private static List<Integer> comoListaEnteros(List<Object> valores, int linea) {
        if (valores == null) {
            return null;
        }
        List<Integer> resultado = new ArrayList<>();
        for (Object v : valores) {
            EntradaTabla entrada = ContextoRegla.comoEntrada(v);
            if (entrada == null) {
                continue;
            }
            BigInteger bi = Tipos.valorEntero(entrada.lexema);
            if (bi != null) {
                resultado.add(bi.intValue());
            }
        }
        return resultado;
    }
}
