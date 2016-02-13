/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import delfos.dataset.loaders.movilens.ml1m.MovieLens1Million;
import delfos.main.managers.CaseUseMode;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @version 27-abr-2015
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class InitialConfiguration extends CaseUseMode {

    private static final InitialConfiguration instance = new InitialConfiguration();

    public static InitialConfiguration getInstance() {
        return instance;
    }

    public static final String INITIAL_CONFIG_FLAG = "--initial-config";
    public static final String CONFIGURED_DATASETS_DIR = "-datasets-dir";
    public static final String CONFIGURED_DATASETS_TO_INSTALL = "-datasets-to-install";

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        if (!isRightManager(consoleParameters)) {
            throw new IllegalStateException("This is not the right manager for the command line '" + consoleParameters.printOriginalParameters());
        }

        Global.showInfoMessage("Initial configuration of '" + Constants.LIBRARY_NAME + "' library\n");

        File configuredDatasetsXML;

        String value = consoleParameters.getValue(LIBRARY_CONFIGURATION_DIRECTORY);

        Set<String> datasetsToInstall = new TreeSet<>();
        if (consoleParameters.isParameterDefined(CONFIGURED_DATASETS_TO_INSTALL)) {
            List<String> values = consoleParameters.getValues(CONFIGURED_DATASETS_TO_INSTALL);

            datasetsToInstall.addAll(values);
        } else {
            datasetsToInstall.addAll(Arrays.asList("ml-100k", "complete-5u-10i", "ssii-partition9"));
        }

        configuredDatasetsXML = new File(value);

        if (configuredDatasetsXML.exists() && !configuredDatasetsXML.isDirectory()) {
            FileNotFoundException fileNotFoundException = new FileNotFoundException("The " + LIBRARY_CONFIGURATION_DIRECTORY + " parameter must specify a directory: " + value);
            ERROR_CODES.CANNOT_READ_LIBRARY_CONFIG_FILE.exit(fileNotFoundException);
        } else {
            ConfigurationManager.setConfigurationDirectory(configuredDatasetsXML);
            File datasetsDirectory = new File(consoleParameters.getValue(CONFIGURED_DATASETS_DIR));
            createInitialConfiguredDatasetsXML(datasetsDirectory, datasetsToInstall);
        }
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void createInitialConfiguredDatasetsXML(File datasetsDirectory, Set<String> datasetsToInstall) {

        Global.showInfoMessage("Configure datasets from directory '" + datasetsDirectory.getAbsolutePath() + "'\n");

        if (!datasetsDirectory.exists()) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(new FileNotFoundException("Cannot find directory '" + datasetsDirectory.getAbsolutePath() + "' of the initial datasets."));
        }

        if (datasetsToInstall.contains("ml-10m")) {
            addMovieLens10m(datasetsDirectory);
            datasetsToInstall.remove("ml-10m");
        }
        if (datasetsToInstall.contains("complete-5u-10i")) {
            addComplete5u10i(datasetsDirectory);
            datasetsToInstall.remove("complete-5u-10i");
        }
        if (datasetsToInstall.contains("ssii-partition9")) {
            addSSIIPartition9(datasetsDirectory);
            datasetsToInstall.remove("ssii-partition9");
        }

        if (datasetsToInstall.contains("ml-100k")) {
            addMovieLens100k(datasetsDirectory);
            datasetsToInstall.remove("ml-100k");
        }

        if (datasetsToInstall.contains("ml-1m")) {
            addMovieLens1m(datasetsDirectory);
            datasetsToInstall.remove("ml-1m");
        }

        if (!datasetsToInstall.isEmpty()) {
            Global.showWarning("Unrecognised datasets specified on parameter " + CONFIGURED_DATASETS_TO_INSTALL);
            datasetsToInstall.stream().forEach((dataset) -> {
                Global.showWarning("\t\t" + dataset);
            });
        }

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

    private void addSSIIPartition9(File datasetsDirectory) {

        String name = "ssii-partition9";
        String description = "Dataset made for the SSII lessons, partition number9";
        File datasetDirectory = new File(datasetsDirectory.getAbsolutePath()
                + File.separator + name);

        Global.showInfoMessage("Searching '" + name + "' dataset in directory '" + datasetDirectory.getAbsolutePath() + "'\n");
        if (!datasetDirectory.exists()) {
            String msg = "Cannot find directory '" + datasetDirectory.getAbsolutePath() + "' of the " + name + "dataset.";
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(new FileNotFoundException(msg));
        }

        File ratingsDataset = new File(datasetDirectory + File.separator + "SSII - ratings9.csv");
        File contentDataset = new File(datasetDirectory + File.separator + "SSII - movies.csv");

        ConfiguredDatasetsFactory.getInstance().addDatasetLoader(
                name,
                description,
                new CSVfileDatasetLoader(
                        ratingsDataset.getAbsolutePath(),
                        contentDataset.getAbsolutePath()
                )
        );
    }

    private void addMovieLens1m(File datasetsDirectory) {
        String name = "ml-1m";
        String description = "MovieLens one million ratings.";
        File ml1mDirectory = new File(datasetsDirectory.getAbsolutePath()
                + File.separator + name);

        Global.showInfoMessage("Searching '"
                + name + "' dataset in directory '"
                + ml1mDirectory.getAbsolutePath() + "'\n");

        if (!ml1mDirectory.exists()) {
            String msg
                    = "Cannot find directory '" + ml1mDirectory.getAbsolutePath()
                    + "' of the '" + name + "' dataset.";

            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(new FileNotFoundException(msg));
        }

        ConfiguredDatasetsFactory.getInstance().addDatasetLoader(
                name,
                description,
                new MovieLens1Million(ml1mDirectory)
        );
    }

    private void addMovieLens10m(File datasetsDirectory) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
