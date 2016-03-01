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
package delfos.rs.recommendation;

import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class RecommendationTest {

    /**
     * Test of getSetOfItems method, of class Recommendation.
     */
    @Test
    public void testComparatorByPreferenceASC() {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        final ContentDataset contentDataset = datasetLoader.getContentDataset();

        Recommendation recommendation_One = new Recommendation(contentDataset.get(1), 5.0);
        Recommendation recommendationFive = new Recommendation(contentDataset.get(5), 1.0);
        Recommendation recommendation_NaN = new Recommendation(contentDataset.get(10), Double.NaN);

        Assert.assertTrue(Recommendation.BY_PREFERENCE_ASC.compare(recommendation_One, recommendation_One) == 0);
        Assert.assertTrue(Recommendation.BY_PREFERENCE_ASC.compare(recommendationFive, recommendationFive) == 0);
        Assert.assertTrue(Recommendation.BY_PREFERENCE_ASC.compare(recommendation_NaN, recommendation_NaN) == 0);

        Assert.assertTrue(Recommendation.BY_PREFERENCE_ASC.compare(recommendation_One, recommendationFive) > 0);
        Assert.assertTrue(Recommendation.BY_PREFERENCE_ASC.compare(recommendation_One, recommendation_NaN) < 0);
        Assert.assertTrue(Recommendation.BY_PREFERENCE_ASC.compare(recommendationFive, recommendation_NaN) < 0);
    }

    /**
     * Test of getSetOfItems method, of class Recommendation.
     */
    @Test
    public void testComparatorByPreferenceDESC() {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        final ContentDataset contentDataset = datasetLoader.getContentDataset();

        Recommendation recommendation_One = new Recommendation(contentDataset.get(1), 5.0);
        Recommendation recommendationFive = new Recommendation(contentDataset.get(5), 1.0);
        Recommendation recommendation_NaN = new Recommendation(contentDataset.get(10), Double.NaN);

        Assert.assertTrue(Recommendation.BY_PREFERENCE_DESC.compare(recommendation_One, recommendation_One) == 0);
        Assert.assertTrue(Recommendation.BY_PREFERENCE_DESC.compare(recommendationFive, recommendationFive) == 0);
        Assert.assertTrue(Recommendation.BY_PREFERENCE_DESC.compare(recommendation_NaN, recommendation_NaN) == 0);

        Assert.assertTrue(Recommendation.BY_PREFERENCE_DESC.compare(recommendation_One, recommendationFive) < 0);
        Assert.assertTrue(Recommendation.BY_PREFERENCE_DESC.compare(recommendation_One, recommendation_NaN) < 0);
        Assert.assertTrue(Recommendation.BY_PREFERENCE_DESC.compare(recommendationFive, recommendation_NaN) < 0);
    }

    /**
     * Test of getSetOfItems method, of class Recommendation.
     */
    @Test
    public void testSorterByPreferenceDESC() {
        System.out.println("============  testSorterByPreferenceDESC  ============");
        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        final ContentDataset contentDataset = datasetLoader.getContentDataset();
        List<Recommendation> recommendations = getRecommendations(datasetLoader);
        Collections.shuffle(recommendations, new Random(0));

        Recommendation[] recommendationsByPreferenceDesc
                = recommendations.parallelStream()
                .sorted(Recommendation.BY_PREFERENCE_DESC)
                .collect(Collectors.toList())
                .toArray(new Recommendation[0]);

        Arrays.asList(recommendationsByPreferenceDesc)
                .forEach(recommendation -> System.out.println(recommendation.toString()));

        Recommendation[] expectedRecommendations = {
            new Recommendation(contentDataset.get(1165), 5),
            new Recommendation(contentDataset.get(79), 4),
            new Recommendation(contentDataset.get(770), 3),
            new Recommendation(contentDataset.get(139), 2),
            new Recommendation(contentDataset.get(752), 1),
            new Recommendation(contentDataset.get(325), Double.NaN),
            new Recommendation(contentDataset.get(335), Double.NaN),
            new Recommendation(contentDataset.get(610), Double.NaN),
            new Recommendation(contentDataset.get(814), Double.NaN)
        };

        Assert.assertArrayEquals(
                "The recommendations are not ordered as expected",
                expectedRecommendations,
                recommendationsByPreferenceDesc);
    }

    /**
     * Test of getSetOfItems method, of class Recommendation.
     */
    @Test
    public void testSorterByPreferenceASC() {
        System.out.println("============  testSorterByPreferenceASC   ============");

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        final ContentDataset contentDataset = datasetLoader.getContentDataset();
        List<Recommendation> recommendations = getRecommendations(datasetLoader);
        Collections.shuffle(recommendations, new Random(0));

        Recommendation[] recommendationsByPreferenceAsc
                = recommendations.parallelStream()
                .sorted(Recommendation.BY_PREFERENCE_ASC)
                .collect(Collectors.toList())
                .toArray(new Recommendation[0]);
        Arrays.asList(recommendationsByPreferenceAsc)
                .forEach(recommendation -> System.out.println(recommendation.toString()));

        Recommendation[] expectedRecommendations = {
            new Recommendation(contentDataset.get(752), 1),
            new Recommendation(contentDataset.get(139), 2),
            new Recommendation(contentDataset.get(770), 3),
            new Recommendation(contentDataset.get(79), 4),
            new Recommendation(contentDataset.get(1165), 5),
            new Recommendation(contentDataset.get(325), Double.NaN),
            new Recommendation(contentDataset.get(335), Double.NaN),
            new Recommendation(contentDataset.get(610), Double.NaN),
            new Recommendation(contentDataset.get(814), Double.NaN)
        };

        Assert.assertArrayEquals(
                "The recommendations are not ordered as expected",
                expectedRecommendations,
                recommendationsByPreferenceAsc);
    }

    public List<Recommendation> getRecommendations(DatasetLoader<? extends Rating> datasetLoader) {

        final ContentDataset contentDataset = datasetLoader.getContentDataset();

        List<Item> items = contentDataset.parallelStream().collect(Collectors.toList());

        Collections.shuffle(items, new Random(0));
        List<Recommendation> recommendations = Arrays.asList(
                new Recommendation(items.remove(0), 1),
                new Recommendation(items.remove(0), 2),
                new Recommendation(items.remove(0), 3),
                new Recommendation(items.remove(0), 4),
                new Recommendation(items.remove(0), 5),
                new Recommendation(items.remove(0), Double.NaN),
                new Recommendation(items.remove(0), Double.NaN),
                new Recommendation(items.remove(0), Double.NaN),
                new Recommendation(items.remove(0), Double.NaN)
        );
        return recommendations;
    }
}
