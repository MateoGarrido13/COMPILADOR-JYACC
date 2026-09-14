public class Main {
    public static void main(String[] args) {
        TablaSimbolos ts = new TablaSimbolos();
        AnalizadorLexico lexico = new AnalizadorLexico(ts);

        // Si tu input.txt contiene: if ( variable1 := 5 + variable2 )
        // Tu output.txt mostrará la traducción en tokens
        lexico.procesarArchivo("input.txt", "output.txt");

        System.out.println("Contenido de la tabla de simbolos:");
        ts.imprimirTabla();
    }
}
