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
public class ExecuteXML extends CaseUseMode {

    public static final String MODE_PARAMETER = "--execute-xml";

    /**
     * The directory that contains the group xml to be executed.
     */
    public static final String XML_DIRECTORY = "-directory";
    public static final String SEED_PARAMETER = "-seed";
    public static final String NUM_EXEC_PARAMETER = "-num-exec";
    public static final String FORCE_EXECUTION = "--force-execution";

    @Override
    public String getModeParameter() {
        return MODE_PARAMETER;
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
        try {
            String xmlExperimentsDirectory = consoleParameters.getValue(ExecuteXML.XML_DIRECTORY);

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
