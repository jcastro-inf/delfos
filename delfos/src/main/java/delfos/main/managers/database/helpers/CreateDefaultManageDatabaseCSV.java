package delfos.main.managers.database.helpers;

import delfos.ConsoleParameters;
import delfos.configfile.rs.single.ChangeableDatasetConfigurationFileParser;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.dataset.loaders.csv.changeable.ChangeableCSVFileDatasetLoader;
import delfos.main.managers.CaseUseMode;

/**
 *
 * @author jcastro
 */
public class CreateDefaultManageDatabaseCSV extends CaseUseMode {

    private static final CreateDefaultManageDatabaseCSV instance = new CreateDefaultManageDatabaseCSV();

    public static CreateDefaultManageDatabaseCSV getInstance() {
        return instance;
    }

    public static final String MODE_PARAMETER = "--create-default-manage-database-csv";

    @Override
    public String getModeParameter() {
        return MODE_PARAMETER;
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
        return MODE_PARAMETER + " creates a default csv-based dataset.";
    }

}
