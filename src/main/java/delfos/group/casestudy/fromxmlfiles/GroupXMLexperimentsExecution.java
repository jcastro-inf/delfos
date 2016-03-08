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
package delfos.group.casestudy.fromxmlfiles;

import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.filefilters.FileFilterByExtension;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.casestudy.GroupCaseStudyConfiguration;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.write.WriteException;
import org.jdom2.JDOMException;

/**
 * Ejecuta los experimentos que hay definidos en el directorio indicado, leyendo
 * los XML que existen para generar los casos de estudio.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 14-May-2013
 */
public class GroupXMLexperimentsExecution {

    private final String experimentsDirectory;
    private final int numExecutions;
    private final String datasetsDirectory;
    private final long seed;

    public GroupXMLexperimentsExecution(String experimentsDirectory, String datasetDirectory, int numExecutions, long seed) {
        this.experimentsDirectory = experimentsDirectory;

        if (!new File(experimentsDirectory).exists()) {
            FileNotFoundException ex = new FileNotFoundException(
                    "The directory '" + experimentsDirectory + "' not exists "
                    + "[" + new File(experimentsDirectory).getAbsolutePath() + "]");

            ERROR_CODES.EXPERIMENT_DIRECTORY_ERROR.exit(ex);
        }
        if (!new File(experimentsDirectory).isDirectory()) {
            IllegalArgumentException ex = new IllegalArgumentException(
                    "The path '" + experimentsDirectory + "' is not a directory "
                    + "[" + new File(experimentsDirectory).getAbsolutePath() + "]");
            ERROR_CODES.EXPERIMENT_DIRECTORY_ERROR.exit(ex);
        }

        if (!new File(datasetDirectory).exists()) {
            IllegalArgumentException ex = new IllegalArgumentException("The file '" + datasetDirectory + "' not exists [" + new File(datasetDirectory).getAbsolutePath() + "]");
            ERROR_CODES.EXPERIMENT_DIRECTORY_ERROR.exit(ex);
        }

        if (!new File(datasetDirectory).isDirectory()) {
            IllegalArgumentException ex = new IllegalArgumentException("The value '" + datasetDirectory + "' not a directory [" + new File(datasetDirectory).getAbsolutePath() + "]");
            ERROR_CODES.EXPERIMENT_DIRECTORY_ERROR.exit(ex);
        }
        this.numExecutions = numExecutions;
        this.datasetsDirectory = datasetDirectory;
        this.seed = seed;
    }

    public void execute() throws CannotLoadContentDataset, CannotLoadRatingsDataset {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

        dateFormat.setTimeZone(TimeZone.getDefault());
        String date = dateFormat.format(new Date());

        File stdLogFile = new File(experimentsDirectory + File.separator + "logs" + File.separator + date + "-log-std.txt");
        File errLogFile = new File(experimentsDirectory + File.separator + "logs" + File.separator + date + "-err-std.txt");

        FileUtilities.createDirectoriesForFile(errLogFile);
        FileUtilities.createDirectoriesForFile(stdLogFile);

        FileWriter stdLogWriter;
        FileWriter errLogWriter;
        try {
            stdLogWriter = new FileWriter(stdLogFile);
            errLogWriter = new FileWriter(errLogFile);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        Global.addStandardOutputLogger(stdLogWriter);
        Global.addErrorOutputLogger(errLogWriter);

        Collection<GroupEvaluationMeasure> groupEvaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();

        File experimentsDirectoryDirectory = new File(experimentsDirectory);
        File datasetsDirectoryDirectory = new File(datasetsDirectory);
        File resultsDirectory = new File(experimentsDirectory + File.separator + "results" + File.separator);

        FileUtilities.createDirectoryPath(resultsDirectory);

        File[] datasetFiles = datasetsDirectoryDirectory.listFiles(new FileFilterByExtension(false, "xml"));
        if (datasetFiles.length == 0) {
            try {
                Global.showln("-------------------- Sleeping 2seconds --------------------");
                Global.showWarning("-------------------- Sleeping 2seconds --------------------");
                Thread.sleep(2000);
            } catch (InterruptedException ex) {

            }
            datasetFiles = datasetsDirectoryDirectory.listFiles(new FileFilterByExtension(false, "xml"));
            if (datasetFiles.length == 0) {
                throw new IllegalStateException("No dataset files in '" + datasetsDirectoryDirectory.getAbsolutePath() + "'");
            }
        }

        List<ExecuteGroupCaseStudy_Task> listOfTasks = new ArrayList<>();

        for (File datasetFile : datasetFiles) {
            try {
                final DatasetLoader<? extends Rating> datasetLoader = GroupCaseStudyXML.loadGroupCaseDescription(datasetFile).getDatasetLoader();

                File[] experimentFiles = experimentsDirectoryDirectory.listFiles(new FileFilterByExtension(false, "xml"));

                if (experimentFiles.length == 0) {
                    throw new IllegalStateException("No experiments files in '" + experimentsDirectoryDirectory.getAbsolutePath() + "'");
                }
                Arrays.sort(experimentFiles, (File o1, File o2) -> o1.getName().compareTo(o2.getName()));

                for (File experimentFile : experimentsDirectoryDirectory.listFiles(new FileFilterByExtension(false, "xml"))) {

                    GroupCaseStudyConfiguration caseStudyConfiguration = GroupCaseStudyXML
                            .loadGroupCaseDescription(experimentFile);

                    listOfTasks.add(new ExecuteGroupCaseStudy_Task(
                            experimentsDirectoryDirectory,
                            experimentFile.getName(),
                            caseStudyConfiguration,
                            datasetLoader,
                            groupEvaluationMeasures,
                            numExecutions,
                            seed));
                }
            } catch (JDOMException | IOException ex) {
                Logger.getLogger(GroupXMLexperimentsExecution.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        listOfTasks.stream().forEach(new GroupCaseStudyExecutor());

        File aggregateFile = FileUtilities.addSufix(resultsDirectory, File.separator + "aggregateResults.xls");
        try {
            GroupCaseStudyExcel.aggregateExcels(
                    resultsDirectory.listFiles(new FileFilterByExtension(false, "xls")),
                    aggregateFile);
        } catch (WriteException ex) {
            Logger.getLogger(GroupXMLexperimentsExecution.class.getName()).log(Level.SEVERE, null, ex);
        }

        Global.showln("Finished.");
        Global.removeErrorOutputLogger(errLogWriter);
        Global.removeStandardOutputLogger(stdLogWriter);

        try {
            errLogWriter.close();
            stdLogWriter.close();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
