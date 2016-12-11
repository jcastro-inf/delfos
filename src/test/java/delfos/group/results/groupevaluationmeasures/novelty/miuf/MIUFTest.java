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
package delfos.group.results.groupevaluationmeasures.novelty.miuf;

import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.user.User;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.bufferedrecommenders.RecommenderSystem_cacheRecommendationModel;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVDModel;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class MIUFTest extends DelfosTest {

    public MIUFTest() {
    }

    @Test
    public void test() {

        ConfiguredDatasetLoader ml100k = new ConfiguredDatasetLoader("ml-100k");

        TryThisAtHomeSVD svd = new TryThisAtHomeSVD(10, 50);
        svd.setSeedValue(0);

        RecommenderSystem_cacheRecommendationModel<TryThisAtHomeSVDModel> svdCache = new RecommenderSystem_cacheRecommendationModel<>();
        svdCache.setRecommenderSystem(svd);

        TryThisAtHomeSVDModel svdModel = svdCache.buildRecommendationModel(ml100k);

        final Map<Integer, Double> iuf_byItem = MIUF.getIUF_byItem(ml100k);

        User targetUser = new User(1);
        Set<Item> candidateItems = new OnlyNewItems().candidateItems(ml100k, targetUser);
        RecommendationsToUser recommendToUser = svdCache.recommendToUser(ml100k, svdModel, targetUser, candidateItems);

        MIUF.MeanByListSize meanByListSize = new MIUF.MeanByListSize(recommendToUser, iuf_byItem);

        System.out.println(meanByListSize.toString());

    }

}
