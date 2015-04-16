package delfos.common.exceptions.dataset;

/**
 * Clase que almacena información del error cuando no se puede cargar el dataset
 * de contenido.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 * @version 30-Octubre-2013 Ahora es una excepción Unchecked.
 */
public class CannotLoadContentDataset extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param msg Mensaje a mostrar.
     */
    public CannotLoadContentDataset(String msg) {
        super(msg);
    }

    /**
     * Crea la excepción a partir de otra excepción que describe el error en
     * detalle.
     *
     * @param cause Excepción con el error detallado.
     */
    public CannotLoadContentDataset(Throwable cause) {
        super(cause);
    }

    public CannotLoadContentDataset(String message, Throwable cause) {
        super(message, cause);
    }

}
