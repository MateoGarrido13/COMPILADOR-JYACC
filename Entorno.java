import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
  Estado semantico que se va armando mientras el parser reduce.

  Guarda declaraciones y el contexto abierto (funcion/metodo/clase). No emite
  errores, eso queda en Verificaciones. El parser es ascendente, por eso la
  funcion y la clase se abren con acciones intermedias y no al reducir la
  declaracion completa.

  Todas las claves de los mapas incluyen el ambito, para que dos declaraciones
  con el mismo nombre en funciones distintas no se pisen.
 */
public class Entorno {

    public static class Parametro {
        public final String nombre;
        public final String tipo;
        public final EntradaTabla entrada;

        public Parametro(String nombre, String tipo, EntradaTabla entrada) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.entrada = entrada;
        }
    }

    public static class FuncionDeclarada {
        public final String nombre;
        public final String tipoRetorno;
        public final List<Parametro> parametros = new ArrayList<>();
        public final int linea;
        public final EntradaTabla entrada;
        public final boolean esMetodo;
        public final String ambitoDeclaracion;
        public final String ambitoPropio;

        public FuncionDeclarada(String nombre, String tipoRetorno, int linea,
                EntradaTabla entrada, boolean esMetodo, String ambitoDeclaracion) {
            this.nombre = nombre;
            this.tipoRetorno = tipoRetorno;
            this.linea = linea;
            this.entrada = entrada;
            this.esMetodo = esMetodo;
            this.ambitoDeclaracion = ambitoDeclaracion;
            this.ambitoPropio = ambitoDeclaracion + ":" + nombre;
        }

        public String clave() {
            return nombre + ":" + ambitoDeclaracion;
        }
    }

    public static class ClaseDeclarada {
        public final String nombre;
        public final int linea;
        public final EntradaTabla entrada;
        public final String ambitoDeclaracion;
        public final String ambitoPropio;
        public final Map<String, String> atributos = new LinkedHashMap<>();
        public final Map<String, FuncionDeclarada> metodos = new LinkedHashMap<>();
        // Claves resueltas (nombre:ambito) de las bases directas, en orden de fuente.
        public final List<String> heredaDe = new ArrayList<>();
        // Una clase con herencia invalida no queda declarada al cerrarse.
        public boolean valida = true;

        public ClaseDeclarada(String nombre, int linea, EntradaTabla entrada,
                String ambitoDeclaracion) {
            this.nombre = nombre;
            this.linea = linea;
            this.entrada = entrada;
            this.ambitoDeclaracion = ambitoDeclaracion;
            this.ambitoPropio = ambitoDeclaracion + ":" + nombre;
        }

        public String clave() {
            return nombre + ":" + ambitoDeclaracion;
        }
    }

    /*
      Atributo efectivo de una clase para el layout de COPY (TP4). Conserva
      el orden base-primero y la clase que lo declara, que es la que decide
      el prefijo de la entrada materializada (objeto.atr u objeto.base.atr).
     */
    public static class AtributoEfectivo {
        public final String nombre;
        public final String tipo;
        public final ClaseDeclarada declarante;

        public AtributoEfectivo(String nombre, String tipo, ClaseDeclarada declarante) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.declarante = declarante;
        }
    }

    public static class TipoEnumerado {
        public final String nombre;
        public final String tipoBase;
        public final List<String> valores = new ArrayList<>();
        public final int linea;
        public final EntradaTabla entrada;
        public final String ambito;

        public TipoEnumerado(String nombre, String tipoBase, int linea,
                EntradaTabla entrada, String ambito) {
            this.nombre = nombre;
            this.tipoBase = tipoBase;
            this.linea = linea;
            this.entrada = entrada;
            this.ambito = ambito;
        }

        public String clave() {
            return nombre + ":" + ambito;
        }
    }

    private final TablaSimbolos tablaSimbolos;

    private final Map<String, String> variables = new LinkedHashMap<>();
    private final Map<String, FuncionDeclarada> funciones = new LinkedHashMap<>();
    private final Map<String, ClaseDeclarada> clases = new LinkedHashMap<>();
    private final Map<String, TipoEnumerado> enumerados = new LinkedHashMap<>();

    private final Deque<FuncionDeclarada> pilaFunciones = new ArrayDeque<>();
    private final Deque<ClaseDeclarada> pilaClases = new ArrayDeque<>();

    public Entorno(TablaSimbolos tablaSimbolos) {
        this.tablaSimbolos = tablaSimbolos;
    }

    public FuncionDeclarada getFuncionActual() {
        return pilaFunciones.peek();
    }

    // Busca por clave completa nombre:ambito, no por nombre pelado.
    public FuncionDeclarada getFuncion(String clave) {
        if (clave == null) {
            return null;
        }
        return funciones.get(clave);
    }

    public EntradaTabla buscarIdentificador(String nombre, String ambito) {
        return tablaSimbolos.buscarIdentificador(nombre, ambito);
    }

    // Resuelve por la cadena de ambitos y solo acepta declaraciones (DEC-3).
    public EntradaTabla resolver(String nombre, String ambito) {
        return tablaSimbolos.resolver(nombre, ambito);
    }

    public ClaseDeclarada getClaseActual() {
        return pilaClases.peek();
    }

    public ClaseDeclarada getClase(String clave) {
        if (clave == null) {
            return null;
        }
        return clases.get(clave);
    }

    public EntradaTabla declararVariable(EntradaTabla id, String tipo, int linea) {
        if (id == null) {
            return null;
        }
        if (pilaFunciones.isEmpty() && !pilaClases.isEmpty()) {
            return declararAtributo(id, tipo, linea);
        }
        String ambito = Globals.AMBITO_ACTUAL;
        String nombre = TablaSimbolos.nombreDe(id.lexema);
        EntradaTabla entrada = tablaSimbolos.declarar(id, nombre, ambito, EntradaTabla.Uso.VARIABLE);
        if (entrada == null) {
            return null;
        }
        entrada.tipoDato = tipo;
        variables.put(nombre + ":" + ambito, tipo);
        materializarAtributosDeObjeto(entrada, tipo, linea);
        return entrada;
    }

    public String tipoDeVariable(String clave) {
        return variables.get(clave);
    }

    public EntradaTabla tiparConstante(EntradaTabla constante) {
        if (constante == null) {
            return null;
        }
        String tipo = Tipos.tipoDeConstante(constante.lexema);
        if (tipo != null) {
            constante.tipoDato = tipo;
        }
        return constante;
    }

    public EntradaTabla aplicarSignoNegativo(EntradaTabla constante, int linea) {
        if (constante == null) {
            return null;
        }
        String tipo = Tipos.tipoDeConstante(constante.lexema);
        return tablaSimbolos.insertarConstante("-" + constante.lexema, tipo, linea);
    }

    public FuncionDeclarada abrirFuncion(EntradaTabla id, String tipoRetorno, int linea) {
        String nombre = id == null ? null : TablaSimbolos.nombreDe(id.lexema);
        String ambitoDeclaracion = ControlAmbito.ambitoDeclaracionDe(nombre, Globals.AMBITO_ACTUAL);
        boolean esMetodo = !pilaClases.isEmpty()
                && pilaClases.peek().ambitoPropio.equals(ambitoDeclaracion);

        EntradaTabla entrada = id;
        if (id != null) {
            EntradaTabla.Uso uso = esMetodo ? EntradaTabla.Uso.METODO : EntradaTabla.Uso.FUNCION;
            entrada = tablaSimbolos.declarar(id, nombre, ambitoDeclaracion, uso);
            if (entrada != null) {
                entrada.tipoDato = tipoRetorno;
            }
        }

        FuncionDeclarada funcion = new FuncionDeclarada(nombre, tipoRetorno, linea,
                entrada, esMetodo, ambitoDeclaracion);
        pilaFunciones.push(funcion);
        return funcion;
    }

    public void agregarParametroFormal(EntradaTabla id, String tipo) {
        FuncionDeclarada funcion = pilaFunciones.peek();
        if (funcion == null) {
            return;
        }
        String ambito = Globals.AMBITO_ACTUAL;
        String nombre = id == null ? null : TablaSimbolos.nombreDe(id.lexema);
        EntradaTabla entrada = id;
        if (id != null) {
            entrada = tablaSimbolos.declarar(id, nombre, ambito, EntradaTabla.Uso.PARAMETRO);
            if (entrada != null) {
                entrada.tipoDato = tipo;
                materializarAtributosDeObjeto(entrada, tipo, entrada.numeroLinea);
            }
        }
        funcion.parametros.add(new Parametro(nombre, tipo, entrada));
    }

    // Cierra el contexto y pasa la funcion a la tabla o a la clase actual.
    public void cerrarFuncion() {
        FuncionDeclarada funcion = pilaFunciones.poll();
        if (funcion == null || funcion.nombre == null) {
            return;
        }
        if (funcion.esMetodo) {
            ClaseDeclarada clase = pilaClases.peek();
            if (clase != null) {
                clase.metodos.put(funcion.nombre, funcion);
            }
        } else {
            funciones.put(funcion.clave(), funcion);
        }
    }

    public ClaseDeclarada abrirClase(EntradaTabla id, int linea) {
        if (id == null) {
            return null;
        }
        String nombre = TablaSimbolos.nombreDe(id.lexema);
        String ambitoDeclaracion = ControlAmbito.ambitoDeclaracionDe(nombre, Globals.AMBITO_ACTUAL);
        EntradaTabla entrada = tablaSimbolos.declarar(id, nombre, ambitoDeclaracion,
                EntradaTabla.Uso.CLASE);
        if (entrada != null) {
            entrada.tipoDato = nombre;
        }
        ClaseDeclarada clase = new ClaseDeclarada(nombre, linea, entrada, ambitoDeclaracion);
        clases.put(clase.clave(), clase);
        pilaClases.push(clase);
        return clase;
    }

    public void cerrarClase() {
        ClaseDeclarada clase = pilaClases.poll();
        if (clase != null && !clase.valida) {
            descartarClase(clase);
        }
    }

    /*
      Una clase con extends invalido no se declara. Se quita del registro y se
      eliminan sus entradas y las de sus miembros (atributos, metodos, parametros
      y variables locales) para que no queden declaraciones a medias.
     */
    private void descartarClase(ClaseDeclarada clase) {
        if (clase == null) {
            return;
        }
        List<String> clasesAnidadas = new ArrayList<>();
        for (ClaseDeclarada otra : clases.values()) {
            if (esAmbitoDentroDe(otra.ambitoDeclaracion, clase.ambitoPropio)) {
                clasesAnidadas.add(otra.clave());
            }
        }
        for (String clave : clasesAnidadas) {
            clases.remove(clave);
        }
        clases.remove(clase.clave());

        tablaSimbolos.eliminarAmbito(clase.ambitoPropio);
        if (clase.entrada != null) {
            tablaSimbolos.eliminar(clase.entrada);
        }
        descartarClavesDeAmbito(clase.ambitoPropio, variables);
        descartarClavesDeAmbito(clase.ambitoPropio, funciones);
    }

    private void descartarClavesDeAmbito(String ambito, Map<String, ?> mapa) {
        List<String> claves = new ArrayList<>();
        for (String clave : mapa.keySet()) {
            if (esAmbitoDentroDe(TablaSimbolos.ambitoDe(clave), ambito)) {
                claves.add(clave);
            }
        }
        for (String clave : claves) {
            mapa.remove(clave);
        }
    }

    private boolean esAmbitoDentroDe(String ambito, String contenedor) {
        return ambito != null && contenedor != null
                && (ambito.equals(contenedor) || ambito.startsWith(contenedor + ":"));
    }

    public EntradaTabla declararAtributo(EntradaTabla id, String tipo, int linea) {
        ClaseDeclarada clase = pilaClases.peek();
        if (id == null || clase == null) {
            return null;
        }
        String nombre = TablaSimbolos.nombreDe(id.lexema);
        EntradaTabla entrada = tablaSimbolos.declarar(id, nombre, clase.ambitoPropio,
                EntradaTabla.Uso.ATRIBUTO);
        if (entrada == null) {
            return null;
        }
        entrada.tipoDato = tipo;
        clase.atributos.put(nombre, tipo);
        materializarAtributosDeObjeto(entrada, tipo, linea);
        return entrada;
    }

    /*
      Agrega una base ya resuelta. Devuelve false si esa clase ya estaba
      en la lista: el llamador informa la base repetida y no la vuelve a guardar.
     */
    public boolean agregarBase(ClaseDeclarada clase, ClaseDeclarada base) {
        if (clase == null || base == null) {
            return false;
        }
        String clave = base.clave();
        if (clase.heredaDe.contains(clave)) {
            return false;
        }
        clase.heredaDe.add(clave);
        return true;
    }

    /*
      Marca la clase abierta como invalida. Al cerrarse no quedara declarada.
     */
    public void invalidarClase(ClaseDeclarada clase) {
        if (clase != null) {
            clase.valida = false;
        }
    }

    /*
      True si `buscada` es `desde` o se alcanza siguiendo heredaDe.
     */
    public boolean alcanzaClase(ClaseDeclarada desde, ClaseDeclarada buscada) {
        if (desde == null || buscada == null) {
            return false;
        }
        return alcanza(desde, buscada.clave(), new HashSet<String>());
    }

    private boolean alcanza(ClaseDeclarada desde, String clave, Set<String> visitadas) {
        if (desde == null || !visitadas.add(desde.clave())) {
            return false;
        }
        if (desde.clave().equals(clave)) {
            return true;
        }
        for (String base : desde.heredaDe) {
            if (alcanza(clasePorNombre(base), clave, visitadas)) {
                return true;
            }
        }
        return false;
    }

    /*
      El prefijo de objeto.clase.miembro puede ser la clase del objeto o una
      base directa o transitiva. Otra clase, aunque exista, no sirve.
     */
    public boolean esPrefijoValido(ClaseDeclarada objeto, ClaseDeclarada prefijo) {
        if (objeto == null || prefijo == null) {
            return false;
        }
        if (objeto.clave().equals(prefijo.clave())) {
            return true;
        }
        Set<String> visitadas = new HashSet<>();
        for (String base : objeto.heredaDe) {
            if (alcanza(clasePorNombre(base), prefijo.clave(), visitadas)) {
                return true;
            }
        }
        return false;
    }

    public void declararObjetos(List<Object> identificadores, String nombreClase, int linea) {
        if (identificadores == null) {
            return;
        }
        EntradaTabla declaracion = resolverTipo(nombreClase);
        if (declaracion == null
                || (declaracion.uso != EntradaTabla.Uso.CLASE
                && declaracion.uso != EntradaTabla.Uso.TIPO_ENUMERADO)) {
            return;
        }
        for (Object elemento : identificadores) {
            declararVariable(ContextoRegla.comoEntrada(elemento), declaracion.lexema, linea);
        }
    }

    /*
      Materializa en la tabla una entrada por cada atributo del objeto. Los
      propios quedan como `objeto.atributo`; los heredados agregan la clase que
      los declara: `objeto.base.atributo`. Se crean al declarar el objeto (o el
      parametro/atributo de tipo clase), no al usarlo, y cada objeto tiene sus
      propias claves.
     */
    private void materializarAtributosDeObjeto(EntradaTabla objeto, String tipo, int linea) {
        if (objeto == null) {
            return;
        }
        ClaseDeclarada clase = claseDe(tipo);
        if (clase == null) {
            return;
        }
        String nombreObjeto = TablaSimbolos.nombreDe(objeto.lexema);
        String ambito = TablaSimbolos.ambitoDe(objeto.lexema);
        if (nombreObjeto == null || nombreObjeto.isEmpty() || ambito.isEmpty()) {
            return;
        }
        List<ClaseDeclarada> declarantes = new ArrayList<>();
        recolectarDeclarantes(clase, declarantes, new HashSet<String>());
        for (ClaseDeclarada declarante : declarantes) {
            String prefijo = declarante.clave().equals(clase.clave())
                    ? nombreObjeto
                    : nombreObjeto + "." + declarante.nombre;
            for (Map.Entry<String, String> atributo : declarante.atributos.entrySet()) {
                tablaSimbolos.declararAtributoDeObjeto(
                        prefijo + "." + atributo.getKey(), ambito,
                        atributo.getValue(), linea);
            }
        }
    }

    private void recolectarDeclarantes(ClaseDeclarada clase, List<ClaseDeclarada> destino,
            Set<String> visitadas) {
        if (clase == null || !visitadas.add(clase.clave())) {
            return;
        }
        for (String base : clase.heredaDe) {
            recolectarDeclarantes(clasePorNombre(base), destino, visitadas);
        }
        destino.add(clase);
    }

    public void descartarPlaceholder(EntradaTabla entrada) {
        if (entrada == null || entrada.declarado) {
            return;
        }
        tablaSimbolos.eliminar(entrada);
    }

    public boolean nombreTipoDisponible(EntradaTabla id) {
        if (id == null) {
            return false;
        }
        String nombre = TablaSimbolos.nombreDe(id.lexema);
        EntradaTabla existente = tablaSimbolos.buscarIdentificador(nombre, Globals.AMBITO_ACTUAL);
        return existente == null || !existente.declarado;
    }

    public boolean registrarTipoEnumerado(EntradaTabla id, List<Object> valores,
            String tipoBase, int linea) {
        if (id == null || !nombreTipoDisponible(id)) {
            return false;
        }
        String ambito = Globals.AMBITO_ACTUAL;
        String nombre = TablaSimbolos.nombreDe(id.lexema);
        EntradaTabla entrada = tablaSimbolos.declarar(id, nombre, ambito,
                EntradaTabla.Uso.TIPO_ENUMERADO);
        if (entrada == null) {
            return false;
        }
        entrada.tipoDato = tipoBase;
        List<String> valoresEnumerado = new ArrayList<>();
        if (valores != null) {
            for (Object elemento : valores) {
                EntradaTabla valor = ContextoRegla.comoEntrada(elemento);
                if (valor != null) {
                    tiparConstante(valor);
                    valoresEnumerado.add(valor.lexema);
                }
            }
        }

        TipoEnumerado definitivo = new TipoEnumerado(nombre, tipoBase, linea, entrada, ambito);
        definitivo.valores.addAll(valoresEnumerado);
        enumerados.put(definitivo.clave(), definitivo);
        return true;
    }

    public boolean esTipoConocido(String nombre) {
        if (nombre == null) {
            return false;
        }
        if (Tipos.esTipoBasico(nombre)) {
            return true;
        }
        EntradaTabla entrada = resolverTipo(nombre);
        return entrada != null
                && (entrada.uso == EntradaTabla.Uso.CLASE
                || entrada.uso == EntradaTabla.Uso.TIPO_ENUMERADO);
    }

    public EntradaTabla resolverTipoDeclarado(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return null;
        }
        String base = TablaSimbolos.nombreDe(nombre);
        EntradaTabla entrada = resolver(base, Globals.AMBITO_ACTUAL);
        if (entrada == null
                || (entrada.uso != EntradaTabla.Uso.CLASE
                && entrada.uso != EntradaTabla.Uso.TIPO_ENUMERADO)) {
            return null;
        }
        return entrada;
    }

    public TipoEnumerado getTipoEnumerado(String tipo) {
        EntradaTabla entrada = resolverTipo(tipo);
        if (entrada == null || entrada.uso != EntradaTabla.Uso.TIPO_ENUMERADO) {
            return null;
        }
        return enumerados.get(entrada.lexema);
    }

    public boolean contieneValorEnumerado(String tipo, EntradaTabla constante) {
        TipoEnumerado enumerado = getTipoEnumerado(tipo);
        if (enumerado == null || enumerado.tipoBase == null || constante == null
                || !enumerado.tipoBase.equals(Tipos.tipoDeConstante(constante.lexema))) {
            return false;
        }
        for (String valor : enumerado.valores) {
            if (Tipos.ULONGINT.equals(enumerado.tipoBase)) {
                BigInteger valorEnumerado = Tipos.valorEntero(valor);
                BigInteger valorConstante = Tipos.valorEntero(constante.lexema);
                if (valorEnumerado != null && valorEnumerado.equals(valorConstante)) {
                    return true;
                }
            } else if (Tipos.DOUBLEF.equals(enumerado.tipoBase)) {
                Double valorEnumerado = Tipos.valorDoublef(valor);
                Double valorConstante = Tipos.valorDoublef(constante.lexema);
                if (valorEnumerado != null && valorConstante != null
                        && valorEnumerado.doubleValue() == valorConstante.doubleValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean esClase(String nombre) {
        EntradaTabla entrada = resolverTipo(nombre);
        return entrada != null && entrada.uso == EntradaTabla.Uso.CLASE;
    }

    // Clase ya resuelta por su clave mangled. Un tipo basico o un enumerado no es clase.
    public ClaseDeclarada claseDe(String tipo) {
        if (tipo == null || tipo.isEmpty()) {
            return null;
        }
        EntradaTabla entrada = resolverTipo(tipo);
        if (entrada == null || entrada.uso != EntradaTabla.Uso.CLASE) {
            return null;
        }
        return getClase(entrada.lexema);
    }

    /*
      D4: dos clases son compatibles para la asignacion de objetos si son la
      misma clase o si tienen la misma cantidad de atributos efectivos en el
      mismo orden de tipos. Los nombres de los atributos y las firmas de los
      metodos no participan. Incluye los heredados, en orden base primero; un
      atributo propio con el mismo nombre reemplaza al heredado sin moverlo de
      posicion. El tipo de un miembro clase o enumerado se compara por su clave,
      no por el tipo base.
     */
    public boolean clasesCompatibles(ClaseDeclarada a, ClaseDeclarada b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.clave().equals(b.clave())) {
            return true;
        }
        return tiposDeAtributos(a).equals(tiposDeAtributos(b));
    }

    private List<String> tiposDeAtributos(ClaseDeclarada clase) {
        return new ArrayList<>(atributosEfectivos(clase).values());
    }

    private Map<String, String> atributosEfectivos(ClaseDeclarada clase) {
        Map<String, String> atributos = new LinkedHashMap<>();
        recolectarAtributos(clase, atributos, new HashSet<String>());
        return atributos;
    }

    /*
      Atributos efectivos en el mismo orden que usa la compatibilidad de COPY,
      pero conservando la clase declarante. Base primero; un atributo propio
      con el mismo nombre reemplaza al heredado sin mover la posicion.
     */
    public List<AtributoEfectivo> atributosEfectivosConDeclarante(ClaseDeclarada clase) {
        Map<String, AtributoEfectivo> atributos = new LinkedHashMap<>();
        recolectarAtributosConDeclarante(clase, atributos, new HashSet<String>());
        return new ArrayList<>(atributos.values());
    }

    private void recolectarAtributosConDeclarante(ClaseDeclarada clase,
            Map<String, AtributoEfectivo> destino, Set<String> visitadas) {
        if (clase == null || !visitadas.add(clase.clave())) {
            return;
        }
        for (String base : clase.heredaDe) {
            recolectarAtributosConDeclarante(clasePorNombre(base), destino, visitadas);
        }
        for (Map.Entry<String, String> atributo : clase.atributos.entrySet()) {
            destino.put(atributo.getKey(), new AtributoEfectivo(
                    atributo.getKey(), tipoDeMiembro(atributo.getValue()), clase));
        }
    }

    private void recolectarAtributos(ClaseDeclarada clase, Map<String, String> destino,
            Set<String> visitadas) {
        if (clase == null || !visitadas.add(clase.clave())) {
            return;
        }
        for (String base : clase.heredaDe) {
            recolectarAtributos(clasePorNombre(base), destino, visitadas);
        }
        for (Map.Entry<String, String> atributo : clase.atributos.entrySet()) {
            destino.put(atributo.getKey(), tipoDeMiembro(atributo.getValue()));
        }
    }

    private String tipoDeMiembro(String tipo) {
        if (tipo == null || tipo.isEmpty()) {
            return Tipos.INDEFINIDO;
        }
        EntradaTabla entrada = resolverTipo(tipo);
        if (entrada != null
                && (entrada.uso == EntradaTabla.Uso.CLASE
                || entrada.uso == EntradaTabla.Uso.TIPO_ENUMERADO)) {
            return entrada.lexema;
        }
        return Tipos.tipoEfectivo(tipo);
    }

    /*
      Tipo que participa en una operacion. Un enumerado vale por su tipo base;
      una clase conserva su clave completa para que dos clases distintas no se
      confundan. Tipos.tipoEfectivo queda como normalizacion para los basicos.
     */
    public String tipoEfectivo(String tipo) {
        if (tipo == null) {
            return Tipos.INDEFINIDO;
        }
        EntradaTabla entrada = resolverTipo(tipo);
        if (entrada != null) {
            if (entrada.uso == EntradaTabla.Uso.TIPO_ENUMERADO) {
                TipoEnumerado enumerado = enumerados.get(entrada.lexema);
                if (enumerado != null && enumerado.tipoBase != null) {
                    return enumerado.tipoBase;
                }
            }
            if (entrada.uso == EntradaTabla.Uso.CLASE) {
                return entrada.lexema;
            }
        }
        return Tipos.tipoEfectivo(tipo);
    }

    /*
      Atributo declarado en la propia clase, sin recorrer ancestros.
      D3/DEC-5: objeto.atributo solo ve miembros propios. El heredado
      exige el prefijo objeto.clase.atributo.
     */
    public EntradaTabla atributoPropio(ClaseDeclarada clase, String nombreAtributo) {
        if (clase == null || nombreAtributo == null || nombreAtributo.isEmpty()) {
            return null;
        }
        String nombre = TablaSimbolos.nombreDe(nombreAtributo);
        if (!clase.atributos.containsKey(nombre)) {
            return null;
        }
        EntradaTabla entrada = tablaSimbolos.buscarIdentificador(nombre, clase.ambitoPropio);
        if (entrada != null && entrada.declarado && entrada.uso == EntradaTabla.Uso.ATRIBUTO) {
            return entrada;
        }
        return null;
    }

    // Atributo declarado en alguna base, directa o transitiva. No mira los propios.
    public EntradaTabla atributoHeredado(ClaseDeclarada clase, String nombreAtributo) {
        if (clase == null || nombreAtributo == null || nombreAtributo.isEmpty()) {
            return null;
        }
        String nombre = TablaSimbolos.nombreDe(nombreAtributo);
        Set<String> visitadas = new HashSet<>();
        for (String base : clase.heredaDe) {
            EntradaTabla hallado = buscarAtributo(clasePorNombre(base), nombre, visitadas);
            if (hallado != null) {
                return hallado;
            }
        }
        return null;
    }

    public FuncionDeclarada metodoPropio(ClaseDeclarada clase, String nombreMetodo) {
        if (clase == null || nombreMetodo == null || nombreMetodo.isEmpty()) {
            return null;
        }
        return clase.metodos.get(TablaSimbolos.nombreDe(nombreMetodo));
    }

    // Metodo declarado en alguna base, directa o transitiva. No mira los propios.
    public FuncionDeclarada metodoHeredado(ClaseDeclarada clase, String nombreMetodo) {
        if (clase == null || nombreMetodo == null || nombreMetodo.isEmpty()) {
            return null;
        }
        String nombre = TablaSimbolos.nombreDe(nombreMetodo);
        Set<String> visitadas = new HashSet<>();
        for (String base : clase.heredaDe) {
            FuncionDeclarada hallado = buscarMetodo(clasePorNombre(base), nombre, visitadas);
            if (hallado != null) {
                return hallado;
            }
        }
        return null;
    }

    /*
      Atributo declarado en la clase del prefijo, o en la clase del objeto si
      no hay prefijo. No sube a las bases: el miembro heredado no se acepta
      por estar en un ancestro de la clase nombrada.
     */
    public EntradaTabla resolverAtributo(EntradaTabla objeto, String nombreClase, String nombreAtributo) {
        if (nombreAtributo == null) {
            return null;
        }
        ClaseDeclarada clase = nombreClase == null || nombreClase.isEmpty()
                ? claseDeObjeto(objeto)
                : clasePorNombre(nombreClase);
        if (clase == null) {
            return null;
        }
        return atributoPropio(clase, nombreAtributo);
    }

    /*
      Metodo declarado en la clase del objeto o en la clase prefijada.
      No recorre ancestros: la llamada heredada tiene que nombrar la clase
      que declara el metodo. La firma que devuelve alimenta el CALLMET.
     */
    public FuncionDeclarada buscarMetodo(EntradaTabla objeto, String nombreClase, String nombreMetodo) {
        if (nombreMetodo == null) {
            return null;
        }
        ClaseDeclarada clase = nombreClase == null || nombreClase.isEmpty()
                ? claseDeObjeto(objeto)
                : clasePorNombre(nombreClase);
        if (clase == null) {
            return null;
        }
        return metodoPropio(clase, nombreMetodo);
    }

    private FuncionDeclarada buscarMetodo(ClaseDeclarada clase, String nombre, Set<String> visitadas) {
        if (clase == null || !visitadas.add(clase.clave())) {
            return null;
        }
        FuncionDeclarada propia = clase.metodos.get(nombre);
        if (propia != null) {
            return propia;
        }
        for (String base : clase.heredaDe) {
            FuncionDeclarada heredada = buscarMetodo(clasePorNombre(base), nombre, visitadas);
            if (heredada != null) {
                return heredada;
            }
        }
        return null;
    }

    public ClaseDeclarada claseDeObjeto(EntradaTabla objeto) {
        if (objeto == null) {
            return null;
        }
        ClaseDeclarada clase = getClase(objeto.lexema);
        if (clase != null) {
            return clase;
        }
        clase = getClase(objeto.tipoDato);
        if (clase != null) {
            return clase;
        }
        return clasePorNombre(objeto.tipoDato);
    }

    public ClaseDeclarada clasePorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        EntradaTabla entrada = resolverTipo(nombre);
        if (entrada != null && entrada.uso == EntradaTabla.Uso.CLASE) {
            ClaseDeclarada clase = getClase(entrada.lexema);
            if (clase != null) {
                return clase;
            }
        }
        String buscado = TablaSimbolos.nombreDe(nombre);
        for (ClaseDeclarada clase : clases.values()) {
            if (clase.nombre.equals(buscado)) {
                return clase;
            }
        }
        return null;
    }

    private EntradaTabla buscarAtributo(ClaseDeclarada clase, String nombre, Set<String> visitadas) {
        if (clase == null || !visitadas.add(clase.clave())) {
            return null;
        }
        EntradaTabla entrada = tablaSimbolos.buscarIdentificador(nombre, clase.ambitoPropio);
        if (entrada != null && entrada.declarado && entrada.uso == EntradaTabla.Uso.ATRIBUTO) {
            return entrada;
        }
        for (String base : clase.heredaDe) {
            EntradaTabla heredada = buscarAtributo(clasePorNombre(base), nombre, visitadas);
            if (heredada != null) {
                return heredada;
            }
        }
        return null;
    }

    // Acepta un nombre pelado o una clave ya resuelta nombre:ambito.
    private EntradaTabla resolverTipo(String nombre) {
        if (nombre == null) {
            return null;
        }
        String base = TablaSimbolos.nombreDe(nombre);
        if (nombre.indexOf(':') >= 0) {
            EntradaTabla exacta = tablaSimbolos.buscarIdentificador(
                    base, TablaSimbolos.ambitoDe(nombre));
            if (exacta != null && exacta.declarado) {
                return exacta;
            }
        }
        return tablaSimbolos.resolver(base, Globals.AMBITO_ACTUAL);
    }

    public String contenidoDeclaraciones() {
        StringBuilder texto = new StringBuilder();
        texto.append("Variables declaradas (").append(variables.size()).append("):\n");
        for (Map.Entry<String, String> entrada : variables.entrySet()) {
            texto.append("  ").append(entrada.getKey()).append(" : ").append(entrada.getValue()).append("\n");
        }
        texto.append("\nFunciones declaradas (").append(funciones.size()).append("):\n");
        for (FuncionDeclarada funcion : funciones.values()) {
            texto.append("  ").append(funcion.tipoRetorno).append(" ").append(funcion.nombre).append("(");
            for (int i = 0; i < funcion.parametros.size(); i++) {
                Parametro parametro = funcion.parametros.get(i);
                texto.append(i > 0 ? ", " : "").append(parametro.tipo).append(" ").append(parametro.nombre);
            }
            texto.append(") - linea ").append(funcion.linea).append("\n");
        }
        texto.append("\nClases declaradas (").append(clases.size()).append("):\n");
        for (ClaseDeclarada clase : clases.values()) {
            texto.append("  ").append(clase.nombre).append(" - linea ").append(clase.linea).append("\n");
            texto.append("    atributos: ").append(clase.atributos.keySet()).append("\n");
            texto.append("    metodos: ").append(clase.metodos.keySet()).append("\n");
            texto.append("    hereda de: ").append(clase.heredaDe).append("\n");
        }
        texto.append("\nTipos enumerados (").append(enumerados.size()).append("):\n");
        for (TipoEnumerado enumerado : enumerados.values()) {
            texto.append("  ").append(enumerado.clave()).append(" [").append(enumerado.tipoBase)
                 .append("] = ").append(enumerado.valores).append("\n");
        }
        return texto.toString();
    }
}
