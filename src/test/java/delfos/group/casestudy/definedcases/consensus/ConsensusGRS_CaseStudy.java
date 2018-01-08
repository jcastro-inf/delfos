package delfos.group.casestudy.definedcases.consensus;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.factories.GroupRecommendationsSelectorFactory;
import delfos.group.grs.consensus.ConsensusGRS;
import delfos.group.grs.consensus.itemselector.GroupRecommendationsSelector;
import delfos.rs.RecommenderSystem;
import delfos.rs.bufferedrecommenders.RecommenderSystem_fixedFilePersistence;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.svd.SVDFoldingIn;
import delfos.rs.persistence.FilePersistence;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 28-sept-2014
 */
public class ConsensusGRS_CaseStudy {

    public ConsensusGRS_CaseStudy() {
    }

    public static final String CONSENSUS_GRS_EXPERIMENT_DIRECTORY
            = Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "experiments" + File.separator
            + "consensus-grs-experiments" + File.separator;

    public static final String CONSENSUS_GRS_INPUT_DIRECTORY
            = Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "experiments" + File.separator
            + "consensus-grs-input";

    public static final String CONSENSUS_GRS_OUTPUT_DIRECTORY
            = Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "experiments" + File.separator
            + "consensus-grs-output";

    public static final String CONSENSUS_GRS_INPUT_DATA_DIRECTORY
            = CONSENSUS_GRS_INPUT_DIRECTORY + File.separator
            + "consensusInputData" + File.separator;

    @Before
    public void before() {
        final File persistenceDirectory = new File(CONSENSUS_GRS_INPUT_DIRECTORY);

        FileUtilities.deleteDirectoryRecursive(persistenceDirectory);
        persistenceDirectory.mkdirs();
        new File(CONSENSUS_GRS_INPUT_DATA_DIRECTORY).mkdirs();
    }

    public static final long SEED_VALUE = 123456L;
    public static final int SIZE_OF_GROUPS = 5;
    public static final int NUM_GROUPS = 30;

    public RecommenderSystem getSVDRecommender() {

        final int numFeatures = 20;
        final int numIterPerFeature = 10;
        SVDFoldingIn svdFoldingIn = new SVDFoldingIn(numFeatures, numIterPerFeature);

        RecommenderSystem_fixedFilePersistence rs = new RecommenderSystem_fixedFilePersistence(svdFoldingIn, new FilePersistence("svd-folding-in-recommendation-model", "dat", new File("./svd-folding-in-recommendation-model/")));
        return rs;
    }

    private GroupFormationTechnique getGroupFormationTechnique() {
        GroupFormationTechnique gft = new FixedGroupSize_OnlyNGroups(NUM_GROUPS, SIZE_OF_GROUPS);
        gft.setSeedValue(SEED_VALUE);
        return gft;
    }

    private DatasetLoader<? extends Rating> getDatasetLoader() {
        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        return datasetLoader;
    }

    private List<ConsensusGRS> getAllConsensusGRS() {
        LinkedList<ConsensusGRS> GRSs = new LinkedList<>();

        final MinimumValue aggregationOperator = new MinimumValue();

        for (GroupRecommendationsSelector groupRecommendationsSelector : GroupRecommendationsSelectorFactory.getInstance().getAllClasses()) {
            ConsensusGRS consensusGRS = new ConsensusGRS(getKnnUserRecommender(), aggregationOperator, groupRecommendationsSelector, false, 0.8);

            File inputDir = new File(CONSENSUS_GRS_INPUT_DIRECTORY + File.separator + groupRecommendationsSelector.getName().substring(0, 2));
            File outputDir = new File(CONSENSUS_GRS_OUTPUT_DIRECTORY + File.separator + groupRecommendationsSelector.getName().substring(0, 2));

            consensusGRS.setParameterValue(ConsensusGRS.CONSENSUS_INPUT_FILES_DIRECTORY, inputDir);
            consensusGRS.setParameterValue(ConsensusGRS.CONSENSUS_OUTPUT_FILES_DIRECTORY, outputDir);
            consensusGRS.setAlias("Consensus_NoConsensus_" + groupRecommendationsSelector.getName());

            GRSs.add(consensusGRS);
        }
        return GRSs;
    }

    public KnnMemoryBasedCFRS getKnnUserRecommender() {
        return new KnnMemoryBasedCFRS();
    }

    public void createConsensusCaseStudyXML() {
        TuringPreparator turingPreparator = new TuringPreparator();

        List<GroupCaseStudy> groupCaseStudys = new ArrayList<>();

        for (ConsensusGRS consensusGRS : getAllConsensusGRS()) {
            GroupCaseStudy groupCaseStudy = new GroupCaseStudy(
                    getDatasetLoader(),
                    consensusGRS,
                    getGroupFormationTechnique(), new HoldOut_Ratings(), new NoPredictionProtocol(), GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                    new RelevanceCriteria(4), 1);

            groupCaseStudy.setAlias(consensusGRS.getAlias());

            groupCaseStudys.add(groupCaseStudy);
        }

        turingPreparator.prepareGroupExperiment(new File(CONSENSUS_GRS_EXPERIMENT_DIRECTORY), groupCaseStudys, getDatasetLoader());

    }

    @Test
    public void createCaseStudyExperiments() throws Exception {
        createConsensusCaseStudyXML();
    }
}
