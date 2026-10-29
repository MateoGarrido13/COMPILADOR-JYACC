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
   10,   10,   10,   10,   10,   10,    5,    5,   11,   17,
   17,   19,   19,   18,   18,   18,   20,   20,    6,   22,
   22,   22,   22,   22,   21,   21,   23,   23,   23,   23,
   24,    7,    7,   25,   25,   26,   26,   26,   27,   30,
   28,   29,   29,   29,   31,   31,    8,    8,   32,   32,
    9,    9,   34,   34,   34,   34,   34,   34,   34,   35,
   35,   12,   12,   12,   13,   13,   13,   13,   13,   13,
   13,   38,   38,   39,   40,   41,   42,   43,   43,   43,
   43,   43,   43,   44,   14,   14,   14,   14,   14,   14,
   15,   15,   15,   15,   15,   16,   16,   36,   36,   36,
   36,   36,   36,   45,   45,   45,   45,   45,   46,   46,
   46,   46,   46,   46,   33,   33,   48,   48,   49,   47,
   47,   47,   50,   50,   52,   52,   51,   51,   37,   37,
   37,   37,   37,   37,   37,   37,
};
final static short yylen[] = {                            2,
    3,    1,    3,    1,    1,    1,    1,    1,    3,    2,
    2,    3,    1,    2,    2,    0,    1,    1,    1,    1,
    1,    2,    1,    1,    1,    1,    3,    3,    3,    1,
    1,    1,    1,    3,    1,    2,    3,    2,    6,    4,
    3,    3,    4,    3,    3,    1,    2,    2,    1,    1,
    2,    5,    4,    2,    0,    1,    1,    1,    3,    2,
    8,    3,    2,    3,    3,    1,    7,    6,    3,    1,
    2,    1,    2,    1,    1,    1,    1,    1,    2,    1,
    3,    3,    3,    3,    2,    1,    6,    8,    6,    7,
    7,    6,    8,    1,    1,    1,    3,    1,    1,    1,
    1,    1,    1,    1,    7,    6,    6,    4,    6,    6,
    5,    5,    4,    4,    4,    5,    4,    3,    3,    3,
    3,    2,    1,    3,    3,    3,    3,    1,    1,    1,
    1,    1,    1,    1,    1,    2,    5,    5,    4,    7,
    6,    4,    3,    1,    3,    1,    3,    1,    3,    5,
    9,    8,    6,   11,   10,    8,
};
final static short yydefred[] = {                         0,
    0,    8,    0,   13,    0,    0,   30,   31,    0,    0,
    2,    0,    4,    5,    6,    7,    0,    0,    0,   14,
    0,    0,    0,   10,    0,    0,  104,    0,    0,   75,
   76,   77,   78,    0,    0,    0,    0,   51,    0,    0,
    0,   17,   19,   20,   21,    0,   18,    0,   23,   24,
   25,   26,    0,    0,    0,    0,    0,    0,    0,   12,
   79,    0,    0,    0,  135,    0,    0,    0,  130,    0,
  132,    0,    0,    0,  131,  133,  134,    0,    0,    9,
   73,    0,   71,    0,   85,    0,    0,   80,    0,    0,
    0,    1,   15,   22,    3,   28,    0,   36,   37,   27,
    0,    0,    0,    0,   33,    0,   32,    0,    0,    0,
   56,   57,   58,    0,   84,   82,    0,    0,    0,    0,
    0,    0,    0,    0,  136,   98,   99,  100,  101,  102,
  103,    0,    0,  122,    0,    0,    0,    0,    0,    0,
    0,    0,   83,    0,    0,    0,    0,    0,   29,   34,
   48,   47,    0,    0,    0,    0,   63,    0,    0,    0,
    0,   54,    0,    0,    0,    0,    0,  146,    0,    0,
    0,    0,    0,    0,    0,   97,    0,  120,  118,  121,
  119,  126,  124,  127,  125,    0,  113,    0,    0,   81,
    0,  108,    0,    0,    0,    0,    0,    0,   45,   64,
    0,   62,   59,   52,    0,    0,    0,    0,    0,    0,
    0,    0,  139,    0,    0,    0,    0,    0,  111,  112,
  116,    0,    0,    0,   68,    0,    0,    0,    0,   39,
   65,    0,    0,    0,  138,  145,    0,  143,  137,    0,
   87,   92,    0,    0,   89,  107,  110,    0,  109,   67,
   69,    0,    0,    0,    0,    0,    0,    0,    0,  141,
    0,   91,   96,    0,   90,  105,   44,    0,   41,    0,
    0,  152,    0,    0,    0,  140,   88,   93,   43,   40,
    0,  151,    0,  147,   61,  155,    0,  154,
};
final static short yydgoto[] = {                          9,
   10,   41,   11,   12,   42,   43,   44,   45,   28,   46,
   47,   29,   30,   31,   32,   33,   17,   98,  108,   18,
  103,  230,  104,   19,  109,  110,  111,  112,  113,  114,
  158,  196,   69,   88,   89,   70,   71,   36,  123,  216,
  264,   72,  132,   37,   73,   74,   75,   76,   77,  169,
  261,  170,
};
final static short yysindex[] = {                      -192,
 -346,    0, -199,    0, -216, -207,    0,    0,    0,  392,
    0,  392,    0,    0,    0,    0, -183,   66, -305,    0,
  -52,   -1,  -32,    0,   67,   71,    0, -292,   57,    0,
    0,    0,    0, -210, -181,   85,  302,    0,   87,   -2,
 -236,    0,    0,    0,    0,  392,    0,   90,    0,    0,
    0,    0, -236,   91,  -18, -147,   96, -185, -257,    0,
    0,  -42, -141,  -40,    0,  120,  144, -140,    0,   35,
    0,  123,  -11,   58,    0,    0,    0,  -16,   51,    0,
    0,  106,    0,   51,    0, -210,  126,    0, -233,   89,
  133,    0,    0,    0,    0,    0, -100,    0,    0,    0,
  -98,  -97,  187,  188,    0,  -48,    0,  -69, -170, -257,
    0,    0,    0,  195,    0,    0,   23,  197,  245,  198,
   51,  199,  201,  -17,    0,    0,    0,    0,    0,    0,
    0,   51,  326,    0,  251,  257,  266,  286,  202,  191,
  212,  213,    0, -149,   51,  203,  -21,  -43,    0,    0,
    0,    0,  392, -185,  208,  228,    0,  229,  -18,  235,
  244,    0, -185,  245,   13,   51,  -30,    0,  274,  275,
   51,  279,  326,  326,  326,    0,  -79,    0,    0,    0,
    0,    0,    0,    0,    0,  263,    0,  273,  276,    0,
  293,    0,   51,  295,  284,  253,  294, -228,    0,    0,
   50,    0,    0,    0,  314,  315,  317,  319,   14,  267,
  245,  320,    0,  -39, -229,  -37,  -36,  308,    0,    0,
    0,  309,   12,  312,    0,  313,  -33,  -31,  360,    0,
    0,  -29,  298,  245,    0,    0,  -89,    0,    0,  316,
    0,    0,  326,  325,    0,    0,    0,  332,    0,    0,
    0,  353,  -50,  369,  -28, -210,  -78,  389,  397,    0,
  364,    0,    0, -215,    0,    0,    0,  414,    0,  429,
   95,    0,  402,  409,  215,    0,    0,    0,    0,    0,
  444,    0,  -75,    0,    0,    0,  437,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0, -234,
    0, -234,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  532,  114,    0,
    0,    0,    0,    5,    0,    1,    0,    0,    0,    0,
    0,    0,    0,    0,    0, -234,    0,    0,    0,    0,
    0,    0,    0,    0,  477,  508,    0,    0,  148,    0,
    0,    0,    0,  265,    0,    0,    0,    0,    0,    0,
    0,    0,  318,  285,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   36,   77,    0,  513,    0,    0,    0,    0,    0,  148,
    0,    0,    0,    0,    0,    0,   81,    0,    0,    0,
    0,    0,    0,  515,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0, -205,    0,    0,  501,    0,    0,    8,    0,
 -221,    0,    0,    0,    0,    0,   -5,    0,    0,  521,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   17,    0,   33,   49,    0,
    0,    0,    0,    0,    0,    0,  471,    0,    0,    0,
    0,    0,    0,    0,    0,    0,   92,    0,    0,  484,
    0,    0,    0,    0,  172,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  204,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   65,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  482,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  344,
    0,    0,    0,  233,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
    0,   63,   46,    0,  576,  577,  578,  579,  -20,    0,
    0,   64,   73,  151,  466,  551,  491,   30,    0,    0,
  -72,    0,    0,    0,  470,    0,    0,    0,    0,    0,
  380,  356,  -80,   27,  -71,   10,  669,    0,    0,    0,
    0,  -15,    0,    0,  -82,    0,    0,    0,    0,    7,
 -197,    0,
};
final static int YYTABLESIZE=925;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                        119,
   86,   68,   68,  260,   72,   63,   61,   67,   61,  119,
  157,   68,   68,   83,  272,   63,  114,  286,  193,    1,
  120,   16,  146,   68,  140,   97,  241,  228,   68,   34,
  209,  135,  115,  136,   53,  129,  129,  129,  129,  129,
  277,  129,  105,   63,   63,   82,   57,   60,  117,  195,
   16,  124,  248,  171,  183,  185,   21,   20,   68,  273,
   34,  177,  164,    1,  106,  144,   35,  197,  165,   91,
  247,  116,   54,   48,   53,   48,   50,  284,   53,   50,
  149,  199,   49,   38,   49,  287,   92,  141,  142,   22,
  205,  150,   39,  143,  131,   68,  130,   59,   95,  137,
   22,  214,  215,  217,  138,   58,   78,    2,   93,   48,
   79,   80,   34,   74,  101,   81,   55,   49,   49,   84,
   49,  149,  149,  149,  149,  149,  150,  149,  168,  191,
  172,  194,  150,  150,  150,  150,  150,  160,  150,  149,
  149,  176,  149,   85,  179,  181,  197,   90,   94,   96,
  150,  150,   99,  150,  100,  106,    7,    8,  117,  121,
   50,  125,   50,  133,   61,  145,    3,    4,   16,   16,
  206,  263,  242,  168,  229,  208,  147,  223,   53,  148,
  212,   53,   53,   53,   53,   53,  278,   53,   68,   23,
   53,  149,   53,   53,   25,   26,   50,   16,   27,   55,
   23,  151,  152,  153,   24,   25,   26,  155,  255,   27,
    3,    4,  259,  115,    5,  198,   48,  238,  236,    6,
  168,    7,    8,  259,   56,   49,  259,  153,    7,    8,
  159,  154,  156,  161,  163,  271,  166,  171,  175,  173,
  258,  174,  186,  168,  153,  153,  153,  153,  153,  187,
  153,  156,  188,  189,  190,   34,   86,   64,   65,   65,
  118,  192,  153,  153,  129,  153,  200,   64,   65,   65,
  118,  201,  114,  156,  156,  156,  156,  156,   64,  156,
   65,   55,   34,   64,  128,   65,  139,  202,  115,   68,
  134,  156,  156,  203,  156,   68,  129,   55,   62,   62,
   86,   68,  204,   50,  117,  129,  129,  129,  129,  129,
   68,  129,  207,   64,  210,   65,  114,  123,  211,  213,
  106,  219,  218,  129,  129,  128,  129,  128,  128,  128,
   68,  220,  115,  222,  221,  224,  149,  227,  126,  127,
  128,  129,  225,  128,  128,  226,  128,  150,  117,  156,
   64,   60,   65,  268,  232,  233,  234,  237,  123,  235,
  239,  123,  240,  243,  106,  244,  245,  246,   66,   74,
  249,  250,  252,  256,  262,  270,  123,  123,   66,  123,
  149,  149,  149,  265,  149,  149,  149,  149,  257,   66,
  266,  150,  150,  150,   66,  150,  150,  150,  150,  122,
   86,   86,   86,   86,   86,   86,   86,   86,   72,   86,
   86,  267,   86,   74,   86,   86,  114,  114,  114,  114,
  114,  114,  114,  114,   66,  114,  114,  269,  114,  274,
  114,  114,  115,  115,  115,  115,  115,  115,  115,  115,
  275,  115,  115,   64,  115,   65,  115,  115,  117,  117,
  117,  117,  117,  117,  117,  117,  276,  117,  117,  153,
  117,   66,  117,  117,  106,  106,  106,  106,  106,  106,
  106,  106,  279,  106,  106,   51,  106,   51,  106,  106,
  149,  149,  149,  142,  149,  149,  149,  280,  156,  149,
  149,  150,  150,  150,  282,  150,  150,  150,  281,  283,
  150,  150,  285,  153,  153,  153,  178,  153,  153,  153,
  153,   51,  180,   74,   74,   74,  259,   74,   74,   74,
  129,  182,   74,   74,  142,  142,  142,  142,  142,  288,
  142,   11,  156,  156,  156,   35,  156,  156,  156,  156,
  128,  184,  142,  142,  167,  142,   65,   38,  102,  107,
   64,   55,   65,   46,   66,   94,   64,   82,   65,   66,
   52,  144,   52,   70,  129,   64,  129,   65,  129,  129,
  129,  129,   95,  123,  148,   13,   14,   15,   16,  162,
  231,   82,  251,    0,  128,   64,  128,   65,  128,  128,
  128,  128,    0,    0,    0,    0,   52,    0,    0,   42,
  107,   22,    0,  153,  153,  153,    0,  153,  153,  153,
    0,    0,  153,  153,    0,  253,    0,  123,   51,    0,
    0,  123,  123,  123,  123,   22,    0,    0,    0,    0,
    0,    0,  156,  156,  156,    0,  156,  156,  156,    0,
    0,  156,  156,   42,  102,    0,    0,    0,    0,    0,
    0,    0,    0,  102,    0,   66,    0,    0,    0,   22,
    0,   66,    0,    0,  129,  129,  129,   66,  129,  129,
  129,   35,    0,  129,  129,    0,   66,    0,   35,    0,
   35,    0,    0,    0,  128,  128,  128,    0,  128,  128,
  128,   40,    0,  128,  128,    0,   66,    0,    0,    0,
    0,   23,   35,   52,   86,   35,   25,   26,    0,    0,
   27,   87,    0,    0,   35,    0,    0,  123,  123,  123,
    0,  123,  123,  123,    0,   23,  123,  123,   86,    0,
   25,   26,    0,    0,   27,    0,    0,    0,    0,  142,
    0,    0,    0,   42,    0,    0,   42,   42,   42,   42,
   42,    0,   42,    0,   35,   42,    0,   42,   42,   23,
    0,    0,    0,  254,   25,   26,    0,    0,   27,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  142,    0,  142,    0,  142,  142,  142,
  142,   23,    0,    0,    0,    0,   25,   26,    5,    0,
   27,   35,    0,    6,    0,    7,    8,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   35,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   35,   35,   35,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  142,  142,  142,    0,  142,  142,  142,
    0,    0,  142,  142,    0,    0,    0,   35,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   35,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   35,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         40,
    0,   45,   45,   93,    0,   46,   59,   40,   59,   40,
   59,   45,   45,   34,   93,   46,    0,   93,   40,  256,
   61,  256,  256,   45,   41,   44,  256,  256,   45,    3,
   61,   43,    0,   45,  256,   41,   42,   43,   44,   45,
  256,   47,  300,   46,   46,  256,   17,   40,    0,   93,
  256,   67,   41,   40,  137,  138,  256,  404,   45,  257,
   34,  133,   40,  256,    0,   86,   59,  148,   46,   40,
   59,   62,  256,   10,   12,   12,   41,  275,  300,   44,
    0,  154,   10,  300,   12,  283,   41,   78,   79,  300,
  163,    0,  300,   84,   60,   45,   62,  403,   53,   42,
  300,  173,  174,  175,   47,   40,   40,  300,   46,   46,
   40,  404,   86,    0,  300,   59,  300,   41,   46,  301,
   44,   41,   42,   43,   44,   45,   97,   47,  119,  145,
  121,  147,   41,   42,   43,   44,   45,  108,   47,   59,
   60,  132,   62,   59,  135,  136,  227,   61,   59,   59,
   59,   60,  300,   62,   59,  413,  414,  415,  300,   40,
   10,  302,   12,   41,   59,   40,  403,  404,  403,  404,
  164,  243,  402,  164,  403,  166,  410,  193,  400,   91,
  171,  403,  404,  405,  406,  407,  402,  409,   45,  400,
  412,   59,  414,  415,  405,  406,   46,  403,  409,  300,
  400,  300,  300,    0,  404,  405,  406,  256,  229,  409,
  403,  404,  302,  256,  407,  153,  153,  211,  209,  412,
  211,  414,  415,  302,  408,  153,  302,   41,  414,  415,
  300,   44,    0,  404,   40,  256,   40,   40,  256,   41,
  234,   41,   41,  234,   41,   42,   43,   44,   45,   59,
   47,  300,   41,   41,  404,  229,  256,  300,  302,  302,
  301,   59,   59,   60,    0,   62,   59,  300,  302,  302,
  301,   44,  256,   41,   42,   43,   44,   45,  300,   47,
  302,  300,  256,  300,    0,  302,  303,   59,  256,   45,
  302,   59,   60,   59,   62,   45,  302,  300,  301,  301,
  300,   45,   59,  153,  256,   41,   42,   43,   44,   45,
   45,   47,  300,  300,   41,  302,  300,    0,   44,   41,
  256,   59,  402,   59,   60,   41,   62,   43,   44,   45,
   45,   59,  300,   41,   59,   41,  256,   44,  304,  305,
  306,  307,   59,   59,   60,   93,   62,  256,  300,  300,
  300,  404,  302,  404,   41,   41,   40,   91,   41,   41,
   41,   44,  402,  401,  300,  402,   59,   59,  411,  256,
   59,   59,  404,  403,   59,  404,   59,   60,  411,   62,
  300,  301,  302,   59,  304,  305,  306,  307,   91,  411,
   59,  300,  301,  302,  411,  304,  305,  306,  307,  256,
  400,  401,  402,  403,  404,  405,  406,  407,  404,  409,
  410,   59,  412,  300,  414,  415,  400,  401,  402,  403,
  404,  405,  406,  407,  411,  409,  410,   59,  412,   41,
  414,  415,  400,  401,  402,  403,  404,  405,  406,  407,
   44,  409,  410,  300,  412,  302,  414,  415,  400,  401,
  402,  403,  404,  405,  406,  407,   93,  409,  410,  256,
  412,  411,  414,  415,  400,  401,  402,  403,  404,  405,
  406,  407,   59,  409,  410,   10,  412,   12,  414,  415,
  400,  401,  402,    0,  404,  405,  406,   59,  256,  409,
  410,  400,  401,  402,   93,  404,  405,  406,  404,   91,
  409,  410,   59,  300,  301,  302,  256,  304,  305,  306,
  307,   46,  256,  400,  401,  402,  302,  404,  405,  406,
  256,  256,  409,  410,   41,   42,   43,   44,   45,   93,
   47,    0,  300,  301,  302,   59,  304,  305,  306,  307,
  256,  256,   59,   60,  300,   62,  302,   40,   58,   59,
  300,  404,  302,   41,  411,   41,  300,  256,  302,   59,
   10,   41,   12,   93,  300,  300,  302,  302,  304,  305,
  306,  307,  401,  256,   93,    0,    0,    0,    0,  110,
  201,  256,  227,   -1,  300,  300,  302,  302,  304,  305,
  306,  307,   -1,   -1,   -1,   -1,   46,   -1,   -1,  256,
  110,  300,   -1,  400,  401,  402,   -1,  404,  405,  406,
   -1,   -1,  409,  410,   -1,  256,   -1,  300,  153,   -1,
   -1,  304,  305,  306,  307,  300,   -1,   -1,   -1,   -1,
   -1,   -1,  400,  401,  402,   -1,  404,  405,  406,   -1,
   -1,  409,  410,  300,  154,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  163,   -1,  411,   -1,   -1,   -1,  300,
   -1,  411,   -1,   -1,  400,  401,  402,  411,  404,  405,
  406,    3,   -1,  409,  410,   -1,  411,   -1,   10,   -1,
   12,   -1,   -1,   -1,  400,  401,  402,   -1,  404,  405,
  406,  300,   -1,  409,  410,   -1,  411,   -1,   -1,   -1,
   -1,  400,   34,  153,  403,   37,  405,  406,   -1,   -1,
  409,  410,   -1,   -1,   46,   -1,   -1,  400,  401,  402,
   -1,  404,  405,  406,   -1,  400,  409,  410,  403,   -1,
  405,  406,   -1,   -1,  409,   -1,   -1,   -1,   -1,  256,
   -1,   -1,   -1,  400,   -1,   -1,  403,  404,  405,  406,
  407,   -1,  409,   -1,   86,  412,   -1,  414,  415,  400,
   -1,   -1,   -1,  404,  405,  406,   -1,   -1,  409,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  300,   -1,  302,   -1,  304,  305,  306,
  307,  400,   -1,   -1,   -1,   -1,  405,  406,  407,   -1,
  409,  133,   -1,  412,   -1,  414,  415,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  153,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  173,  174,  175,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  400,  401,  402,   -1,  404,  405,  406,
   -1,   -1,  409,  410,   -1,   -1,   -1,  229,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  243,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  256,
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
"sentencia_declarativa : asignacion ';'",
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

//#line 565 "gramatica.y"


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
//#line 676 "Parser.java"
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
case 27:
//#line 135 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_VARIABLES, val_peek(2).obj, val_peek(1).obj)); }
break;
case 28:
//#line 137 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_LISTA_VARIABLES)); }
break;
case 29:
//#line 142 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_OBJETOS, val_peek(2).obj, val_peek(1).obj)); }
break;
case 30:
//#line 147 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.ULONGINT)); }
break;
case 31:
//#line 149 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.DOUBLEF)); }
break;
case 33:
//#line 155 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, val_peek(0).obj)); }
break;
case 34:
//#line 160 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 35:
//#line 162 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(0).obj)); }
break;
case 36:
//#line 164 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_COMA_VARIABLES);
          yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(1).obj, val_peek(0).obj)); }
break;
case 37:
//#line 173 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(2).obj, val_peek(0).obj); }
break;
case 38:
//#line 175 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_FUNCION);
          acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(1).obj, null); }
break;
case 39:
//#line 181 "gramatica.y"
{ yyval = val_peek(0); }
break;
case 40:
//#line 186 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 41:
//#line 188 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 42:
//#line 190 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 43:
//#line 193 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_BLOQUE_SIN_END);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 44:
//#line 196 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_BEGIN);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 45:
//#line 202 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 46:
//#line 204 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, val_peek(0).obj)); }
break;
case 47:
//#line 209 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, val_peek(1).obj, val_peek(0).obj)); }
break;
case 48:
//#line 211 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, val_peek(1).obj, val_peek(0).obj)); }
break;
case 49:
//#line 213 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_PARAMETRO, val_peek(0).obj)); }
break;
case 50:
//#line 215 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_TIPO_PARAMETRO, val_peek(0).obj)); }
break;
case 51:
//#line 224 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_CLASE, val_peek(0).obj);
          yyval = val_peek(0); }
break;
case 52:
//#line 230 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, val_peek(4).obj)); }
break;
case 53:
//#line 232 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, val_peek(3).obj)); }
break;
case 54:
//#line 238 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 55:
//#line 240 "gramatica.y"
{ yyval = new ParserVal((Object) null); }
break;
case 59:
//#line 251 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_ATRIBUTO, val_peek(2).obj, val_peek(1).obj)); }
break;
case 60:
//#line 256 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(1).obj, val_peek(0).obj);
          yyval = val_peek(0); }
break;
case 61:
//#line 262 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_METODO, val_peek(7).obj)); }
break;
case 62:
//#line 267 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_EXTENDS, val_peek(1).obj)); }
break;
case 63:
//#line 269 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_LISTA_EXTENDS)); }
break;
case 64:
//#line 271 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_LISTA_EXTENDS)); }
break;
case 65:
//#line 276 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, val_peek(2).obj, val_peek(0).obj)); }
break;
case 66:
//#line 278 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, val_peek(0).obj)); }
break;
case 67:
//#line 286 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_TYPEDEF, val_peek(5).obj, val_peek(2).obj)); }
break;
case 68:
//#line 288 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_VALORES_ENUMERADO, val_peek(4).obj)); }
break;
case 69:
//#line 293 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 70:
//#line 295 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, val_peek(0).obj)); }
break;
case 71:
//#line 303 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 72:
//#line 305 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(0).obj)); }
break;
case 74:
//#line 311 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 79:
//#line 317 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_SENTENCIA)); }
break;
case 81:
//#line 323 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, val_peek(1).obj)); }
break;
case 82:
//#line 328 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 83:
//#line 330 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 84:
//#line 332 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_ASIGNACION_INVALIDA)); }
break;
case 86:
//#line 341 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 87:
//#line 343 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, val_peek(3).obj, val_peek(1).obj)); }
break;
case 88:
//#line 345 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
case 89:
//#line 347 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
break;
case 90:
//#line 349 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
break;
case 91:
//#line 351 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_INVALIDA)); }
break;
case 92:
//#line 358 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF, val_peek(3).obj, val_peek(1).obj)); }
break;
case 93:
//#line 360 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF_ELSE, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
case 94:
//#line 367 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONDICION_IF, val_peek(0).obj)); }
break;
case 95:
//#line 372 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_THEN, val_peek(0).obj)); }
break;
case 96:
//#line 377 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_ELSE, val_peek(0).obj)); }
break;
case 97:
//#line 382 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONDICION, val_peek(2).obj, val_peek(1).obj, val_peek(0).obj)); }
break;
case 98:
//#line 387 "gramatica.y"
{ yyval = new ParserVal((Object) ">="); }
break;
case 99:
//#line 389 "gramatica.y"
{ yyval = new ParserVal((Object) "<="); }
break;
case 100:
//#line 391 "gramatica.y"
{ yyval = new ParserVal((Object) "=="); }
break;
case 101:
//#line 393 "gramatica.y"
{ yyval = new ParserVal((Object) "!="); }
break;
case 102:
//#line 395 "gramatica.y"
{ yyval = new ParserVal((Object) ">"); }
break;
case 103:
//#line 397 "gramatica.y"
{ yyval = new ParserVal((Object) "<"); }
break;
case 104:
//#line 407 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INICIO_REPEAT)); }
break;
case 105:
//#line 412 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_REPEAT_WHILE, val_peek(5).obj, val_peek(2).obj)); }
break;
case 106:
//#line 414 "gramatica.y"
{ acciones.ejecutar(Reglas.CERRAR_REPEAT);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 107:
//#line 417 "gramatica.y"
{ acciones.ejecutar(Reglas.CERRAR_REPEAT);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_CUERPO_ITERACION, val_peek(2).obj)); }
break;
case 108:
//#line 420 "gramatica.y"
{ acciones.ejecutar(Reglas.CERRAR_REPEAT);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_WHILE, val_peek(2).obj)); }
break;
case 109:
//#line 423 "gramatica.y"
{ acciones.ejecutar(Reglas.CERRAR_REPEAT);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
break;
case 110:
//#line 426 "gramatica.y"
{ acciones.ejecutar(Reglas.CERRAR_REPEAT);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
break;
case 111:
//#line 435 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 112:
//#line 437 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 113:
//#line 439 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ARGUMENTO_POUT)); }
break;
case 114:
//#line 441 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 115:
//#line 443 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 116:
//#line 448 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, val_peek(2).obj)); }
break;
case 117:
//#line 450 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, val_peek(1).obj)); }
break;
case 118:
//#line 462 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SUMA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 119:
//#line 464 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.RESTA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 120:
//#line 466 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 121:
//#line 468 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 122:
//#line 470 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERADOR, val_peek(1).obj, val_peek(0).obj)); }
break;
case 124:
//#line 476 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.MULTIPLICACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 125:
//#line 478 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DIVISION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 126:
//#line 480 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 127:
//#line 482 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 129:
//#line 488 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_ID, val_peek(0).obj)); }
break;
case 130:
//#line 490 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE, val_peek(0).obj)); }
break;
case 136:
//#line 500 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE_NEGATIVA, val_peek(0).obj)); }
break;
case 137:
//#line 505 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
case 138:
//#line 507 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_ASIG_DONDE_IGUAL);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
case 139:
//#line 513 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONVERSION_TODF, val_peek(1).obj)); }
break;
case 140:
//#line 518 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_FUNCION, val_peek(6).obj, val_peek(4).obj, val_peek(1).obj)); }
break;
case 141:
//#line 520 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(5).obj, val_peek(3).obj)); }
break;
case 142:
//#line 522 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(3).obj, val_peek(1).obj)); }
break;
case 143:
//#line 527 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 144:
//#line 529 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(0).obj)); }
break;
case 145:
//#line 534 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_REAL_NOMBRADO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 147:
//#line 540 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 148:
//#line 542 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(0).obj)); }
break;
case 149:
//#line 547 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 150:
//#line 549 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO_PREFIJADO, val_peek(4).obj, val_peek(2).obj, val_peek(0).obj)); }
break;
case 151:
//#line 551 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_METODO, val_peek(8).obj, val_peek(6).obj, val_peek(4).obj, val_peek(1).obj)); }
break;
case 152:
//#line 553 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(7).obj, val_peek(5).obj, val_peek(3).obj)); }
break;
case 153:
//#line 555 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
case 154:
//#line 557 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_METODO_PREFIJADO, val_peek(10).obj, val_peek(8).obj, val_peek(6).obj, val_peek(4).obj, val_peek(1).obj)); }
break;
case 155:
//#line 559 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(9).obj, val_peek(7).obj, val_peek(5).obj, val_peek(3).obj)); }
break;
case 156:
//#line 561 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(7).obj, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
//#line 1334 "Parser.java"
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
