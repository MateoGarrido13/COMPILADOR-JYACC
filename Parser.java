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






//#line 31 "gramatica.y"
/* Esta seccion se copia tal cual al comienzo de Parser.java */
//#line 19 "Parser.java"




public class Parser
{

boolean yydebug;        //do I want debug output?
int yynerrs;            //number of errors so far
int yyerrflag;          //was there an error?
int yychar;             //the current working character

//########## MESSAGES ##########
//###############################################################
// method: debug
//###############################################################
void debug(String msg)
{
  if (yydebug)
    System.out.println(msg);
}

//########## STATE STACK ##########
final static int YYSTACKSIZE = 500;  //maximum stack size
int statestk[] = new int[YYSTACKSIZE]; //state stack
int stateptr;
int stateptrmax;                     //highest index of stackptr
int statemax;                        //state when highest index reached
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
//## **user defined:ParserVal
String   yytext;//user variable to return contextual strings
ParserVal yyval; //used to return semantic vals from action routines
ParserVal yylval;//the 'lval' (result) I got from yylex()
ParserVal valstk[] = new ParserVal[YYSTACKSIZE];
int valptr;
//###############################################################
// methods: value stack push,pop,drop,peek.
//###############################################################
final void val_init()
{
  yyval=new ParserVal();
  yylval=new ParserVal();
  valptr=-1;
}
final void val_push(ParserVal val)
{
  try {
    valptr++;
    valstk[valptr]=val;
  }
  catch (ArrayIndexOutOfBoundsException e) {
    int oldsize = valstk.length;
    int newsize = oldsize*2;
    ParserVal[] newstack = new ParserVal[newsize];
    System.arraycopy(valstk,0,newstack,0,oldsize);
    valstk = newstack;
    valstk[valptr]=val;
  }
}
final ParserVal val_pop()
{
  return valstk[valptr--];
}
final void val_drop(int cnt)
{
  valptr -= cnt;
}
final ParserVal val_peek(int relative)
{
  return valstk[valptr-relative];
}
final ParserVal dup_yyval(ParserVal val)
{
  return val;
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
    0,    0,    0,    4,    4,    4,    4,    1,    3,    3,
    3,    3,    3,    3,    2,    2,   10,   10,   10,   10,
   10,   10,   10,   10,   10,    5,    5,   11,   16,   16,
   18,   18,   17,   17,   17,   19,   19,    6,   21,   21,
   21,   21,   21,   20,   20,   22,   22,   22,   22,   23,
    7,    7,   24,   24,   25,   25,   25,   26,   29,   27,
   28,   28,   28,   30,   30,    8,    8,   31,   31,    9,
    9,   33,   33,   33,   33,   33,   33,   33,   35,   35,
   34,   34,   34,   12,   12,   12,   12,   12,   12,   12,
   38,   38,   39,   40,   41,   42,   43,   43,   43,   43,
   43,   43,   44,   13,   13,   13,   13,   13,   13,   14,
   14,   14,   14,   14,   15,   15,   36,   36,   36,   36,
   36,   36,   45,   45,   45,   45,   45,   46,   46,   46,
   46,   46,   46,   32,   32,   48,   48,   49,   47,   47,
   47,   50,   50,   52,   52,   51,   51,   37,   37,   37,
   37,   37,   37,   37,   37,
};
final static short yylen[] = {                            2,
    3,    1,    3,    1,    1,    1,    1,    1,    3,    2,
    2,    3,    1,    2,    2,    0,    1,    1,    1,    1,
    1,    1,    1,    1,    1,    3,    3,    3,    1,    1,
    1,    1,    3,    1,    2,    3,    2,    6,    4,    3,
    3,    4,    3,    3,    1,    2,    2,    1,    1,    2,
    5,    4,    2,    0,    1,    1,    1,    3,    2,    8,
    3,    2,    3,    3,    1,    7,    6,    3,    1,    2,
    1,    2,    1,    1,    1,    1,    1,    2,    1,    3,
    3,    3,    3,    2,    1,    6,    8,    6,    7,    7,
    6,    8,    1,    1,    1,    3,    1,    1,    1,    1,
    1,    1,    1,    7,    6,    6,    4,    6,    6,    5,
    5,    4,    4,    4,    5,    4,    3,    3,    3,    3,
    2,    1,    3,    3,    3,    3,    1,    1,    1,    1,
    1,    1,    1,    1,    2,    5,    5,    4,    7,    6,
    4,    3,    1,    3,    1,    3,    1,    3,    5,    9,
    8,    6,   11,   10,    8,
};
final static short yydefred[] = {                         0,
    0,    8,    0,   13,    0,    0,   29,   30,    0,    0,
    2,    0,    4,    5,    6,    7,    0,    0,    0,   14,
    0,    0,    0,   10,    0,    0,  103,    0,   74,   75,
   76,   77,    0,    0,    0,    0,    0,   50,    0,    0,
    0,   17,   19,   20,   21,    0,   18,   22,   23,   24,
   25,    0,    0,    0,    0,    0,    0,    0,   12,   78,
    0,    0,    0,  134,    0,    0,    0,  129,    0,  131,
    0,    0,    0,  130,  132,  133,    0,    0,    9,    0,
   70,   72,    0,   84,    0,    0,   79,    0,    0,    0,
    1,   15,    3,   27,    0,   35,   36,   26,    0,    0,
    0,    0,   32,    0,   31,    0,    0,    0,   55,   56,
   57,    0,   83,   81,    0,    0,    0,    0,    0,    0,
    0,    0,  135,   97,   98,   99,  100,  101,  102,    0,
    0,  121,    0,    0,    0,    0,    0,    0,    0,    0,
   82,    0,    0,    0,    0,    0,   28,   33,   47,   46,
    0,    0,    0,    0,   62,    0,    0,    0,    0,   53,
    0,    0,    0,    0,    0,  145,    0,    0,    0,    0,
    0,    0,    0,   96,    0,  119,  117,  120,  118,  125,
  123,  126,  124,    0,  112,    0,    0,   80,    0,  107,
    0,    0,    0,    0,    0,    0,   44,   63,    0,   61,
   58,   51,    0,    0,    0,    0,    0,    0,    0,    0,
  138,    0,    0,    0,    0,    0,  110,  111,  115,    0,
    0,    0,   67,    0,    0,    0,    0,   38,   64,    0,
    0,    0,  137,  144,    0,  142,  136,    0,   86,   91,
    0,    0,   88,  106,  109,    0,  108,   66,   68,    0,
    0,    0,    0,    0,    0,    0,    0,  140,    0,   90,
   95,    0,   89,  104,   43,    0,   40,    0,    0,  151,
    0,    0,    0,  139,   87,   92,   42,   39,    0,  150,
    0,  146,   60,  154,    0,  153,
};
final static short yydgoto[] = {                          9,
   10,   41,   11,   12,   42,   43,   44,   45,   28,   46,
   47,   29,   30,   31,   32,   17,   96,  106,   18,  101,
  228,  102,   19,  107,  108,  109,  110,  111,  112,  156,
  194,   68,   87,   34,   88,   69,   70,   36,  121,  214,
  262,   71,  130,   37,   72,   73,   74,   75,   76,  167,
  259,  168,
};
final static short yysindex[] = {                      -173,
 -327,    0, -221,    0, -210, -209,    0,    0,    0,  351,
    0,  351,    0,    0,    0,    0, -226,   59, -301,    0,
  -52,   -3,  -32,    0,   66,   68,    0, -294,    0,    0,
    0,    0,  341,   53, -172,   72,  326,    0,   83, -162,
 -212,    0,    0,    0,    0,  351,    0,    0,    0,    0,
    0, -212,   94,  -18, -139,  104, -228, -255,    0,    0,
  -42, -136,  -40,    0,  125,  144, -134,    0,   35,    0,
  135,  -11,  115,    0,    0,    0,  -16,   51,    0,  131,
    0,    0,   51,    0,  341,  156,    0, -233,  112,  149,
    0,    0,    0,    0, -162,    0,    0,    0,  -88,  -87,
  174,  172,    0,  -45,    0,  -72, -168, -255,    0,    0,
    0,  192,    0,    0,   54,  197,  245,  198,   51,  199,
  202,  -12,    0,    0,    0,    0,    0,    0,    0,   51,
 -183,    0,  251,  257,  266,  286,  211,  194,  215,  231,
    0, -121,   51,  229,  -21,  -43,    0,    0,    0,    0,
  351, -228,  235,  255,    0,  241,  -18,  244,  254,    0,
 -228,  245,    4,   51,  -30,    0,  274,  275,   51,  279,
 -183, -183, -183,    0,  -80,    0,    0,    0,    0,    0,
    0,    0,    0,  264,    0,  276,  277,    0,  297,    0,
   51,  305,  284,  262,  306, -229,    0,    0,   56,    0,
    0,    0,  316,  317,  320,  323,   14,  270,  245,  325,
    0,  -39, -208,  -34,  -31,  313,    0,    0,    0,  314,
   26,  315,    0,  330,  -33,  -28, -199,    0,    0,  -19,
  300,  245,    0,    0,  -78,    0,    0,  353,    0,    0,
 -183,  369,    0,    0,    0,  371,    0,    0,    0,  382,
  -50,  398,   74,  341,  -75,  334,  432,    0,  395,    0,
    0, -205,    0,    0,    0,  436,    0,  444,  108,    0,
  424,  441,  234,    0,    0,    0,    0,    0,  489,    0,
  -73,    0,    0,    0,  456,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0, -204,
    0, -204,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  552,    0,    0,
    0,    0,    5,  114,    0,    1,    0,    0,    0,    0,
    0,    0,    0,    0,    0, -204,    0,    0,    0,    0,
    0,    0,    0,  495,  516,    0,    0,  157,    0,    0,
    0,    0,  265,    0,    0,    0,    0,    0,    0,    0,
    0,  318,  285,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   34,   77,
    0,  521,    0,    0,    0,    0,    0,  157,    0,    0,
    0,    0,    0,    0,   81,    0,    0,    0,    0,    0,
    0,  522,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
 -201,    0,    0,  514,    0,    0,   21,    0, -234,    0,
    0,    0,    0,    0,   -5,    0,    0,  535,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   17,    0,   33,   49,    0,    0,    0,
    0,    0,    0,    0,  485,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   92,    0,    0,  484,    0,    0,
    0,    0,  176,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  204,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   65,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  486,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  302,    0,    0,
    0,  233,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
    0,   16,   30,    0,  580,  581,  583,  584,  -29,    0,
    0,   58,   99,  103,  322,  442,  558,    0,    0,  -63,
    0,    0,    0,  487,    0,    0,    0,    0,    0,  394,
  374,  -93,    8,    0,  -85,  -14,  527,    0,    0,    0,
    0,   76,    0,    0,   11,    0,    0,    0,    0, -102,
 -197,    0,
};
final static int YYTABLESIZE=894;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                        117,
   85,   67,   67,   81,   71,   62,   60,   66,   60,  117,
   33,   67,   67,  155,  258,   62,  113,  270,  191,  284,
  118,   52,  144,   67,  138,   95,  226,   52,   67,   53,
  207,  133,  114,  134,   21,  128,  128,  128,  128,  128,
   33,  128,   62,    1,  103,  175,  114,  239,  116,  193,
  275,   16,  195,  169,   16,  142,  251,  271,   67,  204,
   59,   92,  139,  140,  105,   52,  246,   48,  141,   48,
   91,   99,   80,   54,   49,  282,   20,   49,   22,   34,
  148,   93,    1,  285,  245,  212,  213,  215,  197,   38,
   39,  149,   33,  162,  129,   67,  128,  203,   57,  163,
   22,   58,  166,   48,  170,   77,  236,   78,   49,   79,
   49,   82,   50,   73,   50,  174,   22,   48,  177,  179,
   48,  148,  148,  148,  148,  148,    2,  148,   83,  256,
   84,  195,  149,  149,  149,  149,  149,   54,  149,  148,
  148,  122,  148,   89,   49,  181,  183,  166,   50,  206,
  149,  149,   94,  149,  210,  261,  135,  104,    7,    8,
   97,  136,   98,  115,  119,   52,  196,  123,   52,   52,
   52,   52,   52,  227,   52,  131,  145,   52,   23,   52,
   52,   55,   24,   25,   26,    7,    8,   27,   67,   60,
    3,    4,  234,  240,  166,  143,  276,  253,   16,   16,
   23,   16,  146,  152,  252,   25,   26,  147,   48,   27,
  153,  149,  150,  113,  151,  152,   23,  166,  189,   85,
  192,   25,   26,  257,  269,   27,  257,  157,  257,    3,
    4,  161,  155,    5,   33,  159,  164,  169,    6,  171,
    7,    8,  172,  173,  152,  152,  152,  152,  152,   49,
  152,  184,  185,   50,  154,  186,   85,   63,   64,   64,
  116,   33,  152,  152,  128,  152,  221,   63,   64,   64,
  116,  187,  113,  155,  155,  155,  155,  155,   63,  155,
   64,   54,  188,   63,  127,   64,  137,  190,  114,   67,
  132,  155,  155,  198,  155,   67,  128,   61,  199,  200,
   85,   67,  201,  205,  116,  128,  128,  128,  128,  128,
   67,  128,  202,   63,  208,   64,  113,  122,  209,  211,
  105,  216,  217,  128,  128,  127,  128,  127,  127,  127,
   67,   51,  114,   51,  218,  219,  148,  220,  124,  125,
  126,  127,  223,  127,  127,  222,  127,  149,  116,  225,
   63,   59,   64,  266,  224,  154,  230,  231,  122,  232,
  235,  122,  238,  233,  105,  237,  241,   51,   65,   73,
  242,  243,  244,  247,  272,  250,  122,  122,   65,  122,
  148,  148,  148,  254,  148,  148,  148,  148,  248,   65,
  255,  149,  149,  149,   65,  149,  149,  149,  149,  120,
   85,   85,   85,   85,   85,   85,   85,   85,   71,   85,
   85,  260,   85,   73,   85,   85,  113,  113,  113,  113,
  113,  113,  113,  113,   65,  113,  113,  263,  113,  264,
  113,  113,  114,  114,  114,  114,  114,  114,  114,  114,
  265,  114,  114,   63,  114,   64,  114,  114,  116,  116,
  116,  116,  116,  116,  116,  116,  267,  116,  116,  152,
  116,   65,  116,  116,  105,  105,  105,  105,  105,  105,
  105,  105,   51,  105,  105,  273,  105,  268,  105,  105,
  148,  148,  148,  141,  148,  148,  148,  274,  155,  148,
  148,  149,  149,  149,  277,  149,  149,  149,  100,  105,
  149,  149,  278,  152,  152,  152,  176,  152,  152,  152,
  152,  279,  178,   73,   73,   73,  280,   73,   73,   73,
  128,  180,   73,   73,  141,  141,  141,  141,  141,   35,
  141,  281,  155,  155,  155,  257,  155,  155,  155,  155,
  127,  182,  141,  141,  165,  141,   64,  283,  286,  105,
   63,   11,   64,   34,   65,   37,   63,   41,   64,   35,
   54,   45,   93,   35,  128,   63,  128,   64,  128,  128,
  128,  128,   65,  122,   56,  143,   94,   69,  147,   13,
   14,   80,   15,   16,  127,   63,  127,   64,  127,  127,
  127,  127,  229,  100,  160,    0,   80,   90,  249,    0,
    0,   41,  100,  152,  152,  152,    0,  152,  152,  152,
    0,   35,  152,  152,    0,    0,    0,  122,    0,    0,
    0,  122,  122,  122,  122,   22,    0,    0,    0,    0,
    0,    0,  155,  155,  155,    0,  155,  155,  155,    0,
   22,  155,  155,    0,    0,    0,    0,    0,    0,    0,
   40,    0,  148,    0,    0,   65,    0,   35,    0,    0,
    0,   65,    0,  158,  128,  128,  128,   65,  128,  128,
  128,    0,    0,  128,  128,    0,   65,    0,    0,    0,
    0,    0,    0,    0,  127,  127,  127,    0,  127,  127,
  127,    0,    0,  127,  127,    0,   65,   35,   35,   35,
    0,   41,    0,    0,   41,   41,   41,   41,   41,    0,
   41,    0,    0,   41,    0,   41,   41,  122,  122,  122,
    0,  122,  122,  122,    0,   23,  122,  122,   85,    0,
   25,   26,    0,    0,   27,   86,    0,    0,    0,  141,
   23,    0,    0,    0,    0,   25,   26,    0,    0,   27,
   23,    0,    0,   35,    0,   25,   26,    5,    0,   27,
    0,    0,    6,    0,    7,    8,    0,   35,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   35,    0,    0,  141,    0,  141,    0,  141,  141,  141,
  141,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  141,  141,  141,    0,  141,  141,  141,
    0,    0,  141,  141,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         40,
    0,   45,   45,   33,    0,   46,   59,   40,   59,   40,
    3,   45,   45,   59,   93,   46,    0,   93,   40,   93,
   61,  256,  256,   45,   41,   44,  256,   12,   45,  256,
   61,   43,    0,   45,  256,   41,   42,   43,   44,   45,
   33,   47,   46,  256,  300,  131,   61,  256,    0,   93,
  256,  256,  146,   40,  256,   85,  256,  255,   45,  162,
   40,   46,   77,   78,    0,  300,   41,   10,   83,   12,
   41,  300,  256,  300,   41,  273,  404,   44,  300,   59,
    0,   52,  256,  281,   59,  171,  172,  173,  152,  300,
  300,    0,   85,   40,   60,   45,   62,  161,   40,   46,
  300,  403,  117,   46,  119,   40,  209,   40,   10,  404,
   12,   59,   10,    0,   12,  130,  300,   41,  133,  134,
   44,   41,   42,   43,   44,   45,  300,   47,  301,  232,
   59,  225,   41,   42,   43,   44,   45,  300,   47,   59,
   60,   66,   62,   61,   46,  135,  136,  162,   46,  164,
   59,   60,   59,   62,  169,  241,   42,  413,  414,  415,
  300,   47,   59,  300,   40,  400,  151,  302,  403,  404,
  405,  406,  407,  403,  409,   41,  410,  412,  400,  414,
  415,  408,  404,  405,  406,  414,  415,  409,   45,   59,
  403,  404,  207,  402,  209,   40,  402,  227,  403,  404,
  400,  403,   91,    0,  404,  405,  406,   59,  151,  409,
  256,  300,  300,  256,   41,   44,  400,  232,  143,  403,
  145,  405,  406,  302,  254,  409,  302,  300,  302,  403,
  404,   40,    0,  407,  227,  404,   40,   40,  412,   41,
  414,  415,   41,  256,   41,   42,   43,   44,   45,  151,
   47,   41,   59,  151,  300,   41,  256,  300,  302,  302,
  301,  254,   59,   60,    0,   62,  191,  300,  302,  302,
  301,   41,  256,   41,   42,   43,   44,   45,  300,   47,
  302,  300,  404,  300,    0,  302,  303,   59,  256,   45,
  302,   59,   60,   59,   62,   45,  302,  301,   44,   59,
  300,   45,   59,  300,  256,   41,   42,   43,   44,   45,
   45,   47,   59,  300,   41,  302,  300,    0,   44,   41,
  256,  402,   59,   59,   60,   41,   62,   43,   44,   45,
   45,   10,  300,   12,   59,   59,  256,   41,  304,  305,
  306,  307,   59,   59,   60,   41,   62,  256,  300,   44,
  300,  404,  302,  404,   93,  300,   41,   41,   41,   40,
   91,   44,  402,   41,  300,   41,  401,   46,  411,  256,
  402,   59,   59,   59,   41,  404,   59,   60,  411,   62,
  300,  301,  302,  403,  304,  305,  306,  307,   59,  411,
   91,  300,  301,  302,  411,  304,  305,  306,  307,  256,
  400,  401,  402,  403,  404,  405,  406,  407,  404,  409,
  410,   59,  412,  300,  414,  415,  400,  401,  402,  403,
  404,  405,  406,  407,  411,  409,  410,   59,  412,   59,
  414,  415,  400,  401,  402,  403,  404,  405,  406,  407,
   59,  409,  410,  300,  412,  302,  414,  415,  400,  401,
  402,  403,  404,  405,  406,  407,   59,  409,  410,  256,
  412,  411,  414,  415,  400,  401,  402,  403,  404,  405,
  406,  407,  151,  409,  410,   44,  412,  404,  414,  415,
  400,  401,  402,    0,  404,  405,  406,   93,  256,  409,
  410,  400,  401,  402,   59,  404,  405,  406,   57,   58,
  409,  410,   59,  300,  301,  302,  256,  304,  305,  306,
  307,  404,  256,  400,  401,  402,   93,  404,  405,  406,
  256,  256,  409,  410,   41,   42,   43,   44,   45,    3,
   47,   91,  300,  301,  302,  302,  304,  305,  306,  307,
  256,  256,   59,   60,  300,   62,  302,   59,   93,  108,
  300,    0,  302,   59,  411,   40,  300,  256,  302,   33,
  404,   41,   41,   37,  300,  300,  302,  302,  304,  305,
  306,  307,   59,  256,   17,   41,  401,   93,   93,    0,
    0,  256,    0,    0,  300,  300,  302,  302,  304,  305,
  306,  307,  199,  152,  108,   -1,  256,   40,  225,   -1,
   -1,  300,  161,  400,  401,  402,   -1,  404,  405,  406,
   -1,   85,  409,  410,   -1,   -1,   -1,  300,   -1,   -1,
   -1,  304,  305,  306,  307,  300,   -1,   -1,   -1,   -1,
   -1,   -1,  400,  401,  402,   -1,  404,  405,  406,   -1,
  300,  409,  410,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  300,   -1,   95,   -1,   -1,  411,   -1,  131,   -1,   -1,
   -1,  411,   -1,  106,  400,  401,  402,  411,  404,  405,
  406,   -1,   -1,  409,  410,   -1,  411,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  400,  401,  402,   -1,  404,  405,
  406,   -1,   -1,  409,  410,   -1,  411,  171,  172,  173,
   -1,  400,   -1,   -1,  403,  404,  405,  406,  407,   -1,
  409,   -1,   -1,  412,   -1,  414,  415,  400,  401,  402,
   -1,  404,  405,  406,   -1,  400,  409,  410,  403,   -1,
  405,  406,   -1,   -1,  409,  410,   -1,   -1,   -1,  256,
  400,   -1,   -1,   -1,   -1,  405,  406,   -1,   -1,  409,
  400,   -1,   -1,  227,   -1,  405,  406,  407,   -1,  409,
   -1,   -1,  412,   -1,  414,  415,   -1,  241,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  254,   -1,   -1,  300,   -1,  302,   -1,  304,  305,  306,
  307,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  400,  401,  402,   -1,  404,  405,  406,
   -1,   -1,  409,  410,
};
}
final static short YYFINAL=9;
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
"programa : bloque_ejecutable",
"programa : declaracion_clara sentencias_declarativas bloque_ejecutable",
"declaracion_clara : declaracion_variables",
"declaracion_clara : declaracion_funcion",
"declaracion_clara : declaracion_clase",
"declaracion_clara : declaracion_typedef",
"nombre_programa : ID",
"bloque_ejecutable : BEGIN lista_sentencias END",
"bloque_ejecutable : BEGIN END",
"bloque_ejecutable : BEGIN lista_sentencias",
"bloque_ejecutable : BEGIN error END",
"bloque_ejecutable : END",
"bloque_ejecutable : error END",
"sentencias_declarativas : sentencia_declarativa sentencias_declarativas",
"sentencias_declarativas :",
"sentencia_declarativa : declaracion_variables",
"sentencia_declarativa : declaracion_objetos",
"sentencia_declarativa : declaracion_funcion",
"sentencia_declarativa : declaracion_clase",
"sentencia_declarativa : declaracion_typedef",
"sentencia_declarativa : sentencia_if",
"sentencia_declarativa : sentencia_repeat_while",
"sentencia_declarativa : sentencia_pout",
"sentencia_declarativa : sentencia_ret",
"declaracion_variables : tipo lista_variables ';'",
"declaracion_variables : tipo error ';'",
"declaracion_objetos : ID lista_variables ';'",
"tipo : ULONGINT",
"tipo : DOUBLEF",
"tipo_declarado : tipo",
"tipo_declarado : ID",
"lista_variables : ID ',' lista_variables",
"lista_variables : ID",
"lista_variables : ID lista_variables",
"encabezado_funcion : tipo FUNCTION ID",
"encabezado_funcion : tipo FUNCTION",
"declaracion_funcion : encabezado_funcion '(' lista_parametros_formales ')' sentencias_declarativas cierre_funcion",
"cierre_funcion : BEGIN lista_sentencias END ';'",
"cierre_funcion : BEGIN END ';'",
"cierre_funcion : BEGIN lista_sentencias END",
"cierre_funcion : BEGIN error END ';'",
"cierre_funcion : error END ';'",
"lista_parametros_formales : parametro_formal ',' lista_parametros_formales",
"lista_parametros_formales : parametro_formal",
"parametro_formal : tipo ID",
"parametro_formal : ID ID",
"parametro_formal : tipo",
"parametro_formal : ID",
"encabezado_clase : CLASS ID",
"declaracion_clase : encabezado_clase BEGIN cuerpo_clase END ';'",
"declaracion_clase : encabezado_clase BEGIN cuerpo_clase END",
"cuerpo_clase : miembro_clase cuerpo_clase",
"cuerpo_clase :",
"miembro_clase : declaracion_atributo",
"miembro_clase : declaracion_metodo",
"miembro_clase : sentencia_extends",
"declaracion_atributo : tipo_declarado lista_variables ';'",
"encabezado_metodo : tipo_declarado ID",
"declaracion_metodo : encabezado_metodo '(' lista_parametros_formales ')' BEGIN lista_sentencias END ';'",
"sentencia_extends : EXTENDS lista_clases_heredadas ';'",
"sentencia_extends : EXTENDS ';'",
"sentencia_extends : EXTENDS error ';'",
"lista_clases_heredadas : ID ',' lista_clases_heredadas",
"lista_clases_heredadas : ID",
"declaracion_typedef : TYPEDEF ID '=' '[' lista_valores_enumerado ']' ';'",
"declaracion_typedef : TYPEDEF ID '=' '[' ']' ';'",
"lista_valores_enumerado : constante ',' lista_valores_enumerado",
"lista_valores_enumerado : constante",
"lista_sentencias : sentencia lista_sentencias",
"lista_sentencias : sentencia",
"sentencia : asignacion ';'",
"sentencia : asignacion",
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
"sentencia_if : cuerpo_if ';'",
"sentencia_if : cuerpo_if",
"sentencia_if : IF '(' condicion_if ')' bloque_sentencias error",
"sentencia_if : IF '(' condicion_if ')' bloque_then ELSE bloque_else error",
"sentencia_if : IF condicion ')' bloque_sentencias END_IF ';'",
"sentencia_if : IF '(' condicion error bloque_sentencias END_IF ';'",
"sentencia_if : IF '(' error ')' bloque_sentencias END_IF ';'",
"cuerpo_if : IF '(' condicion_if ')' bloque_sentencias END_IF",
"cuerpo_if : IF '(' condicion_if ')' bloque_then ELSE bloque_else END_IF",
"condicion_if : condicion",
"bloque_then : bloque_sentencias",
"bloque_else : bloque_sentencias",
"condicion : expresion comparador expresion",
"comparador : MAYOR_IGUAL",
"comparador : MENOR_IGUAL",
"comparador : IGUAL_IGUAL",
"comparador : DISTINTO",
"comparador : '>'",
"comparador : '<'",
"inicio_repeat : REPEAT",
"sentencia_repeat_while : inicio_repeat bloque_sentencias WHILE '(' condicion ')' ';'",
"sentencia_repeat_while : inicio_repeat bloque_sentencias WHILE '(' condicion ')'",
"sentencia_repeat_while : inicio_repeat WHILE '(' condicion ')' ';'",
"sentencia_repeat_while : inicio_repeat bloque_sentencias error ';'",
"sentencia_repeat_while : inicio_repeat bloque_sentencias WHILE condicion ')' ';'",
"sentencia_repeat_while : inicio_repeat bloque_sentencias WHILE '(' condicion ';'",
"sentencia_pout : POUT '(' CADENA ')' ';'",
"sentencia_pout : POUT '(' expresion ')' ';'",
"sentencia_pout : POUT '(' ')' ';'",
"sentencia_pout : POUT '(' CADENA ')'",
"sentencia_pout : POUT '(' expresion ')'",
"sentencia_ret : RET '(' expresion ')' ';'",
"sentencia_ret : RET '(' expresion ')'",
"expresion : termino '+' expresion",
"expresion : termino '-' expresion",
"expresion : termino '+' error",
"expresion : termino '-' error",
"expresion : termino CTE",
"expresion : termino",
"termino : factor '*' termino",
"termino : factor '/' termino",
"termino : factor '*' error",
"termino : factor '/' error",
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
"asignacion_en_expresion : ID ASIG '(' expresion ')'",
"conversion : TODF '(' expresion ')'",
"invocacion_funcion : ID '(' lista_parametros_reales ')' '[' lista_orden_evaluacion ']'",
"invocacion_funcion : ID '(' lista_parametros_reales ')' '[' ']'",
"invocacion_funcion : ID '(' lista_parametros_reales ')'",
"lista_parametros_reales : parametro_real ',' lista_parametros_reales",
"lista_parametros_reales : parametro_real",
"parametro_real : ID '=' expresion",
"parametro_real : expresion",
"lista_orden_evaluacion : CTE ',' lista_orden_evaluacion",
"lista_orden_evaluacion : CTE",
"referencia_atributo : ID '.' ID",
"referencia_atributo : ID '.' ID '.' ID",
"referencia_atributo : ID '.' ID '(' lista_parametros_reales ')' '[' lista_orden_evaluacion ']'",
"referencia_atributo : ID '.' ID '(' lista_parametros_reales ')' '[' ']'",
"referencia_atributo : ID '.' ID '(' lista_parametros_reales ')'",
"referencia_atributo : ID '.' ID '.' ID '(' lista_parametros_reales ')' '[' lista_orden_evaluacion ']'",
"referencia_atributo : ID '.' ID '.' ID '(' lista_parametros_reales ')' '[' ']'",
"referencia_atributo : ID '.' ID '.' ID '(' lista_parametros_reales ')'",
};

//#line 564 "gramatica.y"


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
//#line 669 "Parser.java"
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
//#line 74 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PROGRAMA, val_peek(1).obj, val_peek(0).obj)); }
break;
case 2:
//#line 76 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_PROGRAMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.PROGRAMA, null, val_peek(0).obj)); }
break;
case 3:
//#line 79 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_PROGRAMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.PROGRAMA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 8:
//#line 92 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.NOMBRE_PROGRAMA, val_peek(0).obj)); }
break;
case 9:
//#line 97 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, val_peek(1).obj)); }
break;
case 10:
//#line 99 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, (Object) null)); }
break;
case 11:
//#line 101 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_BLOQUE_SIN_END, val_peek(0).obj)); }
break;
case 12:
//#line 103 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_BLOQUE_SIN_END)); }
break;
case 13:
//#line 105 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_BEGIN)); }
break;
case 14:
//#line 107 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_BEGIN)); }
break;
case 15:
//#line 115 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 16:
//#line 117 "gramatica.y"
{ yyval = new ParserVal((Object) null); }
break;
case 26:
//#line 134 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_VARIABLES, val_peek(2).obj, val_peek(1).obj)); }
break;
case 27:
//#line 136 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_LISTA_VARIABLES)); }
break;
case 28:
//#line 141 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_OBJETOS, val_peek(2).obj, val_peek(1).obj)); }
break;
case 29:
//#line 146 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.ULONGINT)); }
break;
case 30:
//#line 148 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.DOUBLEF)); }
break;
case 32:
//#line 154 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, val_peek(0).obj)); }
break;
case 33:
//#line 159 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 34:
//#line 161 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(0).obj)); }
break;
case 35:
//#line 163 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_COMA_VARIABLES);
          yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(1).obj, val_peek(0).obj)); }
break;
case 36:
//#line 172 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(2).obj, val_peek(0).obj); }
break;
case 37:
//#line 174 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_FUNCION);
          acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(1).obj, null); }
break;
case 38:
//#line 180 "gramatica.y"
{ yyval = val_peek(0); }
break;
case 39:
//#line 185 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 40:
//#line 187 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 41:
//#line 189 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 42:
//#line 192 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_BLOQUE_SIN_END);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 43:
//#line 195 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_BEGIN);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 44:
//#line 201 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 45:
//#line 203 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, val_peek(0).obj)); }
break;
case 46:
//#line 208 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, val_peek(1).obj, val_peek(0).obj)); }
break;
case 47:
//#line 210 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, val_peek(1).obj, val_peek(0).obj)); }
break;
case 48:
//#line 212 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_PARAMETRO, val_peek(0).obj)); }
break;
case 49:
//#line 214 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_TIPO_PARAMETRO, val_peek(0).obj)); }
break;
case 50:
//#line 223 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_CLASE, val_peek(0).obj);
          yyval = val_peek(0); }
break;
case 51:
//#line 229 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, val_peek(4).obj)); }
break;
case 52:
//#line 231 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, val_peek(3).obj)); }
break;
case 53:
//#line 237 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 54:
//#line 239 "gramatica.y"
{ yyval = new ParserVal((Object) null); }
break;
case 58:
//#line 250 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_ATRIBUTO, val_peek(2).obj, val_peek(1).obj)); }
break;
case 59:
//#line 255 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(1).obj, val_peek(0).obj);
          yyval = val_peek(0); }
break;
case 60:
//#line 261 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_METODO, val_peek(7).obj)); }
break;
case 61:
//#line 266 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_EXTENDS, val_peek(1).obj)); }
break;
case 62:
//#line 268 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_LISTA_EXTENDS)); }
break;
case 63:
//#line 270 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_LISTA_EXTENDS)); }
break;
case 64:
//#line 275 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, val_peek(2).obj, val_peek(0).obj)); }
break;
case 65:
//#line 277 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, val_peek(0).obj)); }
break;
case 66:
//#line 285 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_TYPEDEF, val_peek(5).obj, val_peek(2).obj)); }
break;
case 67:
//#line 287 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_VALORES_ENUMERADO, val_peek(4).obj)); }
break;
case 68:
//#line 292 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 69:
//#line 294 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, val_peek(0).obj)); }
break;
case 70:
//#line 302 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 71:
//#line 304 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(0).obj)); }
break;
case 73:
//#line 310 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 78:
//#line 316 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_SENTENCIA)); }
break;
case 80:
//#line 322 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, val_peek(1).obj)); }
break;
case 81:
//#line 327 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 82:
//#line 329 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 83:
//#line 331 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_ASIGNACION_INVALIDA)); }
break;
case 85:
//#line 340 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 86:
//#line 342 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, val_peek(3).obj, val_peek(1).obj)); }
break;
case 87:
//#line 344 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
case 88:
//#line 346 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
break;
case 89:
//#line 348 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
break;
case 90:
//#line 350 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_INVALIDA)); }
break;
case 91:
//#line 357 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF, val_peek(3).obj, val_peek(1).obj)); }
break;
case 92:
//#line 359 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF_ELSE, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
case 93:
//#line 366 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONDICION_IF, val_peek(0).obj)); }
break;
case 94:
//#line 371 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_THEN, val_peek(0).obj)); }
break;
case 95:
//#line 376 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_ELSE, val_peek(0).obj)); }
break;
case 96:
//#line 381 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONDICION, val_peek(2).obj, val_peek(1).obj, val_peek(0).obj)); }
break;
case 97:
//#line 386 "gramatica.y"
{ yyval = new ParserVal((Object) ">="); }
break;
case 98:
//#line 388 "gramatica.y"
{ yyval = new ParserVal((Object) "<="); }
break;
case 99:
//#line 390 "gramatica.y"
{ yyval = new ParserVal((Object) "=="); }
break;
case 100:
//#line 392 "gramatica.y"
{ yyval = new ParserVal((Object) "!="); }
break;
case 101:
//#line 394 "gramatica.y"
{ yyval = new ParserVal((Object) ">"); }
break;
case 102:
//#line 396 "gramatica.y"
{ yyval = new ParserVal((Object) "<"); }
break;
case 103:
//#line 406 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INICIO_REPEAT)); }
break;
case 104:
//#line 411 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_REPEAT_WHILE, val_peek(5).obj, val_peek(2).obj)); }
break;
case 105:
//#line 413 "gramatica.y"
{ acciones.ejecutar(Reglas.CERRAR_REPEAT);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 106:
//#line 416 "gramatica.y"
{ acciones.ejecutar(Reglas.CERRAR_REPEAT);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_CUERPO_ITERACION, val_peek(2).obj)); }
break;
case 107:
//#line 419 "gramatica.y"
{ acciones.ejecutar(Reglas.CERRAR_REPEAT);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_WHILE, val_peek(2).obj)); }
break;
case 108:
//#line 422 "gramatica.y"
{ acciones.ejecutar(Reglas.CERRAR_REPEAT);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
break;
case 109:
//#line 425 "gramatica.y"
{ acciones.ejecutar(Reglas.CERRAR_REPEAT);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
break;
case 110:
//#line 434 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 111:
//#line 436 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 112:
//#line 438 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ARGUMENTO_POUT)); }
break;
case 113:
//#line 440 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 114:
//#line 442 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 115:
//#line 447 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, val_peek(2).obj)); }
break;
case 116:
//#line 449 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, val_peek(1).obj)); }
break;
case 117:
//#line 461 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SUMA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 118:
//#line 463 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.RESTA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 119:
//#line 465 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 120:
//#line 467 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 121:
//#line 469 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERADOR, val_peek(1).obj, val_peek(0).obj)); }
break;
case 123:
//#line 475 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.MULTIPLICACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 124:
//#line 477 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DIVISION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 125:
//#line 479 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 126:
//#line 481 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 128:
//#line 487 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_ID, val_peek(0).obj)); }
break;
case 129:
//#line 489 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE, val_peek(0).obj)); }
break;
case 135:
//#line 499 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE_NEGATIVA, val_peek(0).obj)); }
break;
case 136:
//#line 504 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
case 137:
//#line 506 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(
                Reglas.ERR_ASIG_DONDE_IGUAL)); }
break;
case 138:
//#line 512 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONVERSION_TODF, val_peek(1).obj)); }
break;
case 139:
//#line 517 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_FUNCION, val_peek(6).obj, val_peek(4).obj, val_peek(1).obj)); }
break;
case 140:
//#line 519 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(5).obj, val_peek(3).obj)); }
break;
case 141:
//#line 521 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(3).obj, val_peek(1).obj)); }
break;
case 142:
//#line 526 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 143:
//#line 528 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(0).obj)); }
break;
case 144:
//#line 533 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_REAL_NOMBRADO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 146:
//#line 539 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 147:
//#line 541 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(0).obj)); }
break;
case 148:
//#line 546 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 149:
//#line 548 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO_PREFIJADO, val_peek(4).obj, val_peek(2).obj, val_peek(0).obj)); }
break;
case 150:
//#line 550 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_METODO, val_peek(8).obj, val_peek(6).obj, val_peek(4).obj, val_peek(1).obj)); }
break;
case 151:
//#line 552 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(7).obj, val_peek(5).obj, val_peek(3).obj)); }
break;
case 152:
//#line 554 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
case 153:
//#line 556 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_METODO_PREFIJADO, val_peek(10).obj, val_peek(8).obj, val_peek(6).obj, val_peek(4).obj, val_peek(1).obj)); }
break;
case 154:
//#line 558 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(9).obj, val_peek(7).obj, val_peek(5).obj, val_peek(3).obj)); }
break;
case 155:
//#line 560 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(7).obj, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
//#line 1327 "Parser.java"
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
