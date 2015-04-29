package delfos.main.managers.library.install;

import delfos.ConsoleParameters;
import delfos.Constants;
import static delfos.Constants.LIBRARY_CONFIGURATION_DIRECTORY;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.configuration.ConfigurationManager;
import delfos.configuration.scopes.ConfiguredDatasetsScope;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import delfos.dataset.loaders.movilens.ml100k.MovieLens100k;
import delfos.main.managers.CaseUseMode;
import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @version 27-abr-2015
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class InitialConfiguration extends CaseUseMode {

    private static final InitialConfiguration instance = new InitialConfiguration();

    public static InitialConfiguration getInstance() {
        return instance;
    }

    public static final String INITIAL_CONFIG_FLAG = "--initial-config";
    public static final String CONFIGURED_DATASETS_DIR = "-datasets-dir";

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        if (!isRightManager(consoleParameters)) {
            throw new IllegalStateException("This is not the right manager for the command line '" + consoleParameters.printOriginalParameters());
        }

        Global.showInfoMessage("Initial configuration of '" + Constants.LIBRARY_NAME + "' library\n");

        File configuredDatasetsXML;

        String value = consoleParameters.getValue(LIBRARY_CONFIGURATION_DIRECTORY);

        configuredDatasetsXML = new File(value);

        if (configuredDatasetsXML.exists() && !configuredDatasetsXML.isDirectory()) {
            FileNotFoundException fileNotFoundException = new FileNotFoundException("The " + LIBRARY_CONFIGURATION_DIRECTORY + " parameter must specify a directory: " + value);
            ERROR_CODES.CANNOT_READ_LIBRARY_CONFIG_FILE.exit(fileNotFoundException);
        } else {
            ConfigurationManager.setConfigurationDirectory(configuredDatasetsXML);
            File datasetsDirectory = new File(consoleParameters.getValue(CONFIGURED_DATASETS_DIR));
            createInitialConfiguredDatasetsXML(datasetsDirectory);
        }
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void createInitialConfiguredDatasetsXML(File datasetsDirectory) {

        Global.showInfoMessage("Configure datasets from directory '" + datasetsDirectory.getAbsolutePath() + "'\n");

        if (!datasetsDirectory.exists()) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(new FileNotFoundException("Cannot find directory '" + datasetsDirectory.getAbsolutePath() + "' of the initial datasets."));
        }

        addMovieLens100k(datasetsDirectory);
        addComplete5u10i(datasetsDirectory);

        ConfiguredDatasetsScope.getInstance().saveConfiguredDatasets();
        Global.showInfoMessage("Configured datasets were saved in '"
                + ConfiguredDatasetsScope.getInstance().getConfigurationFile() + "'\n");

    }

    private void addMovieLens100k(File datasetsDirectory) throws RuntimeException {

        String name = "ml-100k";
        String description = "MovieLens 100 thousands ratings.";
        File ml100kDirectory = new File(datasetsDirectory.getAbsolutePath()
                + File.separator + name);

        Global.showInfoMessage("Searching '" + name + "' dataset in directory '" + ml100kDirectory.getAbsolutePath() + "'\n");
        if (!ml100kDirectory.exists()) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(new FileNotFoundException("Cannot find directory '" + ml100kDirectory.getAbsolutePath() + "' of the '" + name + "' dataset."));
        }

        ConfiguredDatasetsFactory.getInstance().addDatasetLoader(
                name,
                description,
                new MovieLens100k(ml100kDirectory)
        );
    }

    @Override
    public String getModeParameter() {
        return INITIAL_CONFIG_FLAG;
    }

    private void addComplete5u10i(File datasetsDirectory) {

        String name = "complete-5u-10i";
        String description = "Dataset with sparsity=0 to perform library unitary tests";
        File datasetDirectory = new File(datasetsDirectory.getAbsolutePath()
                + File.separator + name);

        Global.showInfoMessage("Searching '" + name + "' dataset in directory '" + datasetDirectory.getAbsolutePath() + "'\n");
        if (!datasetDirectory.exists()) {
            String msg = "Cannot find directory '" + datasetDirectory.getAbsolutePath() + "' of the " + name + "dataset.";
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(new FileNotFoundException(msg));
        }

        File ratingsDataset = new File(datasetDirectory + File.separator + "ratings.csv");
        File contentDataset = new File(datasetDirectory + File.separator + "content.csv");
        File usersDataset = new File(datasetDirectory + File.separator + "users.csv");

        ConfiguredDatasetsFactory.getInstance().addDatasetLoader(
                name,
                description,
                new CSVfileDatasetLoader(
                        ratingsDataset.getAbsolutePath(),
                        contentDataset.getAbsolutePath(),
                        usersDataset.getAbsolutePath()
                )
        );
    }

}
