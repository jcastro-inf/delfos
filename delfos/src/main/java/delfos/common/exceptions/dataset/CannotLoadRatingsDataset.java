package delfos.common.exceptions.dataset;

/**
 * Excepción que se lanza cuando no se puede cargar el dataset de valoraciones.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2103
 * @version 30-Octubre-2013 Ahora es una excepción Unchecked.
 */
public class CannotLoadRatingsDataset extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param msg Mensaje a mostrar.
     */
    public CannotLoadRatingsDataset(String msg) {
        super(msg);
    }

    /**
     * Crea la excepción a partir de otra excepción que describe el error en
     * detalle.
     *
     * @param cause Excepción con el error detallado.
     */
    public CannotLoadRatingsDataset(Throwable cause) {
        super(cause);
    }
}
