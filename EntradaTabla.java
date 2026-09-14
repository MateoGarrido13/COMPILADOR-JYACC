public class EntradaTabla {
    String lexema;
    int tokenID;
    String tipoDato;
    int direccionMemoria;
    int numeroLinea;

    public EntradaTabla(String lexema, int tokenID) {
        this(lexema, tokenID, 0);
    }

    public EntradaTabla(String lexema, int tokenID, int numeroLinea) {
        this.lexema = lexema;
        this.tokenID = tokenID;
        this.tipoDato = "indefinido";
        this.numeroLinea = numeroLinea;
    }
}