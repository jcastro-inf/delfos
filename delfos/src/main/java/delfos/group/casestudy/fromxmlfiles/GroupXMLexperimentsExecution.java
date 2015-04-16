package delfos.group.casestudy.fromxmlfiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.write.WriteException;
import org.jdom2.JDOMException;
import delfos.common.FileUtilities;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.filefilters.FileFilterByExtension;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.casestudy.GroupCaseStudyConfiguration;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;

/**
 * Ejecuta los experimentos que hay definidos en el directorio indicado, leyendo
 * los XML que existen para generar los casos de estudio.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 14-May-2013
 */
public class GroupXMLexperimentsExecution {

    private final String experimentsFolder;
    private final int numExecutions;
    private final String datasetsFolder;
    private long seed;

    public GroupXMLexperimentsExecution(String experimentsFolder, String datasetFolder, int numExecutions, long seed) {
        this.experimentsFolder = experimentsFolder;

        if (!new File(experimentsFolder).exists()) {
            throw new IllegalArgumentException("The file '" + experimentsFolder + "' not exists [" + new File(experimentsFolder).getAbsolutePath() + "]");
        }
        if (!new File(experimentsFolder).isDirectory()) {
            throw new IllegalArgumentException("The value '" + experimentsFolder + "' not a folder [" + new File(experimentsFolder).getAbsolutePath() + "]");
        }

        if (!new File(datasetFolder).exists()) {
            throw new IllegalArgumentException("The file '" + datasetFolder + "' not exists [" + new File(datasetFolder).getAbsolutePath() + "]");
        }

        if (!new File(datasetFolder).isDirectory()) {
            throw new IllegalArgumentException("The value '" + datasetFolder + "' not a folder [" + new File(datasetFolder).getAbsolutePath() + "]");
        }
        this.numExecutions = numExecutions;
        this.datasetsFolder = datasetFolder;
        this.seed = seed;
    }

    public void execute() throws CannotLoadContentDataset, CannotLoadRatingsDataset {
        Collection<GroupEvaluationMeasure> groupEvaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();

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

        List<ExecuteGroupCaseStudy_Task> listOfTasks = new ArrayList<>();

        for (File datasetFile : datasetFiles) {
            try {
                final DatasetLoader<? extends Rating> datasetLoader = GroupCaseStudyXML.loadGroupCaseDescription(datasetFile).getDatasetLoader();

                File[] experimentFiles = experimentsFolderDirectory.listFiles(new FileFilterByExtension(false, "xml"));

                if (experimentFiles.length == 0) {
                    throw new IllegalStateException("No experiments files in '" + experimentsFolderDirectory.getAbsolutePath() + "'");
                }
                Arrays.sort(experimentFiles, (File o1, File o2) -> o1.getName().compareTo(o2.getName()));

                for (File experimentFile : experimentsFolderDirectory.listFiles(new FileFilterByExtension(false, "xml"))) {

                    GroupCaseStudyConfiguration caseStudyConfiguration = GroupCaseStudyXML.loadGroupCaseDescription(experimentFile);
                    listOfTasks.add(new ExecuteGroupCaseStudy_Task(
                            experimentsFolderDirectory,
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

        MultiThreadExecutionManager<ExecuteGroupCaseStudy_Task> multiThreadExecutionManager
                = new MultiThreadExecutionManager<>(
                        "Execute group case",
                        listOfTasks,
                        GroupCaseStudy_SingleTaskExecute.class);

        multiThreadExecutionManager.run();

        File aggregateFile = FileUtilities.addSufix(resultsFolder, File.separator + "aggregateResults.xls");
        try {
            GroupCaseStudyExcel.aggregateExcels(
                    resultsFolder.listFiles(new FileFilterByExtension(false, "xls")),
                    aggregateFile);
        } catch (WriteException ex) {
            Logger.getLogger(GroupXMLexperimentsExecution.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Finished.");
    }
}
