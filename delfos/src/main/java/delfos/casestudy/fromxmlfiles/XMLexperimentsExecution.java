package delfos.casestudy.fromxmlfiles;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.write.WriteException;
import org.jdom2.JDOMException;
import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.filefilters.FileFilterByExtension;
import delfos.common.parallelwork.notblocking.MultiThreadExecutionManager_NotBlocking;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.casestudy.CaseStudyConfiguration;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel;
import delfos.io.excel.casestudy.CaseStudyExcel;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;

/**
 * Ejecuta los experimentos que hay definidos en el directorio indicado, leyendo
 * los XML que existen para generar los casos de estudio.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 14-May-2013
 */
public class XMLexperimentsExecution {

    private final long seed;
    private final String experimentsFolder;
    private final int numExecutions;
    private final String datasetsFolder;

    public static final String AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME = CaseStudyExcel.AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME;

    public XMLexperimentsExecution(String experimentsFolder, String datasetFolder, int numExecutions, long seed) {
        this.experimentsFolder = experimentsFolder;

        if (!new File(experimentsFolder).exists()) {
            throw new IllegalArgumentException("The file '" + experimentsFolder + "' do not exists [" + new File(experimentsFolder).getAbsolutePath() + "]");
        }
        if (!new File(experimentsFolder).isDirectory()) {
            throw new IllegalArgumentException("The value '" + experimentsFolder + "' not a folder [" + new File(experimentsFolder).getAbsolutePath() + "]");
        }

        if (!new File(datasetFolder).exists()) {
            throw new IllegalArgumentException("The file '" + datasetFolder + "' do not exists [" + new File(datasetFolder).getAbsolutePath() + "]");
        }

        if (!new File(datasetFolder).isDirectory()) {
            throw new IllegalArgumentException("The value '" + datasetFolder + "' not a folder [" + new File(datasetFolder).getAbsolutePath() + "]");
        }
        this.numExecutions = numExecutions;
        this.datasetsFolder = datasetFolder;
        this.seed = seed;
    }

    public void execute() throws CannotLoadContentDataset, CannotLoadRatingsDataset {

        Global.showMessageTimestamped("Execute XMLExperiment allocated in folder '" + experimentsFolder + "'");
        Collection<EvaluationMeasure> evaluationMeasures = EvaluationMeasuresFactory.getInstance().getAllClasses();

        File experimentsFolderDirectory = new File(experimentsFolder);
        File datasetsFolderDirectory = new File(datasetsFolder);
        File resultsFolder = new File(experimentsFolder + File.separator + "results" + File.separator);
        if (resultsFolder.exists()) {
            FileUtilities.deleteDirectoryRecursive(resultsFolder);
        }
        resultsFolder.mkdirs();

        File[] datasetFiles = datasetsFolderDirectory.listFiles(new FileFilterByExtension(false, "xml"));
        if (datasetFiles.length == 0) {
            throw new IllegalStateException("No dataset files in '" + datasetsFolderDirectory.getAbsolutePath() + "'");
        }

        MultiThreadExecutionManager_NotBlocking<ExecuteCaseStudy_Task> multiThreadExecutionManager
                = new MultiThreadExecutionManager_NotBlocking<>(
                        "Execute case",
                        CaseStudy_SingleTaskExecute.class);

        multiThreadExecutionManager.runInBackground();

        for (File datasetFile : datasetFiles) {
            try {
                final DatasetLoader<? extends Rating> datasetLoader = CaseStudyXML.loadCase(datasetFile).getDatasetLoader();

                File[] experimentFiles = experimentsFolderDirectory.listFiles(new FileFilterByExtension(false, "xml"));

                if (experimentFiles.length == 0) {
                    throw new IllegalStateException("No experiments files in '" + experimentsFolderDirectory.getAbsolutePath() + "'");
                }
                Arrays.sort(experimentFiles, (File o1, File o2) -> o1.getName().compareTo(o2.getName()));
                for (File experimentFile : experimentsFolderDirectory.listFiles(new FileFilterByExtension(false, "xml"))) {

                    CaseStudyConfiguration caseStudyConfiguration = CaseStudyXML.loadCase(experimentFile);
                    multiThreadExecutionManager.addTask(new ExecuteCaseStudy_Task(
                            experimentsFolderDirectory,
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

        File aggregateFile = FileUtilities.addSufix(resultsFolder, File.separator + AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME);
        try {
            GroupCaseStudyExcel.aggregateExcels(
                    resultsFolder.listFiles(new FileFilterByExtension(false, "xls")),
                    aggregateFile);
        } catch (WriteException ex) {
            ERROR_CODES.CANNOT_WRITE_CASE_STUDY_XML.exit(ex);
        }

        System.out.println("Finished.");
    }
}
