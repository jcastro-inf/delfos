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
    public static final String XML_DIRECTORY = ExecuteXML.XML_DIRECTORY;
    private static final String SEED_PARAMETER = ExecuteXML.SEED_PARAMETER;
    private static final String NUM_EXEC_PARAMETER = ExecuteXML.NUM_EXEC_PARAMETER;

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

    private ExecuteGroupXML() {
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        try {
            String xmlExperimentsDirectory = consoleParameters.getValue(ExecuteGroupXML.XML_DIRECTORY);

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

            consoleParameters.printUnusedParameters(System.err);
            manageCaseUse(xmlExperimentsDirectory, xmlExperimentsDirectory + File.separator + "dataset" + File.separator, NUM_EJECUCIONES, SEED);
        } catch (UndefinedParameterException ex) {
            consoleParameters.printUnusedParameters(System.err);
        }
    }

    public static void manageCaseUse(String experimentsDirectory, String datasetDirectory, int numExecutions, long seed) {
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
