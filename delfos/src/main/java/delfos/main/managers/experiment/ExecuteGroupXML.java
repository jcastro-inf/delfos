package delfos.main.managers.experiment;

import java.io.File;
import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.group.casestudy.fromxmlfiles.GroupXMLexperimentsExecution;
import delfos.main.managers.CaseUseManager;

/**
 *
 * @version 21-oct-2014
* @author Jorge Castro Gallardo
 */
public class ExecuteGroupXML implements CaseUseManager {

    /**
     * Parámetro de la linea de comandos para especificar que se muestre la
     * interfaz de recomendación.
     */
    public static final String EXECUTE_GROUP_XML = "-executeGroupXML";

    private static class Holder {

        private static final ExecuteGroupXML INSTANCE = new ExecuteGroupXML();
    }

    public static ExecuteGroupXML getInstance() {
        return Holder.INSTANCE;
    }

    public ExecuteGroupXML() {
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isDefined(EXECUTE_GROUP_XML);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        try {
            String xmlExperimentsDirectory = consoleParameters.getValue("-executeGroupXML");

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

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
