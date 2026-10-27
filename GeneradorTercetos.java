import java.util.List;

/*
  Recorre las reducciones y emite tercetos de asignacion a identificador.
  La expresion se pliega a izquierda dentro de cada nivel de precedencia.
  Un grupo anidado (un producto dentro de una suma) se emite antes de usarse.
 */
public class GeneradorTercetos {

    private final ListaTercetos lista = new ListaTercetos();
    private final Reporte reporte;

    public GeneradorTercetos(Reporte reporte) {
        this.reporte = reporte;
    }

    public void generar(List<Reduccion> reducciones) {
        if (reducciones == null) {
            return;
        }
        for (Reduccion reduccion : reducciones) {
            if (reduccion.getRegla() == Reglas.ASIGNACION) {
                generarAsignacion(reduccion);
            }
        }
    }

    public String contenido() {
        return lista.contenido();
    }

    private void generarAsignacion(Reduccion reduccion) {
        Object destino = reduccion.operando(0);
        if (!(destino instanceof EntradaTabla)) { 
            // debe informarse un error se le quiere asignar un valor a una cte, variable sin su referencia
            
            reporte.error(reduccion.getLinea(), "No se puede asignar un valor a una cte o variable sin referencia");
            return;
        }
        Object origen = emitir(reduccion.operando(1));
        if (origen == null) {
            reporte.error(reduccion.getLinea(), "Asignacion sin operando");
            return;
        }
        lista.crear(":=", destino, origen);
    }

    // Devuelve la EntradaTabla hoja o el numero del terceto que resume la expresion.
    private Object emitir(Object valor) {
        if (valor instanceof EntradaTabla || valor instanceof Integer) {
            return valor;
        }
        if (!(valor instanceof ExpresionDiferida)) {
            return null;
        }
        ExpresionDiferida expresion = (ExpresionDiferida) valor;
        List<Object> operandos = expresion.getOperandos();
        List<Character> operadores = expresion.getOperadores();
        if (operandos.isEmpty()) {
            return null;
        }
        Object acumulado = emitir(operandos.get(0));
        if (acumulado == null) {
            return null;
        }
        for (int i = 0; i < operadores.size(); i++) {
            if (i + 1 >= operandos.size()) {
                // debe informarse un error se le quiere operar una expresion con menos operandos que operadores
                reporte.error(reduccion.getLinea(), "Expresion con menos operandos que operadores");
                return null;
            }
            Object derecho = emitir(operandos.get(i + 1));
            if (derecho == null) {
                return null;
            }
            acumulado = lista.crear(String.valueOf(operadores.get(i)), acumulado, derecho);
        }
        return acumulado;
    }
}
