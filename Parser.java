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
  valstk=new ParserVal[YYSTACKSIZE];
  yyval=new ParserVal();
  yylval=new ParserVal();
  valptr=-1;
}
void val_push(ParserVal val)
{
  if (valptr>=YYSTACKSIZE)
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
    0,    0,    0,    4,    4,    4,    4,    1,    3,    3,
    3,    3,    3,    3,    2,    2,   10,   10,   10,   10,
   10,   10,   10,   10,   10,   10,    5,    5,   11,   17,
   17,   19,   19,   18,   18,   18,   20,   20,    6,   22,
   22,   22,   22,   22,   21,   21,   23,   23,   23,   23,
   24,    7,    7,   25,   25,   26,   26,   26,   27,   30,
   28,   29,   29,   29,   31,   31,    8,    8,   32,   32,
    9,    9,   34,   34,   34,   34,   34,   34,   34,   35,
   35,   12,   12,   12,   13,   13,   13,   13,   13,   13,
   13,   13,   13,   38,   39,   39,   39,   39,   39,   39,
   14,   14,   14,   14,   14,   14,   15,   15,   15,   15,
   15,   16,   16,   36,   36,   36,   36,   36,   36,   40,
   40,   40,   40,   40,   41,   41,   41,   41,   41,   41,
   33,   33,   43,   43,   44,   42,   42,   42,   45,   45,
   47,   47,   46,   46,   37,   37,   37,   37,   37,   37,
   37,   37,
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
    3,    3,    3,    3,    7,    9,    6,    8,    6,    8,
    6,    7,    7,    3,    1,    1,    1,    1,    1,    1,
    7,    6,    6,    4,    6,    6,    5,    5,    4,    4,
    4,    5,    4,    3,    3,    3,    3,    2,    1,    3,
    3,    3,    3,    1,    1,    1,    1,    1,    1,    1,
    1,    2,    5,    5,    4,    7,    6,    4,    3,    1,
    3,    1,    3,    1,    3,    5,    9,    8,    6,   11,
   10,    8,
};
final static short yydefred[] = {                         0,
    0,    8,    0,   13,    0,    0,   30,   31,    0,    0,
    2,    0,    4,    5,    6,    7,    0,    0,    0,   14,
    0,    0,    0,   10,    0,    0,    0,    0,    0,   75,
   76,   77,   78,    0,    0,   51,    0,    0,    0,   17,
   19,   20,   21,    0,   18,    0,   23,   24,   25,   26,
    0,    0,    0,    0,    0,    0,    0,   12,   79,    0,
    0,    0,  131,    0,    0,    0,  126,    0,  128,    0,
    0,    0,  127,  129,  130,    0,    0,    0,    0,    0,
   80,    0,    9,   73,   71,    0,    0,    0,    1,   15,
   22,    3,   28,    0,   36,   37,   27,    0,    0,    0,
    0,   33,    0,   32,    0,    0,    0,   56,   57,   58,
    0,   84,   82,    0,    0,    0,    0,    0,    0,    0,
  132,   95,   96,   97,   98,   99,  100,    0,    0,  118,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   83,    0,   29,   34,   48,   47,    0,    0,
    0,    0,   63,    0,    0,    0,    0,   54,    0,    0,
    0,    0,    0,  142,    0,    0,    0,    0,    0,    0,
    0,   94,    0,  116,  114,  117,  115,  122,  120,  123,
  121,    0,  109,    0,    0,   81,    0,  104,    0,    0,
    0,    0,    0,    0,   45,   64,    0,   62,   59,   52,
    0,    0,    0,    0,    0,    0,    0,    0,  135,    0,
    0,    0,    0,  107,  108,  112,    0,    0,    0,   68,
    0,    0,    0,    0,   39,   65,    0,    0,    0,  134,
  141,    0,  139,  133,    0,    0,   89,    0,    0,   91,
  103,  106,    0,  105,   67,   69,    0,    0,    0,    0,
    0,    0,    0,    0,  137,    0,   93,   92,    0,   85,
  101,   44,    0,   41,    0,    0,  148,    0,    0,    0,
  136,   90,    0,   43,   40,    0,  147,    0,  143,   86,
   61,  151,    0,  150,
};
final static short yydgoto[] = {                          9,
   10,   39,   11,   12,   40,   41,   42,   43,   28,   44,
   45,   29,   30,   31,   32,   33,   17,   95,  105,   18,
  100,  225,  101,   19,  106,  107,  108,  109,  110,  111,
  154,  192,   67,   81,   82,   68,   69,   70,  128,   71,
   72,   73,   74,   75,  165,  256,  166,
};
final static short yysindex[] = {                      -216,
 -350,    0,  -50,    0, -212, -196,    0,    0,    0,  324,
    0,  324,    0,    0,    0,    0, -226,   54, -297,    0,
  -59,   10,  -38,    0,   68,   89,  280, -281,   72,    0,
    0,    0,    0,  -48, -164,    0,   85,  -42, -237,    0,
    0,    0,    0,  324,    0,   91,    0,    0,    0,    0,
 -237,   96,   -1, -156,   97, -256, -273,    0,    0,  -17,
 -146,  -14,    0,  108,  -16, -145,    0,   37,    0,  120,
  -35,   44,    0,    0,    0,  -21,   30,  103,  -48,  123,
    0, -233,    0,    0,    0,   30,   73,  106,    0,    0,
    0,    0,    0, -132,    0,    0,    0, -130, -129,  139,
  142,    0,  -47,    0, -105, -192, -273,    0,    0,    0,
  165,    0,    0,   43,  173,   51,  176,   30,  177,  -28,
    0,    0,    0,    0,    0,    0,    0,   30,  293,    0,
   14,   62,   67,   83,  178,  158,  179,  180, -182,   30,
  164,  -34,    0,  -11,    0,    0,    0,    0,  324, -256,
  167,  186,    0,  175,   -1,  182,  188,    0, -256,   51,
  -68,   30,    2,    0,  194,  192,   30,  196,  293,  293,
  293,    0, -153,    0,    0,    0,    0,    0,    0,    0,
    0,  195,    0,  197,  235,    0,  210,    0,   30,  214,
  238,  205,  256, -206,    0,    0,    4,    0,    0,    0,
  261,  272,  279,  281,  -31,  229,   51,  285,    0,  -74,
  -73, -199,  275,    0,    0,    0,  276,   36,  277,    0,
  287,  -30,  -66,  312,    0,    0,  -72,  249,   51,    0,
    0,  -77,    0,    0,  288,  301,    0,  293,  304,    0,
    0,    0,  307,    0,    0,    0,  309,  -56,  313,  -33,
  -48,  -75,  333,  331,    0,  283,    0,    0, -198,    0,
    0,    0,  319,    0,  320,  -22,    0,  291,  295,   86,
    0,    0,  328,    0,    0,  330,    0,  -71,    0,    0,
    0,    0,  298,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0, -210,
    0, -210,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  392,  114,    0,
    0,    0,    0,    5,    0,    0,    0,    0,    0,    0,
    0,    0,    0, -210,    0,    0,    0,    0,    0,    0,
    0,    0,  334,  356,    0,    0,   -7,    0,    0,    0,
    0,  532,    0,    0,    0,    0,    0,    0,    0,    0,
  617,  554,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   94,   95,    0,
  357,    0,    0,    0,    0,    0,   -7,    0,    0,    0,
    0,    0,    0,  201,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0, -203,    0,
    0,  340,    0,    0,   20,    0, -231,    0,    0,    0,
    0,    0,   -6,    0,    0,  359,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    1,    0,   17,   33,    0,    0,    0,    0,    0,
    0,    0,  335,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  233,    0,    0,  585,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  265,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   49,    0,
    0,    0,   65,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  337,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  248,    0,    0,    0,  500,    0,
    0,    0,   81,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
    0,   61,   76,    0,  412,  444,  446,  460,  -13,    0,
    0,   58,   66,   75,   80,  141,  584,   55,    0,    0,
  -25,    0,    0,    0,  369,    0,    0,    0,    0,    0,
  300,  270, -113,   64,  -49,  -15,  701,  -10,    0,  -82,
    0,    0,    0,    0,  -96, -152,    0,
};
final static int YYTABLESIZE=1027;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         59,
  110,   65,   59,   61,   72,  189,   66,  131,  167,  132,
   66,  153,  171,   66,   66,  255,  111,  267,    1,  136,
   85,  282,  141,   66,   53,  116,  102,   66,   66,   52,
  193,   61,  113,   66,  125,  125,  125,  125,  125,    1,
  125,  116,   94,   98,  113,   16,  117,   61,   87,  223,
  179,  181,   16,   20,  120,   61,  237,  272,   66,   60,
  137,  138,  205,  202,  102,  139,   34,   46,   53,   46,
  143,   55,   51,   53,   66,   47,  243,   47,   35,  173,
   88,  191,  160,    2,   48,  133,   48,   36,  161,   49,
  134,   49,   88,   56,  242,   66,  127,   34,  126,  268,
  164,   46,  168,   37,   90,   57,   66,   76,  193,   47,
  233,   66,  172,   74,   89,  175,  177,  279,   48,  210,
  211,  212,   83,   49,  195,  283,   92,   66,   77,  187,
   84,  190,  253,  201,   50,   49,   86,   50,   49,  103,
    7,    8,   34,   96,  164,   87,  204,  118,  146,   91,
   50,  208,   50,  114,   93,   97,  121,    7,    8,  156,
  129,   59,  140,  144,  145,    3,    4,   53,   53,  147,
  148,   53,   53,   53,   53,   53,  142,   53,  218,  149,
   53,   54,   53,   53,   50,  150,    3,    4,  259,  231,
    5,  164,   16,   16,  155,    6,  224,    7,    8,   16,
  145,  238,  239,  273,  159,   21,   46,   78,  151,  194,
  250,  157,  162,  164,   47,  167,  183,  169,  182,  184,
  185,  186,  188,   48,  254,  196,  254,  170,   49,  197,
  254,  203,  146,  198,  206,  207,  209,  266,  112,  119,
  199,  145,  145,  145,  145,  145,  200,  145,  213,   22,
  217,   22,  152,  214,  219,  215,  110,   53,   60,  145,
  145,   62,  145,   63,  149,   62,  130,   63,   62,  174,
   63,   63,  111,  146,  146,  146,  146,  146,   62,  146,
   63,  135,   62,   62,   63,   63,  115,   34,  113,   50,
   63,  146,  146,  216,  146,  125,  220,  221,   53,  222,
  110,  227,  115,  152,   87,  149,  149,  149,  149,  149,
   60,  149,  228,   62,   34,   63,  111,  176,  229,  232,
  102,  230,  178,  149,  149,  234,  149,  235,  236,   62,
  251,   63,  113,  240,  241,  244,   88,  247,  180,  252,
  122,  123,  124,  125,   58,  245,  257,  263,   87,   23,
  163,   23,   63,   24,   25,   26,   25,   26,   27,  258,
   27,   62,  260,   63,  102,  261,   62,  262,   63,   74,
  265,  264,   64,  269,  270,  271,   64,  274,  275,   64,
   88,  276,   62,  277,   63,  278,  280,  254,  281,   64,
  284,   11,   35,   64,   64,   38,   55,   46,   66,  140,
  110,  110,  110,  110,  110,  110,  110,  110,   72,  110,
  110,   13,  110,   74,  110,  110,  111,  111,  111,  111,
  111,  111,  111,  111,   64,  111,  111,   70,  111,  144,
  111,  111,  113,  113,  113,  113,  113,  113,  113,  113,
   64,  113,  113,   14,  113,   15,  113,  113,   87,   87,
   87,   87,   87,   87,   87,   87,  145,   87,   87,   16,
   87,   64,   87,   87,  102,  102,  102,  102,  102,  102,
  102,  102,   64,  102,  102,  158,  102,   64,  102,  102,
   88,   88,   88,   88,   88,   88,   88,   88,  146,   88,
   88,  246,   88,   64,   88,   88,  226,    0,    0,  152,
  145,  145,  145,   42,  145,  145,  145,  145,    0,    0,
    0,    0,    0,   74,   74,   74,    0,   74,   74,   74,
  149,    0,   74,   74,    0,    0,    0,    0,    0,    0,
    0,  125,  146,  146,  146,   78,  146,  146,  146,  146,
  152,  152,  152,  152,  152,    0,  152,   42,   78,    0,
    0,    0,    0,  124,    0,    0,    0,    0,  152,  152,
    0,  152,    0,    0,  149,  149,  149,  248,  149,  149,
  149,  149,  125,  125,  125,  125,  125,    0,  125,   22,
    0,    0,    0,    0,  138,    0,    0,    0,    0,    0,
  125,  125,   22,  125,  124,    0,  124,  124,  124,    0,
  145,  145,  145,    0,  145,  145,  145,    0,    0,  145,
  145,   22,  124,  124,    0,  124,  119,    0,    0,    0,
    0,    0,    0,   38,    0,  138,  138,  138,  138,  138,
    0,  138,  146,  146,  146,    0,  146,  146,  146,   99,
  104,  146,  146,  138,  138,    0,  138,   42,    0,    0,
   42,   42,   42,   42,   42,    0,   42,  119,    0,   42,
  119,   42,   42,    0,  149,  149,  149,    0,  149,  149,
  149,    0,    0,  149,  149,  119,  119,    0,  119,   23,
    0,    0,   79,    0,   25,   26,    0,    0,   27,   80,
  104,    0,   23,    0,    0,   79,    0,   25,   26,    0,
    0,   27,    0,   35,    0,    0,    0,    0,    0,    0,
   35,   23,   35,    0,    0,  249,   25,   26,    0,    0,
   27,    0,    0,   23,    0,    0,    0,   35,   25,   26,
    5,    0,   27,   99,   35,    6,    0,    7,    8,    0,
    0,    0,   99,    0,   35,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  152,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   35,
    0,    0,    0,    0,    0,    0,    0,  125,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  152,
  152,  152,    0,  152,  152,  152,  152,    0,    0,  124,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   35,
    0,  125,    0,  125,    0,  125,  125,  125,  125,    0,
  138,    0,    0,    0,    0,    0,    0,    0,    0,   35,
    0,    0,    0,  124,    0,  124,    0,  124,  124,  124,
  124,    0,    0,    0,    0,    0,    0,    0,    0,   35,
   35,   35,  119,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  138,    0,  138,    0,  138,  138,
  138,  138,    0,    0,    0,    0,    0,    0,    0,  152,
  152,  152,    0,  152,  152,  152,    0,    0,  152,  152,
    0,    0,    0,    0,    0,    0,  119,    0,    0,    0,
  119,  119,  119,  119,   35,    0,    0,    0,    0,    0,
    0,  125,  125,  125,    0,  125,  125,  125,   35,    0,
  125,  125,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   35,    0,  124,  124,  124,    0,  124,  124,  124,
    0,    0,  124,  124,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  138,  138,  138,    0,  138,  138,
  138,    0,    0,  138,  138,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  119,  119,  119,    0,
  119,  119,  119,    0,    0,  119,  119,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         59,
    0,   40,   59,   46,    0,   40,   45,   43,   40,   45,
   45,   59,   41,   45,   45,   93,    0,   93,  256,   41,
   34,   93,  256,   45,  256,   40,  300,   45,   45,  256,
  144,   46,    0,   45,   41,   42,   43,   44,   45,  256,
   47,   40,   44,  300,   60,  256,   61,   46,    0,  256,
  133,  134,  256,  404,   65,   46,  256,  256,   45,   40,
   76,   77,   61,  160,    0,   79,    3,   10,  300,   12,
   86,   17,   12,  300,   45,   10,   41,   12,   59,  129,
    0,   93,   40,  300,   10,   42,   12,  300,   46,   10,
   47,   12,   38,   40,   59,   45,   60,   34,   62,  252,
  116,   44,  118,  300,   44,  403,   45,   40,  222,   44,
  207,   45,  128,    0,   39,  131,  132,  270,   44,  169,
  170,  171,  404,   44,  150,  278,   51,   45,   40,  140,
   59,  142,  229,  159,   41,   41,  301,   44,   44,  413,
  414,  415,   79,  300,  160,   61,  162,   40,   94,   59,
   10,  167,   12,  300,   59,   59,  302,  414,  415,  105,
   41,   59,   40,   91,   59,  403,  404,  300,  400,  300,
  300,  403,  404,  405,  406,  407,  410,  409,  189,   41,
  412,  408,  414,  415,   44,   44,  403,  404,  238,  205,
  407,  207,  403,  404,  300,  412,  403,  414,  415,  403,
    0,  401,  402,  402,   40,  256,  149,  256,  256,  149,
  224,  404,   40,  229,  149,   40,   59,   41,   41,   41,
   41,  404,   59,  149,  302,   59,  302,  256,  149,   44,
  302,  300,    0,   59,   41,   44,   41,  251,  256,  256,
   59,   41,   42,   43,   44,   45,   59,   47,  402,  300,
   41,  300,  300,   59,   41,   59,  256,  300,  301,   59,
   60,  300,   62,  302,    0,  300,  302,  302,  300,  256,
  302,  302,  256,   41,   42,   43,   44,   45,  300,   47,
  302,  303,  300,  300,  302,  302,  301,  224,  256,  149,
  302,   59,   60,   59,   62,  302,   59,   93,  300,   44,
  300,   41,  301,  300,  256,   41,   42,   43,   44,   45,
  301,   47,   41,  300,  251,  302,  300,  256,   40,   91,
  256,   41,  256,   59,   60,   41,   62,  402,  402,  300,
  403,  302,  300,   59,   59,   59,  256,  404,  256,   91,
  304,  305,  306,  307,  404,   59,   59,  404,  300,  400,
  300,  400,  302,  404,  405,  406,  405,  406,  409,   59,
  409,  300,   59,  302,  300,   59,  300,   59,  302,  256,
  404,   59,  411,   41,   44,   93,  411,   59,   59,  411,
  300,  404,  300,   93,  302,   91,   59,  302,   59,  411,
   93,    0,   59,  411,  411,   40,  404,   41,   59,   41,
  400,  401,  402,  403,  404,  405,  406,  407,  404,  409,
  410,    0,  412,  300,  414,  415,  400,  401,  402,  403,
  404,  405,  406,  407,  411,  409,  410,   93,  412,   93,
  414,  415,  400,  401,  402,  403,  404,  405,  406,  407,
  411,  409,  410,    0,  412,    0,  414,  415,  400,  401,
  402,  403,  404,  405,  406,  407,  256,  409,  410,    0,
  412,  411,  414,  415,  400,  401,  402,  403,  404,  405,
  406,  407,  411,  409,  410,  107,  412,  411,  414,  415,
  400,  401,  402,  403,  404,  405,  406,  407,  256,  409,
  410,  222,  412,  411,  414,  415,  197,   -1,   -1,    0,
  300,  301,  302,  256,  304,  305,  306,  307,   -1,   -1,
   -1,   -1,   -1,  400,  401,  402,   -1,  404,  405,  406,
  256,   -1,  409,  410,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,    0,  300,  301,  302,  256,  304,  305,  306,  307,
   41,   42,   43,   44,   45,   -1,   47,  300,  256,   -1,
   -1,   -1,   -1,    0,   -1,   -1,   -1,   -1,   59,   60,
   -1,   62,   -1,   -1,  300,  301,  302,  256,  304,  305,
  306,  307,   41,   42,   43,   44,   45,   -1,   47,  300,
   -1,   -1,   -1,   -1,    0,   -1,   -1,   -1,   -1,   -1,
   59,   60,  300,   62,   41,   -1,   43,   44,   45,   -1,
  400,  401,  402,   -1,  404,  405,  406,   -1,   -1,  409,
  410,  300,   59,   60,   -1,   62,    0,   -1,   -1,   -1,
   -1,   -1,   -1,  300,   -1,   41,   42,   43,   44,   45,
   -1,   47,  400,  401,  402,   -1,  404,  405,  406,   56,
   57,  409,  410,   59,   60,   -1,   62,  400,   -1,   -1,
  403,  404,  405,  406,  407,   -1,  409,   41,   -1,  412,
   44,  414,  415,   -1,  400,  401,  402,   -1,  404,  405,
  406,   -1,   -1,  409,  410,   59,   60,   -1,   62,  400,
   -1,   -1,  403,   -1,  405,  406,   -1,   -1,  409,  410,
  107,   -1,  400,   -1,   -1,  403,   -1,  405,  406,   -1,
   -1,  409,   -1,    3,   -1,   -1,   -1,   -1,   -1,   -1,
   10,  400,   12,   -1,   -1,  404,  405,  406,   -1,   -1,
  409,   -1,   -1,  400,   -1,   -1,   -1,   27,  405,  406,
  407,   -1,  409,  150,   34,  412,   -1,  414,  415,   -1,
   -1,   -1,  159,   -1,   44,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  256,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   79,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  256,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  300,
  301,  302,   -1,  304,  305,  306,  307,   -1,   -1,  256,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  129,
   -1,  300,   -1,  302,   -1,  304,  305,  306,  307,   -1,
  256,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  149,
   -1,   -1,   -1,  300,   -1,  302,   -1,  304,  305,  306,
  307,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  169,
  170,  171,  256,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  300,   -1,  302,   -1,  304,  305,
  306,  307,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  400,
  401,  402,   -1,  404,  405,  406,   -1,   -1,  409,  410,
   -1,   -1,   -1,   -1,   -1,   -1,  300,   -1,   -1,   -1,
  304,  305,  306,  307,  224,   -1,   -1,   -1,   -1,   -1,
   -1,  400,  401,  402,   -1,  404,  405,  406,  238,   -1,
  409,  410,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  251,   -1,  400,  401,  402,   -1,  404,  405,  406,
   -1,   -1,  409,  410,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  400,  401,  402,   -1,  404,  405,
  406,   -1,   -1,  409,  410,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  400,  401,  402,   -1,
  404,  405,  406,   -1,   -1,  409,  410,
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
"sentencia_if : IF '(' condicion ')' bloque_sentencias END_IF ';'",
"sentencia_if : IF '(' condicion ')' bloque_sentencias ELSE bloque_sentencias END_IF ';'",
"sentencia_if : IF '(' condicion ')' bloque_sentencias END_IF",
"sentencia_if : IF '(' condicion ')' bloque_sentencias ELSE bloque_sentencias END_IF",
"sentencia_if : IF '(' condicion ')' bloque_sentencias error",
"sentencia_if : IF '(' condicion ')' bloque_sentencias ELSE bloque_sentencias error",
"sentencia_if : IF condicion ')' bloque_sentencias END_IF ';'",
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
"sentencia_repeat_while : REPEAT bloque_sentencias WHILE '(' condicion ')'",
"sentencia_repeat_while : REPEAT WHILE '(' condicion ')' ';'",
"sentencia_repeat_while : REPEAT bloque_sentencias error ';'",
"sentencia_repeat_while : REPEAT bloque_sentencias WHILE condicion ')' ';'",
"sentencia_repeat_while : REPEAT bloque_sentencias WHILE '(' condicion ';'",
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

//#line 526 "gramatica.y"


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
//#line 690 "Parser.java"
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
case 85:
//#line 340 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF, val_peek(4).obj, val_peek(2).obj)); }
break;
case 86:
//#line 342 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF_ELSE, val_peek(6).obj, val_peek(4).obj, val_peek(2).obj)); }
break;
case 87:
//#line 344 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 88:
//#line 346 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 89:
//#line 348 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, val_peek(3).obj, val_peek(1).obj)); }
break;
case 90:
//#line 350 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
case 91:
//#line 352 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
break;
case 92:
//#line 354 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
break;
case 93:
//#line 356 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_INVALIDA)); }
break;
case 94:
//#line 361 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONDICION, val_peek(2).obj, val_peek(1).obj, val_peek(0).obj)); }
break;
case 101:
//#line 378 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_REPEAT_WHILE, val_peek(5).obj, val_peek(2).obj)); }
break;
case 102:
//#line 380 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 103:
//#line 382 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_CUERPO_ITERACION, val_peek(2).obj)); }
break;
case 104:
//#line 384 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_WHILE, val_peek(2).obj)); }
break;
case 105:
//#line 386 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
break;
case 106:
//#line 388 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
break;
case 107:
//#line 396 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 108:
//#line 398 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 109:
//#line 400 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ARGUMENTO_POUT)); }
break;
case 110:
//#line 402 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 111:
//#line 404 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 112:
//#line 409 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, val_peek(2).obj)); }
break;
case 113:
//#line 411 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, val_peek(1).obj)); }
break;
case 114:
//#line 423 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SUMA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 115:
//#line 425 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.RESTA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 116:
//#line 427 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 117:
//#line 429 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 118:
//#line 431 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERADOR, val_peek(1).obj, val_peek(0).obj)); }
break;
case 120:
//#line 437 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.MULTIPLICACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 121:
//#line 439 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DIVISION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 122:
//#line 441 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 123:
//#line 443 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 125:
//#line 449 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_ID, val_peek(0).obj)); }
break;
case 126:
//#line 451 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE, val_peek(0).obj)); }
break;
case 132:
//#line 461 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE_NEGATIVA, val_peek(0).obj)); }
break;
case 133:
//#line 466 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
case 134:
//#line 468 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_ASIG_DONDE_IGUAL);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
case 135:
//#line 474 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONVERSION_TODF, val_peek(1).obj)); }
break;
case 136:
//#line 479 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_FUNCION, val_peek(6).obj, val_peek(4).obj, val_peek(1).obj)); }
break;
case 137:
//#line 481 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(5).obj, val_peek(3).obj)); }
break;
case 138:
//#line 483 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(3).obj, val_peek(1).obj)); }
break;
case 139:
//#line 488 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 140:
//#line 490 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(0).obj)); }
break;
case 141:
//#line 495 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_REAL_NOMBRADO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 143:
//#line 501 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 144:
//#line 503 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(0).obj)); }
break;
case 145:
//#line 508 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 146:
//#line 510 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO_PREFIJADO, val_peek(4).obj, val_peek(2).obj, val_peek(0).obj)); }
break;
case 147:
//#line 512 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_METODO, val_peek(8).obj, val_peek(6).obj, val_peek(4).obj, val_peek(1).obj)); }
break;
case 148:
//#line 514 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(7).obj, val_peek(5).obj, val_peek(3).obj)); }
break;
case 149:
//#line 516 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
case 150:
//#line 518 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_METODO_PREFIJADO, val_peek(10).obj, val_peek(8).obj, val_peek(6).obj, val_peek(4).obj, val_peek(1).obj)); }
break;
case 151:
//#line 520 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(9).obj, val_peek(7).obj, val_peek(5).obj, val_peek(3).obj)); }
break;
case 152:
//#line 522 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(7).obj, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
//#line 1307 "Parser.java"
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
