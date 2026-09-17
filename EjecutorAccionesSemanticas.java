public final class EjecutorAccionesSemanticas {
    //Llama a el metodo agregar y finalizar de cada accion semantica

    private EjecutorAccionesSemanticas() {
    }

    public static void ejecutar(int accion, ContextoLexico contexto, char caracter) {
        switch (accion) {
            case 1:
            case 2:
                AccionEstructuraAsignacion.agregar(contexto, caracter);
                break;
            case 3:
            case 4:
            case 5:
            case 6:
                AccionCaracterLiteral.agregar(contexto, caracter);
                break;
            case 7:
            case 36:
                AccionPalabraReservada.agregar(contexto, caracter);
                break;
            case 8:
            case 9:
                AccionIdentificador.agregar(contexto, caracter);
                break;
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
                AccionCadena.agregar(contexto, caracter);
                break;
            case 20:
                AccionSimboloLiteral.agregar(contexto, caracter);
                break;
            case 21:
            case 22:
            case 23:
            case 25:
            case 26:
                AccionConstanteEntera.agregar(contexto, caracter);
                break;
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
                AccionConstanteFloat.agregar(contexto, caracter);
                break;
            default:
                contexto.agregar(caracter);
                break;
        }
    }

    public static int finalizar(int accion, ContextoLexico contexto,
            TablaSimbolos tablaSimbolos, Reporte reporte) {
        switch (accion) {
            case 1:
            case 2:
                return AccionEstructuraAsignacion.finalizar(contexto);
            case 3:
            case 4:
            case 5:
            case 6:
                return AccionCaracterLiteral.finalizar(contexto, reporte);
            case 7:
            case 36:
                return AccionPalabraReservada.finalizar(contexto, tablaSimbolos, reporte);
            case 8:
            case 9:
                return AccionIdentificador.finalizar(contexto, tablaSimbolos, reporte);
            case 12:
            case 13:
            case 14:
            case 15:
                return AccionCadena.finalizar(contexto, tablaSimbolos, reporte);
            case 16:
            case 17:
            case 18:
                return 0;
            case 20:
                return AccionSimboloLiteral.finalizar(contexto);
            case 21:
            case 22:
            case 23:
            case 25:
            case 26:
                return AccionConstanteEntera.finalizar(contexto, tablaSimbolos);
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
                return AccionConstanteFloat.finalizar(contexto, tablaSimbolos);
            default:
                return contexto.lexema().isEmpty() ? 0 : contexto.lexema().charAt(0);
        }
    }
}
