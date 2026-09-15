//### This file created by BYACC 1.8(/Java extension  1.15)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//###           04 Mar 02  -- Yuval Oren  -- improved java performance, added options
//###           14 Mar 02  -- Tomas Hurka -- -d support, static initializer workaround
//### Please send bug reports to tom@hukatronic.cz
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";


//#line 25 "gramatica.y"
/* ESTA SECCION SE COPIA TAL CUAL AL COMIENZO DE Parser.java */
//#line 19 "Parser.java"




public class Parser
{

boolean yydebug;        //do I want debug output?
int yynerrs;            //number of errors so far
int yyerrflag;          //was there an error?
int yychar;             //the current working character

//########## MENSAJES ##########
//###############################################################
// method: debug
//###############################################################
void debug(String msg)
{
  if (yydebug)
    System.out.println(msg);
}

//########## STATE STACK ##########
final static int TAM_PILA = 500;  //maximum stack size
int statestk[] = new int[TAM_PILA]; //state stack
int stateptr; //indice actual de la pila de estados
int stateptrmax;                     //indice maximo de stackptr
int statemax;                        //estado cuando indice maximo es alcanzado
//###############################################################
// methods: state stack push,pop,drop,peek
//###############################################################
final void state_push(int state)
{
  try {
		stateptr++;
		statestk[stateptr]=state;
	 }
	 catch (ArrayIndexOutOfBoundsException e) {
     int oldsize = statestk.length;
     int newsize = oldsize * 2;
     int[] newstack = new int[newsize];
     System.arraycopy(statestk,0,newstack,0,oldsize);
     statestk = newstack;
     statestk[stateptr]=state;
  }
}
final int state_pop()
{
  return statestk[stateptr--];
}
final void state_drop(int cnt)
{
  stateptr -= cnt; 
}
final int state_peek(int relative)
{
  return statestk[stateptr-relative];
}
//###############################################################
// method: init_stacks : allocate and prepare stacks
//###############################################################
final boolean init_stacks()
{
  stateptr = -1;
  val_init();
  return true;
}
//###############################################################
// method: dump_stacks : show n levels of the stacks
//###############################################################
void dump_stacks(int count)
{
int i;
  System.out.println("=index==state====value=     s:"+stateptr+"  v:"+valptr);
  for (i=0;i<count;i++)
    System.out.println(" "+i+"    "+statestk[i]+"      "+valstk[i]);
  System.out.println("======================");
}


//########## SEMANTIC VALUES ##########
//public class ParserVal is defined in ParserVal.java


String   yytext;//user variable to return contextual strings
ParserVal yyval; //used to return semantic vals from action routines
ParserVal yylval;//the 'lval' (result) I got from yylex()
ParserVal valstk[];
int valptr;
//###############################################################
// methods: value stack push,pop,drop,peek.
//###############################################################
void val_init()
{
  valstk=new ParserVal[TAM_PILA];
  yyval=new ParserVal();
  yylval=new ParserVal();
  valptr=-1;
}
void val_push(ParserVal val)
{
  if (valptr>=TAM_PILA)
    return;
  valstk[++valptr]=val;
}
ParserVal val_pop()
{
  if (valptr<0)
    return new ParserVal();
  return valstk[valptr--];
}
void val_drop(int cnt)
{
int ptr;
  ptr=valptr-cnt;
  if (ptr<0)
    return;
  valptr = ptr;
}
ParserVal val_peek(int relative)
{
int ptr;
  ptr=valptr-relative;
  if (ptr<0)
    return new ParserVal();
  return valstk[ptr];
}
final ParserVal dup_yyval(ParserVal val)
{
  ParserVal dup = new ParserVal();
  dup.ival = val.ival;
  dup.dval = val.dval;
  dup.sval = val.sval;
  dup.obj = val.obj;
  return dup;
}
//#### end semantic value section ####
public final static short ID=300;
public final static short ASIG=301;
public final static short CTE=302;
public final static short CADENA=303;
public final static short MAYOR_IGUAL=304;
public final static short MENOR_IGUAL=305;
public final static short IGUAL_IGUAL=306;
public final static short DISTINTO=307;
public final static short IF=400;
public final static short ELSE=401;
public final static short END_IF=402;
public final static short BEGIN=403;
public final static short END=404;
public final static short POUT=405;
public final static short RET=406;
public final static short CLASS=407;
public final static short FUNCTION=408;
public final static short REPEAT=409;
public final static short WHILE=410;
public final static short TODF=411;
public final static short TYPEDEF=412;
public final static short EXTENDS=413;
public final static short ULONGINT=414;
public final static short DOUBLEF=415;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    1,    3,    2,    2,    5,    5,    5,    5,    5,
    5,    6,    6,    7,   11,   11,   13,   13,   12,   12,
   14,    8,   15,   15,   16,   17,    9,   18,   18,   19,
   19,   19,   20,   23,   21,   22,   24,   24,   10,   25,
   25,    4,    4,   27,   27,   27,   27,   27,   27,   33,
   33,   28,   28,   28,   29,   29,   29,   29,   36,   37,
   37,   37,   37,   37,   37,   30,   31,   31,   32,   34,
   34,   34,   38,   38,   38,   39,   39,   39,   39,   39,
   39,   26,   26,   41,   42,   40,   40,   43,   43,   44,
   44,   35,   35,   35,
};
final static short yylen[] = {                            2,
    3,    1,    3,    2,    0,    1,    1,    1,    1,    1,
    2,    3,    3,    3,    1,    1,    1,    1,    3,    1,
    0,   12,    3,    1,    2,    0,    7,    2,    0,    1,
    1,    1,    3,    0,   10,    3,    3,    1,    7,    3,
    1,    2,    1,    2,    1,    1,    1,    1,    2,    1,
    3,    3,    3,    3,    7,    9,    7,    7,    3,    1,
    1,    1,    1,    1,    1,    7,    5,    5,    5,    3,
    3,    1,    3,    3,    1,    1,    1,    1,    1,    1,
    1,    1,    2,    5,    4,    7,    4,    3,    1,    3,
    1,    3,    5,    6,
};
final static short yydefred[] = {                         0,
    2,    0,    0,    0,    0,    0,    0,   15,   16,    0,
    0,    6,    7,    8,    9,   10,    0,   11,    0,    0,
   26,    0,    0,    1,    4,    0,    0,    0,    0,   14,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   45,   46,   47,   48,    0,   13,   21,   12,   19,
    0,    0,   49,    0,    0,    0,    0,    0,    0,   50,
    0,    3,   42,   44,    0,    0,   18,    0,   17,    0,
    0,    0,   30,   31,   32,   82,    0,    0,    0,   54,
    0,    0,   77,   52,   79,    0,    0,   78,   80,   81,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   53,
    0,    0,    0,    0,    0,    0,   28,   83,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   60,   61,   62,   63,   64,   65,    0,    0,    0,    0,
    0,    0,   51,    0,    0,    0,    0,    0,   36,    0,
   33,   27,   39,   40,    0,    0,    0,    0,   70,   71,
   73,   74,    0,   93,    0,   59,    0,    0,   67,   68,
   69,    0,   25,    0,    0,   37,    0,    0,    0,    0,
   85,   94,    0,    0,    0,    0,    0,    0,   23,    0,
   88,    0,   84,   58,   57,    0,   55,   66,    0,    0,
    0,    0,    0,    0,    0,    0,   86,   56,    0,    0,
   90,   22,    0,   35,
};
final static short yydgoto[] = {                          2,
    3,   10,   24,   39,   11,   12,   13,   14,   15,   16,
   69,   20,  135,   66,  136,  137,   31,   71,   72,   73,
   74,   75,  140,  103,   78,   83,   40,   41,   42,   43,
   44,   45,   61,  145,   85,   94,  127,   86,   87,   88,
   89,   90,  146,  192,
};
final static short yysindex[] = {                      -240,
    0,    0, -235,   17, -227, -200, -197,    0,    0, -299,
 -235,    0,    0,    0,    0,    0, -198,    0,   61,   47,
    0,   48, -202,    0,    0,   49, -193,   53, -227,    0,
 -292,   22,   55,  -23,   75,   77,   78, -213, -284, -202,
   62,    0,    0,    0,    0, -178,    0,    0,    0,    0,
 -276,  -42,    0,  -45, -176,  -44,  -41,  -37, -202,    0,
 -285,    0,    0,    0,  -37,   86,    0, -173,    0, -172,
 -275, -276,    0,    0,    0,    0, -171,   37,   88,    0,
   34,  100,    0,    0,    0,   51,   46,    0,    0,    0,
   43,  102,  -55,  -39,  104,  105,  106, -255,  110,    0,
 -254,  107,   95,   61,   97,   98,    0,    0,   99,  -42,
  -37,  119,  -37,  -37,  -37,  -37,  -37,  -37, -138, -213,
    0,    0,    0,    0,    0,    0,  -37, -213, -213,  111,
  112,  114,    0,  -37, -137,  123,  121, -173,    0,  126,
    0,    0,    0,    0,  125,  133,  -37,  135,    0,    0,
    0,    0,  141,    0, -219,    0, -218, -317,    0,    0,
    0,  144,    0, -235, -254,    0, -254,  -37,  103,  145,
    0,    0,  130,  132, -213,  136,  138, -203,    0,  160,
    0,  -97,    0,    0,    0, -196,    0,    0, -202, -194,
  169,  122,  155, -188, -202,  -97,    0,    0,  159, -185,
    0,    0,  161,    0,
};
final static short yyrindex[] = {                         0,
    0,    0, -182,    0,    0,    0,    0,    0,    0,    0,
 -182,    0,    0,    0,    0,    0,    0,    0,  163,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0, -181,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
 -179,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0, -179,    0,    0,    0,    0,    0,    0,  134,    0,
  -25,    0,    0,    0,    0,    8,  -12,    0,    0,    0,
  -32,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  167,    0,   42,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  187,    0,    0,    0,
    0,    0,    0,    0,  188,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0, -182,    0,    0,    0,    0,   -3,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  137,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
    0,   14,    0,  -14,    0,    0,    0,    0,    0,    0,
    3,   52,   20,    0,  -68,    0,    0,  162,    0,    0,
    0,    0,    0,   94,  127,    9,   24,    0,    0,    0,
    0,    0,  -65,   21,   13,  101,    0,  -26,    0,    0,
    0,    0,  -52,   40,
};
final static int YYTABLESIZE=374;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         77,
   77,  129,   77,   77,  126,   17,  125,   77,   92,   92,
   92,   92,   92,   17,   92,   76,   76,   76,   76,   76,
    4,   76,   55,   67,   25,   63,   92,   92,   75,   92,
   75,   75,   75,   76,   76,   46,   76,   87,   87,   87,
   87,   87,   33,   87,   98,   67,   75,   75,   72,   75,
   46,   72,   46,   33,  155,   87,   87,   26,   87,    1,
   79,   60,  157,  158,    5,  153,   72,   72,   28,   72,
   70,   46,   19,  111,   84,   18,   93,   96,   97,   55,
   50,   34,  118,  175,  176,  100,   34,  116,  119,  151,
  152,   70,  117,  114,  112,  115,  179,   34,  180,   21,
   20,   19,   22,   23,   29,   30,   48,   47,   32,  186,
   51,   49,   52,   53,   56,  181,   57,   58,   79,   62,
   64,  105,   65,   91,   99,  101,  102,  104,  106,  109,
  108,  110,   46,  148,  149,  150,   68,    8,    9,  113,
   46,   46,  120,   60,  130,  131,  132,  156,  133,  134,
  138,   60,   60,  139,   93,  141,  142,  143,  147,    8,
    9,  154,  163,  164,  165,  167,   17,  170,  168,  159,
  160,    6,  161,  169,  194,  171,    7,  178,    8,    9,
  200,  172,  173,  174,  177,  183,   35,   46,  184,   59,
  185,   36,   37,  182,  187,   38,  188,   35,   60,  189,
  190,   46,   36,   37,  191,  193,   38,   46,  195,   27,
   80,   92,  196,  198,  197,  199,  128,  202,  203,  204,
    5,   20,   43,   92,   29,   38,   41,   24,   89,   91,
   76,  166,    0,  107,  162,  201,  144,    0,    0,    0,
    0,    0,    0,   75,    0,    0,    0,    0,  121,  122,
  123,  124,   87,    0,   81,   81,   76,   76,   81,   76,
   76,   95,   81,   72,   76,    0,    0,    0,   92,    0,
    0,   92,   92,   92,   92,    0,    0,   54,   76,   76,
   76,   76,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   75,   75,   75,   75,    0,    0,    0,    0,    0,
   87,   87,   87,   87,    0,    0,    0,    0,    0,    0,
    0,   72,   72,   72,   72,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   82,   82,    0,    0,   82,
    0,    0,    0,   82,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         45,
   45,   41,   45,   45,   60,    3,   62,   45,   41,   42,
   43,   44,   45,   11,   47,   41,   42,   43,   44,   45,
  256,   47,   46,  300,   11,   40,   59,   60,   41,   62,
   43,   44,   45,   59,   60,   23,   62,   41,   42,   43,
   44,   45,  256,   47,   59,  300,   59,   60,   41,   62,
   38,   44,   40,  256,  120,   59,   60,  256,   62,  300,
   52,   38,  128,  129,  300,  118,   59,   60,   17,   62,
   51,   59,  300,   40,   54,   59,   56,   57,   58,   46,
   29,   40,   40,  401,  402,   65,  300,   42,   46,  116,
  117,   72,   47,   43,   61,   45,  165,  300,  167,  300,
   59,  300,  300,  403,   44,   59,  300,   59,   61,  175,
  403,   59,   91,   59,   40,  168,   40,   40,  110,  404,
   59,   70,  301,  300,  410,   40,  300,  300,  404,   93,
  302,   44,  120,  113,  114,  115,  413,  414,  415,   40,
  128,  129,   41,  120,   41,   41,   41,  127,  404,   40,
   44,  128,  129,   59,  134,   59,   59,   59,   40,  414,
  415,  300,  300,   41,   44,   40,  164,  147,   44,   59,
   59,  407,   59,   41,  189,   41,  412,  164,  414,  415,
  195,   41,  402,  402,   41,   41,  400,  175,   59,  403,
   59,  405,  406,   91,   59,  409,   59,  400,  175,  403,
   41,  189,  405,  406,  302,  402,  409,  195,  403,  408,
  256,  256,   44,   59,   93,  404,  256,   59,  404,   59,
  403,   59,  404,  256,  404,   59,   93,   41,   41,   93,
  256,  138,   -1,   72,  134,  196,  110,   -1,   -1,   -1,
   -1,   -1,   -1,  256,   -1,   -1,   -1,   -1,  304,  305,
  306,  307,  256,   -1,  300,  300,  302,  302,  300,  302,
  302,  303,  300,  256,  302,   -1,   -1,   -1,  301,   -1,
   -1,  304,  305,  306,  307,   -1,   -1,  301,  304,  305,
  306,  307,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  304,  305,  306,  307,   -1,   -1,   -1,   -1,   -1,
  304,  305,  306,  307,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  304,  305,  306,  307,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  411,  411,   -1,   -1,  411,
   -1,   -1,   -1,  411,
};
}
final static short YYFINAL=2;
final static short YYMAXTOKEN=415;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,"'('","')'","'*'","'+'","','",
"'-'","'.'","'/'",null,null,null,null,null,null,null,null,null,null,null,"';'",
"'<'","'='","'>'",null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
"'['",null,"']'",null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,"ID","ASIG","CTE","CADENA","MAYOR_IGUAL","MENOR_IGUAL","IGUAL_IGUAL",
"DISTINTO",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,"IF",
"ELSE","END_IF","BEGIN","END","POUT","RET","CLASS","FUNCTION","REPEAT","WHILE",
"TODF","TYPEDEF","EXTENDS","ULONGINT","DOUBLEF",
};
final static String yyrule[] = {
"$accept : programa",
"programa : nombre_programa sentencias_declarativas bloque_ejecutable",
"nombre_programa : ID",
"bloque_ejecutable : BEGIN lista_sentencias END",
"sentencias_declarativas : sentencia_declarativa sentencias_declarativas",
"sentencias_declarativas :",
"sentencia_declarativa : declaracion_variables",
"sentencia_declarativa : declaracion_objetos",
"sentencia_declarativa : declaracion_funcion",
"sentencia_declarativa : declaracion_clase",
"sentencia_declarativa : declaracion_typedef",
"sentencia_declarativa : error ';'",
"declaracion_variables : tipo lista_variables ';'",
"declaracion_variables : tipo error ';'",
"declaracion_objetos : ID lista_variables ';'",
"tipo : ULONGINT",
"tipo : DOUBLEF",
"tipo_declarado : tipo",
"tipo_declarado : ID",
"lista_variables : ID ',' lista_variables",
"lista_variables : ID",
"$$1 :",
"declaracion_funcion : tipo FUNCTION ID $$1 '(' lista_parametros_formales ')' sentencias_declarativas BEGIN lista_sentencias END ';'",
"lista_parametros_formales : parametro_formal ',' lista_parametros_formales",
"lista_parametros_formales : parametro_formal",
"parametro_formal : tipo_declarado ID",
"$$2 :",
"declaracion_clase : CLASS ID $$2 BEGIN cuerpo_clase END ';'",
"cuerpo_clase : miembro_clase cuerpo_clase",
"cuerpo_clase :",
"miembro_clase : declaracion_atributo",
"miembro_clase : declaracion_metodo",
"miembro_clase : sentencia_extends",
"declaracion_atributo : tipo_declarado lista_variables ';'",
"$$3 :",
"declaracion_metodo : tipo_declarado ID $$3 '(' lista_parametros_formales ')' BEGIN lista_sentencias END ';'",
"sentencia_extends : EXTENDS lista_clases_heredadas ';'",
"lista_clases_heredadas : ID ',' lista_clases_heredadas",
"lista_clases_heredadas : ID",
"declaracion_typedef : TYPEDEF ID '=' '[' lista_valores_enumerado ']' ';'",
"lista_valores_enumerado : constante ',' lista_valores_enumerado",
"lista_valores_enumerado : constante",
"lista_sentencias : sentencia lista_sentencias",
"lista_sentencias : sentencia",
"sentencia : asignacion ';'",
"sentencia : sentencia_if",
"sentencia : sentencia_repeat_while",
"sentencia : sentencia_pout",
"sentencia : sentencia_ret",
"sentencia : error ';'",
"bloque_sentencias : sentencia",
"bloque_sentencias : BEGIN lista_sentencias END",
"asignacion : ID ASIG expresion",
"asignacion : referencia_atributo ASIG expresion",
"asignacion : ID ASIG error",
"sentencia_if : IF '(' condicion ')' bloque_sentencias END_IF ';'",
"sentencia_if : IF '(' condicion ')' bloque_sentencias ELSE bloque_sentencias END_IF ';'",
"sentencia_if : IF '(' condicion error bloque_sentencias END_IF ';'",
"sentencia_if : IF '(' error ')' bloque_sentencias END_IF ';'",
"condicion : expresion comparador expresion",
"comparador : MAYOR_IGUAL",
"comparador : MENOR_IGUAL",
"comparador : IGUAL_IGUAL",
"comparador : DISTINTO",
"comparador : '>'",
"comparador : '<'",
"sentencia_repeat_while : REPEAT bloque_sentencias WHILE '(' condicion ')' ';'",
"sentencia_pout : POUT '(' CADENA ')' ';'",
"sentencia_pout : POUT '(' expresion ')' ';'",
"sentencia_ret : RET '(' expresion ')' ';'",
"expresion : termino '+' expresion",
"expresion : termino '-' expresion",
"expresion : termino",
"termino : factor '*' termino",
"termino : factor '/' termino",
"termino : factor",
"factor : ID",
"factor : constante",
"factor : invocacion_funcion",
"factor : referencia_atributo",
"factor : asignacion_en_expresion",
"factor : conversion",
"constante : CTE",
"constante : '-' CTE",
"asignacion_en_expresion : ID '=' '(' expresion ')'",
"conversion : TODF '(' expresion ')'",
"invocacion_funcion : ID '(' lista_parametros_reales ')' '[' lista_orden_evaluacion ']'",
"invocacion_funcion : ID '(' lista_parametros_reales ')'",
"lista_parametros_reales : expresion ',' lista_parametros_reales",
"lista_parametros_reales : expresion",
"lista_orden_evaluacion : CTE ',' lista_orden_evaluacion",
"lista_orden_evaluacion : CTE",
"referencia_atributo : ID '.' ID",
"referencia_atributo : ID '.' ID '.' ID",
"referencia_atributo : ID '.' ID '(' lista_parametros_reales ')'",
};

//#line 446 "gramatica.y"

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
//#line 468 "Parser.java"
//###############################################################
// method: yylexdebug : check lexer state
//###############################################################
void yylexdebug(int state,int ch)
{
String s=null;
  if (ch < 0) ch=0;
  if (ch <= YYMAXTOKEN) //check index bounds
     s = yyname[ch];    //now get it
  if (s==null)
    s = "illegal-symbol";
  debug("state "+state+", reading "+ch+" ("+s+")");
}





//The following are now global, to aid in error reporting
int yyn;       //next next thing to do
int yym;       //
int yystate;   //current parsing state from state table
String yys;    //current token string


//###############################################################
// method: yyparse : parse input and execute indicated items
//###############################################################
int yyparse()
{
boolean doaction;
  init_stacks();
  yynerrs = 0;
  yyerrflag = 0;
  yychar = -1;          //impossible char forces a read
  yystate=0;            //initial state
  state_push(yystate);  //save it
  val_push(yylval);     //save empty value
  while (true) //until parsing is done, either correctly, or w/error
    {
    doaction=true;
    if (yydebug) debug("loop"); 
    //#### NEXT ACTION (from reduction table)
    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
      {
      if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  yychar:"+yychar);
      if (yychar < 0)      //we want a char?
        {
        yychar = yylex();  //get next token
        if (yydebug) debug(" next yychar:"+yychar);
        //#### ERROR CHECK ####
        if (yychar < 0)    //it it didn't work/error
          {
          yychar = 0;      //change it to default string (no -1!)
          if (yydebug)
            yylexdebug(yystate,yychar);
          }
        }//yychar<0
      yyn = yysindex[yystate];  //get amount to shift by (shift index)
      if ((yyn != 0) && (yyn += yychar) >= 0 &&
          yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
        {
        if (yydebug)
          debug("state "+yystate+", shifting to state "+yytable[yyn]);
        //#### NEXT STATE ####
        yystate = yytable[yyn];//we are in a new state
        state_push(yystate);   //save it
        val_push(yylval);      //push our lval as the input for next rule
        yychar = -1;           //since we have 'eaten' a token, say we need another
        if (yyerrflag > 0)     //have we recovered an error?
           --yyerrflag;        //give ourselves credit
        doaction=false;        //but don't process yet
        break;   //quit the yyn=0 loop
        }

    yyn = yyrindex[yystate];  //reduce
    if ((yyn !=0 ) && (yyn += yychar) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
      {   //we reduced!
      if (yydebug) debug("reduce");
      yyn = yytable[yyn];
      doaction=true; //get ready to execute
      break;         //drop down to actions
      }
    else //ERROR RECOVERY
      {
      if (yyerrflag==0)
        {
        yyerror("syntax error");
        yynerrs++;
        }
      if (yyerrflag < 3) //low error count?
        {
        yyerrflag = 3;
        while (true)   //do until break
          {
          if (stateptr<0)   //check for under & overflow here
            {
            yyerror("stack underflow. aborting...");  //note lower case 's'
            return 1;
            }
          yyn = yysindex[state_peek(0)];
          if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&
                    yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)
            {
            if (yydebug)
              debug("state "+state_peek(0)+", error recovery shifting to state "+yytable[yyn]+" ");
            yystate = yytable[yyn];
            state_push(yystate);
            val_push(yylval);
            doaction=false;
            break;
            }
          else
            {
            if (yydebug)
              debug("error recovery discarding state "+state_peek(0)+" ");
            if (stateptr<0)   //check for under & overflow here
              {
              yyerror("Stack underflow. aborting...");  //capital 'S'
              return 1;
              }
            state_pop();
            val_pop();
            }
          }
        }
      else            //discard this token
        {
        if (yychar == 0)
          return 1; //yyabort
        if (yydebug)
          {
          yys = null;
          if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
          if (yys == null) yys = "illegal-symbol";
          debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
          }
        yychar = -1;  //read another
        }
      }//end error recovery
    }//yyn=0 loop
    if (!doaction)   //any reason not to proceed?
      continue;      //skip action
    yym = yylen[yyn];          //get count of terminals on rhs
    if (yydebug)
      debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
    if (yym>0)                 //if count of rhs not 'nil'
      yyval = val_peek(yym-1); //get current semantic value
    yyval = dup_yyval(yyval); //duplicate yyval if ParserVal is used as semantic value
    switch(yyn)
      {
//########## USER-SUPPLIED ACTIONS ##########
case 1:
//#line 71 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PROGRAMA, val_peek(1).obj, val_peek(0).obj)); }
break;
case 2:
//#line 76 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.NOMBRE_PROGRAMA, val_peek(0).obj)); }
break;
case 3:
//#line 81 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, val_peek(1).obj)); }
break;
case 4:
//#line 91 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 5:
//#line 93 "gramatica.y"
{ yyval = new ParserVal((Object) null); }
break;
case 11:
//#line 103 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_DECLARACION_INVALIDA)); }
break;
case 12:
//#line 109 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_VARIABLES, val_peek(2).obj, val_peek(1).obj)); }
break;
case 13:
//#line 111 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_LISTA_VARIABLES)); }
break;
case 14:
//#line 117 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_OBJETOS, val_peek(2).obj, val_peek(1).obj)); }
break;
case 15:
//#line 122 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.ULONGINT)); }
break;
case 16:
//#line 124 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.DOUBLEF)); }
break;
case 18:
//#line 131 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, val_peek(0).obj)); }
break;
case 19:
//#line 136 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 20:
//#line 138 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(0).obj)); }
break;
case 21:
//#line 151 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(2).obj, val_peek(0).obj); }
break;
case 22:
//#line 155 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION, val_peek(9).obj)); }
break;
case 23:
//#line 160 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 24:
//#line 162 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, val_peek(0).obj)); }
break;
case 25:
//#line 167 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, val_peek(1).obj, val_peek(0).obj)); }
break;
case 26:
//#line 177 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_CLASE, val_peek(0).obj); }
break;
case 27:
//#line 179 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, val_peek(5).obj)); }
break;
case 28:
//#line 184 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 29:
//#line 186 "gramatica.y"
{ yyval = new ParserVal((Object) null); }
break;
case 33:
//#line 197 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_ATRIBUTO, val_peek(2).obj, val_peek(1).obj)); }
break;
case 34:
//#line 202 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(1).obj, val_peek(0).obj); }
break;
case 35:
//#line 205 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_METODO, val_peek(8).obj)); }
break;
case 36:
//#line 210 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_EXTENDS, val_peek(1).obj)); }
break;
case 37:
//#line 215 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, val_peek(2).obj, val_peek(0).obj)); }
break;
case 38:
//#line 217 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, val_peek(0).obj)); }
break;
case 39:
//#line 227 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_TYPEDEF, val_peek(5).obj, val_peek(2).obj)); }
break;
case 40:
//#line 232 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 41:
//#line 234 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, val_peek(0).obj)); }
break;
case 42:
//#line 243 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 43:
//#line 245 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(0).obj)); }
break;
case 49:
//#line 255 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_SENTENCIA)); }
break;
case 51:
//#line 262 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, val_peek(1).obj)); }
break;
case 52:
//#line 272 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 53:
//#line 274 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 54:
//#line 276 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_ASIGNACION_INVALIDA)); }
break;
case 55:
//#line 287 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF, val_peek(4).obj, val_peek(2).obj)); }
break;
case 56:
//#line 289 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF_ELSE, val_peek(6).obj, val_peek(4).obj, val_peek(2).obj)); }
break;
case 57:
//#line 291 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
break;
case 58:
//#line 293 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_INVALIDA)); }
break;
case 59:
//#line 298 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONDICION, val_peek(2).obj, val_peek(1).obj, val_peek(0).obj)); }
break;
case 66:
//#line 316 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_REPEAT_WHILE, val_peek(5).obj, val_peek(2).obj)); }
break;
case 67:
//#line 325 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 68:
//#line 327 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 69:
//#line 333 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, val_peek(2).obj)); }
break;
case 70:
//#line 352 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SUMA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 71:
//#line 354 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.RESTA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 73:
//#line 360 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.MULTIPLICACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 74:
//#line 362 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DIVISION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 76:
//#line 368 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_ID, val_peek(0).obj)); }
break;
case 77:
//#line 370 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE, val_peek(0).obj)); }
break;
case 83:
//#line 382 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE_NEGATIVA, val_peek(0).obj)); }
break;
case 84:
//#line 393 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
case 85:
//#line 402 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONVERSION_TODF, val_peek(1).obj)); }
break;
case 86:
//#line 412 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_FUNCION, val_peek(6).obj, val_peek(4).obj, val_peek(1).obj)); }
break;
case 87:
//#line 414 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(3).obj, val_peek(1).obj)); }
break;
case 88:
//#line 419 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 89:
//#line 421 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(0).obj)); }
break;
case 90:
//#line 426 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 91:
//#line 428 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(0).obj)); }
break;
case 92:
//#line 438 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 93:
//#line 440 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO_PREFIJADO, val_peek(4).obj, val_peek(2).obj, val_peek(0).obj)); }
break;
case 94:
//#line 442 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_METODO, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
//#line 881 "Parser.java"
//########## END OF USER-SUPPLIED ACTIONS ##########
    }//switch
    //#### Now let's reduce... ####
    if (yydebug) debug("reduce");
    state_drop(yym);             //we just reduced yylen states
    yystate = state_peek(0);     //get new state
    val_drop(yym);               //corresponding value drop
    yym = yylhs[yyn];            //select next TERMINAL(on lhs)
    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
      {
      if (yydebug) debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
      yystate = YYFINAL;         //explicitly say we're done
      state_push(YYFINAL);       //and save it
      val_push(yyval);           //also save the semantic value of parsing
      if (yychar < 0)            //we want another character?
        {
        yychar = yylex();        //get next character
        if (yychar<0) yychar=0;  //clean, if necessary
        if (yydebug)
          yylexdebug(yystate,yychar);
        }
      if (yychar == 0)          //Good exit (if lex returns 0 ;-)
         break;                 //quit the loop--all DONE
      }//if yystate
    else                        //else not done yet
      {                         //get next state and push, for next yydefred[]
      yyn = yygindex[yym];      //find out where to go
      if ((yyn != 0) && (yyn += yystate) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
        yystate = yytable[yyn]; //get new state
      else
        yystate = yydgoto[yym]; //else go to new defred
      if (yydebug) debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
      state_push(yystate);     //going again, so push state & val...
      val_push(yyval);         //for next action
      }
    }//main loop
  return 0;//yyaccept!!
}
//## end of method parse() ######################################



//## run() --- for Thread #######################################
/**
 * A default run method, used for operating this parser
 * object in the background.  It is intended for extending Thread
 * or implementing Runnable.  Turn off with -Jnorun .
 */
public void run()
{
  yyparse();
}
//## end of method run() ########################################



//## Constructors ###############################################
/**
 * Default constructor.  Turn off with -Jnoconstruct .

 */
public Parser()
{
  //nothing to do
}


/**
 * Create a parser, setting the debug to true or false.
 * @param debugMe true for debugging, false for no debug.
 */
public Parser(boolean debugMe)
{
  yydebug=debugMe;
}
//###############################################################



}
//################### END OF CLASS ##############################
