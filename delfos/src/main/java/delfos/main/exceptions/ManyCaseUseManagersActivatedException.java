package delfos.main.exceptions;

import java.util.List;
import delfos.ConsoleParameters;

/**
 *
 * @author Jorge Castro Gallardo
 */
public class ManyCaseUseManagersActivatedException extends RuntimeException {

    public <CaseUseManager> ManyCaseUseManagersActivatedException(ConsoleParameters consoleParameters, List<CaseUseManager> caseUseManagers) {
        super("Many case use managers for input '" + consoleParameters.printOriginalParameters() + "'\nCase use managers: " + caseUseManagers.toString());
    }

    public ManyCaseUseManagersActivatedException(String message) {
        super(message);
    }

    public ManyCaseUseManagersActivatedException(Throwable cause) {
        super(cause);
    }

    public ManyCaseUseManagersActivatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
