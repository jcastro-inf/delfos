package delfos.group.casestudy.definedcases.somrs;

import delfos.Constants;
import delfos.common.Global;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.experiment.validation.groupformation.GivenGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.aggregation.GroupModelPseudoUser;
import delfos.group.grs.consensus.ConsensusGRS;
import delfos.group.grs.consensus.itemselector.TopNOfEach;
import delfos.group.grs.recommendations.GroupRecommendationsWithMembersRecommendations;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.recommendationcandidates.RecommendationCandidatesSelector;
import delfos.rs.RecommenderSystem;
import delfos.rs.bufferedrecommenders.RecommenderSystem_fixedFilePersistence;
import delfos.rs.collaborativefiltering.svd.SVDFoldingIn;
import delfos.rs.persistence.FilePersistence;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 28-sept-2014
 */
public class SOMRS_IllustrativeExample {

    public SOMRS_IllustrativeExample() {
    }

    public static final String SOM_RS_DIRECTORY
            = Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "experiments" + File.separator
            + "som-rs" + File.separator;

    public static final String SOM_RS_EXPERIMENT_DIRECTORY
            = SOM_RS_DIRECTORY
            + "experiments";

    public static final String SOM_RS_DATASET_PERSISTENCE_DIRECTORY
            = SOM_RS_DIRECTORY
            + "dataset-persistence";

    public static final String SOM_RS_INPUT_DIRECTORY
            = SOM_RS_DIRECTORY
            + "input";

    public static final String SOM_RS_OUTPUT_DIRECTORY
            = SOM_RS_DIRECTORY
            + "output";

    @Before
    public void before() {
        final File inputDirectory = new File(SOM_RS_INPUT_DIRECTORY);
        final File outputDirectory = new File(SOM_RS_OUTPUT_DIRECTORY);
        final File experimentDirectory = new File(SOM_RS_EXPERIMENT_DIRECTORY);
        final File datasetPersistenceDirectory = new File(SOM_RS_DATASET_PERSISTENCE_DIRECTORY);

        inputDirectory.mkdirs();
        outputDirectory.mkdirs();
        experimentDirectory.mkdirs();
        datasetPersistenceDirectory.mkdirs();
    }

    public static final GroupFormationTechnique givenGroups
            = new GivenGroups(Arrays.asList(new GroupOfUsers(35, 369, 585, 621, 876)).toString());

    private GroupFormationTechnique getGroupFormationTechnique() {
        return givenGroups;
    }

    private RecommenderSystem getSVDRecommender() {
        final int numFeatures = 20;
        final int numIterPerFeature = 10;
        SVDFoldingIn svdFoldingIn = new SVDFoldingIn(numFeatures, numIterPerFeature);

        RecommenderSystem_fixedFilePersistence rs = new RecommenderSystem_fixedFilePersistence(svdFoldingIn, new FilePersistence("svd-folding-in-recommendation-model", "dat", new File("./svd-folding-in-recommendation-model/")));
        rs.setAlias("SVD");
        return rs;
    }

    private ConsensusGRS getConsensusGRS_noConsensus(RecommenderSystem rs) {
        ConsensusGRS consensusGRS = new ConsensusGRS(rs, new MinimumValue(), new TopNOfEach(), false, 0.8);
        consensusGRS.setAlias("GRS_" + rs.getAlias() + "_Minimum");

        return consensusGRS;
    }

    private ConsensusGRS getConsensusGRS_consensus(RecommenderSystem rs, double consensusDegree) {
        ConsensusGRS consensusGRS = new ConsensusGRS(rs, new MinimumValue(), new TopNOfEach(), true, consensusDegree);
        String consensusDegreeStr = new DecimalFormat("0.00").format(consensusDegree);

        consensusGRS.setAlias("GRS_" + rs.getAlias() + "_Minimum_consensusDegree=" + consensusDegreeStr);

        return consensusGRS;
    }

    public void setDirectories(ConsensusGRS consensusGRS) {
        File datasetPersistenceDirectory = new File(SOM_RS_DATASET_PERSISTENCE_DIRECTORY + File.separator + consensusGRS.getAlias());
        File consensusInputDirectory = new File(SOM_RS_INPUT_DIRECTORY + File.separator + consensusGRS.getAlias());
        File consensusOutputDirectory = new File(SOM_RS_OUTPUT_DIRECTORY + File.separator + consensusGRS.getAlias());

        consensusGRS.setParameterValue(ConsensusGRS.DATASET_PERSISTENCE_DIRECTORY, datasetPersistenceDirectory);
        consensusGRS.setParameterValue(ConsensusGRS.CONSENSUS_INPUT_FILES_DIRECTORY, consensusInputDirectory);
        consensusGRS.setParameterValue(ConsensusGRS.CONSENSUS_OUTPUT_FILES_DIRECTORY, consensusOutputDirectory);
    }

    private DatasetLoader<? extends Rating> getDatasetLoader() {
        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        return datasetLoader;
    }

    public List<RecommenderSystem> getCoreRSs() {
        List<RecommenderSystem> coreRSs = new ArrayList<>();

        coreRSs.add(getSVDRecommender());

        return coreRSs;
    }

    private List<ConsensusGRS> getConsensusGRS(RecommenderSystem rs) {
        LinkedList<ConsensusGRS> GRSs = new LinkedList<>();

        double[] consensusDegrees = {};

        {
            ConsensusGRS consensusGRS_noConsensus = getConsensusGRS_noConsensus(rs);
            setDirectories(consensusGRS_noConsensus);
            GRSs.add(consensusGRS_noConsensus);
        }

        for (double consensusDegree : consensusDegrees) {
            ConsensusGRS consensusGRS_consensus = getConsensusGRS_consensus(rs, consensusDegree);
            setDirectories(consensusGRS_consensus);
            GRSs.add(consensusGRS_consensus);
        }

        return GRSs;
    }

    @Test
    public void computeGroupAndMembersRecommendations() throws Exception {
        DatasetLoader<? extends Rating> datasetLoader = getDatasetLoader();

        RecommendationCandidatesSelector candidates = new OnlyNewItems();

        for (RecommenderSystem singleUserRS : getCoreRSs()) {
            for (ConsensusGRS consensusGRS : getConsensusGRS(singleUserRS)) {
                SingleRecommendationModel recommendationModel = consensusGRS.buildRecommendationModel(datasetLoader);

                for (GroupOfUsers groupOfUsers : getGroupFormationTechnique().generateGroups(datasetLoader)) {
                    try {
                        GroupModelPseudoUser groupModel = consensusGRS
                                .buildGroupModel(datasetLoader, recommendationModel, groupOfUsers);

                        GroupRecommendationsWithMembersRecommendations groupRecommendationsWithMembersRecommendations
                                = consensusGRS.recommendOnly(datasetLoader, recommendationModel, groupModel, groupOfUsers,
                                        candidates.candidateItems(datasetLoader, groupOfUsers));
                    } catch (NotEnoughtUserInformation ex) {
                        Global.showWarning("Cannot recommend to group '" + groupOfUsers + "', not enough user information.\n" + ex.toString());
                    }
                }
            }
        }
    }
}
