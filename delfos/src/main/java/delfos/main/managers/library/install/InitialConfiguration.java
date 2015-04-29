package delfos.main.managers.library.install;

import delfos.ConsoleParameters;
import delfos.Constants;
import static delfos.Constants.LIBRARY_CONFIGURATION_DIRECTORY;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.configuration.ConfigurationManager;
import delfos.configuration.scopes.ConfiguredDatasetsScope;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.loaders.movilens.ml100k.MovieLens100k;
import delfos.main.managers.CaseUseModeManager;
import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @version 27-abr-2015
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class InitialConfiguration extends CaseUseModeManager {

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

        String name = "ml-100k";
        String description = "MovieLens 100 thousands ratings.";

        Global.showInfoMessage("Configure datasets from directory '" + datasetsDirectory.getAbsolutePath() + "'\n");

        if (!datasetsDirectory.exists()) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(new FileNotFoundException("Cannot find directory '" + datasetsDirectory.getAbsolutePath() + "' of the initial datasets."));
        }

        File ml100kDirectory = new File(datasetsDirectory.getAbsolutePath()
                + File.separator + "ml-100k");

        Global.showInfoMessage("Searching 'ml-100k' dataset in directory '" + ml100kDirectory.getAbsolutePath() + "'\n");
        if (!ml100kDirectory.exists()) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(new FileNotFoundException("Cannot find directory '" + ml100kDirectory.getAbsolutePath() + "' of the ml-100k dataset."));
        }

        ConfiguredDatasetsFactory.getInstance().addDatasetLoader(
                name,
                description,
                new MovieLens100k(ml100kDirectory)
        );
        ConfiguredDatasetsScope.getInstance().saveConfiguredDatasets();
        Global.showInfoMessage("Configured datasets were saved in '"
                + ConfiguredDatasetsScope.getInstance().getConfigurationFile() + "'\n");

    }

    @Override
    public String getModeParameter() {
        return INITIAL_CONFIG_FLAG;
    }

}
