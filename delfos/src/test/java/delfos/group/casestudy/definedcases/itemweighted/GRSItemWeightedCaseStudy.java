package delfos.group.casestudy.definedcases.itemweighted;

import delfos.Constants;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.MaximumValue;
import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.aggregationoperators.RMSMean;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.itemweighted.AggregationOfIndividualRatings_itemWeighted;
import delfos.group.grs.itemweighted.knn.memory.KnnMemoryBasedNWR_itemWeighted;
import delfos.group.grs.itemweighted.measures.GroupItemWeight;
import delfos.group.grs.itemweighted.measures.NoWeight;
import delfos.group.grs.itemweighted.measures.StandardDeviationWeights;
import delfos.group.grs.itemweighted.measures.Tweak2Weights;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GRSItemWeightedCaseStudy extends DelfosTest {

    public static final String EXPERIMENT_DIRECTORY
            = Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "experiments" + File.separator
            + GRSItemWeightedCaseStudy.class.getSimpleName() + File.separator;

    private static final int NUM_GROUPS = 180, SIZE_OF_GROUPS = 5, SEED_VALUE = 123456;

    private final DatasetLoader<? extends Rating> datasetLoader = new ConfiguredDatasetLoader("ml-100k");

    @Test
    public void createCaseStudyExperiments() throws Exception {
        createConsensusCaseStudyXML();
    }

    public void createConsensusCaseStudyXML() {
        TuringPreparator turingPreparator = new TuringPreparator();

        List<GroupCaseStudy> groupCaseStudys = new ArrayList<>();

        for (GroupRecommenderSystem groupRecommenderSystem : getGRS()) {
            GroupCaseStudy groupCaseStudy = new GroupCaseStudy(
                    getDatasetLoader(),
                    groupRecommenderSystem,
                    getGroupFormationTechnique(), new HoldOutGroupRatedItems(SEED_VALUE), new NoPredictionProtocol(), GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                    new RelevanceCriteria(4), 1);

            groupCaseStudy.setAlias(groupRecommenderSystem.getAlias());

            groupCaseStudys.add(groupCaseStudy);
        }

        turingPreparator.prepareGroupExperiment(new File(EXPERIMENT_DIRECTORY), groupCaseStudys, getDatasetLoader());
    }

    private DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    private List<GroupRecommenderSystem> getGRS() {
        ArrayList<GroupRecommenderSystem> GRSs = new ArrayList<>();

        AggregationOperator[] aggregations = {new Mean(), new MinimumValue(), new MaximumValue(), new RMSMean()};
        GroupItemWeight[] groupItemWeights = {new NoWeight(), new StandardDeviationWeights(), new Tweak2Weights()};

        Number[] completeThresholds = {null, 0, 1, 2, 3, 4, 5};

        for (AggregationOperator aggregation : aggregations) {

            for (GroupItemWeight groupItemWeight : groupItemWeights) {
                for (Number completeThreshold : completeThresholds) {
                    AggregationOfIndividualRatings_itemWeighted grs = new AggregationOfIndividualRatings_itemWeighted(
                            coreRS_itemWeights(),
                            aggregation,
                            groupItemWeight,
                            completeThreshold);
                    GRSs.add(grs);
                }
            }
        }

        return GRSs;
    }

    private static KnnMemoryBasedNWR_itemWeighted coreRS_itemWeights() {
        CollaborativeSimilarityMeasure similarityMeasure = new PearsonCorrelationCoefficient();
        PredictionTechnique predictionTechnique = new WeightedSum();
        int neighborhoodSize = 30;
        int relevanceFactor = 20;
        return new KnnMemoryBasedNWR_itemWeighted(similarityMeasure, relevanceFactor, neighborhoodSize, predictionTechnique);
    }

    private GroupFormationTechnique getGroupFormationTechnique() {
        GroupFormationTechnique gft = new FixedGroupSize_OnlyNGroups(NUM_GROUPS, SIZE_OF_GROUPS);
        gft.setSeedValue(SEED_VALUE);
        return gft;
    }
}
