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
package delfos.similaritymeasures.useruser.demographic;

import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class DemographicSimilarityTest {

    public DemographicSimilarityTest() {
    }

    /**
     * Test of similarity method, of class DemographicSimilarity.
     */
    @Test
    public void testSimilarity() {
        DatasetLoader<? extends Rating> datasetLoaders = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        DemographicSimilarity demographicSimilarity = new DemographicSimilarity();

        User user1 = datasetLoaders.getUsersDataset().get(1);
        User user2 = datasetLoaders.getUsersDataset().get(2);

        double similarity = demographicSimilarity.similarity(datasetLoaders, user1, user2);

        System.out.println("Similarity among " + user1 + " and " + user2 + " is " + similarity);
    }

    /**
     * Test of similarity method, of class DemographicSimilarity.
     */
    @Test
    public void testSearchUser1Neighbors() {
        DatasetLoader<? extends Rating> datasetLoaders = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        DemographicSimilarity demographicSimilarity = new DemographicSimilarity();

        User user1 = datasetLoaders.getUsersDataset().get(1);
        datasetLoaders.getUsersDataset().parallelStream()
                .map(neighbor -> {
                    double similarity = demographicSimilarity.similarity(datasetLoaders, user1, neighbor);
                    return new Neighbor(RecommendationEntity.USER, neighbor, similarity);
                })
                .sequential()
                .sorted(Neighbor.BY_SIMILARITY_DESC)
                .forEach(neighbor -> {
                    System.out.println("Neighbor " + neighbor.getIdNeighbor() + " --> " + neighbor.getSimilarity());
                });

        System.out.println("End");
    }

}
