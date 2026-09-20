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






//#line 27 "gramatica.y"
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
    0,    0,    1,    3,    3,    3,    3,    2,    2,    5,
    5,    5,    5,    5,    5,    5,    5,    5,    5,    6,
    6,    6,    6,    7,   16,   16,   18,   18,   17,   17,
   17,   19,   19,    8,   21,   21,   21,   21,   20,   20,
   22,   22,   22,   22,   23,    9,    9,   24,   24,   25,
   25,   25,   26,   29,   27,   28,   28,   28,   30,   30,
   10,   10,   31,   31,    4,    4,   33,   33,   33,   33,
   33,   33,   33,   34,   34,   11,   11,   11,   12,   12,
   12,   12,   12,   12,   12,   12,   12,   37,   38,   38,
   38,   38,   38,   38,   13,   13,   13,   13,   13,   13,
   14,   14,   14,   14,   14,   15,   15,   35,   35,   35,
   35,   35,   35,   39,   39,   39,   39,   39,   40,   40,
   40,   40,   40,   40,   32,   32,   42,   42,   43,   41,
   41,   41,   44,   44,   45,   45,   36,   36,   36,
};
final static short yylen[] = {                            2,
    3,    2,    1,    3,    2,    2,    1,    2,    0,    1,
    1,    1,    1,    1,    2,    1,    1,    1,    1,    3,
    2,    2,    3,    3,    1,    1,    1,    1,    3,    1,
    2,    3,    2,    6,    4,    3,    2,    3,    3,    1,
    2,    2,    1,    1,    2,    5,    4,    2,    0,    1,
    1,    1,    3,    0,   10,    3,    2,    3,    3,    1,
    7,    6,    3,    1,    2,    1,    2,    1,    1,    1,
    1,    1,    2,    1,    3,    3,    3,    3,    7,    9,
    6,    8,    5,    7,    6,    7,    7,    3,    1,    1,
    1,    1,    1,    1,    7,    6,    6,    6,    6,    6,
    5,    5,    4,    4,    4,    5,    4,    3,    3,    3,
    2,    2,    1,    3,    3,    3,    3,    1,    1,    1,
    1,    1,    1,    1,    1,    2,    5,    5,    4,    7,
    6,    4,    3,    1,    3,    1,    3,    5,    6,
};
final static short yydefred[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,   25,   26,    0,
    0,    0,    0,   10,   11,   12,   13,   14,    0,   16,
   17,   18,   19,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  125,    0,    0,    0,  120,    0,
  122,    0,    0,    0,  121,  123,  124,    0,    0,   45,
    0,    0,    0,    0,    0,   69,   70,   71,   72,   74,
    0,    0,    0,    0,    0,    7,    2,    0,    0,    8,
   15,    0,    0,    0,   22,    0,    0,    0,   31,   78,
   76,   29,    0,   24,    0,    0,    0,    0,    0,    0,
  126,   89,   90,   91,   92,   93,   94,    0,    0,  112,
  111,    0,    0,    0,    0,    0,    0,    0,    0,   73,
    0,    0,   67,    0,    0,    0,    1,    0,    6,   65,
   23,   32,   20,    0,    0,    0,    0,   28,    0,   27,
    0,    0,    0,   50,   51,   52,   77,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   88,    0,  110,
  108,  109,  116,  114,  117,  115,    0,  103,    0,    0,
   75,    0,    0,    0,    0,    0,    4,   42,   41,    0,
    0,    0,    0,   57,    0,    0,    0,    0,   48,    0,
  138,    0,    0,    0,    0,  129,    0,    0,    0,    0,
  101,  102,  106,    0,    0,    0,    0,    0,    0,    0,
    0,   39,   58,    0,   56,    0,   53,   46,  139,  128,
  133,    0,  127,    0,    0,    0,    0,   85,   97,  100,
    0,   99,   98,   62,    0,    0,    0,    0,   34,   59,
    0,    0,  131,    0,   87,   86,    0,   79,   95,   61,
   63,    0,    0,    0,    0,  130,    0,    0,   38,    0,
  135,   80,   35,    0,    0,    0,   55,
};
final static short yydgoto[] = {                         10,
   11,   12,   67,   68,   13,   14,   15,   16,   17,   18,
   55,   56,   57,   58,   59,   24,   25,  131,   26,  126,
  229,  127,   27,  132,  133,  134,  135,  136,  206,  175,
  199,   39,   69,   61,   40,   41,   42,   98,   43,   44,
   45,   46,   47,  142,  234,
};
final static short yysindex[] = {                       370,
  -11,  -40,   14,   47, -279, -241, -190,    0,    0,    0,
  500,   41,  500,    0,    0,    0,    0,    0,   59,    0,
    0,    0,    0, -209,   63,  102, -259, -153,   -8,  -41,
 -144, -142,  111,  -24,    0,  126,  -27, -131,    0,   10,
    0,  135,  -20,   60,    0,    0,    0,  -34,  -15,    0,
  118,   -7, -215,  140,  123,    0,    0,    0,    0,    0,
  -38,  122,  -11,   41, -215,    0,    0, -218, -215,    0,
    0,  136, -113,  137,    0, -222, -268,  -15,    0,    0,
    0,    0,   68,    0,  158,  -15,  160,  -15,  163,  -29,
    0,    0,    0,    0,    0,    0,    0,  -15,   66,    0,
    0,  -26,  -15,  -19,  -16,  164,  148,  168,  169,    0,
 -193,  -15,    0,  -37,  -15,  121,    0, -191,    0,    0,
    0,    0,    0,  -83,  -80,  175,  177,    0,  -53,    0,
  -78, -181, -268,    0,    0,    0,    0,  -15,  -75,  -15,
  182,  187,  -15,  193,   66,   66,   66,    0, -167,    0,
    0,    0,    0,    0,    0,    0,  179,    0,  183,  186,
    0,  202,  -15,  205,  209,  -35,    0,    0,    0,  500,
 -222,  192,  211,    0,  219,   -8,  232,  234,    0,  215,
    0,  223,  -15,  208,  255,    0, -102,  -98, -271,  251,
    0,    0,    0,  252,   70,  254,  259,  260,  227,  281,
  217,    0,    0,   27,    0,  294,    0,    0,    0,    0,
    0,  -84,    0,  284,  285,   66,  287,    0,    0,    0,
  290,    0,    0,    0,  291,  -32, -215,  -52,    0,    0,
 -222,  307,    0,  261,    0,    0,  -46,    0,    0,    0,
    0,  -42,  304,  323,   65,    0,  309,  310,    0,  -30,
    0,    0,    0, -215,  -25,  316,    0,
};
final static short yyrindex[] = {                      -225,
  359,    0,    0,    0,    0,    0,    0,    0,    0,    0,
 -225,    0, -225,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  383,    0,
    0,    0,    0,   53,    0,    0,    0,    0,    0,    0,
    0,    0,   93,   76,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  109,    0,    0,    0,    0,    0,
    0,    0,  327,    0,    0,    0,    0,    0,  197,    0,
    0,    0,  347,  -67,    0,    0,  -14,    0,    0,    0,
    0,    0,    1,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  389,    0,    0,
    0,    0,    0,   99,  119,    0,  350,    0,    0,    0,
    0,    0,  -14,    0,    0,    0,    0,    0,    0,    0,
  353,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  132,    0,  161,  184,
    0,    0,    0,    0,    0,    0,    0,    0,    0, -236,
    0,    0,  355,    0,    0,   64,    0,  408,    0,    0,
    0,    0,    0,   24,    0,    0,    0,    0,  321,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  319,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  231,    0,    0,    0,
  248,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  326,    0,    0,    0,    0,  295,    0,    0,    0,
    0,  447,    0,    0,    0,    0,  272,  429,    0,    0,
    0,    0,    0,    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
    0,   69,  356,  -13,    0,    0,    0,    0,    0,    0,
   38,   62,   79,   88,  128,   48,   26,    0,    0, -157,
    0,    0,    0,  288,    0,    0,    0,    0,    0,  218,
  222, -149,   28,  107,  779,  738,   -9,    0,   46,    0,
    0,    0,    0,  -57,  178,
};
final static int YYTABLESIZE=992;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         37,
  137,  115,  163,   38,   38,  174,  107,   38,  233,   38,
   38,  147,   38,  202,   51,   86,  200,   38,   38,    9,
   50,   32,  102,  132,  103,   38,   33,   90,   38,   38,
    9,  128,   31,   60,   32,   31,   87,   19,   32,  111,
   51,  137,  137,  137,  137,  137,   72,  137,   19,   74,
   19,  118,  119,   48,   79,  120,   82,  198,   52,  137,
  137,   20,  137,  132,  132,  132,  132,  132,  132,   97,
  132,   96,   20,  244,   20,  118,  200,  124,   21,   64,
  180,   70,  132,  132,   52,  132,   49,   22,   33,   21,
   29,   21,  113,  119,  119,  119,  119,  119,   22,  119,
   22,  104,  162,   54,  164,  165,  105,  138,   68,   62,
  221,  119,  119,  139,  119,  118,  118,   71,  118,  118,
  118,   75,   30,  125,  130,  211,   60,   23,  220,  216,
  217,  104,  113,  113,  118,  118,  113,  118,   23,   44,
   23,   76,   44,   77,  129,    8,    9,   78,   68,  154,
  156,  113,  113,  195,  113,   29,  177,   83,    2,   43,
  105,   53,   43,    3,    4,   88,    9,    6,   54,   84,
   91,  104,   60,   60,   60,   99,  110,    9,    9,  112,
  130,  113,  116,  107,    2,  119,  122,  228,   21,    3,
    4,    8,    9,    6,  121,  123,   66,  140,   73,  143,
  105,   79,  172,  145,  157,  149,  158,   19,  159,  160,
  161,  166,  167,  242,   80,  170,  168,  232,  125,  169,
  171,  176,  178,  107,  181,  183,  146,  184,   89,  150,
   81,   20,   21,  186,  190,  100,  153,  191,  201,  155,
  255,  192,  194,   60,  193,  196,  173,   96,   21,  197,
  203,  187,  188,  189,  204,  209,  137,   22,   34,   34,
   35,   35,   34,  210,   35,   34,   35,   35,  106,   35,
   81,   82,   34,   34,   35,   35,   85,  205,  125,  132,
   34,  101,   35,   34,   34,   35,   35,   96,   29,   30,
  207,   29,  208,   30,   84,  213,   51,   23,  212,  214,
  137,  137,  137,  215,  137,  137,  137,  137,  119,  218,
  219,   82,  222,   92,   93,   94,   95,  223,  224,  225,
   83,   51,  237,  132,  226,  132,  173,  132,  132,  132,
  132,  118,   21,  231,   84,   21,   21,   21,   21,   21,
   52,   21,  235,  236,   21,  238,   21,   21,  239,  240,
  245,  243,  119,  246,  119,  247,  119,  119,  119,  119,
   83,  248,  249,  250,   68,   52,  232,  252,  253,   36,
   36,  114,  254,   36,  257,  118,   36,  118,  256,  118,
  118,  118,  118,   36,   36,   30,   33,  104,    5,   49,
   40,   36,  113,  134,   36,   36,  113,  113,  113,  113,
  137,  137,  137,  137,  137,  137,  137,  137,   68,  137,
  137,   64,  137,   60,  137,  137,  105,   30,  136,  117,
  179,  230,  251,  132,  132,  132,  132,  132,  132,  132,
  132,  104,  132,  132,    0,  132,    0,  132,  132,  107,
    2,   30,    0,   65,   66,    3,    4,  241,    0,    6,
    0,    0,  119,  119,  119,  119,  119,  119,  119,  119,
  105,  119,  119,    0,  119,    2,  119,  119,   53,    0,
    3,    4,   51,    0,    6,  118,  118,  118,  118,  118,
  118,  118,  118,  107,  118,  118,   81,  118,    0,  118,
  118,    0,  113,  113,  113,  113,  113,  113,  113,  113,
    0,  113,  113,   96,  113,    0,  113,  113,   68,   68,
   68,   68,   68,   68,   68,   68,   52,   68,   68,    0,
   68,    0,   68,   68,    0,    0,    0,   82,    0,    0,
   81,  104,  104,  104,  104,  104,  104,  104,  104,    0,
  104,  104,    0,  104,    0,  104,  104,   96,    0,    0,
   84,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  105,  105,  105,  105,  105,  105,  105,  105,    0,  105,
  105,   82,  105,    0,  105,  105,   83,    0,    0,    0,
    0,    0,    0,  107,  107,  107,  107,  107,  107,  107,
  107,    0,  107,  107,   84,  107,    0,  107,  107,   66,
   66,    0,    0,   66,    0,    0,    0,    0,   66,    0,
   66,   66,    0,    0,    3,    0,    2,    0,    0,  227,
   83,    3,    4,    0,    0,    6,    0,    0,    0,    0,
   81,   81,   81,   81,   81,   81,   81,   81,   30,   81,
   81,    0,   81,    0,   81,   81,    0,   96,   96,   96,
   96,   96,   96,   96,   96,    0,   96,   96,    0,   96,
    0,   96,   96,   47,    0,    0,    0,    0,    0,    1,
    0,   82,   82,   82,   82,   82,   82,   82,   82,    0,
   82,   82,    0,   82,   36,   82,   82,    0,    0,    0,
    0,    0,    0,    0,   84,   84,    0,   84,   84,   84,
   84,   84,   37,   84,   84,    0,   84,   47,   84,   84,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   83,    0,    0,   83,   83,   83,   83,   83,   36,   83,
   83,    0,   83,    0,   83,   83,    0,   28,    0,    0,
    0,    0,    0,   28,    0,    0,   37,    0,   28,   28,
   28,    0,    0,    0,    0,    0,    0,    0,    3,    0,
    0,    3,    3,    3,    3,    3,    0,    3,    0,    2,
    3,    0,    3,    3,    3,    4,    5,    0,    6,    0,
    0,    7,   30,    8,    9,   30,   30,   30,   30,   30,
   28,   30,    0,    0,   30,    0,   30,   30,    0,   63,
    0,   28,   28,    0,    0,    0,   28,   47,   81,    0,
   47,   47,   47,   47,   47,    0,   47,    0,    0,   47,
    0,   47,   47,    0,    0,    0,  108,  109,   36,    0,
    0,   36,   36,   36,   36,   36,   28,   36,    0,    0,
   36,    0,   36,   36,    0,    0,   37,    0,    0,   37,
    0,   37,   37,   37,    0,   37,  137,    0,   37,    0,
   37,   37,    0,    0,  141,    0,  144,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  148,    0,    0,    0,
  151,  152,   28,   28,   28,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    2,
    0,    0,    0,    0,    3,    4,    5,   28,    6,    0,
    0,    7,    0,    8,    9,    0,  141,    0,  182,    0,
    0,  185,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   28,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   28,    0,    0,    0,    0,    0,    0,
    0,  141,    0,    0,   28,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   28,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         40,
    0,   40,   40,   45,   45,   59,   41,   45,   93,   45,
   45,   41,   45,  171,  256,   40,  166,   45,   45,  256,
  300,   46,   43,    0,   45,   45,    1,   37,   45,   45,
  256,  300,   44,    6,   46,   44,   61,    0,   46,   53,
  256,   41,   42,   43,   44,   45,  256,   47,   11,   24,
   13,   65,    0,   40,   29,   69,   31,   93,  300,   59,
   60,    0,   62,   40,   41,   42,   43,   44,   45,   60,
   47,   62,   11,  231,   13,    0,  226,  300,    0,   11,
  138,   13,   59,   60,  300,   62,   40,    0,   63,   11,
  300,   13,    0,   41,   42,   43,   44,   45,   11,   47,
   13,   42,  112,   40,  114,  115,   47,   40,    0,  300,
   41,   59,   60,   46,   62,   40,   41,   59,   43,   44,
   45,   59,   59,   76,   77,  183,   99,    0,   59,  401,
  402,    0,   40,   41,   59,   60,   44,   62,   11,   41,
   13,   40,   44,  403,  413,  414,  415,  301,   40,  104,
  105,   59,   60,  163,   62,  300,  131,  300,  400,   41,
    0,  403,   44,  405,  406,   40,  403,  409,  410,   59,
  302,   40,  145,  146,  147,   41,   59,  403,  404,   40,
  133,   59,   61,    0,  400,  404,  300,  201,  256,  405,
  406,  414,  415,  409,   59,   59,    0,   40,  408,   40,
   40,  176,  256,   41,   41,   99,   59,  170,   41,   41,
  404,   91,  404,  227,  256,   41,  300,  302,  171,  300,
   44,  300,  404,   40,  300,   44,  256,   41,  256,  256,
    0,  170,  300,   41,  402,  256,  256,   59,  170,  256,
  254,   59,   41,  216,   59,   41,  300,    0,  170,   41,
   59,  145,  146,  147,   44,   41,  256,  170,  300,  300,
  302,  302,  300,   41,  302,  300,  302,  302,  303,  302,
   40,    0,  300,  300,  302,  302,  301,   59,  231,  256,
  300,  302,  302,  300,  300,  302,  302,   40,  300,  301,
   59,  300,   59,  301,    0,   41,  256,  170,   91,  402,
  300,  301,  302,  402,  304,  305,  306,  307,  256,   59,
   59,   40,   59,  304,  305,  306,  307,   59,   59,   93,
    0,  256,  216,  300,   44,  302,  300,  304,  305,  306,
  307,  256,  400,   40,   40,  403,  404,  405,  406,  407,
  300,  409,   59,   59,  412,   59,  414,  415,   59,   59,
   44,  404,  300,   93,  302,  402,  304,  305,  306,  307,
   40,  404,   59,   41,  256,  300,  302,   59,   59,  411,
  411,  410,  403,  411,   59,  300,  411,  302,  404,  304,
  305,  306,  307,  411,  411,   59,   40,  256,    0,  404,
   41,  411,  300,   41,  411,  411,  304,  305,  306,  307,
  400,  401,  402,  403,  404,  405,  406,  407,  300,  409,
  410,   93,  412,   59,  414,  415,  256,   59,   93,   64,
  133,  204,  245,  400,  401,  402,  403,  404,  405,  406,
  407,  300,  409,  410,   -1,  412,   -1,  414,  415,  256,
  400,   59,   -1,  403,  404,  405,  406,  226,   -1,  409,
   -1,   -1,  400,  401,  402,  403,  404,  405,  406,  407,
  300,  409,  410,   -1,  412,  400,  414,  415,  403,   -1,
  405,  406,  256,   -1,  409,  400,  401,  402,  403,  404,
  405,  406,  407,  300,  409,  410,  256,  412,   -1,  414,
  415,   -1,  400,  401,  402,  403,  404,  405,  406,  407,
   -1,  409,  410,  256,  412,   -1,  414,  415,  400,  401,
  402,  403,  404,  405,  406,  407,  300,  409,  410,   -1,
  412,   -1,  414,  415,   -1,   -1,   -1,  256,   -1,   -1,
  300,  400,  401,  402,  403,  404,  405,  406,  407,   -1,
  409,  410,   -1,  412,   -1,  414,  415,  300,   -1,   -1,
  256,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  400,  401,  402,  403,  404,  405,  406,  407,   -1,  409,
  410,  300,  412,   -1,  414,  415,  256,   -1,   -1,   -1,
   -1,   -1,   -1,  400,  401,  402,  403,  404,  405,  406,
  407,   -1,  409,  410,  300,  412,   -1,  414,  415,  403,
  404,   -1,   -1,  407,   -1,   -1,   -1,   -1,  412,   -1,
  414,  415,   -1,   -1,  256,   -1,  400,   -1,   -1,  403,
  300,  405,  406,   -1,   -1,  409,   -1,   -1,   -1,   -1,
  400,  401,  402,  403,  404,  405,  406,  407,  256,  409,
  410,   -1,  412,   -1,  414,  415,   -1,  400,  401,  402,
  403,  404,  405,  406,  407,   -1,  409,  410,   -1,  412,
   -1,  414,  415,  256,   -1,   -1,   -1,   -1,   -1,  300,
   -1,  400,  401,  402,  403,  404,  405,  406,  407,   -1,
  409,  410,   -1,  412,  256,  414,  415,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  400,  401,   -1,  403,  404,  405,
  406,  407,  256,  409,  410,   -1,  412,  300,  414,  415,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  400,   -1,   -1,  403,  404,  405,  406,  407,  300,  409,
  410,   -1,  412,   -1,  414,  415,   -1,    0,   -1,   -1,
   -1,   -1,   -1,    6,   -1,   -1,  300,   -1,   11,   12,
   13,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  400,   -1,
   -1,  403,  404,  405,  406,  407,   -1,  409,   -1,  400,
  412,   -1,  414,  415,  405,  406,  407,   -1,  409,   -1,
   -1,  412,  400,  414,  415,  403,  404,  405,  406,  407,
   53,  409,   -1,   -1,  412,   -1,  414,  415,   -1,  300,
   -1,   64,   65,   -1,   -1,   -1,   69,  400,   30,   -1,
  403,  404,  405,  406,  407,   -1,  409,   -1,   -1,  412,
   -1,  414,  415,   -1,   -1,   -1,   48,   49,  400,   -1,
   -1,  403,  404,  405,  406,  407,   99,  409,   -1,   -1,
  412,   -1,  414,  415,   -1,   -1,  400,   -1,   -1,  403,
   -1,  405,  406,  407,   -1,  409,   78,   -1,  412,   -1,
  414,  415,   -1,   -1,   86,   -1,   88,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   98,   -1,   -1,   -1,
  102,  103,  145,  146,  147,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  400,
   -1,   -1,   -1,   -1,  405,  406,  407,  170,  409,   -1,
   -1,  412,   -1,  414,  415,   -1,  138,   -1,  140,   -1,
   -1,  143,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  201,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  216,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  183,   -1,   -1,  227,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  254,
};
}
final static short YYFINAL=10;
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
"programa : sentencias_declarativas bloque_ejecutable",
"nombre_programa : ID",
"bloque_ejecutable : BEGIN lista_sentencias END",
"bloque_ejecutable : BEGIN lista_sentencias",
"bloque_ejecutable : lista_sentencias END",
"bloque_ejecutable : END",
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
"declaracion_variables : tipo lista_variables",
"declaracion_variables : lista_variables ';'",
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
"cierre_funcion : BEGIN lista_sentencias END",
"cierre_funcion : BEGIN lista_sentencias",
"cierre_funcion : lista_sentencias END ';'",
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
"$$1 :",
"declaracion_metodo : tipo_declarado ID $$1 '(' lista_parametros_formales ')' BEGIN lista_sentencias END ';'",
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
"sentencia_if : IF '(' condicion ')' bloque_sentencias",
"sentencia_if : IF '(' condicion ')' bloque_sentencias ELSE bloque_sentencias",
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
"sentencia_repeat_while : REPEAT bloque_sentencias '(' condicion ')' ';'",
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
"expresion : termino CTE",
"expresion : termino error",
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
"lista_parametros_reales : expresion ',' lista_parametros_reales",
"lista_parametros_reales : expresion",
"lista_orden_evaluacion : CTE ',' lista_orden_evaluacion",
"lista_orden_evaluacion : CTE",
"referencia_atributo : ID '.' ID",
"referencia_atributo : ID '.' ID '.' ID",
"referencia_atributo : ID '.' ID '(' lista_parametros_reales ')'",
};

//#line 506 "gramatica.y"

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
//#line 657 "Parser.java"
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
//#line 73 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_PROGRAMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.PROGRAMA, val_peek(1).obj, val_peek(0).obj)); }
break;
case 3:
//#line 79 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.NOMBRE_PROGRAMA, val_peek(0).obj)); }
break;
case 4:
//#line 84 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, val_peek(1).obj)); }
break;
case 5:
//#line 86 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_BLOQUE_SIN_END, val_peek(0).obj)); }
break;
case 6:
//#line 88 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_BEGIN, val_peek(1).obj)); }
break;
case 7:
//#line 90 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_BEGIN)); }
break;
case 8:
//#line 99 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 9:
//#line 101 "gramatica.y"
{ yyval = new ParserVal((Object) null); }
break;
case 20:
//#line 120 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_VARIABLES, val_peek(2).obj, val_peek(1).obj)); }
break;
case 21:
//#line 122 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_VARIABLES, val_peek(1).obj, val_peek(0).obj)); }
break;
case 22:
//#line 125 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_TIPO_VARIABLES, val_peek(1).obj)); }
break;
case 23:
//#line 127 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_LISTA_VARIABLES)); }
break;
case 24:
//#line 133 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_OBJETOS, val_peek(2).obj, val_peek(1).obj)); }
break;
case 25:
//#line 138 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.ULONGINT)); }
break;
case 26:
//#line 140 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.DOUBLEF)); }
break;
case 28:
//#line 146 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, val_peek(0).obj)); }
break;
case 29:
//#line 151 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 30:
//#line 153 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(0).obj)); }
break;
case 31:
//#line 155 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_COMA_VARIABLES);
          yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(1).obj, val_peek(0).obj)); }
break;
case 32:
//#line 165 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(2).obj, val_peek(0).obj); }
break;
case 33:
//#line 167 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_FUNCION);
          acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(1).obj, null); }
break;
case 34:
//#line 173 "gramatica.y"
{ yyval = val_peek(0); }
break;
case 35:
//#line 178 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 36:
//#line 180 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 37:
//#line 183 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_BLOQUE_SIN_END);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 38:
//#line 186 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_BEGIN);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 39:
//#line 192 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 40:
//#line 194 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, val_peek(0).obj)); }
break;
case 41:
//#line 201 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, val_peek(1).obj, val_peek(0).obj)); }
break;
case 42:
//#line 203 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, val_peek(1).obj, val_peek(0).obj)); }
break;
case 43:
//#line 205 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_PARAMETRO, val_peek(0).obj)); }
break;
case 44:
//#line 207 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_TIPO_PARAMETRO, val_peek(0).obj)); }
break;
case 45:
//#line 217 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_CLASE, val_peek(0).obj);
          yyval = val_peek(0); }
break;
case 46:
//#line 223 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, val_peek(4).obj)); }
break;
case 47:
//#line 225 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, val_peek(3).obj)); }
break;
case 48:
//#line 231 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 49:
//#line 233 "gramatica.y"
{ yyval = new ParserVal((Object) null); }
break;
case 53:
//#line 244 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_ATRIBUTO, val_peek(2).obj, val_peek(1).obj)); }
break;
case 54:
//#line 249 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(1).obj, val_peek(0).obj); }
break;
case 55:
//#line 252 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_METODO, val_peek(8).obj)); }
break;
case 56:
//#line 257 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_EXTENDS, val_peek(1).obj)); }
break;
case 57:
//#line 259 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_LISTA_EXTENDS)); }
break;
case 58:
//#line 261 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_LISTA_EXTENDS)); }
break;
case 59:
//#line 266 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, val_peek(2).obj, val_peek(0).obj)); }
break;
case 60:
//#line 268 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, val_peek(0).obj)); }
break;
case 61:
//#line 278 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_TYPEDEF, val_peek(5).obj, val_peek(2).obj)); }
break;
case 62:
//#line 280 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_VALORES_ENUMERADO, val_peek(4).obj)); }
break;
case 63:
//#line 285 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 64:
//#line 287 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, val_peek(0).obj)); }
break;
case 65:
//#line 296 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 66:
//#line 298 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(0).obj)); }
break;
case 68:
//#line 304 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 73:
//#line 310 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_SENTENCIA)); }
break;
case 75:
//#line 316 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, val_peek(1).obj)); }
break;
case 76:
//#line 321 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 77:
//#line 323 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 78:
//#line 325 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_ASIGNACION_INVALIDA)); }
break;
case 79:
//#line 334 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF, val_peek(4).obj, val_peek(2).obj)); }
break;
case 80:
//#line 336 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF_ELSE, val_peek(6).obj, val_peek(4).obj, val_peek(2).obj)); }
break;
case 81:
//#line 338 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 82:
//#line 340 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 83:
//#line 342 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, val_peek(2).obj, val_peek(0).obj)); }
break;
case 84:
//#line 344 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, val_peek(4).obj, val_peek(2).obj, val_peek(0).obj)); }
break;
case 85:
//#line 346 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
break;
case 86:
//#line 348 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
break;
case 87:
//#line 350 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_INVALIDA)); }
break;
case 88:
//#line 355 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONDICION, val_peek(2).obj, val_peek(1).obj, val_peek(0).obj)); }
break;
case 95:
//#line 373 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_REPEAT_WHILE, val_peek(5).obj, val_peek(2).obj)); }
break;
case 96:
//#line 375 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 97:
//#line 377 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_CUERPO_ITERACION, val_peek(2).obj)); }
break;
case 98:
//#line 379 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_WHILE, val_peek(4).obj, val_peek(2).obj)); }
break;
case 99:
//#line 381 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
break;
case 100:
//#line 383 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
break;
case 101:
//#line 392 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 102:
//#line 394 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 103:
//#line 396 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ARGUMENTO_POUT)); }
break;
case 104:
//#line 398 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 105:
//#line 400 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 106:
//#line 405 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, val_peek(2).obj)); }
break;
case 107:
//#line 407 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, val_peek(1).obj)); }
break;
case 108:
//#line 417 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SUMA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 109:
//#line 419 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.RESTA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 110:
//#line 421 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 111:
//#line 423 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERADOR, val_peek(1).obj, val_peek(0).obj)); }
break;
case 112:
//#line 425 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERADOR, val_peek(1).obj)); }
break;
case 114:
//#line 431 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.MULTIPLICACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 115:
//#line 433 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DIVISION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 116:
//#line 435 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 117:
//#line 437 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 119:
//#line 443 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_ID, val_peek(0).obj)); }
break;
case 120:
//#line 445 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE, val_peek(0).obj)); }
break;
case 126:
//#line 455 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE_NEGATIVA, val_peek(0).obj)); }
break;
case 127:
//#line 461 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
case 128:
//#line 463 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_ASIG_DONDE_IGUAL);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
case 129:
//#line 469 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONVERSION_TODF, val_peek(1).obj)); }
break;
case 130:
//#line 475 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_FUNCION, val_peek(6).obj, val_peek(4).obj, val_peek(1).obj)); }
break;
case 131:
//#line 477 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(5).obj, val_peek(3).obj)); }
break;
case 132:
//#line 479 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(3).obj, val_peek(1).obj)); }
break;
case 133:
//#line 484 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 134:
//#line 486 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(0).obj)); }
break;
case 135:
//#line 491 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 136:
//#line 493 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(0).obj)); }
break;
case 137:
//#line 498 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 138:
//#line 500 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO_PREFIJADO, val_peek(4).obj, val_peek(2).obj, val_peek(0).obj)); }
break;
case 139:
//#line 502 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_METODO, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
//#line 1241 "Parser.java"
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
