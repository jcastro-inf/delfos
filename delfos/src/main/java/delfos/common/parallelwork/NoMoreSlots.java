package delfos.common.parallelwork;

/**
 * Excepción que se lanza cuando no se pueden generar más hebras, debido a la
 * limitación de cpus de la biblioteca.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 21-May-2013
 */
public class NoMoreSlots extends Exception {

    private static final long serialVersionUID = 1L;

    public NoMoreSlots(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMoreSlots(Throwable cause) {
        super(cause);
    }

    public NoMoreSlots(String message) {
        super(message);
    }

    public NoMoreSlots() {
    }
}
