package delfos.main.managers.database.helpers;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.configfile.rs.single.ChangeableDatasetConfigurationFileParser;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.dataset.loaders.database.mysql.changeable.ChangeableMySQLDatasetLoader;
import delfos.main.managers.CaseUseMode;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author jcastro
 */
public class CreateDefaultManageDatabaseMySQL extends CaseUseMode {

    private static final CreateDefaultManageDatabaseMySQL instance = new CreateDefaultManageDatabaseMySQL();

    public static CreateDefaultManageDatabaseMySQL getInstance() {
        return instance;
    }

    public static final String MODE_PARAMETER = "--create-default-manage-database-mysql";

    @Override
    public String getModeParameter() {
        return MODE_PARAMETER;
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        ChangeableDatasetLoader datasetLoader = new ChangeableMySQLDatasetLoader();

        try {
            ChangeableDatasetConfigurationFileParser.saveConfigFile(
                    new File("mysql-db-config.xml"),
                    datasetLoader);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_SAVE_CONFIG_FILE.exit(ex);
        }
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        return MODE_PARAMETER + " creates a default mysql-based dataset.";
    }

}
