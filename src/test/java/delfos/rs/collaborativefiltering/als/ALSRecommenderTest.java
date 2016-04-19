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
package delfos.rs.collaborativefiltering.als;

import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVDModel;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.Set;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class ALSRecommenderTest {

    public ALSRecommenderTest() {
    }

    /**
     * Test of buildRecommendationModel method, of class ALSRecommender.
     */
    @Test
    public void testBuildRecommendationModel() {

        ALSRecommender aLSRecommender = new ALSRecommender();
        aLSRecommender.setSeedValue(123456);

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        TryThisAtHomeSVDModel buildRecommendationModel = aLSRecommender.buildRecommendationModel(datasetLoader);

        User user = datasetLoader.getUsersDataset().get(542);
        Set<Item> candidateItems = new OnlyNewItems().candidateItems(datasetLoader, user);

        RecommendationsToUser recommendToUser = aLSRecommender.recommendToUser(datasetLoader, buildRecommendationModel, user, candidateItems);

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw(20);

        output.writeRecommendations(recommendToUser);

    }
}
