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
package delfos.casestudy.fromxmlfiles;

import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.filefilters.FileFilterByExtension;
import delfos.common.parallelwork.notblocking.MultiThreadExecutionManager_NotBlocking;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.experiment.casestudy.CaseStudyConfiguration;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel;
import delfos.io.excel.casestudy.CaseStudyExcel;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.write.WriteException;
import org.jdom2.JDOMException;

/**
 * Ejecuta los experimentos que hay definidos en el directorio indicado, leyendo los XML que existen para generar los
 * casos de estudio.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 14-May-2013
 */
public class XMLexperimentsExecution {

    private final long seed;
    private final String experimentsDirectory;
    private final int numExecutions;
    private final String datasetsDirectory;

    public static final String AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME = CaseStudyExcel.AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME;

    public XMLexperimentsExecution(String experimentsDirectory, String datasetDirectory, int numExecutions, long seed) {
        this.experimentsDirectory = experimentsDirectory;

        if (!new File(experimentsDirectory).exists()) {
            throw new IllegalArgumentException("The file '" + experimentsDirectory + "' do not exists [" + new File(experimentsDirectory).getAbsolutePath() + "]");
        }
        if (!new File(experimentsDirectory).isDirectory()) {
            throw new IllegalArgumentException("The value '" + experimentsDirectory + "' not a directory [" + new File(experimentsDirectory).getAbsolutePath() + "]");
        }

        if (!new File(datasetDirectory).exists()) {
            throw new IllegalArgumentException("The file '" + datasetDirectory + "' do not exists [" + new File(datasetDirectory).getAbsolutePath() + "]");
        }

        if (!new File(datasetDirectory).isDirectory()) {
            throw new IllegalArgumentException("The value '" + datasetDirectory + "' not a directory [" + new File(datasetDirectory).getAbsolutePath() + "]");
        }
        this.numExecutions = numExecutions;
        this.datasetsDirectory = datasetDirectory;
        this.seed = seed;
    }

    public void execute() throws CannotLoadContentDataset, CannotLoadRatingsDataset {

        Global.showMessageTimestamped("Execute XMLExperiment allocated in directory '" + experimentsDirectory + "'");
        Collection<EvaluationMeasure> evaluationMeasures = EvaluationMeasuresFactory.getInstance().getAllClasses();

        File experimentsDirectoryDirectory = new File(experimentsDirectory);
        File datasetsDirectoryDirectory = new File(datasetsDirectory);
        File resultsDirectory = new File(experimentsDirectory + File.separator + "results" + File.separator);
        if (resultsDirectory.exists()) {
            FileUtilities.deleteDirectoryRecursive(resultsDirectory);
        }
        resultsDirectory.mkdirs();

        File[] datasetFiles = datasetsDirectoryDirectory.listFiles(new FileFilterByExtension(false, "xml"));
        if (datasetFiles.length == 0) {
            throw new IllegalStateException("No dataset files in '" + datasetsDirectoryDirectory.getAbsolutePath() + "'");
        }

        MultiThreadExecutionManager_NotBlocking<ExecuteCaseStudy_Task> multiThreadExecutionManager
                = new MultiThreadExecutionManager_NotBlocking<>(
                        "Execute case",
                        CaseStudy_SingleTaskExecute.class);

        multiThreadExecutionManager.runInBackground();

        for (File datasetFile : datasetFiles) {
            try {
                final DatasetLoader<? extends Rating> datasetLoader = CaseStudyXML.loadCase(datasetFile).getDatasetLoader();

                File[] experimentFiles = experimentsDirectoryDirectory.listFiles(new FileFilterByExtension(false, "xml"));

                if (experimentFiles.length == 0) {
                    throw new IllegalStateException("No experiments files in '" + experimentsDirectoryDirectory.getAbsolutePath() + "'");
                }
                Arrays.sort(experimentFiles, (File o1, File o2) -> o1.getName().compareTo(o2.getName()));
                for (File experimentFile : experimentsDirectoryDirectory.listFiles(new FileFilterByExtension(false, "xml"))) {

                    CaseStudyConfiguration caseStudyConfiguration = CaseStudyXML.loadCase(experimentFile);
                    multiThreadExecutionManager.addTask(new ExecuteCaseStudy_Task(
                            experimentsDirectoryDirectory,
                            experimentFile.getName(),
                            caseStudyConfiguration,
                            datasetLoader,
                            caseStudyConfiguration.getRelevanceCriteria(),
                            evaluationMeasures,
                            numExecutions,
                            seed));
                }
            } catch (JDOMException | IOException ex) {
                ERROR_CODES.CANNOT_READ_CASE_STUDY_XML.exit(ex);
            }
        }

        try {
            multiThreadExecutionManager.waitUntilFinished();
        } catch (InterruptedException ex) {
            Logger.getLogger(XMLexperimentsExecution.class.getName()).log(Level.SEVERE, null, ex);
        }

        File aggregateFile = FileUtilities.addSufix(resultsDirectory, File.separator + AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME);
        try {
            GroupCaseStudyExcel.aggregateExcels(
                    resultsDirectory.listFiles(new FileFilterByExtension(false, "xls")),
                    aggregateFile);
        } catch (WriteException ex) {
            ERROR_CODES.CANNOT_WRITE_CASE_STUDY_XML.exit(ex);
        }
    }
}
