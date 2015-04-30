package delfos.group.rs;

import delfos.common.aggregationoperators.Mean;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.aggregation.GroupModelPseudoUser;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.RecommenderSystemBuildingProgressListener_default;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.rs.explanation.GroupModelWithExplanation;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import delfos.similaritymeasures.CosineCoefficient;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 1.0 23-Jan-2013
 */
public class AggregationOfIndividualRatingsTest extends DelfosTest {

    private final DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ssii-partition9");

    public AggregationOfIndividualRatingsTest() {
    }

    @Test
    public void testWholeProccess() throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound, NotEnoughtUserInformation {
        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        KnnMemoryBasedNWR traditionalRecommenderSystem = new KnnMemoryBasedNWR(new CosineCoefficient(), 30, null, false, 1, 20, new WeightedSum());

        AggregationOfIndividualRatings grs = new AggregationOfIndividualRatings(traditionalRecommenderSystem, new Mean());

        grs.addRecommendationModelBuildingProgressListener(new RecommenderSystemBuildingProgressListener_default(System.out, 5000));
        SingleRecommendationModel recommendationModel = grs.buildRecommendationModel(datasetLoader);

        Set<Integer> notRated = new TreeSet<>(ratingsDataset.allRatedItems());

        notRated.removeAll(ratingsDataset.getUserRated(15743));
        notRated.removeAll(ratingsDataset.getUserRated(24357));
        notRated.removeAll(ratingsDataset.getUserRated(162779));

        final GroupOfUsers group = new GroupOfUsers(15743, 24357, 162779);

        GroupModelWithExplanation<GroupModelPseudoUser, ? extends Object> groupModel = grs.buildGroupModel(datasetLoader, recommendationModel, group);
        Collection<Recommendation> recommendOnly = grs.recommendOnly(datasetLoader, recommendationModel, groupModel, group, notRated);

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw();
        output.writeRecommendations(new GroupRecommendations(group, recommendOnly, RecommendationComputationDetails.EMPTY_DETAILS));

        {
            // 4697 -> 4.300307
            int idItem = 4697;
            double prediction = 4.300307;
            Collection<Recommendation> predictionList = grs.recommendOnly(datasetLoader, recommendationModel, groupModel, group, idItem);
            Assert.assertEquals(1, predictionList.size());
            Assert.assertEquals(idItem, predictionList.iterator().next().getIdItem());
            Assert.assertEquals(prediction, predictionList.iterator().next().getPreference().doubleValue(), 0.0001);
        }

        {
            // 10082 -> 2.50053
            int idItem = 10082;
            double prediction = 2.50053;
            Collection<Recommendation> predictionList = grs.recommendOnly(datasetLoader, recommendationModel, groupModel, group, idItem);
            Assert.assertEquals(1, predictionList.size());
            Assert.assertEquals(idItem, predictionList.iterator().next().getIdItem());
            Assert.assertEquals(prediction, predictionList.iterator().next().getPreference().doubleValue(), 0.0001);
        }
    }
}
