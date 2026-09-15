import java.util.ArrayList;
import java.util.List;

/**
 * RAsociamos cada regla de la gramatica con el
 * codigo ejecutable que la verifica.
 *
 * Este es el unico lugar donde se decide que hace cada produccion, de modo que
 * gramatica.y quede limitado a la gramatica y a la invocacion de la tabla.
 */
public class AccionesSemanticas { 

    public static void registrar(TablaAcciones tabla, Verificaciones v, Reporte reporte) {

        // ===============================================================
        // Estructura general del programa
        // ===============================================================

        tabla.registrar(Reglas.PROGRAMA, ctx -> {
            v.verificarRetornosFueraDeFuncion();
            return ctx.valor(0);
        });

        tabla.registrar(Reglas.NOMBRE_PROGRAMA, ctx -> {
            EntradaTabla nombre = ctx.entrada(0);
            v.verificarFormatoIdentificador(nombre, ctx.getLinea());
            return nombre;
        });

        tabla.registrar(Reglas.LISTA_SENTENCIAS, AccionesSemanticas::concatenarLista);

        // ===============================================================
        // Sentencias declarativas
        // ===============================================================

        tabla.registrar(Reglas.DECL_VARIABLES, ctx -> {
            String tipo = ctx.texto(0);
            List<Object> identificadores = ctx.lista(1);
            for (Object elemento : identificadores) {
                v.declararVariable(ContextoRegla.comoEntrada(elemento), tipo, ctx.getLinea());
            }
            return identificadores;
        });

        tabla.registrar(Reglas.LISTA_VARIABLES, AccionesSemanticas::concatenarLista);

        // Tema 25: declaracion de objetos de una clase.
        tabla.registrar(Reglas.DECL_OBJETOS, ctx -> {
            String nombreClase = ctx.texto(0);
            List<Object> identificadores = ctx.lista(1);
            v.declararObjetos(identificadores, nombreClase, ctx.getLinea());
            return identificadores;
        });

        // Accion intermedia: abre el contexto de la funcion antes de reducir el cuerpo.
        tabla.registrar(Reglas.INICIO_FUNCION, ctx ->
                v.abrirFuncion(ctx.entrada(1), ctx.texto(0), ctx.getLinea()));

        tabla.registrar(Reglas.PARAMETRO_FORMAL, ctx -> {
            EntradaTabla id = ctx.entrada(1);
            v.agregarParametroFormal(id, ctx.texto(0), ctx.getLinea());
            return id;
        });

        tabla.registrar(Reglas.LISTA_PARAMETROS_FORMALES, AccionesSemanticas::concatenarLista);

        tabla.registrar(Reglas.DECL_FUNCION, ctx -> {
            v.cerrarFuncion(ctx.getLinea());
            return ctx.valor(0);
        });

        tabla.registrar(Reglas.DECL_METODO, ctx -> {
            v.cerrarFuncion(ctx.getLinea());
            return ctx.valor(0);
        });

        // Accion intermedia: registra la clase antes de procesar atributos y metodos.
        tabla.registrar(Reglas.INICIO_CLASE, ctx -> v.abrirClase(ctx.entrada(0), ctx.getLinea()));

        tabla.registrar(Reglas.DECL_CLASE, ctx -> {
            Object valor = ctx.valor(0);
            v.cerrarClase();
            return valor;
        });

        tabla.registrar(Reglas.DECL_ATRIBUTO, ctx -> {
            String tipo = ctx.texto(0);
            List<Object> identificadores = ctx.lista(1);
            for (Object elemento : identificadores) {
                v.declararAtributo(ContextoRegla.comoEntrada(elemento), tipo, ctx.getLinea());
            }
            return identificadores;
        });

        // Tema 31: herencia multiple.
        tabla.registrar(Reglas.SENTENCIA_EXTENDS, ctx -> {
            List<Object> padres = ctx.lista(0);
            v.verificarHerencia(padres, ctx.getLinea());
            return padres;
        });

        tabla.registrar(Reglas.LISTA_CLASES_HEREDADAS, AccionesSemanticas::concatenarLista);

        // Tema 23: typedef de enumeraciones.
        tabla.registrar(Reglas.DECL_TYPEDEF, ctx -> {
            EntradaTabla id = ctx.entrada(0);
            v.declararTipoEnumerado(id, ctx.lista(1), ctx.getLinea());
            return id;
        });

        tabla.registrar(Reglas.LISTA_VALORES_ENUMERADO, AccionesSemanticas::concatenarLista);

        // ===============================================================
        // Sentencias ejecutables
        // ===============================================================

        tabla.registrar(Reglas.ASIGNACION, ctx -> {
            EntradaTabla destino = ctx.entrada(0);
            ExpresionDiferida expresion = ctx.expresion(1);
            v.verificarVariableDeclarada(destino, ctx.getLinea());
            v.verificarOperandosDeclarados(expresion, ctx.getLinea());
            return destino;
        });

        tabla.registrar(Reglas.CONDICION, ctx -> {
            v.verificarOperandosDeclarados(ctx.expresion(0), ctx.getLinea());
            v.verificarOperandosDeclarados(ctx.expresion(2), ctx.getLinea());
            return ctx.valor(0);
        });

        tabla.registrar(Reglas.SENTENCIA_RET, ctx -> {
            v.registrarRetorno(ctx.getLinea());
            v.verificarOperandosDeclarados(ctx.expresion(0), ctx.getLinea());
            return ctx.valor(0);
        });

        tabla.registrar(Reglas.SENTENCIA_POUT, ctx -> {
            Object contenido = ctx.valor(0);
            if (contenido instanceof ExpresionDiferida) {
                v.verificarOperandosDeclarados((ExpresionDiferida) contenido, ctx.getLinea());
            }
            return contenido;
        });

        // ===============================================================
        // Expresiones (recursivas a derecha, plegado a izquierda diferido)
        // ===============================================================

        tabla.registrar(Reglas.SUMA, ctx -> combinar(ctx, '+'));
        tabla.registrar(Reglas.RESTA, ctx -> combinar(ctx, '-'));
        tabla.registrar(Reglas.MULTIPLICACION, ctx -> combinar(ctx, '*'));
        tabla.registrar(Reglas.DIVISION, ctx -> combinar(ctx, '/'));

        tabla.registrar(Reglas.FACTOR_ID, ctx -> {
            EntradaTabla id = ctx.entrada(0);
            v.verificarVariableDeclarada(id, ctx.getLinea());
            return ExpresionDiferida.deOperando(id);
        });

        tabla.registrar(Reglas.FACTOR_CONSTANTE, ctx ->
                ExpresionDiferida.deOperando(v.verificarConstante(ctx.entrada(0), ctx.getLinea())));

        // Consideracion c) del TP2: constantes negativas detectadas en el sintactico.
        tabla.registrar(Reglas.FACTOR_CONSTANTE_NEGATIVA, ctx ->
                ExpresionDiferida.deOperando(v.aplicarSignoNegativo(ctx.entrada(0), ctx.getLinea())));

        tabla.registrar(Reglas.FACTOR_CADENA, ctx ->
                ExpresionDiferida.deOperando(ctx.entrada(0)));

        // Tema 17: asignacion de expresiones en expresiones, sin anidamiento.
        tabla.registrar(Reglas.ASIGNACION_EN_EXPRESION, ctx -> {
            EntradaTabla destino = ctx.entrada(0);
            ExpresionDiferida expresion = ctx.expresion(1);
            v.verificarOperandosDeclarados(expresion, ctx.getLinea());
            return v.registrarAsignacionEnExpresion(destino, expresion, ctx.getLinea());
        });

        // Tema 33: conversion explicita de entero a punto flotante.
        tabla.registrar(Reglas.CONVERSION_TODF, ctx -> {
            ExpresionDiferida expresion = ctx.expresion(0);
            v.verificarOperandosDeclarados(expresion, ctx.getLinea());
            return expresion;
        });

        // Tema 19: invocacion con orden de evaluacion obligatorio.
        tabla.registrar(Reglas.INVOCACION_FUNCION, ctx -> {
            EntradaTabla id = ctx.entrada(0);
            List<Object> parametrosReales = ctx.lista(1);
            List<Object> ordenEvaluacion = ctx.valor(2) == null ? null : ctx.lista(2);
            v.verificarInvocacion(id, parametrosReales, ordenEvaluacion, ctx.getLinea());
            return ExpresionDiferida.deOperando(id);
        });

        tabla.registrar(Reglas.LISTA_PARAMETROS_REALES, AccionesSemanticas::concatenarLista);
        tabla.registrar(Reglas.LISTA_ORDEN_EVALUACION, AccionesSemanticas::concatenarLista);

        // Tema 28: acceso tradicional a atributos.
        tabla.registrar(Reglas.ACCESO_ATRIBUTO, ctx -> {
            EntradaTabla objeto = ctx.entrada(0);
            EntradaTabla miembro = ctx.entrada(1);
            v.verificarAccesoAtributo(objeto, miembro, ctx.getLinea());
            return ExpresionDiferida.deOperando(miembro);
        });

        // Tema 31: acceso desambiguado con prefijo de clase.
        tabla.registrar(Reglas.ACCESO_ATRIBUTO_PREFIJADO, ctx -> {
            EntradaTabla objeto = ctx.entrada(0);
            EntradaTabla prefijo = ctx.entrada(1);
            EntradaTabla miembro = ctx.entrada(2);
            v.verificarAccesoPrefijado(objeto, prefijo, miembro, ctx.getLinea());
            return ExpresionDiferida.deOperando(miembro);
        });

        tabla.registrar(Reglas.INVOCACION_METODO, ctx -> {
            EntradaTabla objeto = ctx.entrada(0);
            EntradaTabla metodo = ctx.entrada(1);
            v.verificarAccesoAtributo(objeto, metodo, ctx.getLinea());
            return ExpresionDiferida.deOperando(metodo);
        });

        // ===============================================================
        // Producciones de error: informan y permiten continuar la compilacion
        // ===============================================================

        for (Reglas regla : Reglas.values()) {
            if (regla.esError()) {
                tabla.registrar(regla, ctx -> {
                    reporte.error(ctx.getLinea(), ctx.getRegla().getDescripcion());
                    return null;
                });
            }
        }
    }

    /** Listas recursivas a derecha: el elemento actual mas lo ya acumulado a la derecha. */
    private static Object concatenarLista(ContextoRegla ctx) {
        List<Object> elementos = new ArrayList<>();
        elementos.add(ctx.valor(0));
        elementos.addAll(ctx.lista(1));
        return elementos;
    }

    /** Acumula "termino OP expresion" sin agrupar todavia, para plegar a izquierda despues. */
    private static Object combinar(ContextoRegla ctx, char operador) {
        ExpresionDiferida derecha = ctx.expresion(1);
        if (derecha == null) {
            derecha = new ExpresionDiferida();
        }
        return derecha.anteponer(ctx.valor(0), operador);
    }
}
