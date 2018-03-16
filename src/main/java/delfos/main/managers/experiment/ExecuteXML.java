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
package delfos.main.managers.experiment;

import delfos.ConsoleParameters;
import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.casestudy.fromxmlfiles.XMLexperimentsExecution;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.experiment.Experiment;
import delfos.io.xml.experiment.ExperimentXML;
import delfos.main.Main;
import delfos.main.managers.CaseUseMode;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jdk.nashorn.internal.runtime.options.Option;
import org.jdom2.JDOMException;

/**
 *
 * @version 21-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ExecuteXML extends CaseUseMode {

    public static final String MODE_PARAMETER = "--execute-xml";

    /**
     * The directory that contains the group xml to be executed.
     */
    public static final String XML_DIRECTORY = "-directory";
    public static final String XML_FILE = "-file";
    public static final String SEED_PARAMETER = "-seed";
    public static final String NUM_EXEC_PARAMETER = "-num-exec";
    public static final String FORCE_EXECUTION = "--force-execution";

    public static boolean isAnyResultAggregatedXMLPresent(File xmlExperimentsDirectory) {
        File xmlExperimentResultsDirectory = new File(xmlExperimentsDirectory.getPath() + File.separator + "results" + File.separator);
        if (!xmlExperimentResultsDirectory.exists()) {
            Global.showMessage("xmlExperimentResultsDirectory not exists: '"+xmlExperimentResultsDirectory+"'");
            return true;
        }
        if (!xmlExperimentResultsDirectory.isDirectory()) {
            throw new IllegalStateException("Results directory not found (is a file) ['" + xmlExperimentResultsDirectory.getAbsolutePath() + "']");
        }

        List<File> filesInResultsDirectory = FileUtilities.findInDirectory(xmlExperimentsDirectory);
        List<File> aggregateResultsFiles = filesInResultsDirectory.
                stream().
                filter(file -> {
                    String name = file.getName();

                    boolean isAggrFile = name.contains("_AGGR.xml");
                    return isAggrFile;
                }).
                collect(Collectors.toList());

        return !aggregateResultsFiles.isEmpty();
    }

    public static boolean isForceExecution(ConsoleParameters consoleParameters) {
        return consoleParameters.isFlagDefined(ExecuteGroupXML.FORCE_EXECUTION);
    }

    @Override
    public String getModeParameter() {
        return MODE_PARAMETER;
    }

    public static boolean shouldExecuteTheExperiment(File xmlExperimentFile, Optional<Integer> numExecutionsRequested) {

        Experiment experimentLoaded;
        try {
            experimentLoaded = ExperimentXML.loadExperiment(xmlExperimentFile);
        } catch (JDOMException|IOException e) {
            e.printStackTrace();
            experimentLoaded = null;
            ERROR_CODES.CANNOT_READ_CASE_STUDY_XML.exit(e);
        }

        if(experimentLoaded == null){
            throw new IllegalStateException("Experiment loaded is null.");
        }
        Experiment experimentDescription = experimentLoaded;


        File resultsDirectory = experimentDescription.getResultsDirectory();
        if (!resultsDirectory.exists()) {
            Global.showMessage("Results directory does not exist: "+resultsDirectory+"\n");
            return true;
        }

        List<File> resultsFiles = FileUtilities.findInDirectory(resultsDirectory);

        List<File> resultsXMLFiles = resultsFiles.stream().
                filter(file -> file.getName().endsWith(".xml")).
                collect(Collectors.toList());

        if(resultsXMLFiles.isEmpty()){
            Global.showMessage("No xmls found in results directory\n");
            return true;
        }

        List<Experiment> sameExperiments = resultsXMLFiles.stream().flatMap(file -> {

            Stream<Experiment> ret = Arrays.asList(new Experiment[0]).stream();
            try {
                Experiment experimentResult = ExperimentXML.loadExperiment(file);

                boolean isSameExperiment = experimentDescription.equals(experimentResult);
                if(isSameExperiment) {
                    ret = Arrays.asList(experimentResult).stream();
                }

            } catch (JDOMException|IOException e) {
                e.printStackTrace();
            }
            return ret;
        }).collect(Collectors.toList());

        if(sameExperiments.isEmpty()){
            Global.showMessage("Found experiment results but they have different description.\n");
            return true;
        }

        List<Integer> numExecutionsOfSameExperiment = sameExperiments.
                stream().map(experimentResult ->{
            boolean hasResultsForAllExecutions = experimentResult.hasResultsForAllExecutions();
            if (hasResultsForAllExecutions) {
                return experimentResult.getNumExecutions();
            } else {
                return 0;
            }

        }).collect(Collectors.toList());

        int numExecutionsFound = numExecutionsOfSameExperiment.
                stream().mapToInt(x -> x).max().orElse(0);

        int numExecutions = numExecutionsRequested.orElse(experimentDescription.getNumExecutions());

        Global.showMessage("There are experiment results with "+numExecutionsFound+" executions (requested "+numExecutions+") for "+xmlExperimentFile +"\n");
        return numExecutionsFound < numExecutions;
    }

    private static class Holder {
        private static final ExecuteXML INSTANCE = new ExecuteXML();
    }

    public static ExecuteXML getInstance() {
        return Holder.INSTANCE;
    }

    private ExecuteXML() {
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {

        if(consoleParameters.isParameterDefined(ExecuteXML.XML_FILE)){
            try {
                File xmlExperimentFile = new File(consoleParameters.getValue(ExecuteXML.XML_FILE));
                Optional<Integer> numExecutions = getNumExecutions(consoleParameters);
                Optional<Long> seed = getSeed(consoleParameters);

                consoleParameters.printUnusedParameters(System.err);

                if (ExecuteXML.isForceExecution(consoleParameters) || ExecuteXML.shouldExecuteTheExperiment(xmlExperimentFile,numExecutions)) {
                    Global.showMessageTimestamped("The experiment is going to be executed (" + xmlExperimentFile.getAbsolutePath() + ")");
                    Global.showMessageTimestamped("command: " + consoleParameters.printOriginalParameters());
                    manageCaseUse(
                            xmlExperimentFile,
                            xmlExperimentFile + File.separator + "dataset" + File.separator,
                            numExecutions,
                            seed);
                } else {
                    Global.showMessageTimestamped("The experiment was already executed. (" + xmlExperimentFile.getPath() + ")");
                }
            } catch (UndefinedParameterException ex) {
                consoleParameters.printUnusedParameters(System.err);
            }
        }

        if(consoleParameters.isParameterDefined(ExecuteXML.XML_DIRECTORY)){

            consoleParameters.printUnusedParameters(System.err);
            findInDirectoryAndCallDelfosOneTimeForEachXMLDescriptionFile(consoleParameters);
        }


    }

    private void findInDirectoryAndCallDelfosOneTimeForEachXMLDescriptionFile(ConsoleParameters consoleParameters) {
        Global.showMessage("Directory specified, finding xmls descriptions recursively\n");
        consoleParameters.getValues(ExecuteXML.XML_DIRECTORY).forEach(dir ->{
            Global.showMessage("\t"+dir+"\n");
        });

        List<File> directoriesSpecified = consoleParameters.
                getValues(ExecuteXML.XML_DIRECTORY).stream().
                map(path -> new File(path)).collect(Collectors.toList());

        if(directoriesSpecified.stream().anyMatch(directory -> ! directory.isDirectory())){
            directoriesSpecified.stream().filter(directory -> ! directory.isDirectory()).forEach(file -> {
                Global.showWarning(file + " is not a directory");
            });
        }

        directoriesSpecified.stream().
                flatMap(directory -> FileUtilities.findInDirectory(directory).stream()).
                filter( file -> !file.isDirectory()).
                filter( file -> file.getPath().contains(File.separator+"descriptions"+File.separator)).
                forEach( experimentFile ->{
                    String[] args = {
                            ExecuteXML.MODE_PARAMETER,
                            ExecuteXML.XML_FILE, experimentFile.getPath(),
                            Constants.PRINT_FULL_XML,
                            Constants.RAW_DATA};
                    try {
                        Main.mainWithExceptions(args);
                    } catch (Exception ex) {
                        Global.showWarning("Experiment failed in directory '" + experimentFile.getAbsolutePath());
                        Global.showError(ex);
                    }
                });
    }

    public static void manageCaseUse(File experimentsDirectory, String datasetDirectory, Optional<Integer> numExecutions, Optional<Long> seed) {
        try {
            XMLexperimentsExecution execution = new XMLexperimentsExecution(
                    experimentsDirectory.getPath(),
                    numExecutions,
                    seed);
            execution.execute();
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        }
    }

    public static Optional<Long> getSeed(ConsoleParameters consoleParameters) throws NumberFormatException {
        try {
            return Optional.of(Long.parseLong(consoleParameters.getValue(SEED_PARAMETER)));

        } catch (UndefinedParameterException ex) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> getNumExecutions(ConsoleParameters consoleParameters) throws NumberFormatException {
        try {
            return Optional.of(Integer.parseInt(consoleParameters.getValue(NUM_EXEC_PARAMETER)));
        } catch (UndefinedParameterException ex) {
            return Optional.empty();
        }
    }
}
