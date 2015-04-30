package delfos.main.managers.library;

import delfos.ConsoleParameters;
import delfos.common.Global;
import delfos.main.Main;
import delfos.main.exceptions.ManyCaseUseActivatedException;
import delfos.main.managers.CaseUseMode;
import java.util.List;

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
        List<CaseUseMode> suitableCaseUseManagers = Main.getSuitableCaseUse(Main.getAllCaseUse(), consoleParameters);

        switch (suitableCaseUseManagers.size()) {
            case 0:
                throw new UnsupportedOperationException("A generic help of this library should be provided");
            case 1:
                final CaseUseMode selectedCaseUseManager = suitableCaseUseManagers.get(0);
                Global.showInfoMessage("Requested help for " + selectedCaseUseManager);
                System.out.println(selectedCaseUseManager.getUserFriendlyHelpForThisCaseUse());
                break;
            default:
                Main.manyCaseUseActivated(consoleParameters, suitableCaseUseManagers);
                throw new ManyCaseUseActivatedException(consoleParameters, suitableCaseUseManagers);
        }
    }
}
