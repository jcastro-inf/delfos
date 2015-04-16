package delfos.rs.persistence;

/**
 * Denota un fallo en la lectura/escritura en la persistencia.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class FailureInPersistence extends Exception {

    private static final long serialVersionUID = 1L;

    public FailureInPersistence() {
    }

    public FailureInPersistence(String message) {
        super(message);
    }

    public FailureInPersistence(Throwable cause) {
        super(cause);
    }

    public FailureInPersistence(String message, Throwable cause) {
        super(message, cause);
    }
}
