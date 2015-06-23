package delfos.main.managers;

import delfos.ConsoleParameters;
import delfos.common.Global;
import delfos.main.Main;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author jcastro
 */
public abstract class CaseUseModeWithSubManagers extends CaseUseMode {

    /**
     * Returns all the case use sub managers of this mode.
     *
     * @return
     */
    public Collection<CaseUseSubManager> getAllCaseUseSubManagers() {
        ArrayList<CaseUseSubManager> subManagers = new ArrayList<>();
        subManagers.add(new CaseUseSubManager(this) {

            @Override
            public boolean isRightManager(ConsoleParameters consoleParameters) {
                return getParent().isRightManager(consoleParameters);
            }

            @Override
            public void manageCaseUse(ConsoleParameters consoleParameters) {
                getParent().manageCaseUse(consoleParameters);
            }
        });

        return subManagers;
    }

    public CaseUseSubManager getSuitableCaseUseSubManager(ConsoleParameters consoleParameters) {

        ArrayList<CaseUseSubManager> suitable = new ArrayList<>();

        getAllCaseUseSubManagers().stream()
                .filter((caseUseSubManager) -> (caseUseSubManager.isRightManager(consoleParameters)))
                .forEach((caseUseSubManager) -> {
                    suitable.add(caseUseSubManager);
                });

        if (suitable.size() == 1) {
            return suitable.get(0);
        } else if (suitable.isEmpty()) {
            String msg = "No case use activated for command line "
                    + consoleParameters.printOriginalParameters();

            throw new IllegalStateException(msg);

        } else {
            String msg = "More than one case use manager activated for command line\n"
                    + "\t" + consoleParameters.printOriginalParameters() + "\n"
                    + "Cases activated: " + suitable.toString();

            throw new IllegalStateException(msg);

        }
    }

    @Override
    public final void manageCaseUse(ConsoleParameters consoleParameters) {
        getSuitableCaseUseSubManager(consoleParameters).manageCaseUse(consoleParameters);
    }

    public static void noCaseUseManagersActivated(ConsoleParameters consoleParameters) {
        Main.noCaseUseActivated(consoleParameters);
    }

    public static void manyCaseUseManagersActivated(ConsoleParameters consoleParameters, List<CaseUseSubManager> suitableCaseUseManagers) {
        StringBuilder message = new StringBuilder();

        message.append("========== COMMAND LINE MODES CONFLICT =========================");
        message.append("Conflict on command line parameters: many case use managers activated.\n");
        message.append("Command line arguments\n");
        message.append("\t").append(consoleParameters.printOriginalParameters()).append("\n");
        message.append("CaseUseManagers activated:\n");
        suitableCaseUseManagers.stream().forEach((caseUseManager) -> {
            message.append("\t").append(caseUseManager.getClass().getName()).append("\n");
        });
        message.append("================================================================");

        Global.showWarning(message.toString());
    }

}
