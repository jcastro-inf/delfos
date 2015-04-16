package delfos.main;

import java.util.ArrayList;
import java.util.List;
import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.Constants;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.main.exceptions.ManyCaseUseManagersActivatedException;
import delfos.main.exceptions.NoCaseUseManagersActivatedException;
import delfos.main.managers.CaseUseManager;
import delfos.main.managers.database.DatabaseManager;
import delfos.main.managers.library.Help;

/**
 * Clase que define el punto de entrada de la biblioteca de recomendación cuando
 * se invoca como un programa aparte (java -jar <b>JAR_NAME</b>
 *
 * @author Jorge Castro Gallardo
 */
public class Main {

    public static void mainWithExceptions(ConsoleParameters consoleParameters) {
        mainWithExceptions(consoleParameters.getConsoleRawParameters());
    }

    public static void mainWithExceptions(String[] args) {

        Chronometer c = new Chronometer();
        c.reset();

        ConsoleParameters consoleParameters = ConsoleParameters.parseArguments(args);
        Constants.start(consoleParameters);
        Global.showMessage("Starting\n");

        List<CaseUseManager> caseUseManagers = getAllCaseUseManagers();

        List<CaseUseManager> suitableCaseUseManagers = getSuitableCaseUseManagers(
                caseUseManagers,
                consoleParameters);

        if (Help.getInstance().isRightManager(consoleParameters)) {
            Help.getInstance().manageCaseUse(consoleParameters);
        } else {
            switch (suitableCaseUseManagers.size()) {
                case 0:
                    noCaseUseManagersActivated(consoleParameters);
                    throw new NoCaseUseManagersActivatedException(consoleParameters);
                case 1:
                    suitableCaseUseManagers.get(0).manageCaseUse(consoleParameters);
                    break;
                default:
                    manyCaseUseManagersActivated(consoleParameters, suitableCaseUseManagers);
                    throw new ManyCaseUseManagersActivatedException(consoleParameters, suitableCaseUseManagers);
            }
        }
    }

    public static void main(String[] args) {
        try {
            mainWithExceptions(args);
        } catch (NoCaseUseManagersActivatedException | ManyCaseUseManagersActivatedException ex) {

        }
    }

    public static List<CaseUseManager> getAllCaseUseManagers() {
        ArrayList<CaseUseManager> caseUseManagers = new ArrayList<>();

        caseUseManagers.add(DatabaseManager.getInstance());

        caseUseManagers.add(delfos.main.managers.experiment.ResultAnalysis.getInstance());
        caseUseManagers.add(delfos.main.managers.experiment.SingleUserExperimentGUI.getInstance());

        caseUseManagers.add(delfos.main.managers.library.Version.getInstance());

        caseUseManagers.add(delfos.main.managers.recommendation.singleuser.BuildRecommendationModel.getInstance());
        caseUseManagers.add(delfos.main.managers.recommendation.singleuser.Recommend.getInstance());
        caseUseManagers.add(delfos.main.managers.recommendation.singleuser.gui.swing.BuildConfigurationFileGUI.getInstance());
        caseUseManagers.add(delfos.main.managers.recommendation.singleuser.gui.swing.RecommendationGUI.getInstance());

        caseUseManagers.add(delfos.main.managers.recommendation.nonpersonalised.BuildRecommendationModel.getInstance());
        caseUseManagers.add(delfos.main.managers.recommendation.nonpersonalised.Recommend.getInstance());

        caseUseManagers.add(delfos.main.managers.recommendation.group.BuildRecommendationModel.getInstance());
        caseUseManagers.add(delfos.main.managers.recommendation.group.Recommend.getInstance());

        caseUseManagers.add(delfos.main.managers.experiment.ExecuteGroupXML.getInstance());

        caseUseManagers.add(delfos.main.managers.database.helpers.CreateDefaultManageDatabaseCSV.getInstance());
        caseUseManagers.add(delfos.main.managers.database.helpers.CreateDefaultManageDatabaseMySQL.getInstance());
        caseUseManagers.add(delfos.main.managers.recommendation.nonpersonalised.helpers.CreateDefaultNonPersonalisedRecommender.getInstance());

        return caseUseManagers;
    }

    public static List<CaseUseManager> getSuitableCaseUseManagers(List<CaseUseManager> caseUseManagers, ConsoleParameters consoleParameters) {
        List<CaseUseManager> suitableCaseUseManagers = new ArrayList<>();

        try {
            for (CaseUseManager caseUseManager : caseUseManagers) {

                try {
                    if (caseUseManager.isRightManager(consoleParameters)) {
                        suitableCaseUseManagers.add(caseUseManager);
                    }
                } catch (Throwable ex) {
                    System.out.println(ex.getMessage());
                    ex.printStackTrace(System.out);
                    ERROR_CODES.UNDEFINED_ERROR.exit(ex);
                }
            }
        } catch (Throwable ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
            ERROR_CODES.UNDEFINED_ERROR.exit(ex);
        }

        return suitableCaseUseManagers;
    }

    public static void noCaseUseManagersActivated(ConsoleParameters consoleParameters) {

        StringBuilder message = new StringBuilder();

        message.append("\n\tUnrecognized command line : ");
        message.append(consoleParameters.printOriginalParameters());
        message.append("\n");

        Global.showWarning(message.toString());
    }

    public static void manyCaseUseManagersActivated(ConsoleParameters consoleParameters, List<CaseUseManager> suitableCaseUseManagers) {
        StringBuilder message = new StringBuilder();

        message.append("\n========== COMMAND LINE MODES CONFLICT =========================\n");
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
