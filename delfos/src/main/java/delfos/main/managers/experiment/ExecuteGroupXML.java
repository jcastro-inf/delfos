package delfos.main.managers.experiment;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.group.casestudy.fromxmlfiles.GroupXMLexperimentsExecution;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.main.managers.CaseUseMode;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.jdom2.JDOMException;

/**
 *
 * @version 21-oct-2014
 * @author Jorge Castro Gallardo
 */
public class ExecuteGroupXML extends CaseUseMode {

    public static final String MODE_PARAMETER = "--execute-group-xml";

    /**
     * The directory that contains the group xml to be executed.
     */
    public static final String XML_DIRECTORY = ExecuteXML.XML_DIRECTORY;
    public static final String SEED_PARAMETER = ExecuteXML.SEED_PARAMETER;
    public static final String NUM_EXEC_PARAMETER = ExecuteXML.NUM_EXEC_PARAMETER;
    public static final String FORCE_EXECUTION = ExecuteXML.FORCE_EXECUTION;

    @Override
    public String getModeParameter() {
        return MODE_PARAMETER;
    }

    private boolean isForceExecution(ConsoleParameters consoleParameters) {
        return consoleParameters.isFlagDefined(FORCE_EXECUTION);
    }

    private boolean isAnyResultAggregatedXMLPresent(File xmlExperimentsDirectory) {
        File xmlExperimentResultsDirectory = new File(xmlExperimentsDirectory.getPath() + File.separator + "results" + File.separator);
        if (!xmlExperimentResultsDirectory.exists()) {
            return false;
        }
        if (!xmlExperimentResultsDirectory.isDirectory()) {
            throw new IllegalStateException("Results directory not found (is a file) ['" + xmlExperimentResultsDirectory.getAbsolutePath() + "']");
        }

        List<File> aggregateResults = Arrays.asList(xmlExperimentResultsDirectory.listFiles((File dir, String name) -> name.contains("_AGGR.xml")));
        return !aggregateResults.isEmpty();
    }

    private boolean isNumExecGreaterThanAllTheExisting(File xmlExperimentsDirectory, int NUM_EJECUCIONES) {

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

    private static class Holder {

        private static final ExecuteGroupXML INSTANCE = new ExecuteGroupXML();
    }

    public static ExecuteGroupXML getInstance() {
        return Holder.INSTANCE;
    }

    private ExecuteGroupXML() {
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        try {
            File xmlExperimentsDirectory = new File(consoleParameters.getValue(ExecuteGroupXML.XML_DIRECTORY));
            int NUM_EJECUCIONES = getNumExecutions(consoleParameters);
            long SEED = getSeed(consoleParameters);
            boolean forceReExecution = isForceExecution(consoleParameters);

            consoleParameters.printUnusedParameters(System.err);

            if (shouldExecuteTheExperiment(xmlExperimentsDirectory, NUM_EJECUCIONES, forceReExecution)) {

                Global.showMessageTimestamped("The experiment is going to be executed (" + xmlExperimentsDirectory.getAbsolutePath() + ")");
                manageCaseUse(xmlExperimentsDirectory, xmlExperimentsDirectory + File.separator + "dataset" + File.separator, NUM_EJECUCIONES, SEED);
            } else {
                Global.showMessageTimestamped("The experiment was already executed. (" + xmlExperimentsDirectory.getPath() + ")");
            }
        } catch (UndefinedParameterException ex) {
            consoleParameters.printUnusedParameters(System.err);
        }
    }

    private long getSeed(ConsoleParameters consoleParameters) throws NumberFormatException {
        long SEED;
        {
            long num;
            try {
                num = Long.parseLong(consoleParameters.getValue(SEED_PARAMETER));
            } catch (UndefinedParameterException ex) {
                num = System.currentTimeMillis();
            }
            SEED = num;
        }
        return SEED;
    }

    private int getNumExecutions(ConsoleParameters consoleParameters) throws NumberFormatException {
        final int NUM_EJECUCIONES;
        {
            int num;
            try {
                num = Integer.parseInt(consoleParameters.getValue(NUM_EXEC_PARAMETER));
            } catch (UndefinedParameterException ex) {
                num = 1;
            }
            NUM_EJECUCIONES = num;
        }
        return NUM_EJECUCIONES;
    }

    public static void manageCaseUse(File experimentsDirectory, String datasetDirectory, int numExecutions, long seed) {
        try {
            GroupXMLexperimentsExecution execution = new GroupXMLexperimentsExecution(
                    experimentsDirectory.getPath(),
                    datasetDirectory,
                    numExecutions,
                    seed);
            execution.execute();
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        }
    }

    public boolean shouldExecuteTheExperiment(File xmlExperimentsDirectory, int NUM_EJECUCIONES, boolean forceReExecution) {
        if (forceReExecution) {
            return true;
        } else if (isAnyResultAggregatedXMLPresent(xmlExperimentsDirectory)) {
            return isNumExecGreaterThanAllTheExisting(xmlExperimentsDirectory, NUM_EJECUCIONES);
        } else {
            return true;
        }
    }
}
