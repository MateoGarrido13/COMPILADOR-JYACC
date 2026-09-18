/**
 * Catalogo de reglas de la gramatica.
 
  Cada constante identifica una produccion de gramatica.y y lleva asociada la
  descripcion con la que se informa la estructura sintactica detectada. 
 La marca "estructura" distingue las producciones que el TP pide listar (Asignacion,
  Sentencia IF, etc.) de las auxiliares, que solo propagan valores.
 */
public enum Reglas {

    // ===== Estructura general del programa =====
    PROGRAMA("Programa", true),
    NOMBRE_PROGRAMA("Nombre de programa", false),
    BLOQUE_EJECUTABLE("Bloque BEGIN - END", true),
    LISTA_SENTENCIAS("Lista de sentencias", false),
    SENTENCIA("Sentencia", false),

    // ===== Sentencias declarativas =====
    DECL_VARIABLES("Declaracion de variables", true),
    LISTA_VARIABLES("Lista de variables", false),
    TIPO("Tipo de dato", false),

    INICIO_FUNCION("Encabezado de funcion", false),
    DECL_FUNCION("Declaracion de funcion", true),
    LISTA_PARAMETROS_FORMALES("Lista de parametros formales", false),
    PARAMETRO_FORMAL("Parametro formal", false),

    INICIO_CLASE("Encabezado de clase", false),
    DECL_CLASE("Declaracion de clase", true),
    DECL_ATRIBUTO("Declaracion de atributo", true),
    DECL_METODO("Declaracion de metodo", true),
    SENTENCIA_EXTENDS("Sentencia EXTENDS", true),
    LISTA_CLASES_HEREDADAS("Lista de clases heredadas", false),
    DECL_OBJETOS("Declaracion de objetos", true),

    DECL_TYPEDEF("Definicion de tipo enumerado", true),
    LISTA_VALORES_ENUMERADO("Lista de valores del enumerado", false),

    // ===== Sentencias ejecutables =====
    ASIGNACION("Asignacion", true),
    SENTENCIA_IF("Sentencia IF", true),
    SENTENCIA_IF_ELSE("Sentencia IF con ELSE", true),
    CONDICION("Condicion", false),
    SENTENCIA_REPEAT_WHILE("Sentencia REPEAT - WHILE", true),
    SENTENCIA_POUT("Sentencia POUT", true),
    SENTENCIA_RET("Sentencia RET", true),

    // ===== Expresiones =====
    SUMA("Suma", false),
    RESTA("Resta", false),
    MULTIPLICACION("Multiplicacion", false),
    DIVISION("Division", false),
    FACTOR_ID("Factor identificador", false),
    FACTOR_CONSTANTE("Factor constante", false),
    FACTOR_CONSTANTE_NEGATIVA("Constante negativa", false),
    FACTOR_CADENA("Factor cadena", false),
    ASIGNACION_EN_EXPRESION("Asignacion en expresion", true),
    CONVERSION_TODF("Conversion explicita todf", true),

    INVOCACION_FUNCION("Invocacion a funcion", true),
    LISTA_PARAMETROS_REALES("Lista de parametros reales", false),
    LISTA_ORDEN_EVALUACION("Lista de orden de evaluacion", false),
    ACCESO_ATRIBUTO("Acceso a atributo", true),
    ACCESO_ATRIBUTO_PREFIJADO("Acceso a atributo con prefijo de clase", true),
    INVOCACION_METODO("Invocacion a metodo", true),

    // ===== Producciones de error =====
    ERR_SENTENCIA("Sentencia mal formada", false),
    ERR_FALTA_PUNTO_Y_COMA("Falta ';' al final de la sentencia", false),
    ERR_CONDICION_SIN_CIERRE("Falta parentesis de cierre para la condicion", false),
    ERR_CONDICION_INVALIDA("Condicion mal formada", false),
    ERR_LISTA_VARIABLES("Lista de variables mal formada", false),
    ERR_ASIGNACION_INVALIDA("Asignacion mal formada", false),
    ERR_FALTA_ORDEN_EVALUACION("Falta la lista de orden de evaluacion en la invocacion", false),
    ERR_BLOQUE_SIN_END("Falta END de cierre del bloque", false),
    ERR_DECLARACION_INVALIDA("Declaracion mal formada", false);

//  descripcion con la que se informa la estructura sintactica detectada. 
    private final String descripcion;
    private final boolean estructura;

    Reglas(String descripcion, boolean estructura) {
        this.descripcion = descripcion;
        this.estructura = estructura;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Indica si la reduccion debe informarse en la lista de estructuras detectadas. */
    public boolean esEstructura() {
        return estructura;
    }

    /** Indica si la regla corresponde a una produccion de error. */
    public boolean esError() {
        return name().startsWith("ERR_");
    }
}
