package delfos.common.exceptions.dataset;

/**
 * Clase que almacena información del error cuando no se puede cargar el dataset
 * de confianza.
 *
* @author Jorge Castro Gallardo
 *
 * @version 19-Diciembre-2013
 */
public class CannotLoadTrustDataset extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param msg Mensaje a mostrar.
     */
    public CannotLoadTrustDataset(String msg) {
        super(msg);
    }

    /**
     * Crea la excepción a partir de otra excepción que describe el error en
     * detalle.
     *
     * @param cause Excepción con el error detallado.
     */
    public CannotLoadTrustDataset(Throwable cause) {
        super(cause);
    }
}
