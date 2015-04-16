package delfos.main.managers.library;

import java.util.List;
import delfos.ConsoleParameters;
import delfos.common.Global;
import delfos.main.Main;
import delfos.main.exceptions.ManyCaseUseManagersActivatedException;
import delfos.main.managers.CaseUseManager;

/**
 *
 * @version 21-oct-2014
 * @author Jorge Castro Gallardo
 */
public class Help {

    public static final String PRINT_HELP = "--help";
    public static final String PRINT_HELP_SHORT = "-h";

    public static Help getInstance() {
        return HelpHolder.INSTANCE;
    }

    private static class HelpHolder {

        private static final Help INSTANCE = new Help();
    }

    public Help() {
    }

    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isDefined(PRINT_HELP) || consoleParameters.isDefined(PRINT_HELP_SHORT);
    }

    public void manageCaseUse(ConsoleParameters consoleParameters) {
        List<CaseUseManager> suitableCaseUseManagers = Main.getSuitableCaseUseManagers(Main.getAllCaseUseManagers(), consoleParameters);

        switch (suitableCaseUseManagers.size()) {
            case 0:
                throw new UnsupportedOperationException("A generic help of this library should be provided");
            case 1:
                final CaseUseManager selectedCaseUseManager = suitableCaseUseManagers.get(0);
                Global.showMessage("Requested help for " + selectedCaseUseManager);

                System.out.println(selectedCaseUseManager.getUserFriendlyHelpForThisCaseUse());
                break;
            default:
                Main.manyCaseUseManagersActivated(consoleParameters, suitableCaseUseManagers);
                throw new ManyCaseUseManagersActivatedException(consoleParameters, suitableCaseUseManagers);
        }
    }
}
