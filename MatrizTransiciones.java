public class MatrizTransiciones {

    // Constantes de control
    public static final int ERROR = -1;
    public static final int ESTADO_F = 99; // Estado Final
    public static final int SIN_SEM = 0;   // Sin acción semántica

    /*
     * Columnas (Simbolos):
     * [0]  d           [6]  !          [12] }          [18] l
     * [1]  l           [7]  *          [13] .          [19] d
     * [2]  M           [8]  +          [14] $          [20] otro
     * [3]  <,>         [9]  -          [15] (,),',';   [21] Blanco, Tab
     * [4]  =           [10] /          [16] _          [22] \n
     * [5]  :           [11] {          [17] u
     */

    // Matriz de Transición de Estados
    public static final int[][] MATRIZ_ESTADOS = {
        //   d,   l,   M, <,>,   =,   :,   !,   *,   +,   -,   /,   {,   },   .,   $, (,), _,   u,   l,   d,otro, BT,  \n
        {   10,  16,  15,   4,   2,   1,   3,  99,   9,   9,  99,   5,  -1,  12,  -1,  99, -1,  -1,  -1,  -1, -1, -1, -1}, // Estado 0
        {   -1,  -1,  -1,  -1,  99,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, -1,  -1,  -1,  -1, -1, -1, -1}, // Estado 1
        {   -1,  -1,  -1,  -1,  99,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, -1,  -1,  -1,  -1, -1, 99, -1}, // Estado 2
        {   -1,  -1,  -1,  -1,  99,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, -1,  -1,  -1,  -1, -1, -1, -1}, // Estado 3
        {   -1,  -1,  -1,  -1,  99,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, -1,  -1,  -1,  -1, -1, 99, -1}, // Estado 4
        {    6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   7,   6,   6,   6,   6, -1,  -1,  -1,  -1,  6,  6,  0}, // Estado 5
        {    6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,  99,   6,   6,   6, -1,  -1,  -1,  -1,  6,  6, -1}, // Estado 6
        {    7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   8,   7,   7,   7, -1,  -1,  -1,  -1,  7,  7,  7}, // Estado 7
        {    7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   0,   7,   7,   7, -1,  -1,  -1,  -1,  7,  7,  7}, // Estado 8
        {   10,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, -1,  -1,  -1,  -1, -1, 99, -1}, // Estado 9
        {   10,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  12,  11,  -1, -1,  -1,  -1,  -1, -1, -1, -1}, // Estado 10
        {   -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, -1,  17,  -1,  -1, -1, -1, -1}, // Estado 11
        {   12,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, -1,  -1,  -1,  13, -1, 99, -1}, // Estado 12
        {   14,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  14,  14,  -1,  -1,  -1,  -1,  -1,  -1, -1,  -1,  -1,  -1, -1, -1, -1}, // Estado 13
        {   14,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, -1,  -1,  -1,  -1, -1, 99, -1}, // Estado 14
        {   -1,  -1,  15,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, -1,  -1,  -1,  -1, -1, 99, -1}, // Estado 15
        {   16,  16,   0,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, 16,  -1,  -1,  -1, -1, 99, -1}, // Estado 16
        {   -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, -1,  -1,  18,  -1, -1, -1, -1}, // Estado 17
        {   -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, -1,  -1,  -1,  -1, -1, 99, -1}  // Estado 18
    };

    // Acciones semánticas agrupadas por funcionalidad.
    public static final int SEM_ESTRUCTURA_ASIGNACION = 1;
    public static final int SEM_CARACTER_LITERAL = 2;
    public static final int SEM_PALABRA_RESERVADA = 3;
    public static final int SEM_IDENTIFICADOR = 4;
    public static final int SEM_CADENA = 5;
    public static final int SEM_SIMBOLO_LITERAL = 6;
    public static final int SEM_CONSTANTE_ENTERA = 7;
    public static final int SEM_CONSTANTE_FLOAT = 8;

    // Matriz original, usada solo para construir la matriz agrupada.
    public static final int[][] MATRIZ_SEMANTICAS_ORIGINAL = {
        //   d,   l,   M, <,>,   =,   :,   !,   *,   +,   -,   /,   {,   },   .,   $, (,), _,   u,   l,   d,otro, BT,  \n
        {    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  21,   0,   0,  0,   0,   0,   0,  0,  0,  0}, // Estado 0
        {    0,   0,   0,   0,   1,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  0,   0,   0,   0,  0,  0,  0}, // Estado 1
        {    0,   0,   0,   0,   3,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  0,   0,   0,   0,  0,  2,  0}, // Estado 2
        {    0,   0,   0,   0,   4,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  0,   0,   0,   0,  0,  0,  0}, // Estado 3
        {    0,   0,   0,   0,   5,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  0,   0,   0,   0,  0,  6,  0}, // Estado 4
        {   12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  16,  12,  12,  12,  12,  0,   0,   0,   0, 12, 12, 15}, // Estado 5
        {   13,  13,  13,  13,  13,  13,  13,  13,  13,  13,  13,  13,  14,  13,  13,  13,  0,   0,   0,   0, 13, 13,  0}, // Estado 6
        {   17,  17,  17,  17,  17,  17,  17,  17,  17,  17,  17,  17,   0,  17,  17,  17,  0,   0,   0,   0, 17, 17, 17}, // Estado 7
        {   17,  17,  17,  17,  17,  17,  17,  17,  17,  17,  17,  17,  18,  17,  17,  17,  0,   0,   0,   0, 17, 17, 17}, // Estado 8
        {   22,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  0,   0,   0,   0,  0, 20,  0}, // Estado 9
        {   26,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  21,  23,   0,  0,   0,   0,   0,  0,  0,  0}, // Estado 10
        {    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  0,   0,   0,   0,  0,  0,  0}, // Estado 11
        {   27,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  0,   0,   0,  28,  0, 31,  0}, // Estado 12
        {   29,   0,   0,   0,   0,   0,   0,   0,  29,  29,   0,   0,   0,   0,   0,   0,  0,   0,   0,   0,  0,  0,  0}, // Estado 13
        {   30,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  0,   0,   0,   0,  0, 32,  0}, // Estado 14
        {    0,   0,  36,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  0,   0,   0,   0,  0,  7,  0}, // Estado 15
        {    8,   8,  12,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  8,   0,   0,   0,  0,  9,  0}, // Estado 16
        {    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  0,   0,   0,   0,  0,  0,  0}, // Estado 17
        {    0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  0,   0,   0,   0,  0, 25,  0}  // Estado 18
    };

    // Matriz de acciones semánticas agrupadas (0 = sin acción).
    public static final int[][] MATRIZ_SEMANTICAS = agruparAcciones(MATRIZ_SEMANTICAS_ORIGINAL);

    private static int[][] agruparAcciones(int[][] matrizOriginal) {
        int[][] matrizAgrupada = new int[matrizOriginal.length][];
        for (int estado = 0; estado < matrizOriginal.length; estado++) {
            matrizAgrupada[estado] = new int[matrizOriginal[estado].length];
            for (int columna = 0; columna < matrizOriginal[estado].length; columna++) {
                matrizAgrupada[estado][columna] = agruparAccion(
                    matrizOriginal[estado][columna]);
            }
        }
        return matrizAgrupada;
    }

    private static int agruparAccion(int accionOriginal) {
        if (accionOriginal == SIN_SEM) {
            return SIN_SEM;
        }
        if (accionOriginal == 1 || accionOriginal == 2) {
            return SEM_ESTRUCTURA_ASIGNACION;
        }
        if (accionOriginal >= 3 && accionOriginal <= 6) {
            return SEM_CARACTER_LITERAL;
        }
        if (accionOriginal == 7 || accionOriginal == 36) {
            return SEM_PALABRA_RESERVADA;
        }
        if (accionOriginal == 8 || accionOriginal == 9) {
            return SEM_IDENTIFICADOR;
        }
        if (accionOriginal >= 12 && accionOriginal <= 18) {
            return SEM_CADENA;
        }
        if (accionOriginal == 20) {
            return SEM_SIMBOLO_LITERAL;
        }
        if (accionOriginal >= 21 && accionOriginal <= 26) {
            return SEM_CONSTANTE_ENTERA;
        }
        if (accionOriginal >= 27 && accionOriginal <= 35) {
            return SEM_CONSTANTE_FLOAT;
        }
        return accionOriginal;
    }

/*
 Agrupación | Acciones originales |
| --- | --- |
| SEM 1 | Estructura sintáctica, asignación y definición de una variable |
| SEM 2 | Estructura sintáctica, asignación y definición de una variable |
| SEM 3 a 6 | Lectura de un carácter literal |
| SEM 36 y 7 | Lectura de palabra reservada |
| SEM 8 y 9 | Lectura de identificador |
| SEM 12 a 18 | Procesamiento de cadena |
| SEM 20 | Lectura de símbolo literal |
| SEM 21 a 26 | Lectura de constante entera |
| SEM 26 a 35 | Lectura de constante float |

La tabla siguiente se conserva únicamente como referencia histórica para la
conversión a los ocho códigos agrupados.
*/

/*
 Título | Descripción |
| --- | --- |
| SEM 1 | Asignación y definición de una variable

 |
| SEM 2 | Asignación de una variable

 |
| SEM 3 | Entrego un comparador lógico que evalúa igualdad

 |
| SEM 4 | Entrego el comparador lógico que evalúa la distinción

 |
| SEM 5 | Se interpreta comparador >= / <=

 |
| SEM 6 | Se procesa el comparador > / <

 |
| SEM 36 | Lee MAYÚSCULA, esperando a formar palabra reservada

 |
| SEM 7 | Símbolo blanco, entrega una palabra reservada

 |
| SEM 8 | Lee l,d,'_' , dentro del límite de caracteres

 |
| SEM 9 | Entrego identificador o rechazo si supera límite de caracteres

 |
| SEM 12 | Comienzo a procesar una cadena de una línea

 |
| SEM 13 | Leo carácter en una cadena que aún no se cerró

 |
| SEM 14 | Entrego una cadena de una línea

 |
| SEM 15 | Cadena mal formada: salto de línea en una cadena de una línea

 |
| SEM 16 | Delimita el inicio de un comentario multilínea

 |
| SEM 17 | Lee un carácter cualquiera, dentro de un comentario multilínea

 |
| SEM 18 | Cierra comentario multilínea, descarta lo procesado

 |
| SEM 20 | Símbolo blanco, interpreto el operador matemático +, -

 |
| SEM 21 | Delimito la parte entera de la decimal en una constante

 |
| SEM 22 | Proceso el primer dígito de una constante

 |
| SEM 23 | Proceso el sufijo de una constante entera

 |
| SEM 25 | Entrego constante entera bajo los límites y con sufijo

 |
| SEM 26 | Leo parte entera de una constante

 |
| SEM 27 | Proceso los decimales de un número

 |
| SEM 28 | Delimito el exponente de una constante de su parte decimal

 |
| SEM 29 | Proceso el símbolo/primer dígito del exponente

 |
| SEM 30 | Proceso el exponente

 |
| SEM 31 | Entrego una constante float sin exponente

 |
| SEM 32 | Proceso una constante float con exponente

 |
 */
}