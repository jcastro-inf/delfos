package delfos.main.managers.database.submanagers;

import delfos.ConsoleParameters;
import delfos.common.Global;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_INIT_DATABASE;

/**
 *
 * @author jcastro
 */
public class InitDatabase extends DatabaseCaseUseSubManager {

    public static final InitDatabase instance = new InitDatabase();

    public static InitDatabase getInstance() {
        return instance;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isFlagDefined(MANAGE_RATING_DATABASE_INIT_DATABASE);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader) {
        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("Starting database.\n");
        }
        changeableDatasetLoader.initStructures();

        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("Database started.\n");
        }
    }
}
