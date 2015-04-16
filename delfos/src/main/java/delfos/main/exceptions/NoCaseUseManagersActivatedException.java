package delfos.main.exceptions;

import delfos.ConsoleParameters;

/**
 *
* @author Jorge Castro Gallardo
 */
public class NoCaseUseManagersActivatedException extends RuntimeException {

    public NoCaseUseManagersActivatedException(ConsoleParameters consoleParameters) {
        super("No case use for input '" + consoleParameters.printOriginalParameters() + "'");
    }

    public NoCaseUseManagersActivatedException(String message) {
        super(message);
    }

    public NoCaseUseManagersActivatedException(Throwable cause) {
        super(cause);
    }

    public NoCaseUseManagersActivatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
