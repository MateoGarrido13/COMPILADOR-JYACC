import java.util.List;

public class AccionesDeclaraciones {

    public static void registrar(TablaAcciones tabla, Entorno entorno, Verificaciones v) {
        tabla.registrar(Reglas.PROGRAMA, ctx -> ctx.valor(0));
        tabla.registrar(Reglas.NOMBRE_PROGRAMA, ctx -> ctx.entrada(0));
        tabla.registrar(Reglas.LISTA_SENTENCIAS, AccionesSemanticas::concatenarLista);

        tabla.registrar(Reglas.DECL_VARIABLES, ctx -> {
            String tipo = validarTipoDeclarado(entorno, v, ctx.texto(0), ctx.getLinea());
            List<Object> identificadores = ctx.lista(1);
            if (tipo != null) {
                for (Object elemento : identificadores) {
                    entorno.declararVariable(ContextoRegla.comoEntrada(elemento), tipo, ctx.getLinea());
                }
            }
            return identificadores;
        });

        tabla.registrar(Reglas.LISTA_VARIABLES, AccionesSemanticas::concatenarLista);

        tabla.registrar(Reglas.DECL_OBJETOS, ctx -> {
            String tipo = validarTipoDeclarado(entorno, v, ctx.texto(0), ctx.getLinea());
            List<Object> identificadores = ctx.lista(1);
            if (tipo != null) {
                entorno.declararObjetos(identificadores, tipo, ctx.getLinea());
            }
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
            Entorno.FuncionDeclarada funcion = entorno.getFuncionActual();
            entorno.cerrarFuncion();
            return funcion != null ? funcion : ctx.valor(0);
        });

        tabla.registrar(Reglas.DECL_METODO, ctx -> {
            Entorno.FuncionDeclarada funcion = entorno.getFuncionActual();
            entorno.cerrarFuncion();
            return funcion != null ? funcion : ctx.valor(0);
        });

        tabla.registrar(Reglas.INICIO_CLASE, ctx -> entorno.abrirClase(ctx.entrada(0), ctx.getLinea()));

        tabla.registrar(Reglas.DECL_CLASE, ctx -> {
            Entorno.ClaseDeclarada clase = entorno.getClaseActual();
            Object valor = ctx.valor(0);
            entorno.cerrarClase();
            return clase != null ? clase : valor;
        });

        tabla.registrar(Reglas.DECL_ATRIBUTO, ctx -> {
            String tipo = validarTipoDeclarado(entorno, v, ctx.texto(0), ctx.getLinea());
            List<Object> identificadores = ctx.lista(1);
            if (tipo != null) {
                for (Object elemento : identificadores) {
                    entorno.declararAtributo(ContextoRegla.comoEntrada(elemento), tipo, ctx.getLinea());
                }
            }
            return identificadores;
        });

        tabla.registrar(Reglas.SENTENCIA_EXTENDS, ctx -> {
            List<Object> padres = ctx.lista(0);
            v.registrarHerencia(padres, ctx.getLinea());
            return padres;
        });

        tabla.registrar(Reglas.LISTA_CLASES_HEREDADAS, AccionesSemanticas::concatenarLista);

        tabla.registrar(Reglas.DECL_TYPEDEF, ctx -> {
            EntradaTabla id = ctx.entrada(0);
            List<Object> valores = ctx.lista(1);
            String tipoBase = v.verificarValoresEnumerado(valores, ctx.getLinea());
            boolean nombreDisponible = v.verificarTipoEnumeradoDisponible(id, ctx.getLinea());
            if (tipoBase != null && nombreDisponible) {
                if (!entorno.registrarTipoEnumerado(id, valores, tipoBase, ctx.getLinea())) {
                    v.reportarErrorTipoEnumerado(id, ctx.getLinea());
                }
            }
            return id;
        });

        tabla.registrar(Reglas.LISTA_VALORES_ENUMERADO, AccionesSemanticas::concatenarLista);

        tabla.registrar(Reglas.ASIGNACION, ctx -> {
            Object destinoValor = ctx.valor(0);
            ExpresionDiferida origen = ctx.expresion(1);

            ReferenciaAtributo atributoPropio = referenciaPropia(destinoValor);
            if (atributoPropio != null) {
                ExpresionDiferida acceso = (ExpresionDiferida) destinoValor;
                if (!acceso.esValida() || origen == null || !origen.esValida()) {
                    return null;
                }
                String tipoDestino = entorno.tipoEfectivo(atributoPropio.getTipo());
                String tipoOrigen = entorno.tipoEfectivo(origen.getTipo());
                if (!Tipos.INDEFINIDO.equals(tipoDestino) && !Tipos.INDEFINIDO.equals(tipoOrigen)
                        && !Tipos.mismoTipo(tipoDestino, tipoOrigen)) {
                    String nombre = atributoPropio.atributo == null
                            ? atributoPropio.toString()
                            : TablaSimbolos.nombreDe(atributoPropio.atributo.lexema);
                    v.errorTipos(ctx.getLinea(),
                            "la asignación a '" + nombre + "'",
                            tipoDestino, tipoOrigen);
                    return null;
                }
                if (!v.verificarAsignacionEnumerada(atributoPropio.atributo, origen, ctx.getLinea())) {
                    return null;
                }
                return new Asignacion(atributoPropio, tipoDestino, tipoOrigen);
            }

            EntradaTabla destino = destinoDe(destinoValor);
            if (destinoValor instanceof EntradaTabla) {
                destino = v.verificarAlcance((EntradaTabla) destinoValor);
            } else if (destinoValor instanceof ExpresionDiferida) {

                EntradaTabla unico = ((ExpresionDiferida) destinoValor).operandoUnico();
                if (unico != null) {
                    destino = v.verificarAlcance(unico);
                }
            }

            if (destino == null || !destino.declarado) {
                return null;
            }
            v.verificarAsignable(destino, ctx.getLinea());
            if (!v.esAsignable(destino) || origen == null || !origen.esValida()) {
                return null;
            }

            String tipoDestino = entorno.tipoEfectivo(destino.tipoDato);
            String tipoOrigen = entorno.tipoEfectivo(origen.getTipo());

            Entorno.ClaseDeclarada claseDestino = entorno.claseDe(tipoDestino);
            Entorno.ClaseDeclarada claseOrigen = entorno.claseDe(tipoOrigen);
            if (claseDestino != null && claseOrigen != null
                    && !(destinoValor instanceof ReferenciaAtributo)) {
                if (!entorno.clasesCompatibles(claseDestino, claseOrigen)) {
                    v.errorEstructuraIncompatible(ctx.getLinea(),
                            TablaSimbolos.nombreDe(destino.lexema),
                            claseDestino.clave(), claseOrigen.clave());
                    return null;
                }
                return new Asignacion(destino, tipoDestino, tipoOrigen);
            }
            if (!Tipos.INDEFINIDO.equals(tipoDestino) && !Tipos.INDEFINIDO.equals(tipoOrigen)
                    && !Tipos.mismoTipo(tipoDestino, tipoOrigen)) {
                v.errorTipos(ctx.getLinea(),
                        "la asignación a '" + TablaSimbolos.nombreDe(destino.lexema) + "'",
                        tipoDestino, tipoOrigen);
                return null;
            }
            if (!v.verificarAsignacionEnumerada(destino, origen, ctx.getLinea())) {
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
                return null;
            }
            ExpresionDiferida valor = ctx.expresion(0);
            if (valor == null || !valor.esValida()) {
                return null;
            }
            String tipoRetorno = entorno.tipoEfectivo(funcion.tipoRetorno);
            String tipoValor = entorno.tipoEfectivo(valor.getTipo());
            if (!Tipos.INDEFINIDO.equals(tipoRetorno) && !Tipos.INDEFINIDO.equals(tipoValor)
                    && !Tipos.mismoTipo(tipoRetorno, tipoValor)) {
                v.errorTipos(ctx.getLinea(),
                        "el retorno de la función '" + funcion.nombre + "'",
                        tipoRetorno, tipoValor);
                return null;
            }
            return valor;
        });
    }

    private static ReferenciaAtributo referenciaPropia(Object destinoValor) {
        if (!(destinoValor instanceof ExpresionDiferida)) {
            return null;
        }
        ExpresionDiferida expresion = (ExpresionDiferida) destinoValor;
        if (!expresion.getOperadores().isEmpty()) {
            return null;
        }
        List<Object> operandos = expresion.getOperandos();
        if (operandos.size() == 1 && operandos.get(0) instanceof ReferenciaAtributo) {
            return (ReferenciaAtributo) operandos.get(0);
        }
        return null;
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

    private static String validarTipoDeclarado(Entorno entorno, Verificaciones v,
            String tipo, int linea) {
        if (tipo == null || tipo.isEmpty()) {
            return null;
        }
        if (Tipos.esTipoBasico(tipo)) {
            return tipo;
        }
        EntradaTabla declarado = entorno.resolverTipoDeclarado(tipo);
        if (declarado == null) {
            v.errorTipoNoDeclarado(TablaSimbolos.nombreDe(tipo), linea);
            return null;
        }
        return declarado.lexema;
    }
}
