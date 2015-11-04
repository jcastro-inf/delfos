package delfos.common.exceptions;

/**
 *
 * @author jcastro
 */
public class CouldNotComputeSimilarity extends RuntimeException {

    public CouldNotComputeSimilarity(String message) {
        super(message);
    }

    public CouldNotComputeSimilarity(Throwable cause) {
        super(cause);
    }

    public CouldNotComputeSimilarity(String message, Throwable cause) {
        super(message, cause);
    }

}
