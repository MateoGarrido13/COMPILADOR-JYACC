public class EntradaTabla {
    String lexema;
    int tokenID;
    String tipoDato;
    int direccionMemoria;
    int numeroLinea;
    // Linea de la ultima aparicion. La de alta queda en numeroLinea.
    int lineaAparicion;
    // Identidad en la tabla. La asigna TablaSimbolos al dar de alta la entrada.
    int puntero;
    // Ambito de la declaracion. El monitor no lo reescribe; el lexema si cambia.
    String ambito;

    public EntradaTabla(String lexema, int tokenID) {
        this(lexema, tokenID, 0);
    }

    public EntradaTabla(String lexema, int tokenID, int numeroLinea) {
        this.lexema = lexema;
        this.tokenID = tokenID;
        this.tipoDato = Tipos.INDEFINIDO;
        this.numeroLinea = numeroLinea;
    }
}