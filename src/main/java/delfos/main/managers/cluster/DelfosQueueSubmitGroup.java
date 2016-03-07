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
package delfos.main.managers.cluster;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.FileUtilities;
import delfos.main.managers.CaseUseMode;
import delfos.main.managers.experiment.ExecuteGroupXML;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class DelfosQueueSubmitGroup extends CaseUseMode {

    public static final String MODE_PARAMETER = "--qsub-group";

    private static DelfosQueueSubmitGroup instance = null;

    private DelfosQueueSubmitGroup() {
    }

    public static DelfosQueueSubmitGroup getInstance() {
        if (instance == null) {
            instance = new DelfosQueueSubmitGroup();
        }
        return instance;
    }

    @Override
    public String getModeParameter() {
        return MODE_PARAMETER;
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        int numExec;
        try {
            numExec = Integer.parseInt(consoleParameters.getValue(ExecuteGroupXML.NUM_EXEC_PARAMETER));
        } catch (UndefinedParameterException ex) {
            ERROR_CODES.COMMAND_LINE_PARAMETER_IS_NOT_DEFINED.exit(ex);
            throw new IllegalStateException(ex);
        }

        File directory;
        try {
            directory = new File(consoleParameters.getValue(ExecuteGroupXML.XML_DIRECTORY));
        } catch (UndefinedParameterException ex) {
            ERROR_CODES.COMMAND_LINE_PARAMETER_IS_NOT_DEFINED.exit(ex);
            throw new IllegalStateException(ex);
        }

        qsubExperimentsInDirectory(directory, numExec);
    }

    private void qsubExperimentsInDirectory(File directory, int numExec) {

        File delfosQsubGroupJobScript = new File("delfos-qsub-group-job.sh");
        if (!delfosQsubGroupJobScript.exists()) {
            throw new IllegalStateException(
                    "The 'delfos-qsub-group-job.sh' script is not present in "
                    + "working directory '" + delfosQsubGroupJobScript.getParent() + "'");
        }

        List<File> findInDirectory = FileUtilities.findInDirectory(directory).stream()
                .collect(Collectors.toList());

        List<File> directoriesChild = findInDirectory.parallelStream()
                .filter(directoryChild -> directoryChild.isDirectory())
                .collect(Collectors.toList());

        List<File> datasetDirectories = directoriesChild.parallelStream().
                filter(directoryChild -> directoryChild.getName().equals("dataset"))
                .collect(Collectors.toList());

        List<File> experimentDirectories = datasetDirectories.parallelStream()
                .map(directoryDataset -> directoryDataset.getParentFile())
                .collect(Collectors.toList());

        experimentDirectories.stream().forEachOrdered(experimentDirectory -> {

            int numQueue = (experimentDirectory.hashCode() % 2) + 1;

            Runtime rt = Runtime.getRuntime();
            try {
                final String command = "qsub "
                        + "-q queue" + numQueue + " "
                        + "-v "
                        + "experimentFolder=" + experimentDirectory.getAbsolutePath() + File.separator + ","
                        + "numExec=" + numExec + " "
                        + "./delfos-qsub-group-job.sh;";
                Process pr = rt.exec(command);

                pr.waitFor();
            } catch (IOException ex) {
                Logger.getLogger(DelfosQueueSubmitGroup.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(DelfosQueueSubmitGroup.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(DelfosQueueSubmitGroup.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

    }

}
