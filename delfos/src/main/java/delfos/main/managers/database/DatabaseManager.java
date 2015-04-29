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
import delfos.main.managers.CaseUseModeWithSubManagers;
import delfos.main.managers.CaseUseSubManager;
import delfos.main.managers.database.submanagers.AddItem;
import delfos.main.managers.database.submanagers.AddItemFeatures;
import delfos.main.managers.database.submanagers.AddRating;
import delfos.main.managers.database.submanagers.AddUser;
import delfos.main.managers.database.submanagers.AddUserFeatures;
import delfos.main.managers.database.submanagers.DatasetPrinterManager;
import delfos.main.managers.database.submanagers.InitDatabase;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import org.jdom2.JDOMException;

/**
 *
 * @author Jorge Castro Gallardo
 */
public class DatabaseManager extends CaseUseModeWithSubManagers {

    /**
     * Parametro para especificar que la biblioteca funcione en modo de
     * administración de la base de datos de ratings.
     */
    @Deprecated
    public static final String MANAGE_RATING_DATABASE_OLD = "-manageRatingDatabase";
    /**
     * Parametro para especificar que la biblioteca funcione en modo de
     * administración de la base de datos de ratings.
     */
    public static final String MANAGE_RATING_DATABASE = "-manage-database";

    private static final DatabaseManager instance = new DatabaseManager();

    public static DatabaseManager getInstance() {
        return instance;
    }

    private DatabaseManager() {
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        if (super.isRightManager(consoleParameters)) {
            return true;
        }

        return consoleParameters.deprecatedParameter_isDefined(MANAGE_RATING_DATABASE_OLD, MANAGE_RATING_DATABASE);
    }

    @Override
    public String getModeParameter() {
        return MANAGE_RATING_DATABASE;
    }

    @Override
    public Collection<CaseUseSubManager> getAllCaseUseSubManagers() {
        ArrayList<CaseUseSubManager> caseUseManagers = new ArrayList<>();

        caseUseManagers.add(InitDatabase.getInstance());

        caseUseManagers.add(AddUser.getInstance());
        caseUseManagers.add(AddUserFeatures.getInstance());

        caseUseManagers.add(AddItem.getInstance());
        caseUseManagers.add(AddItemFeatures.getInstance());

        caseUseManagers.add(AddRating.getInstance());

        caseUseManagers.add(DatasetPrinterManager.getInstance());

        return caseUseManagers;
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

}
