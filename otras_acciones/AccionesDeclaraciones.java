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
            validarTipoDeclarado(entorno, v, tipo, ctx.getLinea());
            List<Object> identificadores = ctx.lista(1);
            for (Object elemento : identificadores) {
                entorno.declararVariable(ContextoRegla.comoEntrada(elemento), tipo, ctx.getLinea());
            }
            return identificadores;
        });

        tabla.registrar(Reglas.LISTA_VARIABLES, AccionesSemanticas::concatenarLista);

        tabla.registrar(Reglas.DECL_OBJETOS, ctx -> {
            String tipo = ctx.texto(0);
            validarTipoDeclarado(entorno, v, tipo, ctx.getLinea());
            List<Object> identificadores = ctx.lista(1);
            entorno.declararObjetos(identificadores, tipo, ctx.getLinea());
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
            validarTipoDeclarado(entorno, v, tipo, ctx.getLinea());
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

        tabla.registrar(Reglas.ASIGNACION, ctx -> {
            Object destinoValor = ctx.valor(0);
            ExpresionDiferida origen = ctx.expresion(1);

            EntradaTabla destino = destinoDe(destinoValor);
            if (destinoValor instanceof EntradaTabla) {
                destino = v.verificarAlcance((EntradaTabla) destinoValor);
            } else if (destinoValor instanceof ExpresionDiferida) {
                // Compatibilidad con accesos de atributo previos al motor de tipos.
                EntradaTabla unico = ((ExpresionDiferida) destinoValor).operandoUnico();
                if (unico != null) {
                    destino = v.verificarAlcance(unico);
                }
            }

            // Sin destino declarado o sin expresion confiable no se genera el
            // terceto de asignacion: el error ya fue reportado.
            if (destino == null || !destino.declarado) {
                return null;
            }
            v.verificarAsignable(destino, ctx.getLinea());
            if (!v.esAsignable(destino) || origen == null || !origen.esValida()) {
                return null;
            }

            String tipoDestino = entorno.tipoEfectivo(destino.tipoDato);
            String tipoOrigen = entorno.tipoEfectivo(origen.getTipo());
            if (!Tipos.INDEFINIDO.equals(tipoDestino) && !Tipos.INDEFINIDO.equals(tipoOrigen)
                    && !Tipos.mismoTipo(tipoDestino, tipoOrigen)) {
                v.errorTipos(ctx.getLinea(),
                        "la asignación a '" + TablaSimbolos.nombreDe(destino.lexema) + "'",
                        tipoDestino, tipoOrigen);
                return null;
            }

            if (destinoValor instanceof ReferenciaAtributo) {
                ReferenciaAtributo referencia = (ReferenciaAtributo) destinoValor;
                return referencia.atributo != null ? referencia : destino;
            }
            return destino;
        });
        tabla.registrar(Reglas.CONDICION, ctx -> {
            String tipoIzquierdo = entorno.tipoEfectivo(ExpresionDiferida.tipoDe(ctx.valor(0)));
            String tipoDerecho = entorno.tipoEfectivo(ExpresionDiferida.tipoDe(ctx.valor(2)));
            if (!Tipos.INDEFINIDO.equals(tipoIzquierdo) && !Tipos.INDEFINIDO.equals(tipoDerecho)) {
                if (!Tipos.mismoTipo(tipoIzquierdo, tipoDerecho)) {
                    v.errorTipos(ctx.getLinea(), "la condición", tipoIzquierdo, tipoDerecho);
                    // Sin comparacion valida no se genera el BF ni los saltos.
                    return null;
                }
                if (Tipos.CADENA.equals(tipoIzquierdo)) {
                    v.errorTipos(ctx.getLinea(), "la condición", "un operando numérico", tipoIzquierdo);
                    return null;
                }
            }
            return new Object[] { ctx.valor(0), ctx.valor(1), ctx.valor(2) };
        });
        tabla.registrar(Reglas.SENTENCIA_POUT, ctx -> ctx.valor(0));

        tabla.registrar(Reglas.SENTENCIA_RET, ctx -> {
            Entorno.FuncionDeclarada funcion = entorno.getFuncionActual();
            if (funcion == null) {
                v.errorRetornoFueraDeFuncion(ctx.getLinea());
                return ctx.valor(0);
            }
            String tipoRetorno = entorno.tipoEfectivo(funcion.tipoRetorno);
            String tipoValor = entorno.tipoEfectivo(ExpresionDiferida.tipoDe(ctx.valor(0)));
            if (!Tipos.INDEFINIDO.equals(tipoRetorno) && !Tipos.INDEFINIDO.equals(tipoValor)
                    && !Tipos.mismoTipo(tipoRetorno, tipoValor)) {
                v.errorTipos(ctx.getLinea(),
                        "el retorno de la función '" + funcion.nombre + "'",
                        tipoRetorno, tipoValor);
            }
            return ctx.valor(0);
        });
    }

    private static EntradaTabla destinoDe(Object valor) {
        if (valor instanceof EntradaTabla) {
            return (EntradaTabla) valor;
        }
        if (valor instanceof ReferenciaAtributo) {
            ReferenciaAtributo referencia = (ReferenciaAtributo) valor;
            return referencia.atributo != null ? referencia.atributo : null;
        }
        return null;
    }

    private static void validarTipoDeclarado(Entorno entorno, Verificaciones v,
            String tipo, int linea) {
        if (tipo == null || tipo.isEmpty() || Tipos.esTipoBasico(tipo)) {
            return;
        }
        if (!entorno.esTipoConocido(tipo)) {
            v.errorTipoNoDeclarado(tipo, linea);
        }
    }
}
