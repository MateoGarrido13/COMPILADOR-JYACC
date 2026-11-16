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

    /*
      Envuelve cada valor de la lista de parametros reales en un ParametroReal.
      Si ya viene envuelto (parametro nombrado) se conserva.
     */
    private static List<ParametroReal> envolverParametros(List<Object> reales, int linea) {
        List<ParametroReal> parametros = new ArrayList<>();
        if (reales == null) {
            return parametros;
        }
        for (Object valor : reales) {
            parametros.add((ParametroReal) envolverParametro(valor, linea));
        }
        return parametros;
    }

    /*
      Completa cada ParametroReal con la EntradaTabla del formal que le
      corresponde y con el tipo del real. Devuelve null por parametro cuando no
      se pudo resolver el formal.
     */
    private static List<ParametroReal> asignarFormales(Verificaciones v,
            Entorno.FuncionDeclarada firma, List<ParametroReal> parametros, int linea) {
        List<ParametroReal> resultado = new ArrayList<>();
        for (int i = 0; i < parametros.size(); i++) {
            ParametroReal parametro = parametros.get(i);
            EntradaTabla formal = v.formalDe(firma, parametro, i, linea);
            String tipoReal = v.getEntorno().tipoEfectivo(
                    ExpresionDiferida.tipoDe(parametro.valor));
            resultado.add(parametro.conFormal(formal).conTipoReal(tipoReal));
        }
        return resultado;
    }

    /*
      Arma una invocacion de metodo. La convencion de codigo intermedio es
      (CALLMET, objeto, metodo), precedida por los PAR de los parametros.
      Sin prefijo solo se acepta un metodo propio. Con prefijo, la clase tiene
      que estar en la cadena del objeto y el metodo tiene que estar declarado
      en esa clase. Los parametros se chequean como en el tema 19.
     */
    private static LlamadaFuncion llamadaMetodo(Entorno entorno, Verificaciones v,
            EntradaTabla objeto, EntradaTabla clasePrefijo, EntradaTabla metodo,
            List<Object> parametrosReales, List<Object> ordenRaw, int linea) {
        EntradaTabla objetoResuelto = objeto == null ? null : v.verificarAlcance(objeto);
        String nombreMetodo = metodo == null ? null : TablaSimbolos.nombreDe(metodo.lexema);
        Entorno.FuncionDeclarada firma = resolverMetodo(
                entorno, v, objetoResuelto, clasePrefijo, nombreMetodo, linea);

        List<ParametroReal> parametros = envolverParametros(parametrosReales, linea);
        List<ParametroReal> conFormales = asignarFormales(v, firma, parametros, linea);
        boolean invocacionOk = v.verificarInvocacion(firma, conFormales, ordenRaw, linea);
        List<Integer> ordenEvaluacion = comoListaEnteros(ordenRaw, linea);

        LlamadaFuncion llamada = new LlamadaFuncion(
                firma != null ? firma.entrada : metodo,
                conFormales, ordenEvaluacion, linea, firma, null, objetoResuelto, true);
        if (firma == null || objetoResuelto == null || !invocacionOk) {
            llamada.marcarInvalida();
        }
        return llamada;
    }

    /*
      Resuelve la firma que puede invocarse. Informa falta de prefijo, prefijo
      ajeno a la cadena o metodo ausente en la clase nombrada. Devuelve null
      cuando la llamada no se acepta; el llamador no emite CALLMET.
     */
    private static Entorno.FuncionDeclarada resolverMetodo(Entorno entorno, Verificaciones v,
            EntradaTabla objetoResuelto, EntradaTabla clasePrefijo, String nombreMetodo, int linea) {
        if (objetoResuelto == null) {
            return null;
        }
        String nombreObjeto = nombreVisible(objetoResuelto);
        if (!esReceptorDeAtributo(objetoResuelto)) {
            v.errorNoEsObjeto(nombreObjeto, linea);
            return null;
        }
        Entorno.ClaseDeclarada clase = entorno.claseDe(objetoResuelto.tipoDato);
        if (clase == null) {
            v.errorNoEsObjeto(nombreObjeto, linea);
            return null;
        }
        if (clasePrefijo == null) {
            Entorno.FuncionDeclarada propia = entorno.metodoPropio(clase, nombreMetodo);
            if (propia != null) {
                return propia;
            }
            if (entorno.metodoHeredado(clase, nombreMetodo) != null) {
                v.errorFaltaPrefijo(nombreMetodo, clase.nombre, linea);
            } else {
                v.errorMetodoNoDeclarado(nombreMetodo, linea);
            }
            return null;
        }
        String nombrePrefijo = TablaSimbolos.nombreDe(clasePrefijo.lexema);
        Entorno.ClaseDeclarada prefijo = entorno.claseDe(nombrePrefijo);
        if (prefijo == null || !entorno.esPrefijoValido(clase, prefijo)) {
            v.errorPrefijoNoHeredado(nombrePrefijo, clase.nombre, linea);
            return null;
        }
        Entorno.FuncionDeclarada declarado = entorno.metodoPropio(prefijo, nombreMetodo);
        if (declarado == null) {
            v.errorMetodoInexistente(nombreMetodo, prefijo.nombre, linea);
            return null;
        }
        return declarado;
    }

    public static void registrar(TablaAcciones tabla, Entorno entorno, Verificaciones v) {
        tabla.registrar(Reglas.SUMA, ctx -> AccionesSemanticas.combinar(ctx, '+', v));
        tabla.registrar(Reglas.RESTA, ctx -> AccionesSemanticas.combinar(ctx, '-', v));
        tabla.registrar(Reglas.MULTIPLICACION, ctx -> AccionesSemanticas.combinar(ctx, '*', v));
        tabla.registrar(Reglas.DIVISION, ctx -> AccionesSemanticas.combinar(ctx, '/', v));

        tabla.registrar(Reglas.FACTOR_ID, ctx -> {
            EntradaTabla declaracion = v.resolverIdentificador(ctx.entrada(0), ctx.getLinea(),
                    EntradaTabla.Uso.VARIABLE,
                    EntradaTabla.Uso.PARAMETRO,
                    EntradaTabla.Uso.ATRIBUTO);
            if (declaracion == null) {
                // Ya se reporto el error; el tipo indefinido evita cascadear y
                // la marca de invalida evita generar tercetos para el uso.
                return ExpresionDiferida.deOperando(ctx.entrada(0), Tipos.INDEFINIDO)
                        .marcarInvalida();
            }
            return ExpresionDiferida.deOperando(declaracion);
        });

        tabla.registrar(Reglas.FACTOR_CONSTANTE, ctx ->
                ExpresionDiferida.deOperando(entorno.tiparConstante(ctx.entrada(0))));

        tabla.registrar(Reglas.FACTOR_CONSTANTE_NEGATIVA, ctx -> {
            EntradaTabla constante = entorno.aplicarSignoNegativo(ctx.entrada(0), ctx.getLinea());
            v.verificarRangoConstante(constante, ctx.getLinea());
            return ExpresionDiferida.deOperando(constante);
        });

        tabla.registrar(Reglas.FACTOR_CADENA, ctx ->
                ExpresionDiferida.deOperando(ctx.entrada(0), Tipos.CADENA));

        tabla.registrar(Reglas.ASIGNACION_EN_EXPRESION, ctx -> {
            EntradaTabla destino = v.verificarAlcance(ctx.entrada(0));
            ExpresionDiferida expresion = ctx.expresion(1);
            if (expresion == null) {
                expresion = new ExpresionDiferida();
            }
            // El anidamiento se detecta antes de envolver el resultado: una vez
            // envuelto, la marca de esta asignacion taparia la del RHS.
            boolean anidada = expresion != null && expresion.contieneAsignacion();
            v.verificarAsignacionAnidada(expresion, ctx.getLinea());
            if (destino == null) {
                expresion.conTipo(Tipos.INDEFINIDO);
                expresion.marcarAsignacion();
                expresion.marcarInvalida();
                return expresion;
            }
            v.verificarAsignable(destino, ctx.getLinea());
            boolean asignable = v.esAsignable(destino);
            String tipoDestino = entorno.tipoEfectivo(destino.tipoDato);
            String tipoValor = entorno.tipoEfectivo(expresion.getTipo());
            boolean tiposCompatibles = Tipos.INDEFINIDO.equals(tipoDestino)
                    || Tipos.INDEFINIDO.equals(tipoValor)
                    || Tipos.mismoTipo(tipoDestino, tipoValor);
            if (!tiposCompatibles) {
                v.errorTipos(ctx.getLinea(),
                        "la asignación a '" + TablaSimbolos.nombreDe(destino.lexema) + "'",
                        tipoDestino, tipoValor);
            }
            boolean enumeracionCompatible = v.verificarAsignacionEnumerada(
                    destino, expresion, ctx.getLinea());
            ExpresionDiferida resultado = ExpresionDiferida.deOperando(
                    new AsignacionEnExpresion(destino, expresion, tipoDestino));
            resultado.marcarAsignacion();
            if (!asignable || anidada || !expresion.esValida()
                    || !tiposCompatibles || !enumeracionCompatible) {
                resultado.marcarInvalida();
            }
            return resultado;
        });

        tabla.registrar(Reglas.CONVERSION_TODF, ctx -> {
            ExpresionDiferida expresion = ctx.expresion(0);
            if (expresion == null) {
                // todf sin operando: el error sintactico ya se reporto.
                return new ExpresionDiferida().conTipo(Tipos.DOUBLEF).marcarInvalida();
            }
            ExpresionDiferida resultado = ExpresionDiferida.deOperando(
                    new Conversion(expresion, Tipos.DOUBLEF), Tipos.DOUBLEF);
            if (!expresion.esValida()) {
                // El operando ya reporto un error; no se cascada.
                return resultado.marcarInvalida();
            }
            // El enumerado vale por su tipo base (Entorno.tipoEfectivo).
            String tipoOperando = entorno.tipoEfectivo(expresion.getTipo());
            if (Tipos.INDEFINIDO.equals(tipoOperando)) {
                // Tipo desconocido: ya se reporto el error que lo origino.
                return resultado.marcarInvalida();
            }
            // La unica conversion del lenguaje es entero -> doublef. Un
            // operando que ya es doublef tampoco se convierte a si mismo.
            if (!Tipos.esEntero(tipoOperando)) {
                v.errorConversionTodf(ctx.getLinea());
                return resultado.marcarInvalida();
            }
            return resultado;
        });

        tabla.registrar(Reglas.INVOCACION_FUNCION, ctx -> {
            EntradaTabla funcion = ctx.entrada(0);
            List<Object> parametrosReales = ctx.lista(1);
            List<Object> ordenRaw = ctx.valor(2) == null ? null : ctx.lista(2);

            List<ParametroReal> parametros = envolverParametros(parametrosReales, ctx.getLinea());
            // Resuelve la funcion con Name Mangling; informa si no existe o si
            // el nombre es de una variable (uso incorrecto).
            EntradaTabla resuelta = v.resolverFuncion(funcion, ctx.getLinea());
            Entorno.FuncionDeclarada firma = resuelta == null ? null : entorno.getFuncion(resuelta.lexema);

            List<ParametroReal> conFormales = asignarFormales(v, firma, parametros, ctx.getLinea());
            // Cantidad, orden de evaluacion, formales repetidos y tipos.
            boolean invocacionOk = v.verificarInvocacion(firma, conFormales, ordenRaw, ctx.getLinea());
            List<Integer> ordenEvaluacion = comoListaEnteros(ordenRaw, ctx.getLinea());
            LlamadaFuncion llamada = new LlamadaFuncion(
                    resuelta != null ? resuelta : funcion,
                    conFormales, ordenEvaluacion, ctx.getLinea(), firma, null);
            if (firma == null || !invocacionOk) {
                llamada.marcarInvalida();
            }
            return llamada;
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
                accesoAtributoPropio(entorno, v, ctx.entrada(0), ctx.entrada(1), ctx.getLinea()));

        tabla.registrar(Reglas.ACCESO_ATRIBUTO_PREFIJADO, ctx ->
                accesoAtributoPrefijado(entorno, v, ctx.entrada(0), ctx.entrada(1),
                        ctx.entrada(2), ctx.getLinea()));

        tabla.registrar(Reglas.ERR_FALTA_ORDEN_EVALUACION, ctx -> {
            v.reportar(Reglas.ERR_FALTA_ORDEN_EVALUACION, ctx.getLinea());
            // objeto.metodo(...) y objeto.clase.metodo(...) sin orden. La
            // llamada tradicional heredada suma el error de falta de prefijo.
            if (ctx.cantidad() >= 3) {
                EntradaTabla objeto = ctx.entrada(0);
                EntradaTabla prefijo = ctx.cantidad() >= 4 ? ctx.entrada(1) : null;
                EntradaTabla metodo = ctx.cantidad() >= 4 ? ctx.entrada(2) : ctx.entrada(1);
                EntradaTabla objetoResuelto = objeto == null ? null : v.verificarAlcance(objeto);
                String nombreMetodo = metodo == null ? "?" : TablaSimbolos.nombreDe(metodo.lexema);
                resolverMetodo(entorno, v, objetoResuelto, prefijo, nombreMetodo, ctx.getLinea());
            }
            Object resultado = ctx.cantidad() > 0 ? ctx.valor(0) : null;
            if (ctx.cantidad() >= 3) {
                // La llamada de metodo ya es invalida: no se usa el objeto
                // recuperado como si fuera el valor de la invocacion.
                if (resultado instanceof ExpresionDiferida) {
                    return ((ExpresionDiferida) resultado).conTipo(Tipos.INDEFINIDO).marcarInvalida();
                }
                return ExpresionDiferida.deOperando(resultado, Tipos.INDEFINIDO).marcarInvalida();
            }
            if (resultado instanceof ExpresionDiferida) {
                return ((ExpresionDiferida) resultado).conTipo(Tipos.INDEFINIDO);
            }
            if (resultado instanceof EntradaTabla) {
                return ExpresionDiferida.deOperando(resultado, Tipos.INDEFINIDO);
            }
            return resultado;
        });

        tabla.registrar(Reglas.INVOCACION_METODO, ctx -> {
            List<Object> ordenRaw = ctx.valor(3) == null ? null : ctx.lista(3);
            return llamadaMetodo(entorno, v, ctx.entrada(0), null, ctx.entrada(1),
                    ctx.lista(2), ordenRaw, ctx.getLinea());
        });

        tabla.registrar(Reglas.INVOCACION_METODO_PREFIJADO, ctx -> {
            List<Object> ordenRaw = ctx.valor(4) == null ? null : ctx.lista(4);
            return llamadaMetodo(entorno, v, ctx.entrada(0), ctx.entrada(1), ctx.entrada(2),
                    ctx.lista(3), ordenRaw, ctx.getLinea());
        });
    }

    /*
      Tema 28 y 31: objeto.atributo. El objeto tiene que ser variable, parametro
      o atributo de tipo clase. El atributo se busca solo entre los propios de
      esa clase (D3/DEC-5). Si el nombre existe solo en una base, el error es
      falta de prefijo. Si no existe en la cadena, no existe en la clase.
     */
    private static ExpresionDiferida accesoAtributoPropio(Entorno entorno, Verificaciones v,
            EntradaTabla objeto, EntradaTabla atributo, int linea) {
        entorno.descartarPlaceholder(atributo);
        String nombreAtributo = atributo == null ? "?" : TablaSimbolos.nombreDe(atributo.lexema);
        EntradaTabla objetoResuelto = objeto == null ? null : v.verificarAlcance(objeto);
        String nombreObjeto = nombreVisible(objetoResuelto != null ? objetoResuelto : objeto);
        String texto = nombreObjeto + "." + nombreAtributo;

        if (objetoResuelto == null) {
            return accesoInvalido(objeto, atributo, texto);
        }
        Entorno.ClaseDeclarada clase = claseDelReceptor(entorno, v, objetoResuelto, nombreObjeto, linea);
        if (clase == null) {
            return accesoInvalido(objetoResuelto, atributo, texto);
        }
        EntradaTabla declaracion = entorno.atributoPropio(clase, nombreAtributo);
        if (declaracion == null) {
            if (entorno.atributoHeredado(clase, nombreAtributo) != null) {
                v.errorFaltaPrefijo(nombreAtributo, clase.nombre, linea);
            } else {
                v.errorAtributoInexistente(nombreAtributo, clase.nombre, linea);
            }
            return accesoInvalido(objetoResuelto, atributo, texto);
        }
        ReferenciaAtributo referencia = new ReferenciaAtributo(
                objetoResuelto, declaracion, texto, declaracion.tipoDato);
        return ExpresionDiferida.deOperando(referencia, declaracion.tipoDato);
    }

    /*
      Tema 31: objeto.clase.atributo. La clase prefijada tiene que ser la del
      objeto o una base, y el atributo tiene que estar declarado ahi.
      Devuelve el tipo de ese atributo.
     */
    private static ExpresionDiferida accesoAtributoPrefijado(Entorno entorno, Verificaciones v,
            EntradaTabla objeto, EntradaTabla clasePrefijo, EntradaTabla atributo, int linea) {
        entorno.descartarPlaceholder(atributo);
        String nombreAtributo = atributo == null ? "?" : TablaSimbolos.nombreDe(atributo.lexema);
        String nombrePrefijo = clasePrefijo == null ? "?" : TablaSimbolos.nombreDe(clasePrefijo.lexema);
        EntradaTabla objetoResuelto = objeto == null ? null : v.verificarAlcance(objeto);
        String nombreObjeto = nombreVisible(objetoResuelto != null ? objetoResuelto : objeto);
        String texto = nombreObjeto + "." + nombrePrefijo + "." + nombreAtributo;

        if (objetoResuelto == null) {
            return accesoInvalido(objeto, atributo, texto);
        }
        Entorno.ClaseDeclarada clase = claseDelReceptor(entorno, v, objetoResuelto, nombreObjeto, linea);
        if (clase == null) {
            return accesoInvalido(objetoResuelto, atributo, texto);
        }
        Entorno.ClaseDeclarada prefijo = entorno.claseDe(nombrePrefijo);
        if (prefijo == null || !entorno.esPrefijoValido(clase, prefijo)) {
            v.errorPrefijoNoHeredado(nombrePrefijo, clase.nombre, linea);
            return accesoInvalido(objetoResuelto, atributo, texto);
        }
        EntradaTabla declaracion = entorno.atributoPropio(prefijo, nombreAtributo);
        if (declaracion == null) {
            v.errorAtributoInexistente(nombreAtributo, prefijo.nombre, linea);
            return accesoInvalido(objetoResuelto, atributo, texto);
        }
        ReferenciaAtributo referencia = new ReferenciaAtributo(
                objetoResuelto, declaracion, texto, declaracion.tipoDato);
        return ExpresionDiferida.deOperando(referencia, declaracion.tipoDato);
    }

    private static Entorno.ClaseDeclarada claseDelReceptor(Entorno entorno, Verificaciones v,
            EntradaTabla objeto, String nombreObjeto, int linea) {
        if (!esReceptorDeAtributo(objeto)) {
            v.errorNoEsObjeto(nombreObjeto, linea);
            return null;
        }
        Entorno.ClaseDeclarada clase = entorno.claseDe(objeto.tipoDato);
        if (clase == null) {
            v.errorNoEsObjeto(nombreObjeto, linea);
            return null;
        }
        return clase;
    }

    private static boolean esReceptorDeAtributo(EntradaTabla objeto) {
        return objeto.uso == EntradaTabla.Uso.VARIABLE
                || objeto.uso == EntradaTabla.Uso.PARAMETRO
                || objeto.uso == EntradaTabla.Uso.ATRIBUTO;
    }

    private static String nombreVisible(EntradaTabla entrada) {
        if (entrada == null || entrada.lexema == null) {
            return "?";
        }
        return TablaSimbolos.nombreDe(entrada.lexema);
    }

    private static ExpresionDiferida accesoInvalido(EntradaTabla objeto,
            EntradaTabla atributo, String texto) {
        ReferenciaAtributo referencia = new ReferenciaAtributo(objeto, atributo, texto, null);
        return ExpresionDiferida.deOperando(referencia, Tipos.INDEFINIDO).marcarInvalida();
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
