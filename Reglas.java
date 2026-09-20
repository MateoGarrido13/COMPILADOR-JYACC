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
    ASIGNACION("Asignación", true),
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
    PARAMETRO_REAL_NOMBRADO("Parametro real con asignacion a formal", false),
    LISTA_PARAMETROS_REALES("Lista de parametros reales", false),
    LISTA_ORDEN_EVALUACION("Lista de orden de evaluacion", false),
    ACCESO_ATRIBUTO("Acceso a atributo", true),
    ACCESO_ATRIBUTO_PREFIJADO("Acceso a atributo con prefijo de clase", true),
    INVOCACION_METODO("Invocacion a metodo", true),

    // ===== Producciones de error (PDF de errores a detectar) =====
    ERR_SENTENCIA("Sentencia mal formada", false),
    ERR_DECLARACION_INVALIDA("Declaracion mal formada", false),
    ERR_LISTA_VARIABLES("Lista de variables mal formada", false),
    ERR_ASIGNACION_INVALIDA("Asignacion mal formada", false),
    ERR_CONDICION_INVALIDA("Condicion mal formada", false),

    ERR_FALTA_NOMBRE_PROGRAMA("Falta de nombre de programa", false),
    ERR_FALTA_BEGIN("Falta de delimitador BEGIN de sentencias ejecutables", false),
    ERR_BLOQUE_SIN_END("Falta de delimitador END de sentencias ejecutables", false),
    ERR_FALTA_PUNTO_Y_COMA("Falta ';' al final de la sentencia", false),
    ERR_FALTA_NOMBRE_FUNCION("Falta de nombre en funcion", false),
    ERR_FALTA_COMA_VARIABLES("Falta de ',' en declaracion de variables", false),
    ERR_FALTA_NOMBRE_PARAMETRO("Falta de nombre de parametro formal en declaracion de funcion", false),
    ERR_FALTA_TIPO_PARAMETRO("Falta de tipo de parametro formal en declaracion de funcion", false),
    ERR_FALTA_OPERANDO("Falta de operando en expresion", false),
    ERR_FALTA_OPERADOR("Falta de operador en expresion", false),
    ERR_FALTA_ARGUMENTO_POUT("Falta argumento en sentencia pout", false),
    ERR_FALTA_PARENTESIS_APERTURA("Falta parentesis de apertura para la condicion", false),
    ERR_CONDICION_SIN_CIERRE("Falta parentesis de cierre para la condicion", false),
    ERR_FALTA_CUERPO_ITERACION("Falta de cuerpo en iteracion", false),
    ERR_FALTA_END_IF("Falta de end_if", false),
    ERR_FALTA_WHILE("Falta while", false),
    ERR_ASIG_DONDE_IGUAL("Uso de ':=' donde debe usarse '='", false),
    ERR_ASIGNACION_ANIDADA("Anidamiento de asignacion en expresion", false),
    ERR_FALTA_ORDEN_EVALUACION("Falta del orden de evaluacion y asignacion de parametros", false),
    ERR_FALTA_VALORES_ENUMERADO("Ausencia de valores para la enumeracion", false),
    ERR_FALTA_LISTA_EXTENDS("Ausencia de nombre o lista de clases despues de extends", false);

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
