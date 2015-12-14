package delfos.common.parallelwork;

/**
 * Excepción que se lanza cuando no se pueden generar más hebras, debido a la
 * limitación de cpus de la biblioteca.
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 21-May-2013
 *
 * @deprecated The parallel execution should be done using
 * {@link java.util.function.Function}, by iterating over the list of the
 * objects with the data of the task. Also the objects that perform the
 * execution should be refactored to implement
 * {@link java.util.function.Function} and execute the code over the data
 * object.
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
