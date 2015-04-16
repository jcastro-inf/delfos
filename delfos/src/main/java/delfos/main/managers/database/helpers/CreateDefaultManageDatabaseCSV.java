package delfos.main.managers.database.helpers;

import delfos.ConsoleParameters;
import delfos.configfile.rs.single.ChangeableDatasetConfigurationFileParser;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.dataset.loaders.csv.changeable.ChangeableCSVFileDatasetLoader;
import delfos.main.managers.CaseUseManager;

/**
 *
 * @author jcastro
 */
public class CreateDefaultManageDatabaseCSV implements CaseUseManager {

    private static final CreateDefaultManageDatabaseCSV instance = new CreateDefaultManageDatabaseCSV();

    public static CreateDefaultManageDatabaseCSV getInstance() {
        return instance;
    }

    public static final String PARAMETER = "--create-default-manage-database-csv";

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isDefined(PARAMETER);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        ChangeableDatasetLoader datasetLoader = new ChangeableCSVFileDatasetLoader(
                "dataset-ratings-file.csv",
                "dataset-items-file.csv",
                "dataset-users-file.csv");

        ChangeableDatasetConfigurationFileParser.saveConfigFile(
                "default-manage-database-csv.xml",
                datasetLoader);
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        return PARAMETER + " creates a default csv-based dataset.";
    }

}
