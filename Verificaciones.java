import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
  Chequeos semanticos que necesitan contexto. Se invocan desde AccionesSemanticas.
 
  El parser es ascendente, por eso el contexto se va armando con acciones intermedias
  y no al reducir la declaracion completa.
 
  Verificaciones que no dependen del contexto (por ejemplo, constantes fuera de rango)
  se hacen en AccionesSemanticas y no en esta clase.
 
  - reportar: canal unico de las producciones ERR_*
  - tema 17: asignacion en expresion anidada
  - tema 19: lista de orden de evaluacion ausente o vacia
  - tema 23: enumeracion sin valores
  - Paso de parametros: cantidad, orden de evaluacion y alcance de funcion
 */
public class Verificaciones {

    private final Entorno entorno;
    private final Reporte reporte;

    public Verificaciones(Entorno entorno, Reporte reporte) {
        this.entorno = entorno;
        this.reporte = reporte;
    }

    public Entorno getEntorno() {
        return entorno;
    }

    /*
      Devuelve la declaracion resuelta por la cadena de ambitos. Si no existe,
      reporta el error y devuelve null. Las acciones usan la entrada devuelta
      en lugar del placeholder creado por el lexico.
     */
    public EntradaTabla verificarAlcance(EntradaTabla id) {
        if (id == null) {
            return null;
        }
        String nombre = TablaSimbolos.nombreDe(id.lexema);
        EntradaTabla declaracion = entorno.resolver(nombre, Globals.AMBITO_ACTUAL);
        if (declaracion != null) {
            return declaracion;
        }
        int linea = id.lineaAparicion > 0 ? id.lineaAparicion : id.numeroLinea;
        reporte.error(linea, "Error de ambito '" + nombre + "' no es alcanzable");
        return null;
    }

    /*
      Resuelve un identificador usado como valor y exige que su uso sea uno de
      los permitidos. Se usa en los factores para que una funcion o una clase
      no puedan aparecer donde el lenguaje espera una variable.
     */
    public EntradaTabla resolverIdentificador(EntradaTabla id, int linea,
            EntradaTabla.Uso... usosPermitidos) {
        EntradaTabla declaracion = verificarAlcance(id);
        if (declaracion == null) {
            return null;
        }
        for (EntradaTabla.Uso uso : usosPermitidos) {
            if (declaracion.uso == uso) {
                return declaracion;
            }
        }
        reporte.error(linea, "'" + TablaSimbolos.nombreDe(declaracion.lexema)
                + "' no es un valor utilizable como factor");
        return null;
    }

    /*
      Resuelve el nombre de una funcion invocada como ID(...). Si no existe o
      no es visible informa error; si el nombre existe pero no es una funcion
      (por ejemplo una variable) informa uso incorrecto. Devuelve null cuando
      no se puede invocar.
     */
    public EntradaTabla resolverFuncion(EntradaTabla id, int linea) {
        if (id == null) {
            return null;
        }
        String nombre = TablaSimbolos.nombreDe(id.lexema);
        EntradaTabla declaracion = entorno.resolver(nombre, Globals.AMBITO_ACTUAL);
        if (declaracion == null) {
            reporte.error(linea, "Funcion no declarada '" + nombre + "'");
            return null;
        }
        if (declaracion.uso != EntradaTabla.Uso.FUNCION) {
            reporte.error(linea, "Uso incorrecto de '" + nombre
                    + "': no es una funcion");
            return null;
        }
        return declaracion;
    }

    public void errorMetodoNoDeclarado(String nombre, int linea) {
        reporte.error(linea, "Metodo no declarado o no visible '"
                + (nombre == null ? "?" : nombre) + "'");
    }

    public void reportar(Reglas regla, int linea) {
        if (regla == null) {
            return;
        }
        reporte.error(linea, regla.getDescripcion());
    }

    /*
      Canal unico de errores de tipos. contexto describe la construccion
      (operador, asignacion, condicion, retorno) y esperado/obtenido son los
      tipos ya normalizados con Entorno.tipoEfectivo.
     */
    public void errorTipos(int linea, String contexto, String esperado, String obtenido) {
        reporte.error(linea, "Tipos incompatibles en " + contexto
                + ": se esperaba " + texto(esperado) + " y se encontró " + texto(obtenido));
    }

    /*
      Tema 33: la unica conversion del lenguaje es todf, de un entero
      (ulongint o un enumerado con base ulongint) a doublef. Un operando
      doublef, una cadena o cualquier otro tipo no es convertible.
     */
    public void errorConversionTodf(int linea) {
        reporte.error(linea, "todf solo convierte enteros a doublef");
    }

    public void errorTipoNoDeclarado(String tipo, int linea) {
        reporte.error(linea, "tipo no declarado '" + tipo + "'");
    }

    // Tema 28: el receptor de objeto.atributo no es variable, parametro o atributo de clase.
    public void errorNoEsObjeto(String nombre, int linea) {
        reporte.error(linea, "'" + (nombre == null ? "?" : nombre) + "' no es un objeto");
    }

    // Tema 28: el atributo no esta declarado en la clase que se nombro.
    public void errorAtributoInexistente(String atributo, String clase, int linea) {
        reporte.error(linea, "atributo '" + (atributo == null ? "?" : atributo)
                + "' no existe en la clase '" + (clase == null ? "?" : clase) + "'");
    }

    // Tema 31: el metodo no esta declarado en la clase prefijada.
    public void errorMetodoInexistente(String metodo, String clase, int linea) {
        reporte.error(linea, "metodo '" + (metodo == null ? "?" : metodo)
                + "' no existe en la clase '" + (clase == null ? "?" : clase) + "'");
    }

    // D3: el miembro existe solo en una base y el acceso no trajo el prefijo.
    public void errorFaltaPrefijo(String miembro, String clase, int linea) {
        reporte.error(linea, "falta de prefijo para '" + (miembro == null ? "?" : miembro)
                + "' en la clase '" + (clase == null ? "?" : clase) + "'");
    }

    // El prefijo no es la clase del objeto ni una base de su cadena.
    public void errorPrefijoNoHeredado(String prefijo, String clase, int linea) {
        reporte.error(linea, "el prefijo '" + (prefijo == null ? "?" : prefijo)
                + "' no es una clase heredada de '" + (clase == null ? "?" : clase) + "'");
    }

    public void errorClaseBaseNoDeclarada(String nombre, int linea) {
        reporte.error(linea, "clase base no declarada '" + (nombre == null ? "?" : nombre) + "'");
    }

    public void errorNoEsClase(String nombre, int linea) {
        reporte.error(linea, "'" + (nombre == null ? "?" : nombre) + "' no es una clase");
    }

    public void errorHerenciaCircular(String clase, String base, int linea) {
        reporte.error(linea, "herencia circular entre '" + (clase == null ? "?" : clase)
                + "' y '" + (base == null ? "?" : base) + "'");
    }

    public void errorBaseRepetida(String nombre, int linea) {
        reporte.error(linea, "clase base repetida '" + (nombre == null ? "?" : nombre) + "'");
    }

    /*
      Tema 31: cada base se resuelve con Name Mangling, tiene que existir y
      ser una clase, no puede ser la misma clase ni cerrar un ciclo, y no
      puede repetirse. Las bases validas quedan en heredaDe, en orden.
     */
    public void registrarHerencia(List<Object> padres, int linea) {
        Entorno.ClaseDeclarada clase = entorno.getClaseActual();
        if (clase == null || padres == null) {
            return;
        }
        for (Object elemento : padres) {
            EntradaTabla token = ContextoRegla.comoEntrada(elemento);
            if (token == null) {
                continue;
            }
            String nombre = TablaSimbolos.nombreDe(token.lexema);
            EntradaTabla declaracion = entorno.resolver(nombre, Globals.AMBITO_ACTUAL);
            if (declaracion == null || declaracion.uso != EntradaTabla.Uso.CLASE) {
                if (declaracion != null) {
                    errorNoEsClase(nombre, linea);
                } else if (!entorno.anotarBasePendiente(clase, nombre)) {
                    errorBaseRepetida(nombre, linea);
                } else {
                    errorClaseBaseNoDeclarada(nombre, linea);
                }
                continue;
            }
            Entorno.ClaseDeclarada base = entorno.getClase(declaracion.lexema);
            if (base == null) {
                errorClaseBaseNoDeclarada(nombre, linea);
                continue;
            }
            if (entorno.alcanzaClase(base, clase) || base.basesPendientes.contains(clase.nombre)) {
                errorHerenciaCircular(clase.nombre, base.nombre, linea);
                continue;
            }
            if (!entorno.agregarBase(clase, base)) {
                errorBaseRepetida(base.nombre, linea);
            }
        }
    }

    // D4: las clases no coinciden en cantidad u orden de tipos de atributos.
    public void errorEstructuraIncompatible(int linea, String destino,
            String claseDestino, String claseOrigen) {
        reporte.error(linea, "Incompatibilidad estructural en la asignación a '"
                + destino + "': '" + claseDestino + "' y '" + claseOrigen
                + "' no tienen la misma estructura");
    }

    public void errorRetornoFueraDeFuncion(int linea) {
        reporte.error(linea, "Sentencia ret fuera de una funcion");
    }

    // El destino de una asignacion debe ser variable, parametro o atributo.
    public boolean esAsignable(EntradaTabla destino) {
        return destino != null && destino.declarado
                && (destino.uso == EntradaTabla.Uso.VARIABLE
                || destino.uso == EntradaTabla.Uso.PARAMETRO
                || destino.uso == EntradaTabla.Uso.ATRIBUTO);
    }

    public void verificarAsignable(EntradaTabla destino, int linea) {
        if (destino == null || !destino.declarado) {
            return;
        }
        if (!esAsignable(destino)) {
            reporte.error(linea, "'" + TablaSimbolos.nombreDe(destino.lexema) + "' no es asignable");
        }
    }

    private static String texto(String tipo) {
        return tipo == null || tipo.isEmpty() ? Tipos.INDEFINIDO : tipo;
    }

    // Tema 17: ID = (expr) no puede contener otra asignacion. 
    // La marca viaja por el plegado aritmetico (absorber), pero no por los
    // argumentos de una invocacion: el Tema 19 admite asignaciones en los
    // parametros reales (f(a, b=(4), a+b)) y no deben contarse como anidamiento.
    public void verificarAsignacionAnidada(ExpresionDiferida expresion, int linea) {
        if (expresion != null && expresion.contieneAsignacion()) {
            reportar(Reglas.ERR_ASIGNACION_ANIDADA, linea);
        }
    }

    // Tema 19: falta [orden] o la lista vino vacia. 
    public void verificarOrdenEvaluacion(List<Object> constantes, int linea) {
        if (constantes == null || constantes.isEmpty()) {
            reportar(Reglas.ERR_FALTA_ORDEN_EVALUACION, linea);
        }
    }

    /*
      Valida el orden de evaluacion de los parametros reales contra la cantidad
      de parametros de la funcion.
      
      Reglas:
      - cada posicion indicada debe estar en el rango [1, cantidad]
      - no debe repetirse ninguna posicion
      - la cantidad de posiciones indicadas debe coincidir con la cantidad de
        parametros reales
     */
    public boolean verificarOrdenEvaluacion(List<Object> constantes, int cantidadParametros, int linea) {
        if (constantes == null || constantes.isEmpty()) {
            reportar(Reglas.ERR_FALTA_ORDEN_EVALUACION, linea);
            return false;
        }

        boolean valido = true;
        Set<BigInteger> vistos = new HashSet<>();
        Set<BigInteger> posicionesValidas = new HashSet<>();
        for (Object constante : constantes) {
            EntradaTabla entrada = ContextoRegla.comoEntrada(constante);
            if (entrada == null) {
                reporte.error(linea, "Posicion de orden de evaluacion invalida");
                valido = false;
                continue;
            }
            BigInteger posicion = Tipos.valorEntero(entrada.lexema);
            if (posicion == null) {
                reporte.error(linea, "Posicion de orden de evaluacion no es un entero: "
                        + entrada.lexema);
                valido = false;
                continue;
            }
            if (!vistos.add(posicion)) {
                reporte.error(linea, "Posicion de orden de evaluacion duplicada: " + posicion);
                valido = false;
            }
            if (posicion.compareTo(BigInteger.ONE) < 0
                    || posicion.compareTo(BigInteger.valueOf(cantidadParametros)) > 0) {
                reporte.error(linea, "Posicion de orden de evaluacion fuera de rango: " + posicion);
                valido = false;
            } else {
                posicionesValidas.add(posicion);
            }
        }

        if (constantes.size() != cantidadParametros) {
            reporte.error(linea, "Cantidad de posiciones de orden de evaluacion ("
                    + constantes.size()
                    + ") no coincide con la cantidad de parametros (" + cantidadParametros + ")");
            valido = false;
        }
        if (posicionesValidas.size() != cantidadParametros) {
            reporte.error(linea, "Cantidad de posiciones validas y distintas ("
                    + posicionesValidas.size() + ") no coincide con la cantidad de parametros ("
                    + cantidadParametros + ")");
            valido = false;
        }
        return valido;
    }

    // Verifica que la cantidad de parametros reales coincida con los formales declarados.
    public boolean verificarCantidadParametros(EntradaTabla funcion, List<ParametroReal> parametros, int linea) {
        if (funcion == null) {
            return false;
        }
        String nombre = TablaSimbolos.nombreDe(funcion.lexema);
        EntradaTabla declaracion = entorno.resolver(nombre, Globals.AMBITO_ACTUAL);
        Entorno.FuncionDeclarada fd = declaracion == null ? null : entorno.getFuncion(declaracion.lexema);
        if (fd == null) {
            reporte.error(linea, "Funcion no declarada '" + nombre + "'");
            return false;
        }
        return verificarCantidadParametros(fd, parametros, linea);
    }

    public boolean verificarCantidadParametros(Entorno.FuncionDeclarada firma,
            List<ParametroReal> parametros, int linea) {
        if (firma == null) {
            return false;
        }
        int formales = firma.parametros.size();
        int reales = parametros == null ? 0 : parametros.size();
        if (reales != formales) {
            reporte.error(linea, "Cantidad de parametros reales (" + reales
                    + ") no coincide con los formales declarados (" + formales + ")");
            return false;
        }
        return true;
    }

    /*
      Devuelve la entrada del parametro formal que corresponde a un real.
      Para un real nombrado se busca por nombre; para uno posicional, por la
      posicion en la lista de formales. Si el nombre no existe, se reporta.
     */
    public EntradaTabla formalDe(Entorno.FuncionDeclarada firma, ParametroReal parametro,
            int posicion, int linea) {
        if (firma == null || parametro == null) {
            return null;
        }
        if (parametro.nombreFormal != null) {
            for (Entorno.Parametro formal : firma.parametros) {
                if (parametro.nombreFormal.equals(formal.nombre)) {
                    return formal.entrada;
                }
            }
            reporte.error(linea, "Parametro formal '" + parametro.nombreFormal
                    + "' no existe en la funcion '" + firma.nombre + "'");
            return null;
        }
        if (posicion < 0 || posicion >= firma.parametros.size()) {
            return null;
        }
        return firma.parametros.get(posicion).entrada;
    }

    /*
      Chequeos completos de una invocacion (Tema 19). Recibe los parametros
      reales ya resueltos con su formal. Verifica cantidad, orden de evaluacion,
      formales nombrados no repetidos y el tipo de cada real contra su formal.
      Informa todos los errores con la linea de la invocacion.
     */
    public boolean verificarInvocacion(Entorno.FuncionDeclarada firma,
            List<ParametroReal> parametros, List<Object> ordenRaw, int linea) {
        boolean cantidadOk = verificarCantidadParametros(firma, parametros, linea);
        int cantidad = firma != null ? firma.parametros.size()
                : (parametros == null ? 0 : parametros.size());
        boolean ordenOk = verificarOrdenEvaluacion(ordenRaw, cantidad, linea);
        boolean repetidosOk = verificarFormalesRepetidos(parametros, linea);
        boolean tiposOk = verificarTiposParametros(firma, parametros, linea);
        return cantidadOk && ordenOk && repetidosOk && tiposOk
                && formalesCompletos(parametros);
    }

    // Dos reales no pueden quedar asociados al mismo formal.
    public boolean verificarFormalesRepetidos(List<ParametroReal> parametros, int linea) {
        if (parametros == null) {
            return true;
        }
        Set<String> vistos = new HashSet<>();
        boolean valido = true;
        for (ParametroReal parametro : parametros) {
            if (parametro.formal == null) {
                continue;
            }
            String claveFormal = parametro.formal.lexema;
            if (!vistos.add(claveFormal)) {
                String nombre = TablaSimbolos.nombreDe(claveFormal);
                reporte.error(linea, "Parametro formal '" + nombre
                        + "' repetido en la invocacion");
                valido = false;
            }
        }
        return valido;
    }

    // El tipo de cada real debe ser exactamente el del formal, sin conversiones.
    public boolean verificarTiposParametros(Entorno.FuncionDeclarada firma,
            List<ParametroReal> parametros, int linea) {
        if (firma == null || parametros == null) {
            return false;
        }
        boolean valido = true;
        for (ParametroReal parametro : parametros) {
            if (parametro.formal == null) {
                valido = false;
                continue;
            }
            String tipoFormal = entorno.tipoEfectivo(parametro.formal.tipoDato);
            String tipoReal = entorno.tipoEfectivo(parametro.tipoReal);
            // Sin tipo no se sigue chequeando para no cascadear errores.
            if (Tipos.INDEFINIDO.equals(tipoFormal) || Tipos.INDEFINIDO.equals(tipoReal)) {
                continue;
            }
            if (!Tipos.mismoTipo(tipoFormal, tipoReal)) {
                String nombre = parametro.nombreFormal != null
                        ? "'" + parametro.nombreFormal + "'"
                        : "'" + TablaSimbolos.nombreDe(parametro.formal.lexema) + "'";
                errorTipos(linea, "el parametro real " + nombre
                        + " de '" + firma.nombre + "'", tipoFormal, tipoReal);
                valido = false;
            }
        }
        return valido;
    }

    private static boolean formalesCompletos(List<ParametroReal> parametros) {
        if (parametros == null) {
            return false;
        }
        for (ParametroReal parametro : parametros) {
            if (parametro.formal == null) {
                return false;
            }
        }
        return true;
    }

    // el signo unario puede sacar la constante de rango. 
    public void verificarRangoConstante(EntradaTabla constante, int linea) {
        if (constante == null || constante.lexema == null) {
            return;
        }
        String tipo = constante.tipoDato != null
                ? constante.tipoDato
                : Tipos.tipoDeConstante(constante.lexema);
        if (tipo != null && !Tipos.rangoValido(constante.lexema, tipo)) {
            reporte.error(linea, "Constante fuera de rango '" + constante.lexema + "'");
        }
    }

    public String verificarValoresEnumerado(List<Object> valores, int linea) {
        if (valores == null || valores.isEmpty()) {
            reportar(Reglas.ERR_FALTA_VALORES_ENUMERADO, linea);
            return null;
        }
        String tipoBase = null;
        boolean valido = true;
        for (Object elemento : valores) {
            EntradaTabla valor = ContextoRegla.comoEntrada(elemento);
            if (valor == null || valor.uso != EntradaTabla.Uso.CONSTANTE) {
                reporte.error(linea, "El valor de una enumeracion debe ser una constante numerica");
                valido = false;
                continue;
            }
            String tipo = Tipos.tipoDeConstante(valor.lexema);
            if (tipo == null) {
                reporte.error(linea, "Tipo de constante invalido en la enumeracion");
                valido = false;
                continue;
            }
            if (tipoBase == null) {
                tipoBase = tipo;
            } else if (!Tipos.mismoTipo(tipoBase, tipo)) {
                reporte.error(linea, "Todos los valores de la enumeracion deben tener el mismo tipo base");
                valido = false;
            }
        }
        return valido ? tipoBase : null;
    }

    public boolean verificarTipoEnumeradoDisponible(EntradaTabla id, int linea) {
        if (entorno.nombreTipoDisponible(id)) {
            return true;
        }
        String nombre = id == null ? "?" : TablaSimbolos.nombreDe(id.lexema);
        reporte.error(linea, "Tipo enumerado redeclarado en el mismo ambito '" + nombre + "'");
        return false;
    }

    public void reportarErrorTipoEnumerado(EntradaTabla id, int linea) {
        String nombre = id == null ? "?" : TablaSimbolos.nombreDe(id.lexema);
        reporte.error(linea, "No se pudo registrar el tipo enumerado '" + nombre + "'");
    }

    public boolean verificarAsignacionEnumerada(EntradaTabla destino,
            ExpresionDiferida origen, int linea) {
        if (destino == null || origen == null) {
            return true;
        }
        Entorno.TipoEnumerado tipoDestino = entorno.getTipoEnumerado(destino.tipoDato);
        if (tipoDestino == null) {
            return true;
        }

        EntradaTabla operando = origen.operandoUnico();
        if (operando == null) {
            return true;
        }
        if (operando.uso == EntradaTabla.Uso.CONSTANTE) {
            String tipoConstante = Tipos.tipoDeConstante(operando.lexema);
            if (!Tipos.mismoTipo(tipoDestino.tipoBase, tipoConstante)) {
                return true;
            }
            if (entorno.contieneValorEnumerado(destino.tipoDato, operando)) {
                return true;
            }
            reporte.error(linea, "La constante '" + operando.lexema
                    + "' no pertenece al tipo enumerado '" + tipoDestino.clave() + "'");
            return false;
        }

        Entorno.TipoEnumerado tipoOrigen = entorno.getTipoEnumerado(operando.tipoDato);
        if (tipoOrigen != null && !tipoDestino.clave().equals(tipoOrigen.clave())) {
            errorTipos(linea, "la asignacion a '" + TablaSimbolos.nombreDe(destino.lexema) + "'",
                    tipoDestino.clave(), tipoOrigen.clave());
            return false;
        }
        return true;
    }
}
