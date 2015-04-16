package delfos.main.managers.database.submanagers;

import delfos.ConsoleParameters;
import delfos.common.Global;
import delfos.dataset.changeable.ChangeableDatasetLoader;

/**
 *
 * @author jcastro
 */
public class InitDatabase implements DatabaseManagerCaseUseManager {

    public static final InitDatabase instance = new InitDatabase();

    public static InitDatabase getInstance() {
        return instance;
    }

    /**
     * Parametro para especificar que se debe inicializar la base de datos.
     */
    @Deprecated
    public static final String MANAGE_RATING_DATABASE_INIT_DATABASE_OLD = "-initDatabase";

    /**
     * Parametro para especificar que se debe inicializar la base de datos.
     */
    public static final String MANAGE_RATING_DATABASE_INIT_DATABASE = "--init";

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.deprecatedParameter_isDefined(MANAGE_RATING_DATABASE_INIT_DATABASE_OLD, MANAGE_RATING_DATABASE_INIT_DATABASE);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader) {
        if (Global.isVerboseAnnoying()) {
            Global.showMessage("Starting database.\n");
        }
        changeableDatasetLoader.initStructures();

        if (Global.isVerboseAnnoying()) {
            Global.showMessage("Database started.\n");
        }
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
