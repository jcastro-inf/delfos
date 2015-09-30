package delfos.group.casestudy.definedcases.hesitant;

import delfos.Constants;
import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.aggregationoperators.RMSMean;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.hesitant.HesitantKnnGroupUser;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

public class HesitantGRS_CaseStudy extends DelfosTest {

    public HesitantGRS_CaseStudy() {
    }

    public static final long SEED_VALUE = 77352653L;
    public static final int SIZE_OF_GROUPS = 5;
    public static final int NUM_GROUPS = 30;

    File experimentDirectory = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "experiments" + File.separator
            + "HesitantGRS" + File.separator);

    private GroupFormationTechnique getGroupFormationTechnique() {
        GroupFormationTechnique gft = new FixedGroupSize_OnlyNGroups(NUM_GROUPS, SIZE_OF_GROUPS);
        gft.setSeedValue(SEED_VALUE);
        return gft;
    }

    private DatasetLoader<? extends Rating> getDatasetLoader() {
        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        return datasetLoader;
    }

    private List<GroupRecommenderSystem> getGRSs() {
        LinkedList<GroupRecommenderSystem> GRSs = new LinkedList<>();

        KnnMemoryBasedNWR coreRS = new KnnMemoryBasedNWR();
        coreRS.setParameterValue(KnnMemoryBasedNWR.NEIGHBORHOOD_SIZE, 20);
        coreRS.setParameterValue(KnnMemoryBasedNWR.SIMILARITY_MEASURE, new PearsonCorrelationCoefficient());
        coreRS.setParameterValue(KnnMemoryBasedNWR.PREDICTION_TECHNIQUE, new WeightedSum());
        coreRS.setParameterValue(KnnMemoryBasedNWR.CASE_AMPLIFICATION, 1);
        coreRS.setParameterValue(KnnMemoryBasedNWR.DEFAULT_RATING, false);
        coreRS.setParameterValue(KnnMemoryBasedNWR.RELEVANCE_FACTOR, false);

        Arrays.asList(new Mean(), new RMSMean(), new MinimumValue())
                .stream()
                .forEach((aggregationOpperator) -> {
                    AggregationOfIndividualRatings grs = new AggregationOfIndividualRatings(coreRS, aggregationOpperator);
                    grs.setAlias("AggRat+" + aggregationOpperator.getAlias() + "-KnnU");
                    GRSs.add(grs);
                });

        HesitantKnnGroupUser hesitantGRS = new HesitantKnnGroupUser();
        hesitantGRS.setAlias("HesitantGRS");
        GRSs.add(hesitantGRS);

        return GRSs;
    }

    public void createCaseStudyXML() {

        TuringPreparator turingPreparator = new TuringPreparator();

        List<GroupCaseStudy> groupCaseStudys = new ArrayList<>();

        for (GroupRecommenderSystem grs : getGRSs()) {
            DefaultGroupCaseStudy groupCaseStudy = new DefaultGroupCaseStudy(
                    getDatasetLoader(),
                    grs,
                    getGroupFormationTechnique(),
                    new HoldOutGroupRatedItems(SEED_VALUE),
                    new NoPredictionProtocol(),
                    GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                    new RelevanceCriteria(4), 1);

            groupCaseStudy.setAlias(grs.getAlias());
            groupCaseStudys.add(groupCaseStudy);
        }

        turingPreparator.prepareGroupExperiment(experimentDirectory, groupCaseStudys, getDatasetLoader());
    }

    @Test
    public void testExecute() throws Exception {
        createCaseStudyXML();

        executeAllExperimentsInDirectory(experimentDirectory);
    }

    private void executeAllExperimentsInDirectory(File directory) {
        new TuringPreparator().executeAllExperimentsInDirectory(directory);
    }
}
