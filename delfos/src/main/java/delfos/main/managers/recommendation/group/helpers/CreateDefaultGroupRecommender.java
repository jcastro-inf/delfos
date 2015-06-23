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
