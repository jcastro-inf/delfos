package delfos.group.casestudy.definedcases.consensus;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import delfos.common.FileUtilities;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.consensus.ConsensusGRS;
import delfos.group.grs.consensus.itemselector.TopNOfEach;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.rs.RecommenderSystem;
import delfos.rs.bufferedrecommenders.RecommenderSystem_fixedFilePersistence;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.collaborativefiltering.svd.SVDFoldingIn;
import delfos.rs.persistence.FilePersistence;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 28-sept-2014
 */
public class ConsensusGRS_CaseStudy {

    public ConsensusGRS_CaseStudy() {
    }

    public static final String CONSENSUS_GRS_EXPERIMENT_DIRECTORY = "." + File.separator
            + "experiments" + File.separator
            + "consensus-grs-experiments" + File.separator;

    public static final String CONSENSUS_GRS_INPUT_DIRECTORY = "." + File.separator
            + "experiments" + File.separator
            + "consensus-grs-input";

    public static final String CONSENSUS_GRS_OUTPUT_DIRECTORY = "." + File.separator
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

    public static final long SEED_VALUE = 77352653L;
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

        GRSs.add(new ConsensusGRS(getKnnUserRecommender(), new MinimumValue(), new TopNOfEach(), false, 0.8));
        GRSs.getLast().setAlias("Consensus_NoConsensus");

        double[] consensusDegrees = {
            0.5,
            0.6, 0.65, 0.66, 0.67, 0.68, 0.69,
            0.7, 0.71, 0.72, 0.73, 0.74, 0.75, 0.76, 0.77, 0.78, 0.79,
            0.8, 0.81, 0.82, 0.83, 0.84, 0.85, 0.86, 0.87, 0.88, 0.89,
            0.9
        };

        for (double consensusDegree : consensusDegrees) {

            ConsensusGRS consensusGRS = new ConsensusGRS(getKnnUserRecommender(), new MinimumValue(), new TopNOfEach(), true, consensusDegree);

            String consensusDegreeStr = NumberRounder.round_str(consensusDegree);

            consensusGRS.setAlias("Consensus_" + consensusDegreeStr);
            consensusGRS.setParameterValue(ConsensusGRS.CONSENSUS_OUTPUT_FILES_DIRECTORY, CONSENSUS_GRS_OUTPUT_DIRECTORY);
            GRSs.add(consensusGRS);
        }

        return GRSs;
    }

    public KnnMemoryBasedNWR getKnnUserRecommender() {
        return new KnnMemoryBasedNWR();
    }

    public void createConsensusCaseStudyXML() {
        TuringPreparator turingPreparator = new TuringPreparator();

        List<GroupCaseStudy> groupCaseStudys = new ArrayList<>();

        for (ConsensusGRS consensusGRS : getAllConsensusGRS()) {
            DefaultGroupCaseStudy groupCaseStudy = new DefaultGroupCaseStudy(
                    getDatasetLoader(),
                    consensusGRS,
                    getGroupFormationTechnique(), new HoldOutGroupRatedItems(SEED_VALUE), new NoPredictionProtocol(), GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                    new RelevanceCriteria(4), 1);

            groupCaseStudy.setAlias(consensusGRS.getAlias());

            groupCaseStudys.add(groupCaseStudy);
        }

        turingPreparator.prepareGroupExperiment(new File(CONSENSUS_GRS_EXPERIMENT_DIRECTORY), groupCaseStudys, getDatasetLoader());
    }

    @Test
    public void testExecute() throws Exception {

        createConsensusCaseStudyXML();

        executeAllExperimentsInDirectory(new File(CONSENSUS_GRS_EXPERIMENT_DIRECTORY));
    }

    private void executeAllExperimentsInDirectory(File directory) {
        new TuringPreparator().executeAllExperimentsInDirectory(directory);
    }

}
