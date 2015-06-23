package delfos.group.grs.penalty;

import delfos.Constants;
import delfos.common.aggregationoperators.penalty.functions.PenaltyWholeMatrix;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.penalty.grouper.GrouperByIdItem;
import delfos.rs.RecommenderSystem;
import delfos.rs.bufferedrecommenders.RecommenderSystem_fixedFilePersistence;
import delfos.rs.collaborativefiltering.svd.SVDFoldingIn;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.persistence.FilePersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import java.io.File;
import java.util.Collection;
import org.junit.Test;

/**
 *
 * @version 13-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class PenaltyGRS_RecommendationsTest extends DelfosTest {

    public PenaltyGRS_RecommendationsTest() {
    }

    public RecommenderSystem getCoreRS() {

        SVDFoldingIn sVDFoldingIn = new SVDFoldingIn();
        sVDFoldingIn.setSeedValue(987654321);
        File recommendationModelDirectory = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator + "svd-folding-in-model" + File.separator);
        FilePersistence filePersistence = new FilePersistence("svd-folding-in-model", "dat", recommendationModelDirectory);

        RecommenderSystem svdFixedModel = new RecommenderSystem_fixedFilePersistence(sVDFoldingIn, filePersistence);
        svdFixedModel.setAlias(sVDFoldingIn.getAlias());
        return svdFixedModel;
    }

    @Test
    public void testGRS_RecommendationsCombinatory() throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        PenaltyGRS_Recommendations penaltyGRS_Recommendations = new PenaltyGRS_Recommendations(getCoreRS(), new PenaltyWholeMatrix(1, 1.5), new GrouperByIdItem(3));

        SingleRecommendationModel recommendationModel = penaltyGRS_Recommendations.buildRecommendationModel(datasetLoader);

        GroupOfUsers groupOfUsers = new GroupOfUsers(42, 222, 267);

        GroupOfUsers groupModel = penaltyGRS_Recommendations.buildGroupModel(datasetLoader, recommendationModel, groupOfUsers);

        Collection<Recommendation> recommendations = penaltyGRS_Recommendations.recommendOnly(datasetLoader, recommendationModel, groupModel, groupOfUsers, datasetLoader.getRatingsDataset().allRatedItems());

        new RecommendationsOutputStandardRaw(10).writeRecommendations(new Recommendations(groupOfUsers.getTargetId(), recommendations));

    }

}
