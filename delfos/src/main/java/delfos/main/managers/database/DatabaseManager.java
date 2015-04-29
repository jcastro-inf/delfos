package delfos.main.managers.database;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.configfile.rs.single.ChangeableDatasetConfiguration;
import delfos.configfile.rs.single.ChangeableDatasetConfigurationFileParser;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.main.exceptions.ManyCaseUseActivatedException;
import delfos.main.exceptions.NoCaseUseActivatedException;
import delfos.main.managers.CaseUseModeManager;
import delfos.main.managers.database.submanagers.AddItem;
import delfos.main.managers.database.submanagers.AddItemFeatures;
import delfos.main.managers.database.submanagers.AddRating;
import delfos.main.managers.database.submanagers.AddUser;
import delfos.main.managers.database.submanagers.AddUserFeatures;
import delfos.main.managers.database.submanagers.DatabaseManagerCaseUseManager;
import delfos.main.managers.database.submanagers.DatasetPrinterManager;
import delfos.main.managers.database.submanagers.InitDatabase;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.JDOMException;

/**
 *
 * @author Jorge Castro Gallardo
 */
public class DatabaseManager extends CaseUseModeManager {

    /**
     * Parametro para especificar que la biblioteca funcione en modo de
     * administración de la base de datos de ratings.
     */
    public static final String MANAGE_RATING_DATABASE_OLD = "-manageRatingDatabase";
    /**
     * Parametro para especificar que la biblioteca funcione en modo de
     * administración de la base de datos de ratings.
     */
    public static final String MANAGE_RATING_DATABASE = "-manage-database";

    private DatabaseManager() {
    }

    @Override
    public String getModeParameter() {
        return MANAGE_RATING_DATABASE;
    }

    public static DatabaseManager getInstance() {
        return ManageRatingDatabaseHolder.INSTANCE;
    }

    private static class ManageRatingDatabaseHolder {

        private static final DatabaseManager INSTANCE = new DatabaseManager();
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {

        boolean isThisCaseRight = consoleParameters.deprecatedParameter_isDefined(MANAGE_RATING_DATABASE_OLD, MANAGE_RATING_DATABASE);

        if (isThisCaseRight) {
            List<DatabaseManagerCaseUseManager> suitables = getSuitableCaseUseManagers(consoleParameters);
            if (suitables.size() == 1) {
                return true;
            } else if (suitables.isEmpty()) {
                return false;
            } else {
                throw new ManyCaseUseActivatedException(consoleParameters, suitables);
            }
        } else {
            return false;
        }
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("MODE: Manage rating database\n");
        }

        List<DatabaseManagerCaseUseManager> suitableCaseUseManagers = getSuitableCaseUseManagers(consoleParameters);

        switch (suitableCaseUseManagers.size()) {
            case 0:
                noCaseUseManagersActivated(consoleParameters);
                throw new NoCaseUseActivatedException(consoleParameters);
            case 1:
                ChangeableDatasetLoader changeableDatasetLoader = extractChangeableDatasetHandler(consoleParameters);
                suitableCaseUseManagers.get(0).manageCaseUse(consoleParameters, changeableDatasetLoader);
                changeableDatasetLoader.commitChangesInPersistence();
                break;
            default:
                manyCaseUseManagersActivated(consoleParameters, suitableCaseUseManagers);
                throw new ManyCaseUseActivatedException(consoleParameters, suitableCaseUseManagers);
        }
    }

    public static List<DatabaseManagerCaseUseManager> getAllCaseUseManagers() {
        ArrayList<DatabaseManagerCaseUseManager> caseUseManagers = new ArrayList<>();

        caseUseManagers.add(InitDatabase.getInstance());

        caseUseManagers.add(AddUser.getInstance());
        caseUseManagers.add(AddUserFeatures.getInstance());

        caseUseManagers.add(AddItem.getInstance());
        caseUseManagers.add(AddItemFeatures.getInstance());

        caseUseManagers.add(AddRating.getInstance());

        caseUseManagers.add(DatasetPrinterManager.getInstance());

        return caseUseManagers;
    }

    public static List<DatabaseManagerCaseUseManager> getSuitableCaseUseManagers(ConsoleParameters consoleParameters) {
        List<DatabaseManagerCaseUseManager> suitableCaseUse = new ArrayList<>();

        try {
            for (DatabaseManagerCaseUseManager caseUseManager : getAllCaseUseManagers()) {
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

    public static ChangeableDatasetLoader extractChangeableDatasetHandler(ConsoleParameters consoleParameters) throws RuntimeException {
        try {

            String configurationFile = consoleParameters.deprecatedParameter_getValue(MANAGE_RATING_DATABASE_OLD, MANAGE_RATING_DATABASE);

            //llamada a la clase que realiza el manejo de este caso de uso
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Loading config file: " + new File(configurationFile).getAbsolutePath() + "\n");
            }

            try {
                ChangeableDatasetConfiguration loadConfigFile = ChangeableDatasetConfigurationFileParser.loadConfigFile(configurationFile);
                if (loadConfigFile.datasetLoader instanceof ChangeableDatasetLoader) {
                    return loadConfigFile.datasetLoader;
                } else {
                    IllegalStateException ex = new IllegalStateException("The dataset is not changeable");
                    ERROR_CODES.MANAGE_RATING_DATABASE_DATASET_NOT_CHANGEABLE.exit(ex);
                    throw ex;
                }
            } catch (JDOMException ex) {
                ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
                throw new IllegalStateException(ex);
            } catch (CannotLoadContentDataset ex) {
                ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
                throw new IllegalStateException(ex);
            } catch (CannotLoadRatingsDataset ex) {
                ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
                throw new IllegalStateException(ex);
            } catch (FileNotFoundException ex) {
                ERROR_CODES.CONFIG_FILE_NOT_EXISTS.exit(ex);
                throw new IllegalStateException(ex);
            }
        } catch (UndefinedParameterException ex) {
            ERROR_CODES.MANAGE_RATING_DATABASE_CONFIGURATION_FILE_NOT_DEFINED.exit(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        StringBuilder str = new StringBuilder();

        str.append("\tRATINGS DATABASE MANAGEMENT\n");

        str.append("\t\n");
        str.append("\t\t" + DatabaseManager.MANAGE_RATING_DATABASE
                + " [DATABASE_CONFIGURATION_FILE.xml]: This option is used "
                + "to manage the database of ratings specified in "
                + "[DATABASE_CONFIGURATION_FILE.xml] file\n");
        str.append("\t\n");

        str.append("\t\t" + DatabaseManager.MANAGE_RATING_DATABASE
                + " [DATABASE_CONFIGURATION_FILE.xml] " + InitDatabase.MANAGE_RATING_DATABASE_INIT_DATABASE
                + ": This command initialises the "
                + "database of ratings specified in "
                + "[DATABASE_CONFIGURATION_FILE.xml] file. If the database "
                + "already exists, overwrites the information deleting the "
                + "old data.\n");
        str.append("\t\n");

        str.append("\t\t" + DatabaseManager.MANAGE_RATING_DATABASE
                + " [DATABASE_CONFIGURATION_FILE.xml] " + AddUser.MANAGE_RATING_DATABASE_ADD_USER
                + " [ID_USER]: This command adds the user [ID_USER] to the "
                + "database of ratings specified in "
                + "[DATABASE_CONFIGURATION_FILE.xml] file\n");
        str.append("\t\n");

        str.append("\t\t" + DatabaseManager.MANAGE_RATING_DATABASE
                + " [DATABASE_CONFIGURATION_FILE.xml] " + AddItem.MANAGE_RATING_DATABASE_ADD_ITEM
                + " [ID_ITEM]: This command adds the item [ID_ITEM] to the "
                + "database of ratings specified in "
                + "[DATABASE_CONFIGURATION_FILE.xml] file\n");
        str.append("\t\n");

        str.append("\t\t" + DatabaseManager.MANAGE_RATING_DATABASE
                + " [DATABASE_CONFIGURATION_FILE.xml] " + AddRating.MANAGE_RATING_DATABASE_ADD_RATING + " "
                + AddRating.MANAGE_RATING_DATABASE_ID_USER + " [ID_USER] "
                + AddRating.MANAGE_RATING_DATABASE_ID_ITEM + " [ID_ITEM] "
                + AddRating.MANAGE_RATING_DATABASE_RATING_VALUE + " [RATING_VALUE] "
                + ": This command adds the rating of user [ID_USER] over item"
                + " [ID_ITEM] with a value of [RATING_VALUE] to the database "
                + "of ratings specified in [DATABASE_CONFIGURATION_FILE.xml] "
                + "file\n");
        str.append("\t\n");

        return str.toString();
    }

    public static void noCaseUseManagersActivated(ConsoleParameters consoleParameters) {

        StringBuilder message = new StringBuilder();

        message.append("Unrecognized command line : ");
        message.append(consoleParameters.printOriginalParameters());
        message.append("\n");

        Global.showWarning(message.toString());
    }

    public static void manyCaseUseManagersActivated(ConsoleParameters consoleParameters, List<DatabaseManagerCaseUseManager> suitableCaseUseManagers) {
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
