/**
 * Valor semantico que viaja por la pila del Parser generado por BYACC/J.
 *
 * El parser usa el constructor sin argumentos para inicializar la pila y copia
 * los cuatro campos (ival, dval, sval, obj) en dup_yyval,
 *  por lo que la clase debe conservarlos tal cual. 
 * En esta gramatica solo se usa 'obj', que lleva la referencia a la entrada de la Tabla de Simbolos o el objeto que devuelve la accion semantica.
 */
public class ParserVal {

    public int ival;
    public double dval;
    public String sval;
    public Object obj;

    public ParserVal() {
    }

    public ParserVal(int val) {
        ival = val;
    }

    public ParserVal(double val) {
        dval = val;
    }

    public ParserVal(String val) {
        sval = val;
    }

    public ParserVal(Object val) {
        obj = val;
    }
}
