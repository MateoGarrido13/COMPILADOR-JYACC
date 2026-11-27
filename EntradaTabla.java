public class EntradaTabla {

    public enum Uso {
        INDEFINIDO("indefinido"),
        VARIABLE("variable"),
        PARAMETRO("parametro"),
        FUNCION("funcion"),
        METODO("metodo"),
        CLASE("clase"),
        ATRIBUTO("atributo"),
        TIPO("tipo"),
        TIPO_ENUMERADO("tipo enumerado"),
        CONSTANTE("constante"),
        CADENA("cadena"),
        RESERVADA("reservada");

        private final String descripcion;

        Uso(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    String lexema;
    int tokenID;
    String tipoDato;
    int direccionMemoria;
    int numeroLinea;

    int lineaAparicion;

    int puntero;

    String ambito;

    Uso uso = Uso.INDEFINIDO;

    boolean declarado;

    private String nombreAsm;

    public String getNombreAsm() {
        return nombreAsm;
    }

    void setNombreAsm(String nombreAsm) {
        this.nombreAsm = nombreAsm;
    }

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
