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






//#line 38 "gramatica.y"
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
   47,   47,   46,   46,   37,   37,   37,
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
    3,    1,    3,    1,    3,    5,    6,
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
    0,    0,  146,    0,    0,    0,    0,    0,  135,    0,
    0,    0,    0,  107,  108,  112,    0,    0,    0,   68,
    0,    0,    0,    0,   39,   65,    0,  147,  134,  141,
    0,  139,  133,    0,    0,   89,    0,    0,   91,  103,
  106,    0,  105,   67,   69,    0,    0,    0,    0,    0,
    0,  137,    0,   93,   92,    0,   85,  101,   44,    0,
   41,    0,    0,    0,  136,   90,    0,   43,   40,    0,
  143,   86,   61,
};
final static short yydgoto[] = {                          9,
   10,   39,   11,   12,   40,   41,   42,   43,   28,   44,
   45,   29,   30,   31,   32,   33,   17,   95,  105,   18,
  100,  225,  101,   19,  106,  107,  108,  109,  110,  111,
  154,  192,   67,   81,   82,   68,   69,   70,  128,   71,
   72,   73,   74,   75,  165,  253,  166,
};
final static short yysindex[] = {                      -212,
 -343,    0, -185,    0, -232, -203,    0,    0,    0, -270,
    0, -270,    0,    0,    0,    0, -236,   59, -292,    0,
  -56,   -4,  -38,    0,   80,   84, -193, -291,   66,    0,
    0,    0,    0,  273, -168,    0,   85,  -42, -206,    0,
    0,    0,    0, -270,    0,   79,    0,    0,    0,    0,
 -206,   82,  -44, -153,   91, -174, -255,    0,    0,   67,
 -151,  -34,    0,  111,   83, -150,    0,  242,    0,  112,
  -11,   44,    0,    0,    0,  -16,   14,   96,  273,  116,
    0, -216,    0,    0,    0,   14,   72,  105,    0,    0,
    0,    0,    0, -117,    0,    0,    0, -114, -112,  155,
  160,    0,  -48,    0, -101, -190, -255,    0,    0,    0,
  178,    0,    0,   70,  187,   51,  190,   14,  206,  -20,
    0,    0,    0,    0,    0,    0,    0,   14, -177,    0,
   86,   87,   98,  243,  208,  191,  210,  212, -149,   14,
  195,  -32,    0,  -36,    0,    0,    0,    0, -270, -174,
  223,  225,    0,  231,  -44,  235,  240,    0, -174,   51,
    3,   14,  -18,    0,  259,  267,   14,  272, -177, -177,
 -177,    0,  -87,    0,    0,    0,    0,    0,    0,    0,
    0,  260,    0,  261,  275,    0,  290,    0,   14,  291,
  276,  245,  292, -233,    0,    0,   40,    0,    0,    0,
  300,  305,    0,  309,  -21,  266,   51,  311,    0,  -46,
  -41, -164,  299,    0,    0,    0,  301,   25,  304,    0,
  307,  -31,  -40,  244,    0,    0,  -35,    0,    0,    0,
  -77,    0,    0,  312,  313,    0, -177,  315,    0,    0,
    0,  316,    0,    0,    0,  317,  -49,  323,  -13,  273,
  340,    0,  303,    0,    0, -209,    0,    0,    0,  333,
    0,  334,  -10,  110,    0,    0,  369,    0,    0,  371,
    0,    0,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0, -169,
    0, -169,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  441,  114,    0,
    0,    0,    0,    5,    0,    0,    0,    0,    0,    0,
    0,    0,    0, -169,    0,    0,    0,    0,    0,    0,
    0,    0,  385,  420,    0,    0,   69,    0,    0,    0,
    0,  233,    0,    0,    0,    0,    0,    0,    0,    0,
  318,  285,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   77,   78,    0,
  435,    0,    0,    0,    0,    0,   69,    0,    0,    0,
    0,    0,    0,  201,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0, -230,    0,
    0,  433,    0,    0,   18,    0, -238,    0,    0,    0,
    0,    0,   -6,    0,    0,  463,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    1,    0,   17,   33,    0,    0,    0,    0,    0,
    0,    0,  417,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  265,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   49,    0,    0,
    0,   65,    0,    0,    0,    0,    0,    0,    0,    0,
  418,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0, -225,    0,    0,    0,    0,   81,    0,    0,    0,
    0,    0,    0,
};
final static short yygindex[] = {                         0,
    0,   62,   39,    0,  512,  513,  517,  522,  -19,    0,
    0,   36,   57,   60,   73,   90,   -2,   35,    0,    0,
  -61,    0,    0,    0,  419,    0,    0,    0,    0,    0,
  328,  306,  -68,   48,  -76,  620,  585,  257,    0,  -25,
    0,    0,    0,    0, -104,  263,    0,
};
final static int YYTABLESIZE=835;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         94,
  110,   65,   59,   61,   72,  116,   66,  189,   66,   59,
  153,   61,   66,   66,   85,  252,  111,   53,  167,   52,
  171,  116,  223,   66,  136,   16,  117,   61,   66,   38,
   42,  131,  113,  132,  125,  125,  125,  125,  125,  141,
  125,   61,  205,    1,  102,   46,  266,   46,   87,    1,
   34,   55,  173,   99,  104,  202,  191,   60,   66,  139,
   20,   53,   78,   53,  102,  242,   47,   36,   47,   48,
   21,   48,   88,   51,   42,  193,   35,   89,   78,   46,
   88,   34,   49,  241,   49,  133,   16,    2,  195,   92,
  134,  236,  210,  211,  212,   66,   37,  201,   56,   50,
   47,   50,  232,   48,  104,   90,   22,  179,  181,  160,
   57,   66,   83,   74,   22,  161,   49,   50,   49,   76,
   50,   49,   22,   77,   84,   98,   34,   66,  146,   23,
   66,   66,   86,   50,   25,   26,    5,   91,   27,  156,
   93,    6,   66,    7,    8,   87,   96,   99,  114,   97,
  118,  121,  129,  193,   59,  140,   99,  103,    7,    8,
  256,   53,  144,  145,   53,   53,   53,   53,   53,  224,
   53,   54,   16,   53,   42,   53,   53,   42,   42,   42,
   42,   42,   53,   42,   46,  147,   42,  148,   42,   42,
    3,    4,  267,  142,    5,  149,    3,    4,  155,    6,
  145,    7,    8,  150,  249,   47,   23,  151,   48,   79,
  194,   25,   26,  157,   23,   27,   80,  159,   24,   25,
   26,   49,   23,   27,  251,   79,  162,   25,   26,  167,
  263,   27,  125,   16,   16,  170,  237,  238,   50,    7,
    8,  145,  145,  145,  145,  145,  169,  145,  182,  183,
  184,  152,  185,  188,  186,   53,  110,   53,   60,  145,
  145,   62,  145,   63,  138,   63,  115,   62,  197,   63,
   63,   34,  111,  125,  125,  125,  125,  125,   62,  125,
   63,  196,  115,   62,  124,   63,  135,   66,  113,  198,
  130,  125,  125,  199,  125,  125,   60,   34,  200,  206,
  110,  127,  203,  126,   87,  138,  138,  138,  138,  138,
  207,  138,  209,   62,  213,   63,  111,  119,  214,  215,
  102,  120,  112,  138,  138,  124,  138,  124,  124,  124,
  217,  219,  113,  216,  220,  222,   88,  221,  119,  152,
  227,  174,  176,  124,  124,  228,  124,   58,   87,  229,
  163,  233,   63,  178,  260,  234,  231,  239,  119,  240,
  235,  119,  243,  246,  102,  244,   62,  250,   63,   74,
  254,  255,   64,  257,  258,  259,  119,  119,   64,  119,
   88,  261,   62,  264,   63,   62,   62,   63,   63,   64,
  262,  268,  269,  270,   64,  265,  187,   62,  190,   63,
  110,  110,  110,  110,  110,  110,  110,  110,   72,  110,
  110,  251,  110,   74,  110,  110,  111,  111,  111,  111,
  111,  111,  111,  111,   64,  111,  111,  272,  111,  273,
  111,  111,  113,  113,  113,  113,  113,  113,  113,  113,
   11,  113,  113,   35,  113,  218,  113,  113,   87,   87,
   87,   87,   87,   87,   87,   87,  145,   87,   87,   38,
   87,   64,   87,   87,  102,  102,  102,  102,  102,  102,
  102,  102,   55,  102,  102,   46,  102,   64,  102,  102,
   88,   88,   88,   88,   88,   88,   88,   88,  125,   88,
   88,   66,   88,   64,   88,   88,   64,   64,  180,  247,
  145,  145,  145,  140,  145,  145,  145,  145,   64,   70,
  144,   13,   14,   74,   74,   74,   15,   74,   74,   74,
  138,   16,   74,   74,  226,  158,  271,  245,   78,    0,
    0,    0,  125,    0,  125,    0,  125,  125,  125,  125,
  124,    0,   62,   22,   63,  122,  123,  124,  125,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  138,    0,  138,    0,  138,  138,
  138,  138,   22,  119,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  124,    0,  124,   35,  124,  124,
  124,  124,    0,    0,   35,    0,   35,    0,    0,    0,
  145,  145,  145,    0,  145,  145,  145,    0,    0,  145,
  145,   35,    0,    0,    0,    0,    0,  119,   35,    0,
    0,  119,  119,  119,  119,    0,    0,    0,   35,    0,
    0,    0,  125,  125,  125,    0,  125,  125,  125,    0,
    0,  125,  125,   23,    0,    0,    0,  248,   25,   26,
    0,    0,   27,   64,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   35,  138,  138,  138,    0,  138,  138,
  138,    0,   23,  138,  138,    0,    0,   25,   26,  113,
    0,   27,    0,    0,  124,  124,  124,    0,  124,  124,
  124,    0,    0,  124,  124,  137,  138,    0,    0,    0,
    0,    0,    0,    0,    0,  143,    0,    0,    0,    0,
    0,    0,    0,   35,    0,    0,    0,  119,  119,  119,
    0,  119,  119,  119,    0,    0,  119,  119,    0,    0,
    0,    0,    0,   35,    0,  164,    0,  168,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  172,    0,    0,
  175,  177,    0,   35,   35,   35,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  164,
    0,  204,    0,    0,    0,    0,  208,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   35,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   35,    0,    0,  230,    0,  164,    0,    0,    0,
    0,    0,    0,    0,   35,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         44,
    0,   40,   59,   46,    0,   40,   45,   40,   45,   59,
   59,   46,   45,   45,   34,   93,    0,  256,   40,  256,
   41,   40,  256,   45,   41,  256,   61,   46,   45,  300,
  256,   43,    0,   45,   41,   42,   43,   44,   45,  256,
   47,   46,   61,  256,  300,   10,  256,   12,    0,  256,
    3,   17,  129,   56,   57,  160,   93,   40,   45,   79,
  404,  300,  256,  300,    0,   41,   10,  300,   12,   10,
  256,   12,   38,   12,  300,  144,   59,   39,  256,   44,
    0,   34,   10,   59,   12,   42,  256,  300,  150,   51,
   47,  256,  169,  170,  171,   45,  300,  159,   40,   10,
   44,   12,  207,   44,  107,   44,  300,  133,  134,   40,
  403,   45,  404,    0,  300,   46,   44,   41,   41,   40,
   44,   44,  300,   40,   59,  300,   79,   45,   94,  400,
   45,   45,  301,   44,  405,  406,  407,   59,  409,  105,
   59,  412,   45,  414,  415,   61,  300,  150,  300,   59,
   40,  302,   41,  222,   59,   40,  159,  413,  414,  415,
  237,  400,   91,   59,  403,  404,  405,  406,  407,  403,
  409,  408,  403,  412,  400,  414,  415,  403,  404,  405,
  406,  407,  300,  409,  149,  300,  412,  300,  414,  415,
  403,  404,  402,  410,  407,   41,  403,  404,  300,  412,
    0,  414,  415,   44,  224,  149,  400,  256,  149,  403,
  149,  405,  406,  404,  400,  409,  410,   40,  404,  405,
  406,  149,  400,  409,  302,  403,   40,  405,  406,   40,
  250,  409,    0,  403,  404,  256,  401,  402,  149,  414,
  415,   41,   42,   43,   44,   45,   41,   47,   41,   59,
   41,  300,   41,   59,  404,  300,  256,  300,  301,   59,
   60,  300,   62,  302,    0,  302,  301,  300,   44,  302,
  302,  224,  256,   41,   42,   43,   44,   45,  300,   47,
  302,   59,  301,  300,    0,  302,  303,   45,  256,   59,
  302,   59,   60,   59,   62,  302,  301,  250,   59,   41,
  300,   60,  300,   62,  256,   41,   42,   43,   44,   45,
   44,   47,   41,  300,  402,  302,  300,    0,   59,   59,
  256,   65,  256,   59,   60,   41,   62,   43,   44,   45,
   41,   41,  300,   59,   59,   44,  256,   93,  256,  300,
   41,  256,  256,   59,   60,   41,   62,  404,  300,   41,
  300,   41,  302,  256,  404,  402,   91,   59,   41,   59,
  402,   44,   59,  404,  300,   59,  300,  403,  302,  256,
   59,   59,  411,   59,   59,   59,   59,   60,  411,   62,
  300,   59,  300,   44,  302,  300,  300,  302,  302,  411,
  404,   59,   59,  404,  411,   93,  140,  300,  142,  302,
  400,  401,  402,  403,  404,  405,  406,  407,  404,  409,
  410,  302,  412,  300,  414,  415,  400,  401,  402,  403,
  404,  405,  406,  407,  411,  409,  410,   59,  412,   59,
  414,  415,  400,  401,  402,  403,  404,  405,  406,  407,
    0,  409,  410,   59,  412,  189,  414,  415,  400,  401,
  402,  403,  404,  405,  406,  407,  256,  409,  410,   40,
  412,  411,  414,  415,  400,  401,  402,  403,  404,  405,
  406,  407,  404,  409,  410,   41,  412,  411,  414,  415,
  400,  401,  402,  403,  404,  405,  406,  407,  256,  409,
  410,   59,  412,  411,  414,  415,  411,  411,  256,  256,
  300,  301,  302,   41,  304,  305,  306,  307,  411,   93,
   93,    0,    0,  400,  401,  402,    0,  404,  405,  406,
  256,    0,  409,  410,  197,  107,  264,  222,  256,   -1,
   -1,   -1,  300,   -1,  302,   -1,  304,  305,  306,  307,
  256,   -1,  300,  300,  302,  304,  305,  306,  307,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  300,   -1,  302,   -1,  304,  305,
  306,  307,  300,  256,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  300,   -1,  302,    3,  304,  305,
  306,  307,   -1,   -1,   10,   -1,   12,   -1,   -1,   -1,
  400,  401,  402,   -1,  404,  405,  406,   -1,   -1,  409,
  410,   27,   -1,   -1,   -1,   -1,   -1,  300,   34,   -1,
   -1,  304,  305,  306,  307,   -1,   -1,   -1,   44,   -1,
   -1,   -1,  400,  401,  402,   -1,  404,  405,  406,   -1,
   -1,  409,  410,  400,   -1,   -1,   -1,  404,  405,  406,
   -1,   -1,  409,  411,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   79,  400,  401,  402,   -1,  404,  405,
  406,   -1,  400,  409,  410,   -1,   -1,  405,  406,   60,
   -1,  409,   -1,   -1,  400,  401,  402,   -1,  404,  405,
  406,   -1,   -1,  409,  410,   76,   77,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   86,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  129,   -1,   -1,   -1,  400,  401,  402,
   -1,  404,  405,  406,   -1,   -1,  409,  410,   -1,   -1,
   -1,   -1,   -1,  149,   -1,  116,   -1,  118,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  128,   -1,   -1,
  131,  132,   -1,  169,  170,  171,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  160,
   -1,  162,   -1,   -1,   -1,   -1,  167,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  224,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  237,   -1,   -1,  205,   -1,  207,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  250,
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
"referencia_atributo : ID '.' ID '(' lista_parametros_reales ')'",
};

//#line 541 "gramatica.y"

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
//#line 641 "Parser.java"
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
//#line 82 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PROGRAMA, val_peek(1).obj, val_peek(0).obj)); }
break;
case 2:
//#line 84 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_PROGRAMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.PROGRAMA, null, val_peek(0).obj)); }
break;
case 3:
//#line 87 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_PROGRAMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.PROGRAMA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 8:
//#line 102 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.NOMBRE_PROGRAMA, val_peek(0).obj)); }
break;
case 9:
//#line 107 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, val_peek(1).obj)); }
break;
case 10:
//#line 109 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, (Object) null)); }
break;
case 11:
//#line 111 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_BLOQUE_SIN_END, val_peek(0).obj)); }
break;
case 12:
//#line 113 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_BLOQUE_SIN_END)); }
break;
case 13:
//#line 115 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_BEGIN)); }
break;
case 14:
//#line 117 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_BEGIN)); }
break;
case 15:
//#line 126 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 16:
//#line 128 "gramatica.y"
{ yyval = new ParserVal((Object) null); }
break;
case 27:
//#line 147 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_VARIABLES, val_peek(2).obj, val_peek(1).obj)); }
break;
case 28:
//#line 149 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_LISTA_VARIABLES)); }
break;
case 29:
//#line 155 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_OBJETOS, val_peek(2).obj, val_peek(1).obj)); }
break;
case 30:
//#line 160 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.ULONGINT)); }
break;
case 31:
//#line 162 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.DOUBLEF)); }
break;
case 33:
//#line 168 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, val_peek(0).obj)); }
break;
case 34:
//#line 173 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 35:
//#line 175 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(0).obj)); }
break;
case 36:
//#line 177 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_COMA_VARIABLES);
          yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(1).obj, val_peek(0).obj)); }
break;
case 37:
//#line 187 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(2).obj, val_peek(0).obj); }
break;
case 38:
//#line 189 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_FUNCION);
          acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(1).obj, null); }
break;
case 39:
//#line 195 "gramatica.y"
{ yyval = val_peek(0); }
break;
case 40:
//#line 200 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 41:
//#line 202 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 42:
//#line 204 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 43:
//#line 207 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_BLOQUE_SIN_END);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 44:
//#line 210 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_BEGIN);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 45:
//#line 216 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 46:
//#line 218 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, val_peek(0).obj)); }
break;
case 47:
//#line 225 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, val_peek(1).obj, val_peek(0).obj)); }
break;
case 48:
//#line 227 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, val_peek(1).obj, val_peek(0).obj)); }
break;
case 49:
//#line 229 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_PARAMETRO, val_peek(0).obj)); }
break;
case 50:
//#line 231 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_TIPO_PARAMETRO, val_peek(0).obj)); }
break;
case 51:
//#line 241 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_CLASE, val_peek(0).obj);
          yyval = val_peek(0); }
break;
case 52:
//#line 247 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, val_peek(4).obj)); }
break;
case 53:
//#line 249 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, val_peek(3).obj)); }
break;
case 54:
//#line 255 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 55:
//#line 257 "gramatica.y"
{ yyval = new ParserVal((Object) null); }
break;
case 59:
//#line 268 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_ATRIBUTO, val_peek(2).obj, val_peek(1).obj)); }
break;
case 60:
//#line 273 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(1).obj, val_peek(0).obj);
          yyval = val_peek(0); }
break;
case 61:
//#line 279 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_METODO, val_peek(7).obj)); }
break;
case 62:
//#line 284 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_EXTENDS, val_peek(1).obj)); }
break;
case 63:
//#line 286 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_LISTA_EXTENDS)); }
break;
case 64:
//#line 288 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_LISTA_EXTENDS)); }
break;
case 65:
//#line 293 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, val_peek(2).obj, val_peek(0).obj)); }
break;
case 66:
//#line 295 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, val_peek(0).obj)); }
break;
case 67:
//#line 305 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_TYPEDEF, val_peek(5).obj, val_peek(2).obj)); }
break;
case 68:
//#line 307 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_VALORES_ENUMERADO, val_peek(4).obj)); }
break;
case 69:
//#line 312 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 70:
//#line 314 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, val_peek(0).obj)); }
break;
case 71:
//#line 323 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 72:
//#line 325 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(0).obj)); }
break;
case 74:
//#line 331 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 79:
//#line 337 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_SENTENCIA)); }
break;
case 81:
//#line 343 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, val_peek(1).obj)); }
break;
case 82:
//#line 348 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 83:
//#line 350 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 84:
//#line 352 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_ASIGNACION_INVALIDA)); }
break;
case 85:
//#line 361 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF, val_peek(4).obj, val_peek(2).obj)); }
break;
case 86:
//#line 363 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF_ELSE, val_peek(6).obj, val_peek(4).obj, val_peek(2).obj)); }
break;
case 87:
//#line 365 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 88:
//#line 367 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 89:
//#line 369 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, val_peek(3).obj, val_peek(1).obj)); }
break;
case 90:
//#line 371 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
case 91:
//#line 373 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
break;
case 92:
//#line 375 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
break;
case 93:
//#line 377 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_INVALIDA)); }
break;
case 94:
//#line 382 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONDICION, val_peek(2).obj, val_peek(1).obj, val_peek(0).obj)); }
break;
case 101:
//#line 400 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_REPEAT_WHILE, val_peek(5).obj, val_peek(2).obj)); }
break;
case 102:
//#line 402 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 103:
//#line 404 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_CUERPO_ITERACION, val_peek(2).obj)); }
break;
case 104:
//#line 406 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_WHILE, val_peek(2).obj)); }
break;
case 105:
//#line 408 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
break;
case 106:
//#line 410 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
break;
case 107:
//#line 419 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 108:
//#line 421 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 109:
//#line 423 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ARGUMENTO_POUT)); }
break;
case 110:
//#line 425 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 111:
//#line 427 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 112:
//#line 432 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, val_peek(2).obj)); }
break;
case 113:
//#line 434 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, val_peek(1).obj)); }
break;
case 114:
//#line 444 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SUMA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 115:
//#line 446 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.RESTA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 116:
//#line 448 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 117:
//#line 450 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 118:
//#line 452 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERADOR, val_peek(1).obj, val_peek(0).obj)); }
break;
case 120:
//#line 458 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.MULTIPLICACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 121:
//#line 460 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DIVISION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 122:
//#line 462 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 123:
//#line 464 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 125:
//#line 470 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_ID, val_peek(0).obj)); }
break;
case 126:
//#line 472 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE, val_peek(0).obj)); }
break;
case 132:
//#line 482 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE_NEGATIVA, val_peek(0).obj)); }
break;
case 133:
//#line 488 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
case 134:
//#line 490 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_ASIG_DONDE_IGUAL);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
case 135:
//#line 496 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONVERSION_TODF, val_peek(1).obj)); }
break;
case 136:
//#line 502 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_FUNCION, val_peek(6).obj, val_peek(4).obj, val_peek(1).obj)); }
break;
case 137:
//#line 504 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(5).obj, val_peek(3).obj)); }
break;
case 138:
//#line 506 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(3).obj, val_peek(1).obj)); }
break;
case 139:
//#line 513 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 140:
//#line 515 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(0).obj)); }
break;
case 141:
//#line 520 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_REAL_NOMBRADO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 143:
//#line 526 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 144:
//#line 528 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(0).obj)); }
break;
case 145:
//#line 533 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 146:
//#line 535 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO_PREFIJADO, val_peek(4).obj, val_peek(2).obj, val_peek(0).obj)); }
break;
case 147:
//#line 537 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_METODO, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
//#line 1238 "Parser.java"
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
