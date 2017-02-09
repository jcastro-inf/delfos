package delfos.group.rs;

import delfos.common.aggregationoperators.Mean;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.aggregation.GroupModelPseudoUser;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.RecommenderSystemBuildingProgressListener_default;
import delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.rs.explanation.GroupModelWithExplanation;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.CosineCoefficient;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 23-Jan-2013
 */
public class AggregationOfIndividualRatingsTest extends DelfosTest {

    private final DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ssii-partition9");

    public AggregationOfIndividualRatingsTest() {
    }

    @Test
    public void testWholeProccess() throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound, NotEnoughtUserInformation {
        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        KnnMemoryBasedCFRS knnMemory = new KnnMemoryBasedCFRS();

        knnMemory.setParameterValue(KnnCollaborativeRecommender.SIMILARITY_MEASURE, new CosineCoefficient());
        knnMemory.setParameterValue(KnnCollaborativeRecommender.RELEVANCE_FACTOR, 30);
        knnMemory.setParameterValue(KnnCollaborativeRecommender.DEFAULT_RATING_VALUE, null);
        knnMemory.setParameterValue(KnnCollaborativeRecommender.DEFAULT_RATING, false);
        knnMemory.setParameterValue(KnnCollaborativeRecommender.CASE_AMPLIFICATION, 1);
        knnMemory.setParameterValue(KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE, 20);
        knnMemory.setParameterValue(KnnCollaborativeRecommender.PREDICTION_TECHNIQUE, new WeightedSum());

        AggregationOfIndividualRatings grs = new AggregationOfIndividualRatings(knnMemory, new Mean());

        grs.addRecommendationModelBuildingProgressListener(new RecommenderSystemBuildingProgressListener_default(System.out, 5000));
        SingleRecommendationModel recommendationModel = grs.buildRecommendationModel(datasetLoader);

        Set<Integer> notRatedID = new TreeSet<>(ratingsDataset.allRatedItems());

        notRatedID.removeAll(ratingsDataset.getUserRated(15743));
        notRatedID.removeAll(ratingsDataset.getUserRated(24357));
        notRatedID.removeAll(ratingsDataset.getUserRated(162779));

        Set<Item> notRated = notRatedID.stream()
                .map(idItem -> datasetLoader.getContentDataset().get(idItem))
                .collect(Collectors.toSet());

        final GroupOfUsers group = new GroupOfUsers(Arrays.asList(
                datasetLoader.getUsersDataset().get(15743),
                datasetLoader.getUsersDataset().get(24357),
                datasetLoader.getUsersDataset().get(162779))
        );

        GroupModelWithExplanation<GroupModelPseudoUser, Object> groupModel = grs.buildGroupModel(datasetLoader, recommendationModel, group);
        GroupRecommendations groupRecommendations = grs.recommendOnly(
                datasetLoader,
                recommendationModel,
                groupModel,
                group,
                notRated);

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw();
        output.writeRecommendations(groupRecommendations);

        {
            // 4697 -> 4.300307
            int idItem = 4697;
            Item item = datasetLoader.getContentDataset().get(idItem);

            Set<Item> candidateItems = Arrays.asList(item).stream().collect(Collectors.toSet());
            double prediction = 4.300307;
            Collection<Recommendation> predictionList = grs.recommendOnly(
                    datasetLoader,
                    recommendationModel,
                    groupModel,
                    group,
                    candidateItems).getRecommendations();

            Assert.assertEquals(1, predictionList.size());
            Assert.assertEquals(idItem, predictionList.iterator().next().getIdItem());
            Assert.assertEquals(prediction, predictionList.iterator().next().getPreference().doubleValue(), 0.0001);
        }

        {
            // 10082 -> 2.50053
            int idItem = 10082;
            double prediction = 2.50053;
            Item item = datasetLoader.getContentDataset().get(idItem);
            Set<Item> candidateItems = Arrays.asList(item).stream().collect(Collectors.toSet());
            Collection<Recommendation> predictionList = grs.recommendOnly(
                    datasetLoader,
                    recommendationModel,
                    groupModel,
                    group,
                    candidateItems).getRecommendations();

            Assert.assertEquals(1, predictionList.size());
            Assert.assertEquals(idItem, predictionList.iterator().next().getIdItem());
            Assert.assertEquals(prediction, predictionList.iterator().next().getPreference().doubleValue(), 0.0001);
        }
    }
}
