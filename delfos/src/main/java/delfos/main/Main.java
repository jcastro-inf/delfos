package delfos.main;

import delfos.CommandLineParametersError;
import delfos.ConsoleParameters;
import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.main.exceptions.ManyCaseUseActivatedException;
import delfos.main.exceptions.NoCaseUseActivatedException;
import delfos.main.managers.CaseUseMode;
import delfos.main.managers.database.DatabaseManager;
import delfos.main.managers.library.Help;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que define el punto de entrada de la biblioteca de recomendación cuando
 * se invoca como un programa aparte (java -jar {@link Constants#LIBRARY_NAME})
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

        ConsoleParameters consoleParameters;
        try {
            consoleParameters = ConsoleParameters.parseArguments(args);
        } catch (CommandLineParametersError ex) {
            Global.show(ex.getUserFriendlyMsg());
            Global.showWarning(ex.getMessage());
            ERROR_CODES.COMMAND_LINE_PARAMETERS_ERROR.exit(ex);
            throw new IllegalArgumentException(ex);
        }
        Constants.initLibraryGeneralParameters(consoleParameters);

        List<CaseUseMode> caseUses = getAllCaseUse();

        List<CaseUseMode> suitableCaseUse = getSuitableCaseUse(
                caseUses,
                consoleParameters);

        if (Help.getInstance().isRightManager(consoleParameters)) {
            Help.getInstance().manageCaseUse(consoleParameters);
        } else {
            switch (suitableCaseUse.size()) {
                case 0:
                    noCaseUseActivated(consoleParameters);
                    throw new NoCaseUseActivatedException(consoleParameters);
                case 1:
                    suitableCaseUse.get(0).manageCaseUse(consoleParameters);
                    break;
                default:
                    manyCaseUseActivated(consoleParameters, suitableCaseUse);
                    throw new ManyCaseUseActivatedException(consoleParameters, suitableCaseUse);
            }
        }
    }

    public static void main(String[] args) {
        try {
            mainWithExceptions(args);
        } catch (NoCaseUseActivatedException | ManyCaseUseActivatedException ex) {

        }
    }

    public static List<CaseUseMode> getAllCaseUse() {
        ArrayList<CaseUseMode> caseUse = new ArrayList<>();

        caseUse.add(DatabaseManager.getInstance());

        caseUse.add(delfos.main.managers.experiment.ResultAnalysis.getInstance());
        caseUse.add(delfos.main.managers.experiment.SingleUserExperimentGUI.getInstance());

        caseUse.add(delfos.main.managers.library.Version.getInstance());

        caseUse.add(delfos.main.managers.recommendation.singleuser.SingleUserRecommendation.getInstance());
        caseUse.add(delfos.main.managers.recommendation.singleuser.gui.swing.BuildConfigurationFileGUI.getInstance());
        caseUse.add(delfos.main.managers.recommendation.singleuser.gui.swing.RecommendationGUI.getInstance());

        caseUse.add(delfos.main.managers.recommendation.nonpersonalised.NonPersonalisedRecommendation.getInstance());

        caseUse.add(delfos.main.managers.recommendation.group.GroupRecommendation.getInstance());

        caseUse.add(delfos.main.managers.experiment.ExecuteGroupXML.getInstance());
        caseUse.add(delfos.main.managers.experiment.ExecuteXML.getInstance());

        caseUse.add(delfos.main.managers.database.helpers.CreateDefaultManageDatabaseCSV.getInstance());
        caseUse.add(delfos.main.managers.database.helpers.CreateDefaultManageDatabaseMySQL.getInstance());
        caseUse.add(delfos.main.managers.database.helpers.InitDatabaseGUI.getInstance());

        caseUse.add(delfos.main.managers.recommendation.nonpersonalised.helpers.CreateDefaultNonPersonalisedRecommender.getInstance());

        caseUse.add(delfos.main.managers.recommendation.group.helpers.CreateDefaultGroupRecommender.getInstance());

        caseUse.add(delfos.main.managers.library.install.InitialConfiguration.getInstance());

        return caseUse;
    }

    public static List<CaseUseMode> getSuitableCaseUse(List<CaseUseMode> caseUse, ConsoleParameters consoleParameters) {
        List<CaseUseMode> suitableCaseUse = new ArrayList<>();

        try {
            for (CaseUseMode caseUseManager : caseUse) {

                try {
                    if (caseUseManager.isRightManager(consoleParameters)) {
                        suitableCaseUse.add(caseUseManager);
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

        return suitableCaseUse;
    }

    public static void noCaseUseActivated(ConsoleParameters consoleParameters) {
        StringBuilder message = new StringBuilder();

        message
                .append("No mode specified (Unrecognized command line): ").append("\n")
                .append("\t").append(consoleParameters.printOriginalParameters()).append("\n");

        Global.showWarning(message.toString());
    }

    public static void manyCaseUseActivated(ConsoleParameters consoleParameters, List<CaseUseMode> suitableCaseUse) {
        StringBuilder message = new StringBuilder();

        message.append("\n========== COMMAND LINE MODES CONFLICT =========================\n");
        message.append("Conflict on command line parameters: many case use activated.\n");
        message.append("Command line arguments\n");
        message.append("\t").append(consoleParameters.printOriginalParameters()).append("\n");
        message.append("CaseUse activated:\n");
        suitableCaseUse.stream().forEach((caseUseManager) -> {
            message.append("\t").append(caseUseManager.getClass().getName()).append("\n");
        });
        message.append("================================================================");

        Global.showWarning(message.toString());
    }
}
