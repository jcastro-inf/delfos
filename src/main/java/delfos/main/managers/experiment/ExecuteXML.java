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
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.casestudy.fromxmlfiles.XMLexperimentsExecution;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.experiment.Experiment;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.io.xml.experiment.ExperimentXML;
import delfos.main.managers.CaseUseMode;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            Global.showMessage("xmlExperimentResultsDirectory exists: '"+xmlExperimentResultsDirectory+"'");
            return false;
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

    public static boolean isNumExecGreaterThanAllTheExisting(File xmlExperimentsDirectory, int NUM_EJECUCIONES) {
        File xmlExperimentResultsDirectory = new File(xmlExperimentsDirectory.getPath() + File.separator + "results" + File.separator);
        List<File> aggregateResults = Arrays.asList(xmlExperimentResultsDirectory.listFiles((File dir, String name) -> name.contains("_AGGR.xml")));
        boolean isNumExecGreaterThanTheExisting = true;
        for (File resultAggregatedXML : aggregateResults) {
            try {
                int resultAggregatedXML_numExec = GroupCaseStudyXML.extractResultNumExec(resultAggregatedXML);
                if (resultAggregatedXML_numExec >= NUM_EJECUCIONES) {
                    isNumExecGreaterThanTheExisting = false;
                }
            } catch (JDOMException | IOException ex) {
                ERROR_CODES.CANNOT_READ_CASE_STUDY_XML.exit(ex);
            }
        }
        return isNumExecGreaterThanTheExisting;
    }

    public static boolean isForceExecution(ConsoleParameters consoleParameters) {
        return consoleParameters.isFlagDefined(ExecuteGroupXML.FORCE_EXECUTION);
    }

    @Override
    public String getModeParameter() {
        return MODE_PARAMETER;
    }

    public static boolean shouldExecuteTheExperiment(File xmlExperimentsDirectory, int NUM_EJECUCIONES, boolean forceReExecution) {
        if (forceReExecution) {
            Global.showMessageTimestamped("Execution is being forced"+"\n");
            return true;
        }

        Global.showMessageTimestamped("Cheking isAnyResultAggregatedXMLPresent("+xmlExperimentsDirectory+")"+"\n");
        if (isAnyResultAggregatedXMLPresent(xmlExperimentsDirectory)) {
            Global.showMessageTimestamped("XML is not present"+"\n");
            return isNumExecGreaterThanAllTheExisting(xmlExperimentsDirectory, NUM_EJECUCIONES);
        } else {
            Global.showMessageTimestamped("XML is not present"+"\n");
            return true;
        }
    }


    public static boolean shouldExecuteTheExperimentOfFile(File xmlExperimentFile, int NUM_EJECUCIONES, boolean forceReExecution) {

        if (forceReExecution) {
            return true;
        } else {
            try {
                Experiment experiment = ExperimentXML.loadExperiment(xmlExperimentFile);

                File results = experiment.getResultsDirectory();
                if (!results.exists()) {
                    return true;
                }

                List<File> resultsFiles = FileUtilities.findInDirectory(results);

                List<File> resultsXMLFiles = resultsFiles.stream().
                        filter(file -> file.getName().endsWith(".xml")).
                        collect(Collectors.toList());

                boolean isResultWithSpecifiedExecutionsPresent = resultsXMLFiles.stream().
                        anyMatch(file -> {
                            try {
                                Experiment experimentResult = ExperimentXML.loadExperiment(file);

                                boolean isSameExperiment = experiment.equals(experimentResult);
                                if(!isSameExperiment)
                                    return false;

                                boolean hasResultsForAllExecutions = experimentResult.hasResultsForAllExecutions();

                                return hasResultsForAllExecutions;
                            } catch (JDOMException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return false;
                        });

                return !isResultWithSpecifiedExecutionsPresent;
            } catch (JDOMException e) {
                e.printStackTrace();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }

        }
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

        if(consoleParameters.isParameterDefined(ExecuteXML.XML_DIRECTORY)){
            try {
                File xmlExperimentsDirectory = new File(consoleParameters.getValue(ExecuteXML.XML_DIRECTORY));
                Optional<Integer> numExecutions = getNumExecutions(consoleParameters);
                Optional<Long> seed = getSeed(consoleParameters);
                boolean forceReExecution = ExecuteXML.isForceExecution(consoleParameters);

                consoleParameters.printUnusedParameters(System.err);

                if (ExecuteXML.shouldExecuteTheExperiment(xmlExperimentsDirectory, numExecutions.orElse(1), forceReExecution)) {
                    Global.showMessageTimestamped("The experiment is going to be executed (" + xmlExperimentsDirectory.getAbsolutePath() + ")");
                    Global.showMessageTimestamped("command: " + consoleParameters.printOriginalParameters());
                    manageCaseUse(
                            xmlExperimentsDirectory,
                            xmlExperimentsDirectory + File.separator + "dataset" + File.separator,
                            numExecutions,
                            seed);
                } else {
                    Global.showMessageTimestamped("The experiment was already executed. (" + xmlExperimentsDirectory.getPath() + ")");
                }
            } catch (UndefinedParameterException ex) {
                consoleParameters.printUnusedParameters(System.err);
            }
        }

        if(consoleParameters.isParameterDefined(ExecuteXML.XML_FILE)){
            try {
                File xmlExperimentFile = new File(consoleParameters.getValue(ExecuteXML.XML_FILE));
                Optional<Integer> numExecutions = getNumExecutions(consoleParameters);
                Optional<Long> seed = getSeed(consoleParameters);
                boolean forceReExecution = ExecuteXML.isForceExecution(consoleParameters);

                consoleParameters.printUnusedParameters(System.err);

                if (ExecuteXML.shouldExecuteTheExperimentOfFile(xmlExperimentFile, numExecutions.orElse(1), forceReExecution)) {
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
