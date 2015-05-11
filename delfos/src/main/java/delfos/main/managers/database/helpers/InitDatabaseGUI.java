package delfos.main.managers.database.helpers;

import delfos.ConsoleParameters;
import delfos.main.managers.CaseUseMode;
import delfos.main.managers.database.DatabaseManager;
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_INIT_DATABASE_GUI;
import delfos.view.dataset.changeable.ChangeableDatasetDefinitorSwingGUI;
import java.io.File;

/**
 *
 * @author jcastro
 */
public class InitDatabaseGUI extends CaseUseMode {

    public static final InitDatabaseGUI instance = new InitDatabaseGUI();

    public static InitDatabaseGUI getInstance() {
        return instance;
    }

    private InitDatabaseGUI() {

    }

    @Override
    public String getModeParameter() {
        return MANAGE_RATING_DATABASE_INIT_DATABASE_GUI;
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        File configFile;
        if (consoleParameters.isParameterDefined(DatabaseManager.MANAGE_RATING_DATABASE_CONFIG_XML)) {
            String configFileStr = consoleParameters.getValue(DatabaseManager.MANAGE_RATING_DATABASE_CONFIG_XML);
            configFile = new File(configFileStr);
        } else {
            configFile = new File(DatabaseManager.MANAGE_RATING_DATABASE_DEFAULT_CONFIG_XML);
        }

        ChangeableDatasetDefinitorSwingGUI gui = new ChangeableDatasetDefinitorSwingGUI(configFile, true);

        gui.setVisible(true);
    }

}
