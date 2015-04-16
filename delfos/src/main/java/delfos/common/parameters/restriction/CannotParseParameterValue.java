package delfos.common.parameters.restriction;

/**
 *
* @author Jorge Castro Gallardo
 */
public class CannotParseParameterValue extends Exception {

    public CannotParseParameterValue(String message) {
        super(message);
    }

    public CannotParseParameterValue(Throwable cause) {
        super(cause);
    }

    public CannotParseParameterValue(String message, Throwable cause) {
        super(message, cause);
    }
}
