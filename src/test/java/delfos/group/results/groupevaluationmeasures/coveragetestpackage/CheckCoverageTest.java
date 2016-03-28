package delfos.group.results.groupevaluationmeasures.coveragetestpackage;

import delfos.Constants;
import delfos.common.DateCollapse;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import delfos.experiment.ExperimentListerner_default;
import delfos.experiment.casestudy.ExecutionProgressListener_default;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.GivenGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.filtered.GroupRecommenderSystemWithPreFilter;
import delfos.group.grs.filtered.filters.NoFilter;
import delfos.group.grs.filtered.filters.OutliersRatingsFilter;
import delfos.group.grs.persistence.GroupRecommenderSystem_fixedFilePersistence;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.rs.RecommenderSystemBuildingProgressListener_default;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.FilePersistence;
import java.io.File;
import java.util.Collection;
import org.junit.Test;

/**
 * Test para comprobar la cobertura. Ahora mismo se utiliza como un main, pero
 * se hace así para no 'ensuciar' el código de la biblioteca.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 07-May-2013
 */
public class CheckCoverageTest {

    private static CSVfileDatasetLoader datasetLoader;
    private static GroupFormationTechnique groupFormationTechnique;
    /**
     * ============== CONSTANTS ====================
     */
    private static final FilePersistence filePersistence = new FilePersistence("modeloParaEvaluarCoverage_forGroups", "dat");
    private static final int NUM_EJECUCIONES = 1;
    private static final long SEED = 6288;

    private static final File experimentResultsDirectory = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator);

    public CheckCoverageTest() {
    }

    @Test
    public void dummyTest() {
    }

    //@BeforeClass
    public static void setUpClass() throws FailureInPersistence, CannotLoadContentDataset, CannotLoadRatingsDataset {

        datasetLoader = new CSVfileDatasetLoader("datasets" + File.separator + "SSII - ratings9.csv", "datasets" + File.separator + "SSII - peliculas.csv");

        GroupOfUsers[] groups = new GroupOfUsers[1];
        groups[0] = new GroupOfUsers(1774684, 1887988, 2394147);
        groupFormationTechnique = new GivenGroups(groups);
        GroupRecommenderSystem_fixedFilePersistence grs = new GroupRecommenderSystem_fixedFilePersistence(
                new AggregationOfIndividualRatings(
                        new KnnModelBasedCFRS()), filePersistence);

        groupFormationTechnique.generateGroups(datasetLoader);

        try {
            Object grsModel = grs.loadRecommendationModel(filePersistence, datasetLoader.getRatingsDataset().allUsers(), datasetLoader.getContentDataset().allIDs());
        } catch (Exception ex) {
            Global.showError(ex);
            Global.showWarning("\n\nHay que generar el modelo \n");
            grs.addRecommendationModelBuildingProgressListener(new RecommenderSystemBuildingProgressListener_default(System.out, 5000));
            Object build = grs.buildRecommendationModel(datasetLoader);
            grs.saveRecommendationModel(filePersistence, build);
        }
    }

    //@Test
    public void testWithFilter() throws CannotLoadContentDataset, CannotLoadRatingsDataset, UserNotFound, UserNotFound, ItemNotFound {

        GroupRecommenderSystem_fixedFilePersistence rs = new GroupRecommenderSystem_fixedFilePersistence(new GroupRecommenderSystemWithPreFilter(
                new AggregationOfIndividualRatings(new KnnModelBasedCFRS()),
                new OutliersRatingsFilter(0.5, 0.2, true)), filePersistence);

        Collection<GroupEvaluationMeasure> evaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();

        GroupCaseStudy caseStudy = new GroupCaseStudy(
                datasetLoader,
                rs,
                groupFormationTechnique, new HoldOut_Ratings(), new NoPredictionProtocol(),
                evaluationMeasures,
                datasetLoader.getDefaultRelevanceCriteria(), NUM_EJECUCIONES);
        caseStudy.addExperimentListener(new ExperimentListerner_default(System.out, 10000));
        caseStudy.addExecutionProgressListener(new ExecutionProgressListener_default(System.out, 10000));

        String caseStudyAlias = "CheckCoverageTest_" + GroupCaseStudyXML.getCaseStudyFileNameTimestamped(caseStudy);

        caseStudy.setAlias(caseStudyAlias);
        caseStudy.setSeedValue(SEED);
        caseStudy.execute();

        GroupCaseStudyXML.saveCaseResults(caseStudy, experimentResultsDirectory);
        Global.showInfoMessage("================ FIN CON FILTRO=================== \n");
    }

    public void testWithoutFilter() throws CannotLoadContentDataset, CannotLoadRatingsDataset, UserNotFound, UserNotFound, ItemNotFound {
        Global.showInfoMessage("================ INIT SIN FILTRO =================== \n");

        GroupRecommenderSystem_fixedFilePersistence rs = new GroupRecommenderSystem_fixedFilePersistence(new GroupRecommenderSystemWithPreFilter(
                new AggregationOfIndividualRatings(new KnnModelBasedCFRS()),
                new NoFilter()), filePersistence);

        Collection<GroupEvaluationMeasure> evaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();

        GroupCaseStudy caseStudy = new GroupCaseStudy(
                datasetLoader,
                rs,
                groupFormationTechnique, new HoldOut_Ratings(), new NoPredictionProtocol(),
                evaluationMeasures,
                datasetLoader.getDefaultRelevanceCriteria(), NUM_EJECUCIONES);
        caseStudy.addExperimentListener(new ExperimentListerner_default(System.out, 10000));
        caseStudy.addExecutionProgressListener(new ExecutionProgressListener_default(System.out, 10000));

        caseStudy.addExecutionProgressListener((String proceso, int percent, long remainingMiliSeconds) -> {
            Global.showln(proceso + " --> " + percent + "% (" + DateCollapse.collapse(remainingMiliSeconds) + ")");
        });

        String defaultFileName = GroupCaseStudyXML.getCaseStudyFileNameTimestamped(caseStudy);

        caseStudy.setAlias("CheckCoverageTest_noFilter_" + defaultFileName);

        caseStudy.setSeedValue(SEED);
        caseStudy.execute();

        GroupCaseStudyXML.saveCaseResults(caseStudy, experimentResultsDirectory);

        Global.showInfoMessage("================ FIN SIN FILTRO=================== \n");
    }
}
