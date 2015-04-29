package delfos.main.managers.recommendation.nonpersonalised;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.main.managers.CaseUseSubManager;
import delfos.main.managers.recommendation.ArgumentsRecommendation;
import delfos.rs.nonpersonalised.NonPersonalisedRecommender;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.PersistenceMethodStrategy;

/**
 *
 * @version 20-oct-2014
 * @author Jorge Castro Gallardo
 */
public class BuildRecommendationModel extends CaseUseSubManager {

    public static BuildRecommendationModel getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {

        private static final BuildRecommendationModel INSTANCE = new BuildRecommendationModel();
    }

    private BuildRecommendationModel() {
        super(NonPersonalisedRecommendation.getInstance());
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isDefined(NonPersonalisedRecommendation.NON_PERSONALISED_MODE)
                && consoleParameters.isDefined(ArgumentsRecommendation.BUILD_RECOMMENDATION_MODEL);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        String configurationFile = ArgumentsRecommendation.extractConfigurationFile(consoleParameters);

        RecommenderSystemConfiguration rsc = RecommenderSystemConfigurationFileParser.loadConfigFile(configurationFile);

        if (rsc.recommenderSystem instanceof NonPersonalisedRecommender) {
            NonPersonalisedRecommender nonPersonalisedRecommender = (NonPersonalisedRecommender) rsc.recommenderSystem;
            Object recomenderSystemModel = buildRecommendationModel(nonPersonalisedRecommender, rsc.datasetLoader);
            saveRecommendationModel(rsc, recomenderSystemModel);
        } else {
            IllegalStateException ise = new IllegalStateException(rsc.recommenderSystem.getAlias() + " is not a non-personalised recommender system (Must implement " + NonPersonalisedRecommender.class);
            ERROR_CODES.NOT_A_RECOMMENDER_SYSTEM.exit(ise);
        }
    }

    private Object buildRecommendationModel(
            NonPersonalisedRecommender<? extends Object> nonPersonalisedRecommender,
            DatasetLoader<? extends Rating> datasetLoader) {

        Object recomenderSystemModel = null;
        try {
            Global.showMessageTimestamped("Building recommendation model.");
            recomenderSystemModel = nonPersonalisedRecommender.buildRecommendationModel(datasetLoader);
            Global.showMessageTimestamped("Built recommendation model.");
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (CannotLoadUsersDataset ex) {
            ERROR_CODES.CANNOT_LOAD_USERS_DATASET.exit(ex);
            throw new IllegalStateException(ex);
        }
        return recomenderSystemModel;
    }

    private void saveRecommendationModel(RecommenderSystemConfiguration rsc, Object recomenderSystemModel) {
        try {
            Global.showMessageTimestamped("Saving recommendation model.");
            PersistenceMethodStrategy.saveModel(rsc.recommenderSystem, rsc.persistenceMethod, recomenderSystemModel);
            Global.showMessageTimestamped("Saved recommendation model.");
        } catch (FailureInPersistence ex) {
            ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex);
        }
    }

    public String getSynopsis() {
        return "EMPTY_SYNOPSIS";
    }
}
