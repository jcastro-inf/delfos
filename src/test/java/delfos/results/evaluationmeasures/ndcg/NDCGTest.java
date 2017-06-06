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
import delfos.rs.recommendation.Recommendation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class NDCGTest {

    public NDCGTest() {
    }

    /**
     * Test of computeDCG method, of class NDCG.
     */
    @Test
    public void testComputeDCG() {
        System.out.println("computeDCG");

        DatasetLoader<Rating> datasetLoader = (DatasetLoader<Rating>) ConfiguredDatasetsFactory.getInstance()
                .getDatasetLoader("ml-100k");

        User user = datasetLoader.getUsersDataset().get(1);

        Map<Long, ? extends Rating> userRatingsByItemId = datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId());
        Map<Item, Rating> collect = userRatingsByItemId.values().stream().collect(Collectors.toMap(r -> r.getItem(), r -> r));

        List<Rating> sortedRatings = collect.values().stream().sorted(Rating.SORT_BY_RATING_DESC).collect(Collectors.toList());

        List<Recommendation> recommendations
                = new Random(0).ints(10, 0, sortedRatings.size()).distinct().boxed()
                .map(i -> sortedRatings.get(i))
                .map(rating -> {
                    final double preference = rating.getRatingValue().doubleValue();
                    final Item item = rating.getItem();

                    return new Recommendation(item, preference);
                })
                .collect(Collectors.toList());

        List<Recommendation> recommendationPerfect = recommendations.stream().map(r -> userRatingsByItemId.get(r.getItem().getId()))
                .map(rating -> new Recommendation(rating.getItem(), rating.getRatingValue().doubleValue()))
                .sorted(Recommendation.BY_PREFERENCE_DESC)
                .collect(Collectors.toList());

        double dcg = NDCG.computeDCG(recommendations, userRatingsByItemId);

        double dcg_perfect = NDCG.computeDCG(recommendationPerfect, userRatingsByItemId);
        double ndcg = dcg / dcg_perfect;

        double dcg_expected = 14.023716584582989;
        double dcg_perfect_expected = 16.65435365823412;
        double ndcg_expected = 0.842045081566374;

        assertEquals(dcg_expected, dcg, 0.00001);
        assertEquals(dcg_perfect_expected, dcg_perfect, 0.00001);
        assertEquals(ndcg_expected, ndcg, 0.00001);
    }

    /**
     * Test of computeDCG method, of class NDCG.
     */
    @Test
    public void testComputeDCG_45321() {
        System.out.println("computeDCG");

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

        double dcg = NDCG.computeDCG(recommendations, userRatingsByItemId);

        double dcg_perfect = NDCG.computeDCG(recommendationPerfect, userRatingsByItemId);
        double ndcg = dcg / dcg_perfect;

        double dcg_expected = 12.323465818787765;
        double ndcg_expected = 1.0;

        assertEquals(dcg_expected, dcg, 0.00001);
        assertEquals(ndcg_expected, ndcg, 0.00001);
    }

    /**
     * Test of computeDCG method, of class NDCG.
     */
    @Test
    public void testComputeDCG_54321() {
        System.out.println("computeDCG");

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

        double dcg = NDCG.computeDCG(recommendations, userRatingsByItemId);

        double dcg_perfect = NDCG.computeDCG(recommendationPerfect, userRatingsByItemId);
        double ndcg = dcg / dcg_perfect;

        double dcg_expected = 12.323465818787765;
        double ndcg_expected = 1;

        assertEquals(dcg_expected, dcg, 0.00001);
        assertEquals(ndcg_expected, ndcg, 0.00001);
    }

    /**
     * Test of computeDCG method, of class NDCG.
     */
    @Test
    public void testComputeDCG_54123() {
        System.out.println("computeDCG");

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

        double dcg = NDCG.computeDCG(recommendations, userRatingsByItemId);

        double dcg_perfect = NDCG.computeDCG(recommendationPerfect, userRatingsByItemId);
        double ndcg = dcg / dcg_perfect;

        double dcg_expected = 11.922959427791637;
        double ndcg_expected = 0.9675005070095186;

        assertEquals(dcg_expected, dcg, 0.00001);
        assertEquals(ndcg_expected, ndcg, 0.00001);

    }

}
