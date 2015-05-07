package delfos.main.managers.experiment;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.group.casestudy.fromxmlfiles.GroupXMLexperimentsExecution;
import delfos.main.managers.CaseUseMode;
import java.io.File;

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
    public static final String EXECUTE_GROUP_XML_DIRECTORY = "-directory";

    @Override
    public String getModeParameter() {
        return MODE_PARAMETER;
    }

    private static class Holder {

        private static final ExecuteGroupXML INSTANCE = new ExecuteGroupXML();
    }

    public static ExecuteGroupXML getInstance() {
        return Holder.INSTANCE;
    }

    public ExecuteGroupXML() {
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        try {
            String xmlExperimentsDirectory = consoleParameters.getValue(ExecuteGroupXML.MODE_PARAMETER);

            final int NUM_EJECUCIONES;
            {
                int num;
                try {
                    num = Integer.parseInt(consoleParameters.getValue("-numExec"));
                } catch (UndefinedParameterException ex) {
                    num = 1;
                }
                NUM_EJECUCIONES = num;
            }

            long SEED;
            {
                long num;
                try {
                    num = Long.parseLong(consoleParameters.getValue("-seed"));
                } catch (UndefinedParameterException ex) {
                    num = System.currentTimeMillis();
                }
                SEED = num;
            }

            consoleParameters.printUnusedParameters(System.err);
            xmlExperimentsExecution(xmlExperimentsDirectory, xmlExperimentsDirectory + File.separator + "dataset" + File.separator, NUM_EJECUCIONES, SEED);
        } catch (UndefinedParameterException ex) {
            consoleParameters.printUnusedParameters(System.err);
        }
    }

    private static void xmlExperimentsExecution(String experimentsDirectory, String datasetDirectory, int numExecutions, long seed) {
        try {
            GroupXMLexperimentsExecution execution = new GroupXMLexperimentsExecution(
                    experimentsDirectory,
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
}
