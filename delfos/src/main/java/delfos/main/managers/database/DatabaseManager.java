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
    public static final String MODE_PARAMETER = "--manage-database";

    /**
     * Paramter to specify a different configuration file for the database.
     */
    public static final String MANAGE_RATING_DATABASE_CONFIG_XML = "-database-config";
    /**
     * Paramter to specify a different configuration file for the database.
     */
    public static final String MANAGE_RATING_DATABASE_DEFAULT_CONFIG_XML = "db-config.xml";
    /**
     * Parametro para especificar que se debe inicializar la base de datos.
     */
    public static final String MANAGE_RATING_DATABASE_INIT_DATABASE = "--init-database";
    /**
     * Starts a swing interface to define the database xml and initialisate it.
     */
    public static final String MANAGE_RATING_DATABASE_INIT_DATABASE_GUI = "--init-database-x";

    /**
     * Parametro para especificar que la biblioteca añada un usuario a la base
     * de datos que está siendo administrada.
     */
    public static final String MANAGE_RATING_DATABASE_ADD_USER = "-add-user";

    /**
     * Parametro para especificar que la biblioteca añada un producto a la base
     * de datos que está siendo administrada.
     */
    public static final String MANAGE_RATING_DATABASE_ADD_ITEM = "-add-item";

    /**
     * Parametro para especificar que la biblioteca añada un usuario a la base
     * de datos que está siendo administrada.
     */
    public static final String MANAGE_RATING_DATABASE_ADD_RATING = "--add-rating";

    public static final String MANAGE_RATING_DATABASE_ID_USER = "-user";
    /**
     * Parametro para especificar a la biblioteca el producto con el que se está
     * trabajando.
     */
    public static final String MANAGE_RATING_DATABASE_ID_ITEM = "-item";
    /**
     * Parametro para especificar a la biblioteca el valor del rating que se
     * desea añadir.
     */
    public static final String MANAGE_RATING_DATABASE_RATING_VALUE = "-value";

    /**
     * Parámetro para especificar que se use el modo de añadir características a
     * un usuario.
     */
    public static final String MANAGE_RATING_DATABASE_ADD_USER_FEATURES = "-add-user-features";

    /**
     * Parámetro para especificar que se use el modo de añadir características a
     * un producto.
     */
    public static final String MANAGE_RATING_DATABASE_ADD_ITEM_FEATURES = "-add-item-features";

    /**
     * Parámetro para especificar las características que se añaden en los modos
     * {@link AddUserFeatures} y {@link AddItemFeatures}.
     */
    public static final String MANAGE_RATING_DATABASE_FEATURES = "-features";
    /**
     * Cadena que denota el nombre de una entidad {@link EntityWithFeatures}.
     * Por ejemplo, en una base de datos se utilizará esta cadena como la
     * columna que contiene el nombre de cada producto. (usuario, producto,
     * etc.).
     */
    public static final String ENTITY_NAME = "name";

    private static final DatabaseManager instance = new DatabaseManager();

    public static DatabaseManager getInstance() {
        return instance;
    }

    private DatabaseManager() {
    }

    @Override
    public String getModeParameter() {
        return MODE_PARAMETER;
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

            File configurationFile;
            if (consoleParameters.isParameterDefined(MANAGE_RATING_DATABASE_CONFIG_XML)) {
                String configurationFileStr = consoleParameters.getValue(MANAGE_RATING_DATABASE_CONFIG_XML);
                configurationFile = new File(configurationFileStr);
            } else {
                configurationFile = new File(MANAGE_RATING_DATABASE_DEFAULT_CONFIG_XML);
            }

            //llamada a la clase que realiza el manejo de este caso de uso
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Loading config file: " + configurationFile.getAbsolutePath() + "\n");
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
        str.append("\t\t" + DatabaseManager.MODE_PARAMETER
                + DatabaseManager.MANAGE_RATING_DATABASE_CONFIG_XML
                + " [DATABASE_CONFIGURATION_FILE.xml]: This option is used "
                + "to manage the database of ratings specified in "
                + "[DATABASE_CONFIGURATION_FILE.xml] file\n");
        str.append("\t\n");

        str.append("\t\t" + DatabaseManager.MODE_PARAMETER
                + DatabaseManager.MANAGE_RATING_DATABASE_CONFIG_XML
                + " [DATABASE_CONFIGURATION_FILE.xml] "
                + DatabaseManager.MANAGE_RATING_DATABASE_INIT_DATABASE
                + ": This command initialises the "
                + "database of ratings specified in "
                + "[DATABASE_CONFIGURATION_FILE.xml] file. If the database "
                + "already exists, overwrites the information deleting the "
                + "old data.\n");
        str.append("\t\n");

        str.append("\t\t" + DatabaseManager.MODE_PARAMETER
                + DatabaseManager.MANAGE_RATING_DATABASE_CONFIG_XML
                + " [DATABASE_CONFIGURATION_FILE.xml] " + MANAGE_RATING_DATABASE_ADD_USER
                + " [ID_USER]: This command adds the user [ID_USER] to the "
                + "database of ratings specified in "
                + "[DATABASE_CONFIGURATION_FILE.xml] file\n");
        str.append("\t\n");

        str.append("\t\t" + DatabaseManager.MODE_PARAMETER
                + DatabaseManager.MANAGE_RATING_DATABASE_CONFIG_XML
                + " [DATABASE_CONFIGURATION_FILE.xml] " + MANAGE_RATING_DATABASE_ADD_ITEM
                + " [ID_ITEM]: This command adds the item [ID_ITEM] to the "
                + "database of ratings specified in "
                + "[DATABASE_CONFIGURATION_FILE.xml] file\n");
        str.append("\t\n");

        str.append("\t\t" + DatabaseManager.MODE_PARAMETER
                + DatabaseManager.MANAGE_RATING_DATABASE_CONFIG_XML
                + " [DATABASE_CONFIGURATION_FILE.xml] " + MANAGE_RATING_DATABASE_ADD_RATING + " "
                + MANAGE_RATING_DATABASE_ID_USER + " [ID_USER] "
                + MANAGE_RATING_DATABASE_ID_ITEM + " [ID_ITEM] "
                + MANAGE_RATING_DATABASE_RATING_VALUE + " [RATING_VALUE] "
                + ": This command adds the rating of user [ID_USER] over item"
                + " [ID_ITEM] with a value of [RATING_VALUE] to the database "
                + "of ratings specified in [DATABASE_CONFIGURATION_FILE.xml] "
                + "file\n");
        str.append("\t\n");

        return str.toString();
    }

}
