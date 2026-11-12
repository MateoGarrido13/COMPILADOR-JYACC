import java.util.List;

/*
  Reescribe la clave de los identificadores que ya tienen ambito de declaracion.
  Se lanza al entrar y al salir de una funcion. No decide cuando cambia el ambito.
 */
public class MonitorAmbito {

    private static TablaSimbolos tabla;

    public static void usar(TablaSimbolos simbolos) {
        tabla = simbolos;
    }

    public static void reclamar(List<EntradaTabla> entradas, String ambitoNuevo) {
        if (tabla == null || entradas == null || ambitoNuevo == null) {
            return;
        }
        for (EntradaTabla entrada : entradas) {
            String nombre = TablaSimbolos.nombreDe(entrada.lexema);
            entrada.ambito = ambitoNuevo;
            tabla.renombrar(entrada, nombre + ":" + ambitoNuevo);
        }
    }

    public static void alEntrar(String ambitoAnterior, String ambitoNuevo) {
        if (tabla == null || ambitoAnterior == null || ambitoNuevo == null) {
            return;
        }
        List<EntradaTabla> entradas = tabla.conAmbito();
        for (EntradaTabla entrada : entradas) {
            if (!ambitoAnterior.equals(TablaSimbolos.ambitoDe(entrada.lexema))) {
                continue;
            }
            String nombre = TablaSimbolos.nombreDe(entrada.lexema);
            tabla.renombrar(entrada, nombre + ":" + ambitoNuevo);
        }
    }

    public static void alSalir(String ambitoViejo) {
        if (tabla == null || ambitoViejo == null) {
            return;
        }
        List<EntradaTabla> entradas = tabla.conAmbito();
        for (EntradaTabla entrada : entradas) {
            if (ambitoViejo.equals(entrada.ambito)) {
                tabla.eliminar(entrada);
            } else if (ambitoViejo.equals(TablaSimbolos.ambitoDe(entrada.lexema))) {
                int corte = entrada.lexema.lastIndexOf(':');
                if (corte > 0) {
                    tabla.renombrar(entrada, entrada.lexema.substring(0, corte));
                }
            }
        }
    }
}
