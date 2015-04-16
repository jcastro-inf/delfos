package delfos.main.managers.recommendation.nonpersonalised.helpers;

import delfos.ConsoleParameters;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.main.managers.CaseUseManager;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.nonpersonalised.NonPersonalisedRecommender;
import delfos.rs.nonpersonalised.meanrating.wilsonscoreonterval.WilsonScoreLowerBound;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.output.sort.SortBy;
import delfos.rs.persistence.FilePersistence;

/**
 *
 * @author jcastro
 */
public class CreateDefaultNonPersonalisedRecommender implements CaseUseManager {

    private static final CreateDefaultNonPersonalisedRecommender instance = new CreateDefaultNonPersonalisedRecommender();

    public static CreateDefaultNonPersonalisedRecommender getInstance() {
        return instance;
    }

    public static final String PARAMETER = "--create-default-non-personalised-csv";

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isDefined(PARAMETER);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        DatasetLoader datasetLoader = new ConfiguredDatasetLoader("ml-100k");

        NonPersonalisedRecommender<? extends Object> nonPersonalisedRecommender = new WilsonScoreLowerBound();
        FilePersistence filePersistence = new FilePersistence("recommendation-model-" + nonPersonalisedRecommender.getAlias().toLowerCase(), "data");

        RecommenderSystemConfigurationFileParser.saveConfigFile(
                "non-personalised.xml",
                nonPersonalisedRecommender,
                datasetLoader,
                filePersistence,
                new OnlyNewItems(),
                new RecommendationsOutputStandardRaw(SortBy.SORT_BY_PREFERENCE));
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
