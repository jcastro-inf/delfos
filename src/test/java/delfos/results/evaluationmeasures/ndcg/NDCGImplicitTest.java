/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.results.evaluationmeasures.ndcg;

import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.main.managers.recommendation.singleuser.Recommend;
import delfos.rs.nonpersonalised.randomrecommender.RandomRecommendationModel;
import delfos.rs.nonpersonalised.randomrecommender.RandomRecommender;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import delfos.rs.recommendation.RecommendationsToUser;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author jcastro
 */
public class NDCGImplicitTest {

    public NDCGImplicitTest() {
    }

    /**
     * Test of computeDCG method, of class NDCG.
     */
    @Test
    public void testComputeDCG() {
        System.out.println("testComputeDCG");

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory
                .getInstance()
                .getDatasetLoader("ml-100k");

        User user = datasetLoader.getUsersDataset().get(1);

        Map<Long, ? extends Rating> userRatingsByItemId = datasetLoader
                .getRatingsDataset()
                .getUserRatingsRated(user.getId());

        Map<Item, Rating> userRatingsByItem = userRatingsByItemId.values().stream()
                .collect(Collectors.toMap(rating -> rating.getItem(), rating -> rating));

        Set<Item> candidateItems = datasetLoader
                .getContentDataset()
                .stream()
                .collect(Collectors.toSet());

        RandomRecommender randomRecommender = new RandomRecommender();
        randomRecommender.setSeedValue(123456L);
        RandomRecommendationModel<Long> recommendationModel = randomRecommender.
                buildRecommendationModel(datasetLoader);

        RecommendationsToUser recommendationsToUser = randomRecommender.recommendToUser(
                datasetLoader,
                recommendationModel,
                user,
                candidateItems);

        List<Recommendation> recommendations = recommendationsToUser
                .getRecommendations().stream()
                .sorted(Recommendation.BY_PREFERENCE_DESC)
                .collect(Collectors.toList());

        List<Recommendation> recommendationsPerfect = userRatingsByItem.entrySet().stream()
                .map(entryItemRating -> new Recommendation(entryItemRating.getKey(), entryItemRating.getValue().getRatingValue().doubleValue()))
                .sorted(Recommendation.BY_PREFERENCE_DESC)
                .collect(Collectors.toList());

        double dcg = NDCGImplicit.computeDCG(recommendations, userRatingsByItemId);
        double dcg_perfect = NDCGImplicit.computeDCG(recommendationsPerfect, userRatingsByItemId);

        double ndcg = dcg / dcg_perfect;

        double dcg_expected = 113.2835112192644;
        double dcg_perfect_expected = 174.31149492536096;
        double ndcg_expected = 0.6498912264378872;

        assertEquals(dcg_expected, dcg, 0.00001);
        assertEquals(dcg_perfect_expected, dcg_perfect, 0.00001);
        assertEquals(ndcg_expected, ndcg, 0.00001);
    }

    @Test
    public void testComputeNDCG_zeroPerfectDCG(){
        System.out.println("testComputeNDCG_zeroPerfectDCG");


        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory
                .getInstance()
                .getDatasetLoader("ml-100k");

        User user = datasetLoader.getUsersDataset().get(1);

        Map<Long, ? extends Rating> userRatingsByItemId = datasetLoader
                .getRatingsDataset()
                .getUserRatingsRated(user.getId()).values()
                .stream()
                .map(rating -> rating.copyWithRatingValue(0.0))
                .collect(Collectors.toMap(
                        rating -> rating.getIdItem(),
                        rating->rating
                ));

        Map<Item, Rating> userRatingsByItem = userRatingsByItemId
                .values().stream()
                .collect(Collectors.toMap(
                        rating -> rating.getItem(),
                        rating -> rating.copyWithRatingValue(0.0)
                ));

        Set<Item> candidateItems = datasetLoader
                .getContentDataset()
                .stream()
                .collect(Collectors.toSet());

        RandomRecommender randomRecommender = new RandomRecommender();
        randomRecommender.setSeedValue(123456L);
        RandomRecommendationModel<Long> recommendationModel = randomRecommender.
                buildRecommendationModel(datasetLoader);

        RecommendationsToUser recommendationsToUser = randomRecommender
                .recommendToUser(
                    datasetLoader,
                    recommendationModel,
                    user,
                    candidateItems
                );

        List<Recommendation> recommendations = recommendationsToUser
                .getRecommendations().stream()
                .sorted(Recommendation.BY_PREFERENCE_DESC)
                .collect(Collectors.toList());

        List<Recommendation> recommendationsPerfect = userRatingsByItem.entrySet().stream()
                .map(entryItemRating -> new Recommendation(entryItemRating.getKey(), entryItemRating.getValue().getRatingValue().doubleValue()))
                .sorted(Recommendation.BY_PREFERENCE_DESC)
                .collect(Collectors.toList());

        double dcg = NDCGImplicit.computeDCG(recommendations, userRatingsByItemId);
        double dcg_perfect = NDCGImplicit.computeDCG(recommendationsPerfect, userRatingsByItemId);

        double ndcg = NDCGImplicit.computeNDCG(recommendations,userRatingsByItemId);

        double dcg_expected = 0.0;
        double dcg_perfect_expected = 0.0;
        double ndcg_expected = 0.0;

        assertEquals(dcg_expected, dcg, 0.00001);
        assertEquals(dcg_perfect_expected, dcg_perfect, 0.00001);
        assertEquals(ndcg_expected, ndcg, 0.00001);

    }

    /**
     * Test of computeDCG method, of class NDCGImplicit
     */
    @Test
    public void testComputeDCG_45321() {
        System.out.println("testComputeDCG_45321");

        DatasetLoader<Rating> datasetLoader = (DatasetLoader<Rating>) ConfiguredDatasetsFactory.getInstance()
                .getDatasetLoader("ml-100k");

        User user = datasetLoader.getUsersDataset().get(1);

        Map<Long, ? extends Rating> userRatingsByItemId = datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId());

        List<Recommendation> recommendations = Arrays.asList(
                new Recommendation(datasetLoader.getContentDataset().getItem(229), 4.8),
                new Recommendation(datasetLoader.getContentDataset().getItem(124), 4.6),
                new Recommendation(datasetLoader.getContentDataset().getItem(130), 4.2),
                new Recommendation(datasetLoader.getContentDataset().getItem(105), 3.8),
                new Recommendation(datasetLoader.getContentDataset().getItem(219), 3.6)
        );

        List<Recommendation> recommendationPerfect = recommendations.stream().map(r -> userRatingsByItemId.get(r.getItem().getId()))
                .map(rating -> new Recommendation(rating.getItem(), rating.getRatingValue().doubleValue()))
                .sorted(Recommendation.BY_PREFERENCE_DESC)
                .collect(Collectors.toList());

        double dcg = NDCGImplicit.computeDCG(recommendations, userRatingsByItemId);

        double dcg_perfect = NDCGImplicit.computeDCG(recommendationPerfect, userRatingsByItemId);
        double ndcg = dcg / dcg_perfect;

        double dcg_expected = 12.323465818787765;
        double ndcg_expected = 1.0;

        assertEquals(dcg_expected, dcg, 0.00001);
        assertEquals(ndcg_expected, ndcg, 0.00001);
    }

    /**
     * Test of computeDCG method, of class NDCGImplicit.
     */
    @Test
    public void testComputeDCG_54321() {
        System.out.println("testComputeDCG_54321");

        DatasetLoader<Rating> datasetLoader = (DatasetLoader<Rating>) ConfiguredDatasetsFactory.getInstance()
                .getDatasetLoader("ml-100k");

        User user = datasetLoader.getUsersDataset().get(1);

        Map<Long, ? extends Rating> userRatingsByItemId = datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId());

        List<Recommendation> recommendations = Arrays.asList(
                new Recommendation(datasetLoader.getContentDataset().getItem(124), 4.8),
                new Recommendation(datasetLoader.getContentDataset().getItem(229), 4.6),
                new Recommendation(datasetLoader.getContentDataset().getItem(130), 4.2),
                new Recommendation(datasetLoader.getContentDataset().getItem(105), 3.8),
                new Recommendation(datasetLoader.getContentDataset().getItem(219), 3.6)
        );

        List<Recommendation> recommendationPerfect = recommendations.stream().map(r -> userRatingsByItemId.get(r.getItem().getId()))
                .map(rating -> new Recommendation(rating.getItem(), rating.getRatingValue().doubleValue()))
                .sorted(Recommendation.BY_PREFERENCE_DESC)
                .collect(Collectors.toList());

        double dcg = NDCGImplicit.computeDCG(recommendations, userRatingsByItemId);

        double dcg_perfect = NDCGImplicit.computeDCG(recommendationPerfect, userRatingsByItemId);
        double ndcg = dcg / dcg_perfect;

        double dcg_expected = 12.323465818787765;
        double ndcg_expected = 1;

        assertEquals(dcg_expected, dcg, 0.00001);
        assertEquals(ndcg_expected, ndcg, 0.00001);
    }

    /**
     * Test of computeDCG method, of class NDCGImplicit.
     */
    @Test
    public void testComputeDCG_54123() {
        System.out.println("testComputeDCG_54123");

        DatasetLoader<Rating> datasetLoader = (DatasetLoader<Rating>) ConfiguredDatasetsFactory.getInstance()
                .getDatasetLoader("ml-100k");

        User user = datasetLoader.getUsersDataset().get(1);

        Map<Long, ? extends Rating> userRatingsByItemId = datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId());

        List<Recommendation> recommendations = Arrays.asList(
                new Recommendation(datasetLoader.getContentDataset().getItem(124), 4.8),
                new Recommendation(datasetLoader.getContentDataset().getItem(229), 4.6),
                new Recommendation(datasetLoader.getContentDataset().getItem(219), 3.6),
                new Recommendation(datasetLoader.getContentDataset().getItem(105), 3.8),
                new Recommendation(datasetLoader.getContentDataset().getItem(130), 4.2)
        );

        List<Recommendation> recommendationPerfect = recommendations.stream().map(r -> userRatingsByItemId.get(r.getItem().getId()))
                .map(rating -> new Recommendation(rating.getItem(), rating.getRatingValue().doubleValue()))
                .sorted(Recommendation.BY_PREFERENCE_DESC)
                .collect(Collectors.toList());

        double dcg = NDCGImplicit.computeDCG(recommendations, userRatingsByItemId);

        double dcg_perfect = NDCGImplicit.computeDCG(recommendationPerfect, userRatingsByItemId);
        double ndcg = dcg / dcg_perfect;

        double dcg_expected = 11.922959427791637;
        double ndcg_expected = 0.9675005070095186;

        assertEquals(dcg_expected, dcg, 0.00001);
        assertEquals(ndcg_expected, ndcg, 0.00001);

    }

}
