import java.util.List;

/*
 Acciones de programa, declaraciones, sentencias y contexto de funcion/clase.
 */
public class AccionesDeclaraciones {

    public static void registrar(TablaAcciones tabla, Entorno entorno, Verificaciones v) {
        tabla.registrar(Reglas.PROGRAMA, ctx -> ctx.valor(0));
        tabla.registrar(Reglas.NOMBRE_PROGRAMA, ctx -> ctx.entrada(0));
        tabla.registrar(Reglas.LISTA_SENTENCIAS, AccionesSemanticas::concatenarLista);

        tabla.registrar(Reglas.DECL_VARIABLES, ctx -> {
            String tipo = ctx.texto(0);
            List<Object> identificadores = ctx.lista(1);
            for (Object elemento : identificadores) {
                entorno.declararVariable(ContextoRegla.comoEntrada(elemento), tipo, ctx.getLinea());
            }
            return identificadores;
        });

        tabla.registrar(Reglas.LISTA_VARIABLES, AccionesSemanticas::concatenarLista);

        tabla.registrar(Reglas.DECL_OBJETOS, ctx -> {
            List<Object> identificadores = ctx.lista(1);
            entorno.declararObjetos(identificadores, ctx.texto(0), ctx.getLinea());
            return identificadores;
        });

        tabla.registrar(Reglas.INICIO_FUNCION, ctx ->
                entorno.abrirFuncion(ctx.entrada(1), ctx.texto(0), ctx.getLinea()));

        tabla.registrar(Reglas.PARAMETRO_FORMAL, ctx -> {
            EntradaTabla id = ctx.entrada(1);
            entorno.agregarParametroFormal(id, ctx.texto(0));
            return id;
        });

        tabla.registrar(Reglas.LISTA_PARAMETROS_FORMALES, AccionesSemanticas::concatenarLista);

        tabla.registrar(Reglas.DECL_FUNCION, ctx -> {
            entorno.cerrarFuncion();
            return ctx.valor(0);
        });

        tabla.registrar(Reglas.DECL_METODO, ctx -> {
            entorno.cerrarFuncion();
            return ctx.valor(0);
        });

        tabla.registrar(Reglas.INICIO_CLASE, ctx -> entorno.abrirClase(ctx.entrada(0), ctx.getLinea()));

        tabla.registrar(Reglas.DECL_CLASE, ctx -> {
            Object valor = ctx.valor(0);
            entorno.cerrarClase();
            return valor;
        });

        tabla.registrar(Reglas.DECL_ATRIBUTO, ctx -> {
            String tipo = ctx.texto(0);
            List<Object> identificadores = ctx.lista(1);
            for (Object elemento : identificadores) {
                entorno.declararAtributo(ContextoRegla.comoEntrada(elemento), tipo, ctx.getLinea());
            }
            return identificadores;
        });

        tabla.registrar(Reglas.SENTENCIA_EXTENDS, ctx -> {
            List<Object> padres = ctx.lista(0);
            entorno.registrarHerencia(padres);
            return padres;
        });

        tabla.registrar(Reglas.LISTA_CLASES_HEREDADAS, AccionesSemanticas::concatenarLista);

        tabla.registrar(Reglas.DECL_TYPEDEF, ctx -> {
            EntradaTabla id = ctx.entrada(0);
            List<Object> valores = ctx.lista(1);
            v.verificarValoresEnumerado(valores, ctx.getLinea());
            entorno.registrarTipoEnumerado(id, valores, ctx.getLinea());
            return id;
        });

        tabla.registrar(Reglas.LISTA_VALORES_ENUMERADO, AccionesSemanticas::concatenarLista);

        tabla.registrar(Reglas.ASIGNACION, ctx -> ctx.entrada(0));
        tabla.registrar(Reglas.CONDICION, ctx -> ctx.valor(0));
        tabla.registrar(Reglas.SENTENCIA_POUT, ctx -> ctx.valor(0));

        tabla.registrar(Reglas.SENTENCIA_RET, ctx -> ctx.valor(0));
    }
}
