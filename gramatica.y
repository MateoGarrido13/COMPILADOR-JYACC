/* ==========================================================================
   Diseno de Compiladores I - Trabajo Practico Nro. 2
   Especificacion YACC (BYACC/J) del Analizador Sintactico

   Temas particulares asignados al grupo:
     6  - ulongint : enteros largos sin signo de 32 bits, sufijo "$ul"
     8  - doublef  : punto flotante de 64 bits, exponente con la letra "d"
     9  - cadenas de una linea delimitadas por llaves          (Analizador Lexico)
     13 - sentencia iterativa  repeat <bloque> while (<cond>);
     16 - comentarios multilinea  {{ ... }}                    (Analizador Lexico)
     17 - asignacion de expresiones en expresiones con "=" y parentesis
     19 - invocacion con orden de evaluacion obligatorio entre corchetes
     23 - typedef de enumeraciones
     25 - declaracion de clases sin codigo de estructura
     28 - acceso tradicional a atributos con "."
     31 - herencia multiple con desambiguado por prefijado
     33 - conversion explicita  todf(<expresion>)

    Gramatica recursiva a derecha , las reducciones invocan a acciones.ejecutar(Regla.X), pasmos como parametro que regla 
    TablaAcciones resuelve que codigo ejecutable corresponde, registra la reduccion en la lista de reglas e informa la estructura sintactica detectada.

   ========================================================================== */

%{
/* Esta seccion se copia tal cual al comienzo de Parser.java */
%}

/* --------------------------------------------------------------------------
   Los numeros de token coinciden con las constantes de Globals, de modo que el
   Analizador Lexico del TP1 se integra sin modificar los valores que devuelve.
   -------------------------------------------------------------------------- */

%token ID            300
%token ASIG          301
%token CTE           302
%token CADENA        303
%token MAYOR_IGUAL   304
%token MENOR_IGUAL   305
%token IGUAL_IGUAL   306
%token DISTINTO      307

%token IF            400
%token ELSE          401
%token END_IF        402
%token BEGIN         403
%token END           404
%token POUT          405
%token RET           406
%token CLASS         407
%token FUNCTION      408
%token REPEAT        409
%token WHILE         410
%token TODF          411
%token TYPEDEF       412
%token EXTENDS       413
%token ULONGINT      414
%token DOUBLEF       415

%start programa

%%

/* ==========================================================================
   PROGRAMA
   Nombre de programa, bloque de sentencias declarativas y bloque ejecutable
   delimitado por BEGIN y END.
   ========================================================================== */

programa
    : nombre_programa sentencias_declarativas bloque_ejecutable
        { $$ = new ParserVal(acciones.ejecutar(Reglas.PROGRAMA, $2.obj, $3.obj)); }
    ;

nombre_programa
    : ID
        { $$ = new ParserVal(acciones.ejecutar(Reglas.NOMBRE_PROGRAMA, $1.obj)); }
    ;

bloque_ejecutable
    : BEGIN lista_sentencias END
        { $$ = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, $2.obj)); }
    ;

/* ==========================================================================
   SENTENCIAS DECLARATIVAS
   ========================================================================== */

/* Recursiva a derecha; el bloque declarativo puede estar ausente. */
sentencias_declarativas
    : sentencia_declarativa sentencias_declarativas
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, $1.obj, $2.obj)); }
    | /* vacio */
        { $$ = new ParserVal((Object) null); }
    ;

sentencia_declarativa
    : declaracion_variables
    | declaracion_objetos
    | declaracion_funcion
    | declaracion_clase
    | declaracion_typedef
    | error ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_DECLARACION_INVALIDA)); }
    ;

/* <tipo> <lista_de_variables> ; */
declaracion_variables
    : tipo lista_variables ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_VARIABLES, $1.obj, $2.obj)); }
    | tipo error ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_LISTA_VARIABLES)); }
    ;

/* Temas 23 y 25: variables de un tipo enumerado u objetos de una clase. */
declaracion_objetos
    : ID lista_variables ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_OBJETOS, $1.obj, $2.obj)); }
    ;

tipo
    : ULONGINT
        { $$ = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.ULONGINT)); }
    | DOUBLEF
        { $$ = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.DOUBLEF)); }
    ;

/* Tipo admitido donde tambien pueden aparecer tipos definidos por el usuario. */
tipo_declarado
    : tipo
    | ID
        { $$ = new ParserVal(acciones.ejecutar(Reglas.TIPO, $1.obj)); }
    ;

lista_variables
    : ID ',' lista_variables
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, $1.obj, $3.obj)); }
    | ID
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, $1.obj)); }
    ;

/* --------------------------------------------------------------------------
   Declaracion de funciones

   La accion intermedia registra el encabezado antes de que se reduzcan los
   parametros y el cuerpo. Es necesaria porque el parser es ascendente: sin ella
   los parametros se reducirian antes de existir la funcion que los contiene.
   -------------------------------------------------------------------------- */

declaracion_funcion
    : tipo FUNCTION ID
        { acciones.ejecutar(Reglas.INICIO_FUNCION, $1.obj, $3.obj); }
      '(' lista_parametros_formales ')'
      sentencias_declarativas
      BEGIN lista_sentencias END ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION, $3.obj)); }
    ;

lista_parametros_formales
    : parametro_formal ',' lista_parametros_formales
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, $1.obj, $3.obj)); }
    | parametro_formal
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, $1.obj)); }
    ;

parametro_formal
    : tipo_declarado ID
        { $$ = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, $1.obj, $2.obj)); }
    ;

/* --------------------------------------------------------------------------
   Tema 25: declaracion de clases (sin codigo de estructura)
   Tema 31: herencia multiple mediante EXTENDS
   -------------------------------------------------------------------------- */

declaracion_clase
    : CLASS ID
        { acciones.ejecutar(Reglas.INICIO_CLASE, $2.obj); }
      BEGIN cuerpo_clase END ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, $2.obj)); }
    ;

cuerpo_clase
    : miembro_clase cuerpo_clase
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, $1.obj, $2.obj)); }
    | /* vacio */
        { $$ = new ParserVal((Object) null); }
    ;

miembro_clase
    : declaracion_atributo
    | declaracion_metodo
    | sentencia_extends
    ;

declaracion_atributo
    : tipo_declarado lista_variables ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_ATRIBUTO, $1.obj, $2.obj)); }
    ;

declaracion_metodo
    : tipo_declarado ID
        { acciones.ejecutar(Reglas.INICIO_FUNCION, $1.obj, $2.obj); }
      '(' lista_parametros_formales ')'
      BEGIN lista_sentencias END ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_METODO, $2.obj)); }
    ;

sentencia_extends
    : EXTENDS lista_clases_heredadas ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_EXTENDS, $2.obj)); }
    ;

lista_clases_heredadas
    : ID ',' lista_clases_heredadas
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, $1.obj, $3.obj)); }
    | ID
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, $1.obj)); }
    ;

/* --------------------------------------------------------------------------
   Tema 23: enumeraciones
   typedef ID = [ <lista_valores> ];
   -------------------------------------------------------------------------- */

declaracion_typedef
    : TYPEDEF ID '=' '[' lista_valores_enumerado ']' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_TYPEDEF, $2.obj, $5.obj)); }
    ;

lista_valores_enumerado
    : constante ',' lista_valores_enumerado
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, $1.obj, $3.obj)); }
    | constante
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, $1.obj)); }
    ;

/* ==========================================================================
   SENTENCIAS EJECUTABLES
   ========================================================================== */

lista_sentencias
    : sentencia lista_sentencias
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, $1.obj, $2.obj)); }
    | sentencia
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, $1.obj)); }
    ;

sentencia
    : asignacion ';'
    | sentencia_if
    | sentencia_repeat_while
    | sentencia_pout
    | sentencia_ret
    | error ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_SENTENCIA)); }
    ;

/* Un bloque puede ser una sola sentencia o un grupo delimitado por BEGIN END. */
bloque_sentencias
    : sentencia
    | BEGIN lista_sentencias END
        { $$ = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, $2.obj)); }
    ;

/* --------------------------------------------------------------------------
   Asignacion. El lado izquierdo es un identificador o una referencia a
   atributo (tema 28) y el lado derecho una expresion aritmetica.
   -------------------------------------------------------------------------- */

asignacion
    : ID ASIG expresion
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, $1.obj, $3.obj)); }
    | referencia_atributo ASIG expresion
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, $1.obj, $3.obj)); }
    | ID ASIG error
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_ASIGNACION_INVALIDA)); }
    ;

/* --------------------------------------------------------------------------
   Seleccion. Cada rama es un bloque de sentencias ejecutables y el else
   puede estar ausente. El END_IF explicito evita la ambiguedad del else
   colgante descripta en el apunte de Yacc.
   -------------------------------------------------------------------------- */

sentencia_if
    : IF '(' condicion ')' bloque_sentencias END_IF ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF, $3.obj, $5.obj)); }
    | IF '(' condicion ')' bloque_sentencias ELSE bloque_sentencias END_IF ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF_ELSE, $3.obj, $5.obj, $7.obj)); }
    | IF '(' condicion error bloque_sentencias END_IF ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
    | IF '(' error ')' bloque_sentencias END_IF ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_INVALIDA)); }
    ;

condicion
    : expresion comparador expresion
        { $$ = new ParserVal(acciones.ejecutar(Reglas.CONDICION, $1.obj, $2.obj, $3.obj)); }
    ;

comparador
    : MAYOR_IGUAL
    | MENOR_IGUAL
    | IGUAL_IGUAL
    | DISTINTO
    | '>'
    | '<'
    ;

/* --------------------------------------------------------------------------
   Tema 13: repeat <bloque_de_sentencias_ejecutables> while ( <condicion> );
   -------------------------------------------------------------------------- */

sentencia_repeat_while
    : REPEAT bloque_sentencias WHILE '(' condicion ')' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_REPEAT_WHILE, $2.obj, $5.obj)); }
    ;

/* --------------------------------------------------------------------------
   Salida por pantalla: pout(<cadena>); o pout(<expresion>);
   -------------------------------------------------------------------------- */

sentencia_pout
    : POUT '(' CADENA ')' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, $3.obj)); }
    | POUT '(' expresion ')' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, $3.obj)); }
    ;

/* Retorno de funcion: puede aparecer en cualquier lugar del cuerpo. */
sentencia_ret
    : RET '(' expresion ')' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, $3.obj)); }
    ;

/* ==========================================================================
   EXPRESIONES

   Recursivas a derecha. La asociatividad a izquierda de "-" y "/" se recupera
   en la accion semantica: ExpresionDiferida acumula operandos en orden de
   codigo fuente y el plegado a izquierda se hace al consumir la expresion.

   La precedencia sigue resuelta por la estratificacion expresion / termino /
   factor, que no se ve afectada por la recursion a derecha.

   El enunciado no admite anidamiento de expresiones con parentesis, por lo que
   factor no deriva '(' expresion ')'.
   ========================================================================== */

expresion
    : termino '+' expresion
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SUMA, $1.obj, $3.obj)); }
    | termino '-' expresion
        { $$ = new ParserVal(acciones.ejecutar(Reglas.RESTA, $1.obj, $3.obj)); }
    | termino
    ;

termino
    : factor '*' termino
        { $$ = new ParserVal(acciones.ejecutar(Reglas.MULTIPLICACION, $1.obj, $3.obj)); }
    | factor '/' termino
        { $$ = new ParserVal(acciones.ejecutar(Reglas.DIVISION, $1.obj, $3.obj)); }
    | factor
    ;

factor
    : ID
        { $$ = new ParserVal(acciones.ejecutar(Reglas.FACTOR_ID, $1.obj)); }
    | constante
        { $$ = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE, $1.obj)); }
    | invocacion_funcion
    | referencia_atributo
    | asignacion_en_expresion
    | conversion
    ;

/* Consideracion c) del TP2: el signo se detecta en el Analisis Sintactico y la
   accion vuelve a controlar el rango y actualiza la Tabla de Simbolos. */
constante
    : CTE
    | '-' CTE
        { $$ = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE_NEGATIVA, $2.obj)); }
    ;

/* --------------------------------------------------------------------------
   Tema 17: asignacion de expresiones en expresiones.
   Usa "=" y la expresion asignada va entre parentesis, sin anidamiento.
   El anidamiento lo detecta la accion semantica, no la gramatica.
   -------------------------------------------------------------------------- */

asignacion_en_expresion
    : ID '=' '(' expresion ')'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, $1.obj, $4.obj)); }
    ;

/* --------------------------------------------------------------------------
   Tema 33: conversion explicita de entero a punto flotante de 64 bits.
   -------------------------------------------------------------------------- */

conversion
    : TODF '(' expresion ')'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.CONVERSION_TODF, $3.obj)); }
    ;

/* --------------------------------------------------------------------------
   Tema 19: el orden de evaluacion de los parametros reales es obligatorio.
   ID ( <lista_parametros_reales> ) [ <lista_constantes> ]
   -------------------------------------------------------------------------- */

invocacion_funcion
    : ID '(' lista_parametros_reales ')' '[' lista_orden_evaluacion ']'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_FUNCION, $1.obj, $3.obj, $6.obj)); }
    | ID '(' lista_parametros_reales ')'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, $1.obj, $3.obj)); }
    ;

lista_parametros_reales
    : expresion ',' lista_parametros_reales
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, $1.obj, $3.obj)); }
    | expresion
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, $1.obj)); }
    ;

lista_orden_evaluacion
    : CTE ',' lista_orden_evaluacion
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, $1.obj, $3.obj)); }
    | CTE
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, $1.obj)); }
    ;

/* --------------------------------------------------------------------------
   Tema 28: acceso tradicional a atributos y metodos con "."
   Tema 31: desambiguado indicando la clase que aporta el miembro heredado.
   -------------------------------------------------------------------------- */

referencia_atributo
    : ID '.' ID
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO, $1.obj, $3.obj)); }
    | ID '.' ID '.' ID
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO_PREFIJADO, $1.obj, $3.obj, $5.obj)); }
    | ID '.' ID '(' lista_parametros_reales ')'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_METODO, $1.obj, $3.obj, $5.obj)); }
    ;

%%

/* ==========================================================================
   SECCION DE CODIGO
   ========================================================================== */

private AnalizadorLexico lexico;
private TablaAcciones acciones;
private Reporte reporte;

public Parser(AnalizadorLexico lexico, TablaAcciones acciones, Reporte reporte) {
    this.lexico = lexico;
    this.acciones = acciones;
    this.reporte = reporte;
}

/* yyparse invoca a yylex cada vez que necesita un token. El Analizador Lexico
   deja en Globals.yylval la referencia a la entrada de la Tabla de Simbolos. */
int yylex() {
    int token = lexico.yylex();
    this.yylval = new ParserVal(Globals.yylval);
    return token;
}

/* Ante un error el parser informa y continua, gracias a las producciones que
   usan el token error como punto de sincronizacion. */
public void yyerror(String mensaje) {
    reporte.error(Globals.numeroLinea, "Error sintactico: " + mensaje);
}
