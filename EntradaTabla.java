public class EntradaTabla {
    String lexema;
    int tokenID;
    String tipoDato;
    int direccionMemoria;
    int numeroLinea;
    // Identidad en la tabla. La asigna TablaSimbolos al dar de alta la entrada.
    int puntero;

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