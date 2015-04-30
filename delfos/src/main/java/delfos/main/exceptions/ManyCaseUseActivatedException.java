package delfos.main.exceptions;

import java.util.List;
import delfos.ConsoleParameters;

/**
 *
 * @author Jorge Castro Gallardo
 */
public class ManyCaseUseActivatedException extends RuntimeException {

    public <CaseUseManager> ManyCaseUseActivatedException(ConsoleParameters consoleParameters, List<CaseUseManager> caseUseManagers) {
        super("Many case use managers for input '" + consoleParameters.printOriginalParameters() + "'\nCase use managers: " + caseUseManagers.toString());
    }

    public ManyCaseUseActivatedException(String message) {
        super(message);
    }

    public ManyCaseUseActivatedException(Throwable cause) {
        super(cause);
    }

    public ManyCaseUseActivatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
