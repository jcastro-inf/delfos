package delfos.group;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import delfos.common.Global;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.ExperimentListerner_default;
import delfos.experiment.casestudy.ExecutionProgressListener_onlyChanges;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.filtered.GroupRecommenderSystemWithPostFilter;
import delfos.group.grs.filtered.filters.OutliersRatingsFilter;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.experiment.validation.validationtechniques.CrossFoldValidation_Items;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.rs.GenericRecommenderSystem;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;

/**
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 14-May-2013
 */
public class FilterCaseStudy implements Runnable {

    private static DatasetLoader[] datasets;
    /**
     * ============== CONSTANTS ====================
     */
    private final int NUM_EJECUCIONES;
    private final int NUM_GROUPS;
    private final int SIZE_OF_GROUPS;
    private final long SEED;
    private String folder = null;

    public FilterCaseStudy(
            int NUM_EJECUCIONES,
            int NUM_GROUPS,
            int SIZE_OF_GROUPS,
            long SEED) {
        this.NUM_EJECUCIONES = NUM_EJECUCIONES;
        this.NUM_GROUPS = NUM_GROUPS;
        this.SIZE_OF_GROUPS = SIZE_OF_GROUPS;
        this.SEED = SEED;

        Global.showMessage("Ejecuciones    " + NUM_EJECUCIONES + "\n");
        Global.showMessage("Grupos         " + NUM_GROUPS + "\n");
        Global.showMessage("Tamaño grupos  " + SIZE_OF_GROUPS + "\n");
        Global.showMessage("Semilla        " + SEED + "\n");
        init();
    }

    private void init() {
        File datasetsDirectory = new File("experiments" + File.separator + "dataset" + File.separator + "dumbFile.txt").getParentFile();
        datasets = getDatasets(datasetsDirectory).toArray(new DatasetLoader[0]);
    }

    public void execute() throws CannotLoadRatingsDataset, CannotLoadContentDataset, UserNotFound, ItemNotFound {

        Global.showMessage("SEED OF THIS EXPERIMENT = " + SEED + "\n");

        ArrayList<GroupRecommenderSystem> grsList;
        if (folder != null) {
            grsList = getRecommenders(new File(folder));
        } else {
            grsList = getRecommenders();
        }

        Collection<GroupEvaluationMeasure> evaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();

        int i = 1;
        for (GroupRecommenderSystem groupRecommenderSystem : grsList) {
            for (DatasetLoader<? extends Rating> datasetLoader : datasets) {
                GroupCaseStudy caseStudy = new DefaultGroupCaseStudy(
                        datasetLoader,
                        groupRecommenderSystem,
                        new FixedGroupSize_OnlyNGroups(NUM_GROUPS, SIZE_OF_GROUPS), new CrossFoldValidation_Items(), new NoPredictionProtocol(),
                        evaluationMeasures,
                        datasetLoader.getDefaultRelevanceCriteria(), NUM_EJECUCIONES);

                caseStudy.addExecutionProgressListener(new ExecutionProgressListener_onlyChanges(System.out, 10000));
                caseStudy.addExperimentListener(new ExperimentListerner_default(System.out, 10000));

                caseStudy.setSeedValue(SEED);
                String defaultFileName = GroupCaseStudyXML.getDefaultFileName(caseStudy);
                GroupCaseStudyXML.saveCaseDescription(caseStudy, defaultFileName + ".tmp");
                caseStudy.execute();
                GroupCaseStudyXML.saveCaseResults(caseStudy, caseStudy.getGroupRecommenderSystem().getAlias(), defaultFileName);

                Global.showMessage("================ FIN Sistema " + i + " de " + grsList.size() + "=================== \n");
                i++;
            }
        }
    }

    public static ArrayList<GroupRecommenderSystem> getRecommenders() {
        ArrayList<GroupRecommenderSystem> grsList = new ArrayList<>();

        grsList.add(new GroupRecommenderSystemWithPostFilter(new KnnMemoryBasedCFRS(), new OutliersRatingsFilter(2, 0.8, true), new MinimumValue()));

        return grsList;
    }

    public static ArrayList<GroupRecommenderSystem> getRecommenders(File folder) {
        ArrayList<GroupRecommenderSystem> grsList = new ArrayList<>();
        if (!folder.exists()) {
            throw new IllegalArgumentException("The file " + folder.getAbsolutePath() + " does not exists.");
        }
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("The file " + folder.getAbsolutePath() + " is not a directory.");
        }
        File[] listOfFiles = folder.listFiles((File pathname) -> pathname.getName().endsWith(".xml"));

        {
            Collections.sort(Arrays.asList(listOfFiles));
        }

        for (File configurationFile : listOfFiles) {
            try {
                RecommenderSystemConfiguration loadConfigFile = RecommenderSystemConfigurationFileParser.loadConfigFile(configurationFile.getAbsolutePath());
                GenericRecommenderSystem rs = loadConfigFile.recommenderSystem;

                rs.setAlias(configurationFile.getName());

                if (rs instanceof GroupRecommenderSystem) {
                    GroupRecommenderSystem groupRecommenderSystem = (GroupRecommenderSystem) rs;
                    grsList.add(groupRecommenderSystem);
                } else {
                    throw new IllegalArgumentException("The recommender in " + configurationFile.getAbsolutePath() + " is not a group recommender system.");
                }
            } catch (CannotLoadContentDataset | CannotLoadRatingsDataset ex) {
                Logger.getLogger(FilterCaseStudy.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return grsList;
    }

    public static ArrayList<DatasetLoader> getDatasets(File folder) {
        ArrayList<DatasetLoader> ret = new ArrayList<>();
        if (!folder.exists()) {
            throw new IllegalArgumentException("The file " + folder.getAbsolutePath() + " does not exists.");
        }
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("The file " + folder.getAbsolutePath() + " is not a directory.");
        }
        File[] listOfFiles = folder.listFiles((File pathname) -> pathname.getName().endsWith(".xml"));

        for (File configurationFile : listOfFiles) {
            try {
                RecommenderSystemConfiguration loadConfigFile = RecommenderSystemConfigurationFileParser.loadConfigFile(configurationFile.getAbsolutePath());
                DatasetLoader<? extends Rating> datasetLoader = loadConfigFile.datasetLoader;
                ret.add(datasetLoader);
            } catch (CannotLoadContentDataset | CannotLoadRatingsDataset ex) {
                Logger.getLogger(FilterCaseStudy.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return ret;
    }

    void setFolder(String folder) {
        this.folder = folder;
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (CannotLoadRatingsDataset | CannotLoadContentDataset | UserNotFound | ItemNotFound ex) {
            Logger.getLogger(FilterCaseStudy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
