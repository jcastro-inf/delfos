package delfos.main.managers.database.helpers;

import delfos.ConsoleParameters;
import delfos.configfile.rs.single.ChangeableDatasetConfigurationFileParser;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.dataset.loaders.database.mysql.changeable.ChangeableMySQLDatasetLoader;
import delfos.main.managers.CaseUseMode;

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

        ChangeableDatasetConfigurationFileParser.saveConfigFile(
                "default-manage-database-mysql.xml",
                datasetLoader);
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        return MODE_PARAMETER + " creates a default mysql-based dataset.";
    }

}
