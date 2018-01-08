package delfos.similaritymeasures.useruser;

import delfos.configureddatasets.ConfiguredDataset;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.RecommenderSystemBuildingProgressListener_default;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryModel;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRSModel;
import delfos.rs.output.RecommendationsOutputMethod;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.output.sort.SortBy;
import delfos.rs.recommendation.RecommendationsToUser;
import org.junit.Test;

import java.util.Set;

/**
 * Created by jcastro on 27/06/17.
 */
public class JaccardTest {

    @Test
    public void testInKnnMemoryBasedCFRS(){

        KnnMemoryBasedCFRS ubcf = new KnnMemoryBasedCFRS();

        ubcf.setSIMILARITY_MEASURE(new Jaccard());
        ubcf.setRELEVANCE_FACTOR_VALUE(null);

        DatasetLoader<? extends Rating> ml100k = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        KnnMemoryModel knnMemoryModel = ubcf.buildRecommendationModel(ml100k);
        User user125 = ml100k.getUsersDataset().getUser(125);
        Set<Item> candidateItems = new OnlyNewItems().candidateItems(ml100k, user125);

        RecommendationsToUser recommendationsToUser = ubcf.recommendToUser(ml100k, knnMemoryModel, user125, candidateItems);

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw()
                .setShowSortedByPreference()
                .setTopN(5);

        output.writeRecommendations(recommendationsToUser);
    }

    @Test
    public void testInKnnModelBasedCFRS(){

        KnnModelBasedCFRS ibcf = new KnnModelBasedCFRS();

        ibcf.setSIMILARITY_MEASURE(new Jaccard());
        ibcf.setRELEVANCE_FACTOR_VALUE(null);

        DatasetLoader<? extends Rating> ml100k = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        RatingsDataset<? extends Rating> ratingsDataset = ml100k.getRatingsDataset();
        ibcf.addRecommendationModelBuildingProgressListener(new RecommenderSystemBuildingProgressListener_default(System.out,5000));

        KnnModelBasedCFRSModel recommendationModel = ibcf.buildRecommendationModel(ml100k);
        User user125 = ml100k.getUsersDataset().getUser(125);
        Set<Item> candidateItems = new OnlyNewItems().candidateItems(ml100k, user125);

        RecommendationsToUser recommendationsToUser = ibcf.recommendToUser(ml100k, recommendationModel, user125, candidateItems);

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw()
                .setShowSortedByPreference()
                .setTopN(5);

        output.writeRecommendations(recommendationsToUser);
    }
}
