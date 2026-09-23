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

    Gramatica recursiva a derecha. Las reducciones invocan a acciones.ejecutar(Reglas.X).
    TablaAcciones resuelve el codigo, registra la reduccion e informa la estructura.
    Las producciones ERR_* cubren el PDF de errores a detectar del grupo; Verificaciones
    aplica lo que requiere contexto (RET ausente, enum vacio, orden ausente).

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
   ========================================================================== */

programa
    : nombre_programa sentencias_declarativas bloque_ejecutable
        { $$ = new ParserVal(acciones.ejecutar(Reglas.PROGRAMA, $2.obj, $3.obj)); }
    | sentencias_declarativas bloque_ejecutable
        { acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_PROGRAMA);
          $$ = new ParserVal(acciones.ejecutar(Reglas.PROGRAMA, $1.obj, $2.obj)); }
    ;

nombre_programa
    : ID
        { $$ = new ParserVal(acciones.ejecutar(Reglas.NOMBRE_PROGRAMA, $1.obj)); }
    ;

bloque_ejecutable
    : BEGIN lista_sentencias END
        { $$ = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, $2.obj)); }
    | BEGIN lista_sentencias
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_BLOQUE_SIN_END, $2.obj)); }
    | lista_sentencias END
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_BEGIN, $1.obj)); }
    | END
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_BEGIN)); }
    ;

/* ==========================================================================
   SENTENCIAS DECLARATIVAS
   ========================================================================== */

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
    | asignacion ';'
    | sentencia_if
    | sentencia_repeat_while
    | sentencia_pout
    | sentencia_ret
    ;

/* <tipo> <lista_de_variables> ; */
declaracion_variables
    : tipo lista_variables ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_VARIABLES, $1.obj, $2.obj)); }
    | tipo lista_variables
        { acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_VARIABLES, $1.obj, $2.obj)); }
    | lista_variables ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_TIPO_VARIABLES, $1.obj)); }
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
    | ID lista_variables
        { acciones.ejecutar(Reglas.ERR_FALTA_COMA_VARIABLES);
          $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, $1.obj, $2.obj)); }
    ;

/* --------------------------------------------------------------------------
   Declaracion de funciones
   -------------------------------------------------------------------------- */

encabezado_funcion
    : tipo FUNCTION ID
        { acciones.ejecutar(Reglas.INICIO_FUNCION, $1.obj, $3.obj); }
    | tipo FUNCTION
        { acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_FUNCION);
          acciones.ejecutar(Reglas.INICIO_FUNCION, $1.obj, null); }
    ;

declaracion_funcion
    : encabezado_funcion '(' lista_parametros_formales ')' sentencias_declarativas cierre_funcion
        { $$ = $6; }
    ;

cierre_funcion
    : BEGIN lista_sentencias END ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
    | BEGIN lista_sentencias END
        { acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
    | BEGIN lista_sentencias
        { acciones.ejecutar(Reglas.ERR_BLOQUE_SIN_END);
          $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
    | lista_sentencias END ';'
        { acciones.ejecutar(Reglas.ERR_FALTA_BEGIN);
          $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
    ;

lista_parametros_formales
    : parametro_formal ',' lista_parametros_formales
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, $1.obj, $3.obj)); }
    | parametro_formal
        { $$ = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, $1.obj)); }
    ;

/* tipo ID cubre ulongint/doublef; ID ID cubre tipos definidos por el usuario.
   Las alternativas cortas detectan falta de nombre o de tipo. */
parametro_formal
    : tipo ID
        { $$ = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, $1.obj, $2.obj)); }
    | ID ID
        { $$ = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, $1.obj, $2.obj)); }
    | tipo
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_PARAMETRO, $1.obj)); }
    | ID
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_TIPO_PARAMETRO, $1.obj)); }
    ;

/* --------------------------------------------------------------------------
   Tema 25: declaracion de clases (sin codigo de estructura)
   Tema 31: herencia multiple mediante EXTENDS
   -------------------------------------------------------------------------- */

encabezado_clase
    : CLASS ID
        { acciones.ejecutar(Reglas.INICIO_CLASE, $2.obj);
          $$ = $2; }
    ;

declaracion_clase
    : encabezado_clase BEGIN cuerpo_clase END ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, $1.obj)); }
    | encabezado_clase BEGIN cuerpo_clase END
        { acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          $$ = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, $1.obj)); }
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
    | EXTENDS ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_LISTA_EXTENDS)); }
    | EXTENDS error ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_LISTA_EXTENDS)); }
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
    | TYPEDEF ID '=' '[' ']' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_VALORES_ENUMERADO, $2.obj)); }
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
    | asignacion
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
    | sentencia_if
    | sentencia_repeat_while
    | sentencia_pout
    | sentencia_ret
    | error ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_SENTENCIA)); }
    ;

bloque_sentencias
    : sentencia
    | BEGIN lista_sentencias END
        { $$ = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, $2.obj)); }
    ;

asignacion
    : ID ASIG expresion
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, $1.obj, $3.obj)); }
    | referencia_atributo ASIG expresion
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, $1.obj, $3.obj)); }
    | ID ASIG error
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_ASIGNACION_INVALIDA)); }
    ;

/* --------------------------------------------------------------------------
   Seleccion
   -------------------------------------------------------------------------- */

sentencia_if
    : IF '(' condicion ')' bloque_sentencias END_IF ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF, $3.obj, $5.obj)); }
    | IF '(' condicion ')' bloque_sentencias ELSE bloque_sentencias END_IF ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF_ELSE, $3.obj, $5.obj, $7.obj)); }
    | IF '(' condicion ')' bloque_sentencias END_IF
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
    | IF '(' condicion ')' bloque_sentencias ELSE bloque_sentencias END_IF
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
    | IF '(' condicion ')' bloque_sentencias
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, $3.obj, $5.obj)); }
    | IF '(' condicion ')' bloque_sentencias ELSE bloque_sentencias
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, $3.obj, $5.obj, $7.obj)); }
    | IF condicion ')' bloque_sentencias END_IF ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
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
   Tema 13: repeat <bloque> while ( <condicion> );
   -------------------------------------------------------------------------- */

sentencia_repeat_while
    : REPEAT bloque_sentencias WHILE '(' condicion ')' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_REPEAT_WHILE, $2.obj, $5.obj)); }
    | REPEAT bloque_sentencias WHILE '(' condicion ')'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
    | REPEAT WHILE '(' condicion ')' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_CUERPO_ITERACION, $4.obj)); }
    | REPEAT bloque_sentencias '(' condicion ')' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_WHILE, $2.obj, $4.obj)); }
    | REPEAT bloque_sentencias WHILE condicion ')' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
    | REPEAT bloque_sentencias WHILE '(' condicion ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
    ;

/* --------------------------------------------------------------------------
   Salida por pantalla
   -------------------------------------------------------------------------- */

sentencia_pout
    : POUT '(' CADENA ')' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, $3.obj)); }
    | POUT '(' expresion ')' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, $3.obj)); }
    | POUT '(' ')' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ARGUMENTO_POUT)); }
    | POUT '(' CADENA ')'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
    | POUT '(' expresion ')'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
    ;

sentencia_ret
    : RET '(' expresion ')' ';'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, $3.obj)); }
    | RET '(' expresion ')'
        { acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          $$ = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, $3.obj)); }
    ;

/* ==========================================================================
   EXPRESIONES
   ========================================================================== */

expresion
    : termino '+' expresion
        { $$ = new ParserVal(acciones.ejecutar(Reglas.SUMA, $1.obj, $3.obj)); }
    | termino '-' expresion
        { $$ = new ParserVal(acciones.ejecutar(Reglas.RESTA, $1.obj, $3.obj)); }
    | termino '+' error
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, $1.obj)); }
    | termino CTE
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERADOR, $1.obj, $2.obj)); }
    | termino error
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERADOR, $1.obj)); }
    | termino
    ;

termino
    : factor '*' termino
        { $$ = new ParserVal(acciones.ejecutar(Reglas.MULTIPLICACION, $1.obj, $3.obj)); }
    | factor '/' termino
        { $$ = new ParserVal(acciones.ejecutar(Reglas.DIVISION, $1.obj, $3.obj)); }
    | factor '*' error
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, $1.obj)); }
    | factor '/' error
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, $1.obj)); }
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

constante
    : CTE
    | '-' CTE
        { $$ = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE_NEGATIVA, $2.obj)); }
    ;

/* Tema 17: "=" con parentesis. ':=' en ese contexto es error. */
asignacion_en_expresion
    : ID '=' '(' expresion ')'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, $1.obj, $4.obj)); }
    | ID ASIG '(' expresion ')'
        { acciones.ejecutar(Reglas.ERR_ASIG_DONDE_IGUAL);
          $$ = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, $1.obj, $4.obj)); }
    ;

conversion
    : TODF '(' expresion ')'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.CONVERSION_TODF, $3.obj)); }
    ;

/* Tema 19: orden de evaluacion obligatorio entre corchetes. */
invocacion_funcion
    : ID '(' lista_parametros_reales ')' '[' lista_orden_evaluacion ']'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_FUNCION, $1.obj, $3.obj, $6.obj)); }
    | ID '(' lista_parametros_reales ')' '[' ']'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, $1.obj, $3.obj)); }
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

int yylex() {
    int token = lexico.yylex();
    this.yylval = new ParserVal(Globals.yylval);
    return token;
}

/* El mensaje generico de Yacc se silencia: cada produccion ERR_* informa el
   faltante concreto del PDF a traves de TablaAcciones y Verificaciones. */
public void yyerror(String mensaje) {
}
