public class EntradaTabla {

    /*
      Clasificacion semantica de la entrada. Se usa para distinguir una variable de una
      funcion, un atributo, etc. No reemplaza al tokenID: el token dice como se reconocio
      el lexema; el uso dice que representa en el programa.
     */
    public enum Uso {
        INDEFINIDO("indefinido"),
        VARIABLE("variable"),
        PARAMETRO("parametro"),
        FUNCION("funcion"),
        METODO("metodo"),
        CLASE("clase"),
        ATRIBUTO("atributo"),
        TIPO("tipo"),
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
    // Linea de la ultima aparicion. La de alta queda en numeroLinea.
    int lineaAparicion;
    // Identidad en la tabla. La asigna TablaSimbolos al dar de alta la entrada.
    int puntero;
    // Ambito de la declaracion. El monitor no lo reescribe; el lexema si cambia.
    String ambito;
    // Que representa esta entrada dentro del programa.
    Uso uso = Uso.INDEFINIDO;
    // Diferencia una declaracion de un simple uso que creo un placeholder.
    boolean declarado;

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
