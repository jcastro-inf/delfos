package delfos.main.exceptions;

import delfos.ConsoleParameters;

/**
 *
* @author Jorge Castro Gallardo
 */
public class NoCaseUseActivatedException extends RuntimeException {

    public NoCaseUseActivatedException(ConsoleParameters consoleParameters) {
        super("No case use for input '" + consoleParameters.printOriginalParameters() + "'");
    }

    public NoCaseUseActivatedException(String message) {
        super(message);
    }

    public NoCaseUseActivatedException(Throwable cause) {
        super(cause);
    }

    public NoCaseUseActivatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
