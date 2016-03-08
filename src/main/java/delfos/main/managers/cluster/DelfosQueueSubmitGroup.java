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
import delfos.common.Global;
import delfos.main.managers.CaseUseMode;
import delfos.main.managers.experiment.ExecuteGroupXML;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

        if (consoleParameters.isParameterDefined(ExecuteGroupXML.XML_DIRECTORY)) {
            File directory;
            try {
                directory = new File(consoleParameters.getValue(ExecuteGroupXML.XML_DIRECTORY));
                qsubExperimentsInDirectory(directory, numExec);
            } catch (UndefinedParameterException ex) {
                ERROR_CODES.COMMAND_LINE_PARAMETER_IS_NOT_DEFINED.exit(ex);
                throw new IllegalStateException(ex);
            }
        }

        if (consoleParameters.isParameterDefined("-xml")) {
            File xml;
            try {
                xml = new File(consoleParameters.getValue("-xml"));
                qsubExperimentXML(xml, numExec);
            } catch (UndefinedParameterException ex) {
                ERROR_CODES.COMMAND_LINE_PARAMETER_IS_NOT_DEFINED.exit(ex);
                throw new IllegalStateException(ex);
            }
        }

    }

    private void qsubExperimentXML(File xml, int numExec) {

        File delfosQsubGroupJobScript = new File("delfos-qsub-group-job.sh");
        if (!delfosQsubGroupJobScript.exists()) {
            throw new IllegalStateException(
                    "The 'delfos-qsub-group-job.sh' script is not present in "
                    + "working directory '" + delfosQsubGroupJobScript.getParent() + "'");
        }

        File experimentDirectory = xml.getParentFile();

        final int mod = Math.abs(experimentDirectory.hashCode()) % 2;

        int numQueue = mod + 1;

        final String command = "qsub "
                + "-q queue" + numQueue + " "
                + "-v "
                + "experimentFolder=" + experimentDirectory.getAbsolutePath() + File.separator + ","
                + "numExec=" + numExec + " "
                + "./delfos-qsub-group-job.sh";

        executeCommand(command);

        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            Logger.getLogger(DelfosQueueSubmitGroup.class.getName()).log(Level.SEVERE, null, ex);
        }

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

        Global.showMessage("Find returned  " + findInDirectory.size() + " results\n");

        List<File> directoriesChild = findInDirectory.parallelStream()
                .filter(directoryChild -> directoryChild.isDirectory())
                .collect(Collectors.toList());

        Global.showMessage("Relevant directories found " + directoriesChild.size() + "\n");

        List<File> datasetDirectories = directoriesChild.parallelStream().
                filter(directoryChild -> directoryChild.getName().equals("dataset"))
                .collect(Collectors.toList());

        List<File> experimentDirectories = datasetDirectories.parallelStream()
                .map(directoryDataset -> directoryDataset.getParentFile())
                .collect(Collectors.toList());

        Global.showMessage("Submitting " + experimentDirectories.size() + " experiments\n");

        experimentDirectories.stream().forEachOrdered(experimentDirectory -> {
            final int mod = Math.abs(experimentDirectory.hashCode()) % 2;

            int numQueue = mod + 1;

            final String command = "qsub "
                    + "-q queue" + numQueue + " "
                    + "-v "
                    + "experimentFolder=\"" + experimentDirectory.getAbsolutePath() + File.separator + "\","
                    + "numExec=" + numExec + " "
                    + "./delfos-qsub-group-job.sh";

            executeCommand(command);

            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(DelfosQueueSubmitGroup.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

    }

    private void executeCommand(String command) {

        try {
            Runtime rt = Runtime.getRuntime();
            Global.showMessage("\n\n" + command + "\n\n");

            Process proc = rt.exec(command);
            proc.waitFor();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String s = null;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

            if (proc.exitValue() != 0) {
                IllegalStateException ise = new IllegalStateException(
                        "Executed command returned error code " + proc.exitValue());
                ERROR_CODES.UNDEFINED_ERROR.exit(ise);
            }
        } catch (IOException ex) {
            Logger.getLogger(DelfosQueueSubmitGroup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DelfosQueueSubmitGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
