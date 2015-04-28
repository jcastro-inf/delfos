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

    private final String experimentsDirectory;
    private final int numExecutions;
    private final String datasetsDirectory;
    private long seed;

    public GroupXMLexperimentsExecution(String experimentsDirectory, String datasetDirectory, int numExecutions, long seed) {
        this.experimentsDirectory = experimentsDirectory;

        if (!new File(experimentsDirectory).exists()) {
            throw new IllegalArgumentException("The file '" + experimentsDirectory + "' not exists [" + new File(experimentsDirectory).getAbsolutePath() + "]");
        }
        if (!new File(experimentsDirectory).isDirectory()) {
            throw new IllegalArgumentException("The value '" + experimentsDirectory + "' not a directory [" + new File(experimentsDirectory).getAbsolutePath() + "]");
        }

        if (!new File(datasetDirectory).exists()) {
            throw new IllegalArgumentException("The file '" + datasetDirectory + "' not exists [" + new File(datasetDirectory).getAbsolutePath() + "]");
        }

        if (!new File(datasetDirectory).isDirectory()) {
            throw new IllegalArgumentException("The value '" + datasetDirectory + "' not a directory [" + new File(datasetDirectory).getAbsolutePath() + "]");
        }
        this.numExecutions = numExecutions;
        this.datasetsDirectory = datasetDirectory;
        this.seed = seed;
    }

    public void execute() throws CannotLoadContentDataset, CannotLoadRatingsDataset {
        Collection<GroupEvaluationMeasure> groupEvaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();

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

                    GroupCaseStudyConfiguration caseStudyConfiguration = GroupCaseStudyXML.loadGroupCaseDescription(experimentFile);
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

        MultiThreadExecutionManager<ExecuteGroupCaseStudy_Task> multiThreadExecutionManager
                = new MultiThreadExecutionManager<>(
                        "Execute group case",
                        listOfTasks,
                        GroupCaseStudy_SingleTaskExecute.class);

        multiThreadExecutionManager.run();

        File aggregateFile = FileUtilities.addSufix(resultsDirectory, File.separator + "aggregateResults.xls");
        try {
            GroupCaseStudyExcel.aggregateExcels(
                    resultsDirectory.listFiles(new FileFilterByExtension(false, "xls")),
                    aggregateFile);
        } catch (WriteException ex) {
            Logger.getLogger(GroupXMLexperimentsExecution.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Finished.");
    }
}
