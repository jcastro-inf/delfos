package delfos.group.grs.penalty;

import delfos.group.grs.penalty.PenaltyGRS_Recommendations;
import java.io.File;
import java.util.List;
import org.junit.Test;
import delfos.common.aggregationoperators.penalty.functions.PenaltyWholeMatrix;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.SingleRecommenderSystemModel;
import delfos.group.grs.penalty.grouper.GrouperByIdItem;
import delfos.rs.RecommenderSystem;
import delfos.rs.bufferedrecommenders.RecommenderSystem_fixedFilePersistence;
import delfos.rs.collaborativefiltering.svd.SVDFoldingIn;
import delfos.rs.persistence.FilePersistence;
import delfos.rs.recommendation.Recommendation;

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
        File recommendationModelDirectory = new File("test-temp" + File.separator + "svd-folding-in-model" + File.separator);
        FilePersistence filePersistence = new FilePersistence("svd-folding-in-model", "dat", recommendationModelDirectory);

        RecommenderSystem svdFixedModel = new RecommenderSystem_fixedFilePersistence(sVDFoldingIn, filePersistence);
        svdFixedModel.setAlias(sVDFoldingIn.getAlias());
        return svdFixedModel;
    }

    @Test
    public void testGRS_RecommendationsCombinatory() throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        PenaltyGRS_Recommendations penaltyGRS_Recommendations = new PenaltyGRS_Recommendations(getCoreRS(), new PenaltyWholeMatrix(1, 1.5), new GrouperByIdItem(3));

        SingleRecommenderSystemModel recommendationModel = penaltyGRS_Recommendations.build(datasetLoader);

        GroupOfUsers groupOfUsers = new GroupOfUsers(42, 222, 267);

        GroupOfUsers groupModel = penaltyGRS_Recommendations.buildGroupModel(datasetLoader, recommendationModel, groupOfUsers);

        List<Recommendation> recommendations = penaltyGRS_Recommendations.recommendOnly(datasetLoader, recommendationModel, groupModel, groupOfUsers, datasetLoader.getRatingsDataset().allRatedItems());

        for (Recommendation recommendation : recommendations.subList(0, Math.min(recommendations.size(), 10))) {
            System.out.println(recommendation);
        }

    }

}
