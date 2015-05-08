package delfos.main.managers.database.helpers;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.configfile.rs.single.ChangeableDatasetConfigurationFileParser;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.dataset.loaders.csv.changeable.ChangeableCSVFileDatasetLoader;
import delfos.main.managers.CaseUseMode;
import java.io.File;
import java.io.IOException;

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

        try {
            ChangeableDatasetConfigurationFileParser.saveConfigFile(
                    new File("csv-db-config.xml"),
                    datasetLoader);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_SAVE_CONFIG_FILE.exit(ex);
        }
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        return MODE_PARAMETER + " creates a default csv-based dataset.";
    }

}
