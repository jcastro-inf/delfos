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
package delfos.main.managers.recommendation.group.helpers;

import delfos.ConsoleParameters;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.aggregation.AggregationOfIndividualRecommendations;
import delfos.main.managers.CaseUseMode;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.output.sort.SortBy;
import delfos.rs.persistence.FilePersistence;

/**
 *
 * @author jcastro
 */
public class CreateDefaultGroupRecommender extends CaseUseMode {

    private static final CreateDefaultGroupRecommender instance = new CreateDefaultGroupRecommender();

    public static CreateDefaultGroupRecommender getInstance() {
        return instance;
    }

    public static final String MODE_PARAMETER = "--create-default-group-recommender";

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        DatasetLoader datasetLoader = new ConfiguredDatasetLoader("ml-100k");

        RecommenderSystem singleUserRS = new TryThisAtHomeSVD(17, 50);

        GroupRecommenderSystem groupRecommender
                = new AggregationOfIndividualRecommendations(
                        singleUserRS,
                        new MinimumValue());
        groupRecommender.setAlias("grs-recommendation-least-missery");

        FilePersistence filePersistence = new FilePersistence("recommendation-model-" + groupRecommender.getAlias().toLowerCase(), "data");

        RecommenderSystemConfigurationFileParser.saveConfigFile(
                "default-group-recommender.xml",
                groupRecommender,
                datasetLoader,
                filePersistence,
                new OnlyNewItems(),
                new RecommendationsOutputStandardRaw(SortBy.SORT_BY_PREFERENCE));
    }

    @Override
    public String getModeParameter() {
        return MODE_PARAMETER;
    }

}
