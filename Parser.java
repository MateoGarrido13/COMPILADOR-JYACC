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
    6,    6,    7,   16,   16,   18,   18,   17,   17,   17,
   19,   19,    8,   21,   21,   21,   21,   20,   20,   22,
   22,   22,   22,   23,    9,    9,   24,   24,   25,   25,
   25,   26,   29,   27,   28,   28,   28,   30,   30,   10,
   10,   31,   31,    4,    4,   33,   33,   33,   33,   33,
   33,   33,   34,   34,   11,   11,   11,   12,   12,   12,
   12,   12,   12,   12,   12,   12,   37,   38,   38,   38,
   38,   38,   38,   13,   13,   13,   13,   13,   13,   14,
   14,   14,   14,   14,   15,   15,   35,   35,   35,   35,
   35,   35,   39,   39,   39,   39,   39,   40,   40,   40,
   40,   40,   40,   32,   32,   42,   42,   43,   41,   41,
   41,   44,   44,   46,   46,   45,   45,   36,   36,   36,
};
final static short yylen[] = {                            2,
    3,    2,    1,    3,    2,    2,    1,    2,    0,    1,
    1,    1,    1,    1,    2,    1,    1,    1,    1,    3,
    2,    3,    3,    1,    1,    1,    1,    3,    1,    2,
    3,    2,    6,    4,    3,    2,    3,    3,    1,    2,
    2,    1,    1,    2,    5,    4,    2,    0,    1,    1,
    1,    3,    0,   10,    3,    2,    3,    3,    1,    7,
    6,    3,    1,    2,    1,    2,    1,    1,    1,    1,
    1,    2,    1,    3,    3,    3,    3,    7,    9,    6,
    8,    5,    7,    6,    7,    7,    3,    1,    1,    1,
    1,    1,    1,    7,    6,    6,    6,    6,    6,    5,
    5,    4,    4,    4,    5,    4,    3,    3,    3,    2,
    2,    1,    3,    3,    3,    3,    1,    1,    1,    1,
    1,    1,    1,    1,    2,    5,    5,    4,    7,    6,
    4,    3,    1,    3,    1,    3,    1,    3,    5,    6,
};
final static short yydefred[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,   24,   25,    0,
    0,    0,    0,   10,   11,   12,   13,   14,    0,   16,
   17,   18,   19,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  124,    0,    0,    0,  119,    0,  121,    0,
    0,    0,  120,  122,  123,    0,    0,   44,    0,    0,
    0,    0,    0,   68,   69,   70,   71,   73,    0,    0,
    0,    0,    0,    7,    2,    0,    0,    8,   15,    0,
    0,    0,    0,    0,    0,    0,   30,   77,   75,    0,
   23,    0,    0,    0,    0,    0,    0,  125,   88,   89,
   90,   91,   92,   93,    0,    0,  111,  110,    0,    0,
    0,    0,    0,    0,    0,    0,   72,    0,    0,   66,
    0,    0,    0,    1,    0,    6,   64,   22,   31,   20,
    0,    0,    0,    0,   27,    0,   26,    0,    0,    0,
   49,   50,   51,   76,   28,    0,    0,    0,    0,  135,
    0,    0,    0,    0,    0,    0,    0,   87,    0,  109,
  107,  108,  115,  113,  116,  114,    0,  102,    0,    0,
   74,    0,    0,    0,    0,    0,    4,   41,   40,    0,
    0,    0,    0,   56,    0,    0,    0,    0,   47,    0,
  139,    0,    0,    0,    0,    0,  128,    0,    0,    0,
    0,  100,  101,  105,    0,    0,    0,    0,    0,    0,
    0,    0,   38,   57,    0,   55,    0,   52,   45,  140,
  127,  134,    0,  132,  126,    0,    0,    0,    0,   84,
   96,   99,    0,   98,   97,   61,    0,    0,    0,    0,
   33,   58,    0,    0,  130,    0,   86,   85,    0,   78,
   94,   60,   62,    0,    0,    0,    0,  129,    0,    0,
   37,    0,  136,   79,   34,    0,    0,    0,   54,
};
final static short yydgoto[] = {                         10,
   11,   12,   65,   66,   13,   14,   15,   16,   17,   18,
   53,   54,   55,   56,   57,   24,   31,  128,   25,  123,
  231,  124,   26,  129,  130,  131,  132,  133,  207,  175,
  200,   37,   67,   59,   38,   39,   40,   95,   41,   42,
   43,   44,   45,  141,  236,  142,
};
final static short yysindex[] = {                       511,
   16,  -40,   19,   48, -279,  388, -264,    0,    0,    0,
  537,  441,  537,    0,    0,    0,    0,    0,   -8,    0,
    0,    0,    0, -178,   63, -363, -194,   14,  -19, -182,
   64,  -23,    0,   89,  -16, -171,    0,   39,    0,  103,
   -4,   33,    0,    0,    0,  -27,  -32,    0,   86,   26,
  414,  106,   98,    0,    0,    0,    0,    0,  -38,   87,
   16,  441,  414,    0,    0, -254,  414,    0,    0,  107,
 -131,  111, -250, -289,  -32, -129,    0,    0,    0,  -24,
    0,  133,   37,  135,  -32,  136,  -34,    0,    0,    0,
    0,    0,    0,    0,  -32,  484,    0,    0,  -15,  -32,
   11,   34,  139,  123,  142,  144,    0, -218,  -32,    0,
  -37,  -32,   96,    0, -214,    0,    0,    0,    0,    0,
 -108, -107,  153,  151,    0,  -53,    0, -104, -206, -289,
    0,    0,    0,    0,    0,   37, -101,  -32,    9,    0,
  159,  158,  -32,  164,  484,  484,  484,    0, -198,    0,
    0,    0,    0,    0,    0,    0,  147,    0,  148,  150,
    0,  169,  -32,  171,  172,  -20,    0,    0,    0,  537,
 -250,  155,  176,    0,  156,   14,  162,  167,    0,  182,
    0,  188,  -36,  141,   37,  192,    0, -168, -167, -274,
  177,    0,    0,    0,  179,   51,  180,  183,  186,  160,
  199,  504,    0,    0,  -51,    0,  211,    0,    0,    0,
    0,    0,  -83,    0,    0,  195,  196,  484,  200,    0,
    0,    0,  202,    0,    0,    0,  215,  -25,  414, -146,
    0,    0, -250,  247,    0,  201,    0,    0, -106,    0,
    0,    0,    0, -105,  238,  263,   13,    0,  259,  264,
    0,  -81,    0,    0,    0,  414,  -64,  266,    0,
};
final static short yyrindex[] = {                      -244,
  424,    0,    0,    0,    0,    0,    0,    0,    0,    0,
 -244,    0, -244,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  210,    0,    0,
    0,   53,    0,    0,    0,    0,    0,    0,    0,    0,
   93,   76,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  109,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  197,    0,    0,    0,
  301,  347,    0,  -62,    0,    0,    0,    0,    0,    1,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  349,    0,    0,    0,    0,    0,
   61,   70,    0,  309,    0,    0,    0,    0,    0,  -62,
    0,    0,    0,    0,    0,    0,    0,    0,  -10,    0,
    0,  310,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  132,    0,  161,  184,
    0,    0,    0,    0,    0,    0,    0,    0,    0, -241,
    0,    0,  293,    0,    0,  -12,    0,  371,    0,    0,
    0,    0,    0,   24,    0,    0,    0,    0,    0,  321,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  261,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  231,    0,
    0,    0,  248,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  269,    0,    0,    0,    0,  295,    0,
    0,    0,    0,  455,    0,    0,    0,    0,  272,  403,
    0,    0,    0,    0,    0,    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
    0,   41,  294,   91,    0,    0,    0,    0,    0,    0,
   74,  130,  163,  168,  178,   17,   80,    0,    0, -152,
    0,    0,    0,  233,    0,    0,    0,    0,    0,  181,
  138, -139,   71,  223,  796,  706,  116,    0,   38,    0,
    0,    0,    0,  -79,  117,    0,
};
final static int YYTABLESIZE=981;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         35,
  138,  112,  163,  143,   36,  174,  147,   36,   36,  235,
  125,    9,   36,  104,    9,  136,   83,   36,  203,   36,
   48,  137,   30,  131,   36,   36,  201,   53,   36,   36,
  118,  118,  118,  118,  118,   60,  118,   84,   99,   74,
  100,  138,  138,  138,  138,  138,   29,  138,   83,  121,
   69,   62,  118,   68,   30,   36,  180,   76,   46,  138,
  138,   30,  138,  131,  131,  131,  131,  131,  131,  183,
  131,   30,  199,   19,  101,  117,   58,   70,   36,  102,
  246,   36,  131,  131,   19,  131,   19,   47,  201,  122,
  127,  223,  112,  118,  118,  118,  118,  118,   94,  118,
   93,   43,   73,   72,   43,  214,   75,   77,   67,  222,
   42,  118,  118,   42,  118,  117,  117,   80,  117,  117,
  117,   28,   81,  126,    8,    9,  218,  219,   85,   20,
   88,  103,  112,  112,  117,  117,  112,  117,  154,  156,
   20,  108,   20,   96,  107,  109,  127,  113,   67,  116,
   87,  112,  112,  115,  112,  135,  110,  117,    9,    9,
  104,    9,   21,    8,    9,  118,   58,   22,  119,  120,
   28,  103,  138,   21,  143,   21,  145,   23,   22,  157,
   22,  158,  159,  106,  160,  161,  166,  122,   23,  167,
   23,  168,  169,  170,  171,  176,   65,  178,  181,  184,
  104,  185,  172,  191,  187,  192,  193,  177,  194,  195,
  202,  197,  198,  204,  206,   58,   58,   58,  234,  205,
  208,  146,  210,  106,  162,  209,  164,  165,  211,   71,
   80,  213,  215,  216,  217,  220,   78,  221,  224,   86,
  150,  225,  228,   19,  226,  118,  173,   95,  173,  122,
  233,   97,  227,  237,  238,   77,  138,  245,  240,   32,
  241,   33,   32,   32,   33,   33,  153,   32,   29,   33,
   80,   81,   32,  242,   33,  103,   33,   82,  196,  131,
   32,   33,   33,   32,   32,   33,   33,   95,   58,  155,
  247,  118,  230,  248,   83,  249,  251,   98,  250,   20,
  138,  138,  138,  252,  138,  138,  138,  138,  118,   82,
   32,   81,   33,   28,  234,   28,   29,  254,  149,  244,
   82,  256,  255,  131,  259,  131,   29,  131,  131,  131,
  131,  117,   21,   32,   83,   33,  139,   22,   33,  258,
   32,   48,   89,   90,   91,   92,  257,   23,    5,   39,
  133,   59,  118,   63,  118,  114,  118,  118,  118,  118,
   82,  137,  179,  253,   67,  243,    0,  188,  189,  190,
   34,  111,    0,   34,   34,  117,    0,  117,   34,  117,
  117,  117,  117,   34,    0,  232,    0,  103,    0,    0,
    0,   34,  112,    0,   34,   34,  112,  112,  112,  112,
  138,  138,  138,  138,  138,  138,  138,  138,   67,  138,
  138,    0,  138,    0,  138,  138,  104,    0,    0,    0,
    0,   34,    0,  131,  131,  131,  131,  131,  131,  131,
  131,  103,  131,  131,    0,  131,    0,  131,  131,  106,
  239,    0,    0,    0,   34,    0,    0,   34,    0,    0,
    0,    0,  118,  118,  118,  118,  118,  118,  118,  118,
  104,  118,  118,    0,  118,   29,  118,  118,    0,    0,
    0,    0,    0,    0,    0,  117,  117,  117,  117,  117,
  117,  117,  117,  106,  117,  117,   80,  117,    0,  117,
  117,    0,  112,  112,  112,  112,  112,  112,  112,  112,
    0,  112,  112,   95,  112,    0,  112,  112,   67,   67,
   67,   67,   67,   67,   67,   67,    0,   67,   67,    0,
   67,    0,   67,   67,    0,    0,    0,   81,    0,    0,
   80,  103,  103,  103,  103,  103,  103,  103,  103,    0,
  103,  103,    0,  103,    0,  103,  103,   95,    0,    0,
   83,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  104,  104,  104,  104,  104,  104,  104,  104,    0,  104,
  104,   81,  104,    0,  104,  104,   82,    0,    0,    0,
    0,    0,    0,  106,  106,  106,  106,  106,  106,  106,
  106,    0,  106,  106,   83,  106,    0,  106,  106,   65,
   65,    0,   21,   65,    0,    0,    0,    0,   65,   29,
   65,   65,   29,   29,   29,   29,   29,    0,   29,    0,
   82,   29,    0,   29,   29,    0,   46,    0,    0,    0,
   80,   80,   80,   80,   80,   80,   80,   80,    0,   80,
   80,    0,   80,   49,   80,   80,   21,   95,   95,   95,
   95,   95,   95,   95,   95,    0,   95,   95,   35,   95,
    0,   95,   95,    0,    0,    0,    0,    0,    0,   49,
   46,   81,   81,   81,   81,   81,   81,   81,   81,    3,
   81,   81,    0,   81,    0,   81,   81,   50,    0,    0,
    0,    0,    0,    0,   83,   83,   49,   83,   83,   83,
   83,   83,   35,   83,   83,   27,   83,    0,   83,   83,
   36,   27,    0,   50,    0,    0,   27,   27,   27,    0,
   82,    0,    0,   82,   82,   82,   82,   82,    0,   82,
   82,    0,   82,    0,   82,   82,    0,    0,    0,   49,
   50,    0,    0,    0,    0,    0,   21,    0,    0,   21,
   21,   21,   21,   21,   36,   21,   27,    0,   21,   49,
   21,   21,    0,    0,    0,    0,    0,   27,   27,    0,
   46,    0,   27,   46,   46,   46,   46,   46,    0,   46,
    0,    0,   46,   50,   46,   46,    0,    2,    0,    0,
   51,    0,    3,    4,    0,    0,    6,   52,    0,    0,
    0,   27,   35,   50,    0,   35,   35,   35,   35,   35,
    1,   35,    0,    2,   35,    0,   35,   35,    3,    4,
    0,    0,    6,    3,   79,    0,    3,    3,    3,    3,
    3,    0,    3,    0,    0,    3,   61,    3,    3,    0,
    2,  105,  106,   63,   64,    3,    4,    0,    0,    6,
   27,   27,   27,    0,   36,    0,    0,   36,    0,   36,
   36,   36,    0,   36,    0,    0,   36,    0,   36,   36,
  134,    0,    0,    0,    0,   27,    0,    0,  140,    0,
  144,    0,    0,    2,    0,    0,   51,    0,    3,    4,
  148,    0,    6,    0,  151,  152,    0,    0,    0,    0,
    0,    0,    0,    2,    0,    0,  229,   27,    3,    4,
    2,    0,    6,    0,    0,    3,    4,    5,    0,    6,
    0,    0,    7,   27,    8,    9,    0,    0,    0,    0,
    0,  140,    0,  182,   27,    0,    2,    0,  186,    0,
    0,    3,    4,    5,    0,    6,    0,    0,    7,    0,
    8,    9,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   27,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  212,    0,
  140,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         40,
    0,   40,   40,   40,   45,   59,   41,   45,   45,   93,
  300,  256,   45,   41,  256,   40,   40,   45,  171,   45,
  300,   46,   46,    0,   45,   45,  166,   40,   45,   45,
   41,   42,   43,   44,   45,  300,   47,   61,   43,  403,
   45,   41,   42,   43,   44,   45,   59,   47,   40,  300,
   59,   11,    0,   13,   46,   45,  136,   44,   40,   59,
   60,   46,   62,   40,   41,   42,   43,   44,   45,   61,
   47,   46,   93,    0,   42,    0,    6,  256,   45,   47,
  233,   45,   59,   60,   11,   62,   13,   40,  228,   73,
   74,   41,    0,   41,   42,   43,   44,   45,   60,   47,
   62,   41,   40,   24,   44,  185,  301,   28,    0,   59,
   41,   59,   60,   44,   62,   40,   41,  300,   43,   44,
   45,  300,   59,  413,  414,  415,  401,  402,   40,    0,
  302,    0,   40,   41,   59,   60,   44,   62,  101,  102,
   11,   51,   13,   41,   59,   40,  130,   61,   40,  404,
   35,   59,   60,   63,   62,   76,   59,   67,  403,  404,
    0,  403,    0,  414,  415,   59,   96,    0,  300,   59,
  300,   40,   40,   11,   40,   13,   41,    0,   11,   41,
   13,   59,   41,    0,   41,  404,   91,  171,   11,  404,
   13,  300,  300,   41,   44,  300,    0,  404,  300,   41,
   40,   44,  256,  402,   41,   59,   59,  128,   59,   41,
  170,   41,   41,   59,   59,  145,  146,  147,  302,   44,
   59,  256,   41,   40,  109,   59,  111,  112,   41,  408,
    0,   91,   41,  402,  402,   59,  256,   59,   59,  256,
  256,   59,   44,  170,   59,  256,  300,    0,  300,  233,
   40,  256,   93,   59,   59,  176,  256,  404,   59,  300,
   59,  302,  300,  300,  302,  302,  256,  300,   59,  302,
   40,    0,  300,   59,  302,  303,  302,  301,  163,  256,
  300,  302,  302,  300,  300,  302,  302,   40,  218,  256,
   44,  302,  202,   93,    0,  402,   59,  302,  404,  170,
  300,  301,  302,   41,  304,  305,  306,  307,  256,  301,
  300,   40,  302,  300,  302,  300,  301,   59,   96,  229,
    0,  403,   59,  300,   59,  302,  301,  304,  305,  306,
  307,  256,  170,  300,   40,  302,  300,  170,  302,  404,
   40,  404,  304,  305,  306,  307,  256,  170,    0,   41,
   41,   59,  300,   93,  302,   62,  304,  305,  306,  307,
   40,   93,  130,  247,  256,  228,   -1,  145,  146,  147,
  411,  410,   -1,  411,  411,  300,   -1,  302,  411,  304,
  305,  306,  307,  411,   -1,  205,   -1,  256,   -1,   -1,
   -1,  411,  300,   -1,  411,  411,  304,  305,  306,  307,
  400,  401,  402,  403,  404,  405,  406,  407,  300,  409,
  410,   -1,  412,   -1,  414,  415,  256,   -1,   -1,   -1,
   -1,  411,   -1,  400,  401,  402,  403,  404,  405,  406,
  407,  300,  409,  410,   -1,  412,   -1,  414,  415,  256,
  218,   -1,   -1,   -1,  411,   -1,   -1,  411,   -1,   -1,
   -1,   -1,  400,  401,  402,  403,  404,  405,  406,  407,
  300,  409,  410,   -1,  412,  256,  414,  415,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  400,  401,  402,  403,  404,
  405,  406,  407,  300,  409,  410,  256,  412,   -1,  414,
  415,   -1,  400,  401,  402,  403,  404,  405,  406,  407,
   -1,  409,  410,  256,  412,   -1,  414,  415,  400,  401,
  402,  403,  404,  405,  406,  407,   -1,  409,  410,   -1,
  412,   -1,  414,  415,   -1,   -1,   -1,  256,   -1,   -1,
  300,  400,  401,  402,  403,  404,  405,  406,  407,   -1,
  409,  410,   -1,  412,   -1,  414,  415,  300,   -1,   -1,
  256,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  400,  401,  402,  403,  404,  405,  406,  407,   -1,  409,
  410,  300,  412,   -1,  414,  415,  256,   -1,   -1,   -1,
   -1,   -1,   -1,  400,  401,  402,  403,  404,  405,  406,
  407,   -1,  409,  410,  300,  412,   -1,  414,  415,  403,
  404,   -1,  256,  407,   -1,   -1,   -1,   -1,  412,  400,
  414,  415,  403,  404,  405,  406,  407,   -1,  409,   -1,
  300,  412,   -1,  414,  415,   -1,  256,   -1,   -1,   -1,
  400,  401,  402,  403,  404,  405,  406,  407,   -1,  409,
  410,   -1,  412,  256,  414,  415,  300,  400,  401,  402,
  403,  404,  405,  406,  407,   -1,  409,  410,  256,  412,
   -1,  414,  415,   -1,   -1,   -1,   -1,   -1,   -1,  256,
  300,  400,  401,  402,  403,  404,  405,  406,  407,  256,
  409,  410,   -1,  412,   -1,  414,  415,  300,   -1,   -1,
   -1,   -1,   -1,   -1,  400,  401,  256,  403,  404,  405,
  406,  407,  300,  409,  410,    0,  412,   -1,  414,  415,
  256,    6,   -1,  300,   -1,   -1,   11,   12,   13,   -1,
  400,   -1,   -1,  403,  404,  405,  406,  407,   -1,  409,
  410,   -1,  412,   -1,  414,  415,   -1,   -1,   -1,  256,
  300,   -1,   -1,   -1,   -1,   -1,  400,   -1,   -1,  403,
  404,  405,  406,  407,  300,  409,   51,   -1,  412,  256,
  414,  415,   -1,   -1,   -1,   -1,   -1,   62,   63,   -1,
  400,   -1,   67,  403,  404,  405,  406,  407,   -1,  409,
   -1,   -1,  412,  300,  414,  415,   -1,  400,   -1,   -1,
  403,   -1,  405,  406,   -1,   -1,  409,  410,   -1,   -1,
   -1,   96,  400,  300,   -1,  403,  404,  405,  406,  407,
  300,  409,   -1,  400,  412,   -1,  414,  415,  405,  406,
   -1,   -1,  409,  400,   29,   -1,  403,  404,  405,  406,
  407,   -1,  409,   -1,   -1,  412,  300,  414,  415,   -1,
  400,   46,   47,  403,  404,  405,  406,   -1,   -1,  409,
  145,  146,  147,   -1,  400,   -1,   -1,  403,   -1,  405,
  406,  407,   -1,  409,   -1,   -1,  412,   -1,  414,  415,
   75,   -1,   -1,   -1,   -1,  170,   -1,   -1,   83,   -1,
   85,   -1,   -1,  400,   -1,   -1,  403,   -1,  405,  406,
   95,   -1,  409,   -1,   99,  100,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  400,   -1,   -1,  403,  202,  405,  406,
  400,   -1,  409,   -1,   -1,  405,  406,  407,   -1,  409,
   -1,   -1,  412,  218,  414,  415,   -1,   -1,   -1,   -1,
   -1,  136,   -1,  138,  229,   -1,  400,   -1,  143,   -1,
   -1,  405,  406,  407,   -1,  409,   -1,   -1,  412,   -1,
  414,  415,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  256,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  183,   -1,
  185,
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

//#line 512 "gramatica.y"

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
//#line 656 "Parser.java"
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
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_LISTA_VARIABLES)); }
break;
case 23:
//#line 131 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_OBJETOS, val_peek(2).obj, val_peek(1).obj)); }
break;
case 24:
//#line 136 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.ULONGINT)); }
break;
case 25:
//#line 138 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, Tipos.DOUBLEF)); }
break;
case 27:
//#line 144 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.TIPO, val_peek(0).obj)); }
break;
case 28:
//#line 149 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 29:
//#line 151 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(0).obj)); }
break;
case 30:
//#line 153 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_COMA_VARIABLES);
          yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VARIABLES, val_peek(1).obj, val_peek(0).obj)); }
break;
case 31:
//#line 163 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(2).obj, val_peek(0).obj); }
break;
case 32:
//#line 165 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_FUNCION);
          acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(1).obj, null); }
break;
case 33:
//#line 171 "gramatica.y"
{ yyval = val_peek(0); }
break;
case 34:
//#line 176 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 35:
//#line 178 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 36:
//#line 181 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_BLOQUE_SIN_END);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 37:
//#line 184 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_BEGIN);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_FUNCION)); }
break;
case 38:
//#line 190 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 39:
//#line 192 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_FORMALES, val_peek(0).obj)); }
break;
case 40:
//#line 199 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, val_peek(1).obj, val_peek(0).obj)); }
break;
case 41:
//#line 201 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_FORMAL, val_peek(1).obj, val_peek(0).obj)); }
break;
case 42:
//#line 203 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_NOMBRE_PARAMETRO, val_peek(0).obj)); }
break;
case 43:
//#line 205 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_TIPO_PARAMETRO, val_peek(0).obj)); }
break;
case 44:
//#line 215 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_CLASE, val_peek(0).obj);
          yyval = val_peek(0); }
break;
case 45:
//#line 221 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, val_peek(4).obj)); }
break;
case 46:
//#line 223 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_CLASE, val_peek(3).obj)); }
break;
case 47:
//#line 229 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 48:
//#line 231 "gramatica.y"
{ yyval = new ParserVal((Object) null); }
break;
case 52:
//#line 242 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_ATRIBUTO, val_peek(2).obj, val_peek(1).obj)); }
break;
case 53:
//#line 247 "gramatica.y"
{ acciones.ejecutar(Reglas.INICIO_FUNCION, val_peek(1).obj, val_peek(0).obj); }
break;
case 54:
//#line 250 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_METODO, val_peek(8).obj)); }
break;
case 55:
//#line 255 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_EXTENDS, val_peek(1).obj)); }
break;
case 56:
//#line 257 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_LISTA_EXTENDS)); }
break;
case 57:
//#line 259 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_LISTA_EXTENDS)); }
break;
case 58:
//#line 264 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, val_peek(2).obj, val_peek(0).obj)); }
break;
case 59:
//#line 266 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_CLASES_HEREDADAS, val_peek(0).obj)); }
break;
case 60:
//#line 276 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DECL_TYPEDEF, val_peek(5).obj, val_peek(2).obj)); }
break;
case 61:
//#line 278 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_VALORES_ENUMERADO, val_peek(4).obj)); }
break;
case 62:
//#line 283 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 63:
//#line 285 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_VALORES_ENUMERADO, val_peek(0).obj)); }
break;
case 64:
//#line 294 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(1).obj, val_peek(0).obj)); }
break;
case 65:
//#line 296 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_SENTENCIAS, val_peek(0).obj)); }
break;
case 67:
//#line 302 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 72:
//#line 308 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_SENTENCIA)); }
break;
case 74:
//#line 314 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.BLOQUE_EJECUTABLE, val_peek(1).obj)); }
break;
case 75:
//#line 319 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 76:
//#line 321 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 77:
//#line 323 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_ASIGNACION_INVALIDA)); }
break;
case 78:
//#line 332 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF, val_peek(4).obj, val_peek(2).obj)); }
break;
case 79:
//#line 334 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_IF_ELSE, val_peek(6).obj, val_peek(4).obj, val_peek(2).obj)); }
break;
case 80:
//#line 336 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 81:
//#line 338 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 82:
//#line 340 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, val_peek(2).obj, val_peek(0).obj)); }
break;
case 83:
//#line 342 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_END_IF, val_peek(4).obj, val_peek(2).obj, val_peek(0).obj)); }
break;
case 84:
//#line 344 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
break;
case 85:
//#line 346 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
break;
case 86:
//#line 348 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_INVALIDA)); }
break;
case 87:
//#line 353 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONDICION, val_peek(2).obj, val_peek(1).obj, val_peek(0).obj)); }
break;
case 94:
//#line 371 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_REPEAT_WHILE, val_peek(5).obj, val_peek(2).obj)); }
break;
case 95:
//#line 373 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 96:
//#line 375 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_CUERPO_ITERACION, val_peek(2).obj)); }
break;
case 97:
//#line 377 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_WHILE, val_peek(4).obj, val_peek(2).obj)); }
break;
case 98:
//#line 379 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PARENTESIS_APERTURA)); }
break;
case 99:
//#line 381 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_CONDICION_SIN_CIERRE)); }
break;
case 100:
//#line 390 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 101:
//#line 392 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_POUT, val_peek(2).obj)); }
break;
case 102:
//#line 394 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ARGUMENTO_POUT)); }
break;
case 103:
//#line 396 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 104:
//#line 398 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA)); }
break;
case 105:
//#line 403 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, val_peek(2).obj)); }
break;
case 106:
//#line 405 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_FALTA_PUNTO_Y_COMA);
          yyval = new ParserVal(acciones.ejecutar(Reglas.SENTENCIA_RET, val_peek(1).obj)); }
break;
case 107:
//#line 415 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.SUMA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 108:
//#line 417 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.RESTA, val_peek(2).obj, val_peek(0).obj)); }
break;
case 109:
//#line 419 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 110:
//#line 421 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERADOR, val_peek(1).obj, val_peek(0).obj)); }
break;
case 111:
//#line 423 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERADOR, val_peek(1).obj)); }
break;
case 113:
//#line 429 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.MULTIPLICACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 114:
//#line 431 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.DIVISION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 115:
//#line 433 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 116:
//#line 435 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_OPERANDO, val_peek(2).obj)); }
break;
case 118:
//#line 441 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_ID, val_peek(0).obj)); }
break;
case 119:
//#line 443 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE, val_peek(0).obj)); }
break;
case 125:
//#line 453 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.FACTOR_CONSTANTE_NEGATIVA, val_peek(0).obj)); }
break;
case 126:
//#line 459 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
case 127:
//#line 461 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_ASIG_DONDE_IGUAL);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
case 128:
//#line 467 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.CONVERSION_TODF, val_peek(1).obj)); }
break;
case 129:
//#line 473 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_FUNCION, val_peek(6).obj, val_peek(4).obj, val_peek(1).obj)); }
break;
case 130:
//#line 475 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(5).obj, val_peek(3).obj)); }
break;
case 131:
//#line 477 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(3).obj, val_peek(1).obj)); }
break;
case 132:
//#line 484 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(2).obj, val_peek(0).obj)); }
break;
case 133:
//#line 486 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_PARAMETROS_REALES, val_peek(0).obj)); }
break;
case 134:
//#line 491 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.PARAMETRO_REAL_NOMBRADO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 136:
//#line 497 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(2).obj, val_peek(0).obj)); }
break;
case 137:
//#line 499 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.LISTA_ORDEN_EVALUACION, val_peek(0).obj)); }
break;
case 138:
//#line 504 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO, val_peek(2).obj, val_peek(0).obj)); }
break;
case 139:
//#line 506 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ACCESO_ATRIBUTO_PREFIJADO, val_peek(4).obj, val_peek(2).obj, val_peek(0).obj)); }
break;
case 140:
//#line 508 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.INVOCACION_METODO, val_peek(5).obj, val_peek(3).obj, val_peek(1).obj)); }
break;
//#line 1240 "Parser.java"
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
