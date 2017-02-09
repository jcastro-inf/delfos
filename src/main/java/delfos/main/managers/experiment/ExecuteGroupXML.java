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
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.group.casestudy.fromxmlfiles.GroupXMLexperimentsExecution;
import delfos.main.managers.CaseUseMode;
import java.io.File;

/**
 *
 * @version 21-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
            int NUM_EJECUCIONES = ExecuteXML.getNumExecutions(consoleParameters);
            long SEED = ExecuteXML.getSeed(consoleParameters);
            boolean forceReExecution = ExecuteXML.isForceExecution(consoleParameters);

            consoleParameters.printUnusedParameters(System.err);

            if (ExecuteXML.shouldExecuteTheExperiment(xmlExperimentsDirectory, NUM_EJECUCIONES, forceReExecution)) {

                Global.showMessageTimestamped("The experiment is going to be executed (" + xmlExperimentsDirectory.getAbsolutePath() + ")");
                Global.showMessageTimestamped("command: " + consoleParameters.printOriginalParameters());
                manageCaseUse(xmlExperimentsDirectory, xmlExperimentsDirectory + File.separator + "dataset" + File.separator, NUM_EJECUCIONES, SEED);
            } else {
                Global.showMessageTimestamped("The experiment was already executed. (" + xmlExperimentsDirectory.getPath() + ")");
            }
        } catch (UndefinedParameterException ex) {
            consoleParameters.printUnusedParameters(System.err);
        }
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

}
