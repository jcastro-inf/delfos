package delfos.common.exceptions;

/**
 * Excepción que se lanza cuando no se puede predecir una valoración.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2103
 */
public class CouldNotPredictRating extends Exception {
    
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param msg Mensaje a mostrar.
     */
    public CouldNotPredictRating(String msg) {
        super(msg);
    }
}
