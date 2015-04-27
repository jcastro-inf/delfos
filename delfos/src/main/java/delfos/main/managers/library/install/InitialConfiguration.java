/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.main.managers.library.install;

import delfos.ConsoleParameters;
import static delfos.Constants.LIBRARY_CONFIGURATION_DIRECTORY;
import delfos.ERROR_CODES;
import delfos.configuration.ConfigurationManager;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.loaders.movilens.ml100k.MovieLens100k;
import delfos.main.managers.CaseUseManager;
import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @version 27-abr-2015
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class InitialConfiguration implements CaseUseManager {

    private static final InitialConfiguration instance = new InitialConfiguration();

    public static InitialConfiguration getInstance() {
        return instance;
    }

    public static final String INITIAL_CONFIG_FLAG = "--initial-config";
    public static final String INITIAL_CONFIG_XML_FILE = "-configured-datasets";

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isDefined(INITIAL_CONFIG_FLAG);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        if (!isRightManager(consoleParameters)) {
            throw new IllegalStateException("This is not the right manager for the command line '" + consoleParameters.printOriginalParameters());
        }

        File configuredDatasetsXML;

        String value = consoleParameters.getValue(INITIAL_CONFIG_XML_FILE);

        configuredDatasetsXML = new File(value);

        if (!configuredDatasetsXML.exists()) {
            FileNotFoundException fileNotFoundException = new FileNotFoundException("The specified file does not exist: " + value);
            ERROR_CODES.CANNOT_READ_LIBRARY_CONFIG_FILE.exit(fileNotFoundException);
        } else if (!configuredDatasetsXML.isDirectory()) {
            FileNotFoundException fileNotFoundException = new FileNotFoundException("The " + LIBRARY_CONFIGURATION_DIRECTORY + " parameter must specify a directory: " + value);
            ERROR_CODES.CANNOT_READ_LIBRARY_CONFIG_FILE.exit(fileNotFoundException);
        } else {
            createInitialConfiguredDatasetsXML(configuredDatasetsXML);
        }
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void createInitialConfiguredDatasetsXML(File configuredDatasetsDirectory) {
        ConfigurationManager.setConfigurationDirectory(configuredDatasetsDirectory);

        String name = "ml-100k";
        String description = "MovieLens 100 thousands ratings.";

        String path = System.getenv("DELFOS_PATH");
        File ml100kDirectory = new File(path + File.separator + "datasets" + File.separator);

        ConfiguredDatasetsFactory.getInstance()
                .addDatasetLoader(
                        name,
                        description,
                        new MovieLens100k(ml100kDirectory)
                );

    }

}
